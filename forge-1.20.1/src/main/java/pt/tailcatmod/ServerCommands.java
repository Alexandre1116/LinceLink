package pt.tailcatmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.HttpUtil;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import pt.tailcatmod.transport.Invite;
import pt.tailcatmod.transport.TailcatInstaller;
import pt.tailcatmod.transport.Tunnel;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/** Dedicated server tunnel; all state changes run on the server thread. */
@Mod.EventBusSubscriber(modid = "tailcatmod")
public final class ServerCommands {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Tunnel tunnel;
    private static String invite;
    private static volatile boolean starting;
    private static long generation;

    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("lincelink")
            .requires(source -> source.hasPermission(2) || source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player && source.getServer().isSingleplayerOwner(player.getGameProfile()))
            .then(Commands.literal("start").executes(ctx -> start(ctx.getSource())))
            .then(Commands.literal("invite").executes(ctx -> invite(ctx.getSource())))
            .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
            .then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource()))));
    }

    private static int start(CommandSourceStack source) {
        if (starting || tunnel != null && tunnel.alive()) {
            source.sendFailure(Component.literal("LinceLink is already starting or running.")); return 0;
        }
        reset();
        MinecraftServer server = source.getServer();
        if (!server.isDedicatedServer() && !server.isPublished() && !server.publishServer(server.getDefaultGameType(), false, HttpUtil.getAvailablePort())) {
            source.sendFailure(Component.literal("LinceLink: could not open the LAN server.")); return 0;
        }
        int port = server.getPort();
        if (port < 1 || port > 65535) { source.sendFailure(Component.literal("Invalid server port.")); return 0; }
        long request = ++generation;
        starting = true;
        source.sendSuccess(() -> Component.literal("LinceLink: preparing Tailcat..."), false);
        TailcatInstaller.ensure(FMLPaths.GAMEDIR.get()).whenComplete((path, installError) -> server.execute(() -> {
            if (request != generation) return;
            if (installError != null) { LOGGER.error("LinceLink: Tailcat installation failed", installError); reset(); source.sendFailure(Component.literal("LinceLink: Tailcat installation failed. Check the server logs.")); return; }
            Tunnel session = new Tunnel(); tunnel = session;
            try {
                session.start(FMLPaths.GAMEDIR.get(), true, "", port).whenComplete((address, error) -> server.execute(() -> {
                    if (request != generation || tunnel != session) return;
                    starting = false;
                    if (error != null) { LOGGER.error("LinceLink: tunnel could not start", error); reset(); source.sendFailure(Component.literal("LinceLink: tunnel could not start. Check the server logs.")); }
                    else {
                        invite = new Invite(SharedConstants.getCurrentVersion().getName(), address, port).encode();
                        source.sendSuccess(() -> Component.literal("LinceLink: ready. Use /lincelink invite to see the invitation."), false);
                    }
                }));
            } catch (Exception error) { LOGGER.error("LinceLink: tunnel could not start", error); reset(); source.sendFailure(Component.literal("LinceLink: tunnel could not start.")); }
        }));
        return 1;
    }

    private static int invite(CommandSourceStack source) {
        if (invite == null || tunnel == null || !tunnel.alive()) { source.sendFailure(Component.literal("LinceLink is not running.")); return 0; }
        String code = invite;
        source.sendSuccess(() -> Component.literal("[Click to copy invite]").withStyle(style -> style
                .withColor(ChatFormatting.AQUA).withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy the LinceLink invite"))))
                .append(Component.literal("\n" + code).withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code)))), false);
        return 1;
    }
    private static int status(CommandSourceStack source) {
        String state = starting ? "starting" : tunnel != null && tunnel.alive() && invite != null ? "running" : "stopped";
        source.sendSuccess(() -> Component.literal("LinceLink: " + state), false); return 1;
    }
    private static int stop(CommandSourceStack source) {
        reset(); source.sendSuccess(() -> Component.literal("LinceLink: stopped."), false); return 1;
    }
    private static void reset() {
        generation++; starting = false; invite = null;
        if (tunnel != null) tunnel.close();
        tunnel = null;
    }
    @SubscribeEvent public static void serverStopping(ServerStoppingEvent event) { reset(); }
}
