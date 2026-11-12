package pt.tailcatmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pt.tailcatmod.transport.Tunnel;

@Mod("tailcatmod")
public final class TailcatMod {
    static Tunnel tunnel;
    static String invite;
    static boolean hosting, joined;
    static void stop() {
        if (tunnel != null) tunnel.close();
        tunnel = null; invite = null; hosting = false; joined = false;
    }
    public TailcatMod() { Runtime.getRuntime().addShutdownHook(new Thread(TailcatMod::stop)); }
    @Mod.EventBusSubscriber(modid = "tailcatmod", value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent public static void screen(ScreenEvent.Init.Post event) {
            var client = Minecraft.getInstance();
            var screen = event.getScreen();
            if ((screen instanceof PauseScreen && client.hasSingleplayerServer()) || screen instanceof JoinMultiplayerScreen) {
                boolean host = screen instanceof PauseScreen;
                event.addListener(Button.builder(Component.literal(host ? "Abrir para LinceLink" : "Conectar com LinceLink"),
                    button -> client.setScreen(new TailcatScreen(screen, host))).bounds(8, 8, 180, 20).build());
            }
        }
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event) {
            var client = Minecraft.getInstance();
            if (tunnel == null) return;
            if (hosting && client.getSingleplayerServer() == null) stop();
            else if (!hosting) {
                if (client.level != null) joined = true;
                else if (joined) stop();
            }
        }
    }
}
