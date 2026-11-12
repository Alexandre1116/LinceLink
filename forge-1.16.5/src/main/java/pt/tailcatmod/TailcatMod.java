package pt.tailcatmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
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
        @SubscribeEvent public static void screen(GuiScreenEvent.InitGuiEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            net.minecraft.client.gui.screen.Screen screen = event.getGui();
            if ((screen instanceof IngameMenuScreen && client.isSingleplayer()) || screen instanceof MultiplayerScreen) {
                boolean host = screen instanceof IngameMenuScreen;
                event.addWidget(new Button(8, 8, 180, 20, new StringTextComponent(host ? "Abrir para LinceLink" : "Conectar com LinceLink"), button -> client.displayGuiScreen(new TailcatScreen(screen, host))));
            }
        }
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft client = Minecraft.getInstance();
            if (tunnel == null) return;
            if (hosting && client.getIntegratedServer() == null) stop();
            else if (!hosting) {
                if (client.world != null) joined = true;
                else if (joined) stop();
            }
        }
    }
}
