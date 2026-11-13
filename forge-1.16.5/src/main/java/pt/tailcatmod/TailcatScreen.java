package pt.tailcatmod;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.TextFieldWidget;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import pt.tailcatmod.transport.*;

final class TailcatScreen extends Screen {
    private final Screen parent;
    private final boolean host;
    private TextFieldWidget input;
    private String status = "";
    private Button action;
    private boolean busy;
    private int request;
    TailcatScreen(Screen parent, boolean host) {
        super(new StringTextComponent(host ? net.minecraft.client.resources.I18n.format("lincelink.ui.host") : net.minecraft.client.resources.I18n.format("lincelink.ui.join")));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new TextFieldWidget(font, x, y, 300, 20, new StringTextComponent(net.minecraft.client.resources.I18n.format("lincelink.ui.invite")));
        input.setMaxStringLength(8192);
        if (host && TailcatMod.invite != null) input.setText(TailcatMod.invite);
        input.setEnabled(!host);
        addButton(input);
        action = addButton(new Button(x, y + 28, 145, 20, new StringTextComponent(host ? net.minecraft.client.resources.I18n.format("lincelink.ui.start") : net.minecraft.client.resources.I18n.format("lincelink.ui.connect")), b -> start()));
        action.active = !busy && !(host && TailcatMod.invite != null);
        addButton(new Button(x + 155, y + 28, 145, 20, new StringTextComponent(host ? net.minecraft.client.resources.I18n.format("lincelink.ui.copy") : net.minecraft.client.resources.I18n.format("lincelink.ui.paste")), b -> {
            if (host) { if (TailcatMod.invite != null) minecraft.keyboardListener.setClipboardString(TailcatMod.invite); }
            else input.setText(minecraft.keyboardListener.getClipboardString());
        }));
        addButton(new Button(x, y + 56, 300, 20, new StringTextComponent(net.minecraft.client.resources.I18n.format("lincelink.ui.stop")), b -> {
            request++; TailcatMod.stop(); busy = false; input.setText(""); status = net.minecraft.client.resources.I18n.format("lincelink.ui.stopped"); action.active = true;
        }));
        addButton(new Button(x, y + 84, 300, 20, new StringTextComponent(net.minecraft.client.resources.I18n.format("lincelink.ui.back")), b -> closeScreen()));
    }
    private static int freePort() throws java.io.IOException {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) { return socket.getLocalPort(); }
    }
    private void start() {
        if (busy) return;
        try { if (!host) Invite.parse(input.getText(), "1.16.5"); }
        catch (Exception e) { status = net.minecraft.client.resources.I18n.format("lincelink.ui.error") + " " + e.getMessage(); return; }
        busy = true; action.active = false; status = net.minecraft.client.resources.I18n.format("lincelink.ui.preparing");
        int token = ++request;
        TailcatInstaller.ensure(minecraft.gameDir.toPath()).whenComplete((path, error) -> minecraft.execute(() -> {
            if (request != token) return;
            if (error != null) { busy = false; action.active = true; status = net.minecraft.client.resources.I18n.format("lincelink.ui.error") + " " + error.getMessage(); }
            else openTunnel();
        }));
    }
    private void openTunnel() {
        try {
            String argument;
            int port;
            if (host) {
                net.minecraft.server.integrated.IntegratedServer server = minecraft.getIntegratedServer();
                if (server == null) throw new IllegalStateException(net.minecraft.client.resources.I18n.format("lincelink.ui.need_world"));
                if (!server.getPublic() && !server.shareToLAN(server.getGameType(), false, freePort()))
                    throw new IllegalStateException(net.minecraft.client.resources.I18n.format("lincelink.ui.lan_failed"));
                port = server.getServerPort();
                argument = "";
            } else {
                Invite invite = Invite.parse(input.getText(), "1.16.5");
                argument = invite.address(); port = invite.port();
            }
            TailcatMod.stop();
            Tunnel session = new Tunnel();
            TailcatMod.tunnel = session; TailcatMod.hosting = host;
            busy = true; action.active = false; status = net.minecraft.client.resources.I18n.format("lincelink.ui.starting");
            session.start(minecraft.gameDir.toPath(), host, argument, port).whenComplete((value, error) -> minecraft.execute(() -> {
                if (TailcatMod.tunnel != session) return;
                busy = false;
                if (error != null) {
                    TailcatMod.stop(); action.active = true;
                    status = net.minecraft.client.resources.I18n.format("lincelink.ui.error") + " " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
                } else if (host) {
                    TailcatMod.invite = new Invite("1.16.5", value, port).encode();
                    input.setText(TailcatMod.invite); status = net.minecraft.client.resources.I18n.format("lincelink.ui.opened");
                } else {
                    String local = "127.0.0.1:" + value;
                    minecraft.displayGuiScreen(new ConnectingScreen(parent, minecraft, new ServerData("LinceLink", local, false)));
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = net.minecraft.client.resources.I18n.format("lincelink.ui.error") + " " + e.getMessage();
        }
    }
    @Override public void render(MatrixStack context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawCenteredString(context, font, title, width / 2, height / 2 - 70, 0xFFFFFF);
        int lineY = height / 2 + 72;
        for (net.minecraft.util.IReorderingProcessor line : font.trimStringToWidth(new StringTextComponent(status), 300)) {
            font.drawTextWithShadow(context, line, width / 2 - 150, lineY, 0xFFFFFF); lineY += 10;
        }
    }
    @Override public void closeScreen() {
        if (busy) { request++; if (!host) TailcatMod.stop(); busy = false; }
        minecraft.displayGuiScreen(parent);
    }
}
