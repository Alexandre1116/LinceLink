package pt.tailcatmod;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Adds latency only while vanilla draws Tab, allowing vanilla to size its columns. */
@Mod.EventBusSubscriber(modid = "tailcatmod", value = Dist.CLIENT)
public final class TabPingOverlay {
    private record Name(Component original, Component displayed) {}
    private static final Map<PlayerInfo, Name> NAMES = new IdentityHashMap<>();

    private TabPingOverlay() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforeTab(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_LIST.id())) return;
        restore();
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || !client.options.keyPlayerList.isDown()) return;
        for (PlayerInfo info : client.getConnection().getOnlinePlayers()) {
            Component original = info.getTabListDisplayName();
            Component base = original != null ? original : PlayerTeam.formatNameForTeam(
                    info.getTeam(), Component.literal(info.getProfile().getName()));
            int ping = info.getLatency();
            ChatFormatting color = ping < 0 ? ChatFormatting.GRAY : ping < 150 ? ChatFormatting.GREEN
                    : ping < 300 ? ChatFormatting.YELLOW : ChatFormatting.RED;
            Component displayed = base.copy().append(Component.literal(" [" + (ping < 0 ? "--" : ping) + " ms]").withStyle(color));
            PeerRoutes.Route route = RouteNetwork.route(info.getProfile().getId());
            displayed = displayed.copy().append(Component.literal(" [").append(UiText.route(route)).append("]").withStyle(
                    route == PeerRoutes.Route.DIRECT ? ChatFormatting.AQUA : route == PeerRoutes.Route.DERP
                            ? ChatFormatting.GOLD : ChatFormatting.GRAY));
            NAMES.put(info, new Name(original, displayed));
            info.setTabListDisplayName(displayed);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void afterTab(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_LIST.id())) restore();
    }

    // Also clean up if another mod cancels Tab after our Pre listener ran.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void afterGui(RenderGuiEvent.Post event) { restore(); }

    @SubscribeEvent
    public static void disconnected(ClientPlayerNetworkEvent.LoggingOut event) { restore(); }

    private static void restore() {
        NAMES.forEach((info, name) -> {
            if (info.getTabListDisplayName() == name.displayed()) info.setTabListDisplayName(name.original());
        });
        NAMES.clear();
    }
}
