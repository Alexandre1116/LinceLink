package pt.tailcatmod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import pt.tailcatmod.transport.Tunnel;

final class RouteNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("tailcatmod", "routes"), () -> PROTOCOL,
            RouteNetwork::compatible, RouteNetwork::compatible);
    private record Host(Tunnel tunnel, PeerRoutes peers) {}
    private record Hello(String key) {}
    private record Snapshot(Map<UUID, PeerRoutes.Route> routes) {}
    private static volatile Host host;
    private static final Map<UUID, String> IDENTITIES = new ConcurrentHashMap<>();
    private static Map<UUID, PeerRoutes.Route> displayed = Map.of();
    private static ClientPacketListener lastConnection;
    private static String lastIdentity;
    private static int ticks;

    private static boolean compatible(String version) {
        return PROTOCOL.equals(version) || NetworkRegistry.ABSENT.equals(version) || NetworkRegistry.ACCEPTVANILLA.equals(version);
    }
    static void register() {
        CHANNEL.registerMessage(0, Hello.class, (message, buffer) -> buffer.writeUtf(message.key(), 72),
                buffer -> new Hello(buffer.readUtf(72)), RouteNetwork::receiveHello, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1, Snapshot.class, RouteNetwork::encodeSnapshot, RouteNetwork::decodeSnapshot,
                RouteNetwork::receiveSnapshot, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    static void startHost(Tunnel tunnel) {
        Host state = new Host(tunnel, new PeerRoutes()); host = state;
        tunnel.onStatus(json -> state.peers().accept(json, System.currentTimeMillis()));
    }
    static void reset() { host = null; displayed = Map.of(); lastConnection = null; lastIdentity = null; }
    static PeerRoutes.Route route(UUID player) { return displayed.getOrDefault(player, PeerRoutes.Route.UNKNOWN); }

    static void clientTick() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null || Minecraft.getInstance().level == null) {
            displayed = Map.of(); lastConnection = null; lastIdentity = null; return;
        }
        if (lastConnection != connection) { displayed = Map.of(); lastIdentity = null; lastConnection = connection; }
        if (!CHANNEL.isRemotePresent(connection.getConnection())) return;
        Tunnel tunnel = TailcatMod.tunnel;
        String identity = tunnel != null && !TailcatMod.hosting ? tunnel.clientPublicKey() : "";
        if (identity == null || identity.equals(lastIdentity)) return;
        CHANNEL.sendToServer(new Hello(identity)); lastIdentity = identity;
    }
    private static void receiveHello(Hello message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null && (message.key().isEmpty() || message.key().matches("nodekey:[0-9a-f]{64}"))) {
            context.enqueueWork(() -> IDENTITIES.put(sender.getUUID(), message.key()));
        }
        context.setPacketHandled(true);
    }
    private static void encodeSnapshot(Snapshot message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.routes().size());
        message.routes().forEach((id, route) -> { buffer.writeUUID(id); buffer.writeEnum(route); });
    }
    private static Snapshot decodeSnapshot(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > 1024) throw new IllegalArgumentException("Invalid route count");
        Map<UUID, PeerRoutes.Route> routes = new HashMap<>();
        for (int i = 0; i < count; i++) routes.put(buffer.readUUID(), buffer.readEnum(PeerRoutes.Route.class));
        return new Snapshot(Map.copyOf(routes));
    }
    private static void receiveSnapshot(Snapshot message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> displayed = message.routes()); context.setPacketHandled(true);
    }
    @Mod.EventBusSubscriber(modid = "tailcatmod", value = Dist.CLIENT)
    public static final class Events {
        @SubscribeEvent public static void serverTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || ++ticks % 20 != 0) return;
            var server = event.getServer();
            var players = server.getPlayerList().getPlayers();
            Set<UUID> connected = new HashSet<>();
            Map<UUID, PeerRoutes.Route> routes = new HashMap<>();
            Host current = host;
            for (ServerPlayer player : players) {
                UUID id = player.getUUID(); connected.add(id);
                PeerRoutes.Route route;
                if (server.isSingleplayerOwner(player.getGameProfile())) route = PeerRoutes.Route.LOCAL;
                else {
                    String key = IDENTITIES.get(id);
                    if (key == null) route = PeerRoutes.Route.UNKNOWN;
                    else if (key.isEmpty()) route = PeerRoutes.Route.NORMAL;
                    else route = current != null && current.tunnel().alive()
                            ? current.peers().route(key, System.currentTimeMillis()) : PeerRoutes.Route.UNKNOWN;
                }
                routes.put(id, route);
            }
            IDENTITIES.keySet().retainAll(connected);
            Snapshot snapshot = new Snapshot(Map.copyOf(routes));
            for (ServerPlayer player : players) {
                if (CHANNEL.isRemotePresent(player.connection.connection)) CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), snapshot);
            }
        }
        @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { IDENTITIES.remove(event.getEntity().getUUID()); }
        @SubscribeEvent public static void stopped(ServerStoppedEvent event) { IDENTITIES.clear(); host = null; ticks = 0; }
    }
}
