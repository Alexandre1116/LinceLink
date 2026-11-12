package pt.tailcatmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import pt.tailcatmod.transport.Tunnel;

public final class TailcatMod implements ClientModInitializer {
    static Tunnel tunnel;
    static String invite;
    static boolean hosting, joined;
    static void stop() {
        if (tunnel != null) tunnel.close();
        tunnel = null; invite = null; hosting = false; joined = false;
    }
    @Override public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if ((screen instanceof GameMenuScreen && client.isInSingleplayer()) || screen instanceof MultiplayerScreen) {
                boolean host = screen instanceof GameMenuScreen;
                Screens.getButtons(screen).add(new ButtonWidget(8, 8, 180, 20, new net.minecraft.text.LiteralText(host ? "Abrir para LinceLink" : "Conectar com LinceLink"), button -> client.setScreen(new TailcatScreen(screen, host))));
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> stop());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (tunnel == null) return;
            if (hosting && client.getServer() == null) stop();
            else if (!hosting) {
                if (client.world != null) joined = true;
                else if (joined) stop();
            }
        });
    }
}
