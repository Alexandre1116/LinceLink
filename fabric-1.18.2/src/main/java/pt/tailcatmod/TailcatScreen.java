package pt.tailcatmod;

import net.minecraft.SharedConstants;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.client.util.NetworkUtils;
import pt.tailcatmod.transport.*;

final class TailcatScreen extends Screen {
    private final Screen parent;
    private final boolean host;
    private TextFieldWidget input;
    private String status = "";
    private ButtonWidget action;
    private boolean busy;
    private int request;
    TailcatScreen(Screen parent, boolean host) {
        super(new net.minecraft.text.LiteralText(host ? net.minecraft.client.resource.language.I18n.translate("lincelink.ui.host") : net.minecraft.client.resource.language.I18n.translate("lincelink.ui.join")));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new TextFieldWidget(textRenderer, x, y, 300, 20, new net.minecraft.text.LiteralText(net.minecraft.client.resource.language.I18n.translate("lincelink.ui.invite")));
        input.setMaxLength(8192);
        if (host && TailcatMod.invite != null) input.setText(TailcatMod.invite);
        input.setEditable(!host);
        addDrawableChild(input);
        action = addDrawableChild(new ButtonWidget(x, y + 28, 145, 20, new net.minecraft.text.LiteralText(host ? net.minecraft.client.resource.language.I18n.translate("lincelink.ui.start") : net.minecraft.client.resource.language.I18n.translate("lincelink.ui.connect")), b -> start()));
        action.active = !busy && !(host && TailcatMod.invite != null);
        addDrawableChild(new ButtonWidget(x + 155, y + 28, 145, 20, new net.minecraft.text.LiteralText(host ? net.minecraft.client.resource.language.I18n.translate("lincelink.ui.copy") : net.minecraft.client.resource.language.I18n.translate("lincelink.ui.paste")), b -> {
            if (host) { if (TailcatMod.invite != null) client.keyboard.setClipboard(TailcatMod.invite); }
            else input.setText(client.keyboard.getClipboard());
        }));
        addDrawableChild(new ButtonWidget(x, y + 56, 300, 20, new net.minecraft.text.LiteralText(net.minecraft.client.resource.language.I18n.translate("lincelink.ui.stop")), b -> {
            request++; TailcatMod.stop(); busy = false; input.setText(""); status = net.minecraft.client.resource.language.I18n.translate("lincelink.ui.stopped"); action.active = true;
        }));
        addDrawableChild(new ButtonWidget(x, y + 84, 300, 20, new net.minecraft.text.LiteralText(net.minecraft.client.resource.language.I18n.translate("lincelink.ui.back")), b -> close()));
    }
    private void start() {
        if (busy) return;
        try {
            if (!host) Invite.parse(input.getText(), SharedConstants.getGameVersion().getName());
        } catch (Exception e) { status = net.minecraft.client.resource.language.I18n.translate("lincelink.ui.error") + " " + e.getMessage(); return; }
        busy = true; action.active = false; status = net.minecraft.client.resource.language.I18n.translate("lincelink.ui.preparing");
        int token = ++request;
        TailcatInstaller.ensure(client.runDirectory.toPath()).whenComplete((path, error) -> client.execute(() -> {
            if (request != token) return;
            if (error != null) { busy = false; action.active = true; status = net.minecraft.client.resource.language.I18n.translate("lincelink.ui.error") + " " + error.getMessage(); }
            else openTunnel();
        }));
    }
    private void openTunnel() {
        try {
            String argument;
            int port;
            if (host) {
                net.minecraft.server.integrated.IntegratedServer server = client.getServer();
                if (server == null) throw new IllegalStateException(net.minecraft.client.resource.language.I18n.translate("lincelink.ui.need_world"));
                if (!server.isRemote() && !server.openToLan(server.getDefaultGameMode(), false, NetworkUtils.findLocalPort()))
                    throw new IllegalStateException(net.minecraft.client.resource.language.I18n.translate("lincelink.ui.lan_failed"));
                port = server.getServerPort();
                argument = "";
            } else {
                Invite invite = Invite.parse(input.getText(), SharedConstants.getGameVersion().getName());
                argument = invite.address(); port = invite.port();
            }
            TailcatMod.stop();
            Tunnel session = new Tunnel();
            TailcatMod.tunnel = session; TailcatMod.hosting = host;
            busy = true; action.active = false; status = net.minecraft.client.resource.language.I18n.translate("lincelink.ui.starting");
            session.start(client.runDirectory.toPath(), host, argument, port).whenComplete((value, error) -> client.execute(() -> {
                if (TailcatMod.tunnel != session) return;
                busy = false;
                if (error != null) {
                    TailcatMod.stop(); action.active = true;
                    status = net.minecraft.client.resource.language.I18n.translate("lincelink.ui.error") + " " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
                } else if (host) {
                    TailcatMod.invite = new Invite(SharedConstants.getGameVersion().getName(), value, port).encode();
                    input.setText(TailcatMod.invite); status = net.minecraft.client.resource.language.I18n.translate("lincelink.ui.opened");
                } else {
                    String local = "127.0.0.1:" + value;
                    ConnectScreen.connect(parent, client, ServerAddress.parse(local), new ServerInfo("LinceLink", local, false));
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = net.minecraft.client.resource.language.I18n.translate("lincelink.ui.error") + " " + e.getMessage();
        }
    }
    @Override public void render(MatrixStack context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawCenteredText(context, textRenderer, title, width / 2, height / 2 - 70, 0xFFFFFF);
        int lineY = height / 2 + 72;
        for (net.minecraft.text.OrderedText line : textRenderer.wrapLines(new net.minecraft.text.LiteralText(status), 300)) {
            textRenderer.drawWithShadow(context, line, width / 2 - 150, lineY, 0xFFFFFF); lineY += 10;
        }
    }
    @Override public void close() {
        if (busy) { request++; if (!host) TailcatMod.stop(); busy = false; }
        client.setScreen(parent);
    }
}
