package pt.tailcatmod;

import java.io.IOException;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.server.integrated.IntegratedServer;
import pt.tailcatmod.transport.*;

final class TailcatScreen extends GuiScreen {
    private final GuiScreen parent;
    private final boolean host;
    private GuiTextField input;
    private GuiButton action;
    private boolean busy;
    private String status = "";
    TailcatScreen(GuiScreen parent, boolean host) { this.parent = parent; this.host = host; }
    @Override public void initGui() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new GuiTextField(0, fontRenderer, x, y, 300, 20);
        input.setMaxStringLength(8192); input.setEnabled(!host); input.setFocused(!host);
        if (host && TailcatMod.invite != null) input.setText(TailcatMod.invite);
        buttonList.add(action = new GuiButton(1, x, y + 28, 145, 20, host ? "Abrir mundo" : "Conectar"));
        action.enabled = !busy && !(host && TailcatMod.invite != null);
        buttonList.add(new GuiButton(2, x + 155, y + 28, 145, 20, host ? "Copiar convite" : "Colar convite"));
        buttonList.add(new GuiButton(3, x, y + 56, 300, 20, "Parar Tailcat"));
        buttonList.add(new GuiButton(4, x, y + 84, 300, 20, "Voltar"));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 1) start();
        if (button.id == 2) { if (host) { if (TailcatMod.invite != null) setClipboardString(TailcatMod.invite); } else input.setText(getClipboardString()); }
        if (button.id == 3) { TailcatMod.stop(); busy = false; input.setText(""); action.enabled = true; status = "Ligação terminada."; }
        if (button.id == 4) back();
    }
    private void back() { if (busy && !host) TailcatMod.stop(); mc.displayGuiScreen(parent); }
    private void start() {
        try {
            String address; int port;
            if (host) {
                IntegratedServer server = mc.getIntegratedServer();
                if (server == null) throw new IllegalStateException("Abre primeiro um mundo.");
                if (!server.getPublic() && server.shareToLAN(server.getWorld(0).getWorldInfo().getGameType(), false) == null)
                    throw new IllegalStateException("Falha ao abrir o servidor LAN.");
                port = server.getServerPort(); address = "";
            } else { Invite invite = Invite.parse(input.getText(), "1.12.2"); address = invite.address(); port = invite.port(); }
            TailcatMod.stop();
            Tunnel session = new Tunnel(); TailcatMod.tunnel = session; TailcatMod.hosting = host;
            busy = true; action.enabled = false; status = "A iniciar Tailcat…";
            session.start(mc.mcDataDir.toPath(), host, address, port).whenComplete((value, error) -> mc.addScheduledTask(() -> {
                if (TailcatMod.tunnel != session) return;
                busy = false;
                if (error != null) { TailcatMod.stop(); action.enabled = true; status = "Falha: " + error.getMessage(); }
                else if (host) { TailcatMod.invite = new Invite("1.12.2", value, port).encode(); input.setText(TailcatMod.invite); status = "Mundo aberto. Copia e envia o convite."; }
                else mc.displayGuiScreen(new GuiConnecting(parent, mc, new ServerData("Tailcat", "127.0.0.1:" + value, false)));
            }));
        } catch (Exception e) { TailcatMod.stop(); busy = false; action.enabled = true; status = "Falha: " + e.getMessage(); }
    }
    @Override public void drawScreen(int mouseX, int mouseY, float delta) {
        drawDefaultBackground(); input.drawTextBox(); super.drawScreen(mouseX, mouseY, delta);
        drawCenteredString(fontRenderer, host ? "Abrir para Tailcat" : "Conectar com Tailcat", width / 2, height / 2 - 70, 0xFFFFFF);
        fontRenderer.drawSplitString(status, width / 2 - 150, height / 2 + 72, 300, 0xFFFFFF);
    }
    @Override protected void keyTyped(char ch, int key) throws IOException { if (key == 1) back(); else input.textboxKeyTyped(ch, key); }
    @Override protected void mouseClicked(int x, int y, int button) throws IOException { super.mouseClicked(x,y,button); input.mouseClicked(x,y,button); }
    @Override public void updateScreen() { input.updateCursorCounter(); }
}
