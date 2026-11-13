package pt.tailcatmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.client.event.GuiScreenEvent;
import pt.tailcatmod.transport.Tunnel;

@Mod(modid="tailcatmod", name="LinceLink", version="0.2.0", clientSideOnly=true, acceptedMinecraftVersions="[1.12.2]")
@Mod.EventBusSubscriber(modid="tailcatmod", value=Side.CLIENT)
public final class TailcatMod {
    static Tunnel tunnel;
    static String invite;
    static boolean hosting, joined;
    static void stop() {
        if (tunnel != null) tunnel.close();
        tunnel = null; invite = null; hosting = false; joined = false;
    }
    public TailcatMod() { Runtime.getRuntime().addShutdownHook(new Thread(TailcatMod::stop)); }
    @SubscribeEvent public static void screen(GuiScreenEvent.InitGuiEvent.Post e) {
        boolean host = e.getGui() instanceof GuiIngameMenu && Minecraft.getMinecraft().isSingleplayer();
        if (host || e.getGui() instanceof GuiMultiplayer)
            e.getButtonList().add(new GuiButton(88652, 8, 8, 180, 20, host ? net.minecraft.client.resources.I18n.format("lincelink.ui.host") : net.minecraft.client.resources.I18n.format("lincelink.ui.join")));
    }
    @SubscribeEvent public static void click(GuiScreenEvent.ActionPerformedEvent.Post e) {
        if (e.getButton().id == 88652) Minecraft.getMinecraft().displayGuiScreen(new TailcatScreen(e.getGui(), e.getGui() instanceof GuiIngameMenu));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || tunnel == null) return;
        Minecraft client = Minecraft.getMinecraft();
        if (hosting && client.getIntegratedServer() == null) stop();
        else if (!hosting) { if (client.world != null) joined = true; else if (joined) stop(); }
    }
}
