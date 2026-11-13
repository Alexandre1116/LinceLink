package pt.tailcatmod;

import net.minecraft.SharedConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.HttpUtil;
import pt.tailcatmod.transport.*;

final class TailcatScreen extends Screen {
    private final Screen parent;
    private final boolean host;
    private EditBox input;
    private String status = "";
    private Button action;
    private boolean busy;
    private int request;
    TailcatScreen(Screen parent, boolean host) {
        super(new TextComponent(host ? net.minecraft.client.resources.language.I18n.get("lincelink.ui.host") : net.minecraft.client.resources.language.I18n.get("lincelink.ui.join")));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new EditBox(font, x, y, 300, 20, new TextComponent(net.minecraft.client.resources.language.I18n.get("lincelink.ui.invite")));
        input.setMaxLength(8192);
        if (host && TailcatMod.invite != null) input.setValue(TailcatMod.invite);
        input.setEditable(!host);
        addRenderableWidget(input);
        action = addRenderableWidget(new Button(x, y + 28, 145, 20, new TextComponent(host ? net.minecraft.client.resources.language.I18n.get("lincelink.ui.start") : net.minecraft.client.resources.language.I18n.get("lincelink.ui.connect")), b -> start()));
        action.active = !busy && !(host && TailcatMod.invite != null);
        addRenderableWidget(new Button(x + 155, y + 28, 145, 20, new TextComponent(host ? net.minecraft.client.resources.language.I18n.get("lincelink.ui.copy") : net.minecraft.client.resources.language.I18n.get("lincelink.ui.paste")), b -> {
            if (host) { if (TailcatMod.invite != null) minecraft.keyboardHandler.setClipboard(TailcatMod.invite); }
            else input.setValue(minecraft.keyboardHandler.getClipboard());
        }));
        addRenderableWidget(new Button(x, y + 56, 300, 20, new TextComponent(net.minecraft.client.resources.language.I18n.get("lincelink.ui.stop")), b -> {
            request++; TailcatMod.stop(); busy = false; input.setValue(""); status = net.minecraft.client.resources.language.I18n.get("lincelink.ui.stopped"); action.active = true;
        }));
        addRenderableWidget(new Button(x, y + 84, 300, 20, new TextComponent(net.minecraft.client.resources.language.I18n.get("lincelink.ui.back")), b -> onClose()));
    }
    private void start() {
        if (busy) return;
        try {
            if (!host) Invite.parse(input.getValue(), SharedConstants.getCurrentVersion().getName());
        } catch (Exception e) { status = net.minecraft.client.resources.language.I18n.get("lincelink.ui.error") + " " + e.getMessage(); return; }
        busy = true; action.active = false; status = net.minecraft.client.resources.language.I18n.get("lincelink.ui.preparing");
        int token = ++request;
        TailcatInstaller.ensure(minecraft.gameDirectory.toPath()).whenComplete((path, error) -> minecraft.execute(() -> {
            if (request != token) return;
            if (error != null) { busy = false; action.active = true; status = net.minecraft.client.resources.language.I18n.get("lincelink.ui.error") + " " + error.getMessage(); }
            else openTunnel();
        }));
    }
    private void openTunnel() {
        try {
            String argument;
            int port;
            if (host) {
                var server = minecraft.getSingleplayerServer();
                if (server == null) throw new IllegalStateException(net.minecraft.client.resources.language.I18n.get("lincelink.ui.need_world"));
                if (!server.isPublished() && !server.publishServer(server.getDefaultGameType(), false, HttpUtil.getAvailablePort()))
                    throw new IllegalStateException(net.minecraft.client.resources.language.I18n.get("lincelink.ui.lan_failed"));
                port = server.getPort();
                argument = "";
            } else {
                Invite invite = Invite.parse(input.getValue(), SharedConstants.getCurrentVersion().getName());
                argument = invite.address(); port = invite.port();
            }
            TailcatMod.stop();
            Tunnel session = new Tunnel();
            TailcatMod.tunnel = session; TailcatMod.hosting = host;
            busy = true; action.active = false; status = net.minecraft.client.resources.language.I18n.get("lincelink.ui.starting");
            session.start(minecraft.gameDirectory.toPath(), host, argument, port).whenComplete((value, error) -> minecraft.execute(() -> {
                if (TailcatMod.tunnel != session) return;
                busy = false;
                if (error != null) {
                    TailcatMod.stop(); action.active = true;
                    status = net.minecraft.client.resources.language.I18n.get("lincelink.ui.error") + " " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
                } else if (host) {
                    TailcatMod.invite = new Invite(SharedConstants.getCurrentVersion().getName(), value, port).encode();
                    input.setValue(TailcatMod.invite); status = net.minecraft.client.resources.language.I18n.get("lincelink.ui.opened");
                } else {
                    String local = "127.0.0.1:" + value;
                    ConnectScreen.startConnecting(parent, minecraft, ServerAddress.parseString(local), new ServerData("LinceLink", local, false));
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = net.minecraft.client.resources.language.I18n.get("lincelink.ui.error") + " " + e.getMessage();
        }
    }
    @Override public void render(PoseStack context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawCenteredString(context, font, title, width / 2, height / 2 - 70, 0xFFFFFF);
        int lineY = height / 2 + 72;
        for (net.minecraft.util.FormattedCharSequence line : font.split(new TextComponent(status), 300)) {
            font.drawShadow(context, line, width / 2 - 150, lineY, 0xFFFFFF); lineY += 10;
        }
    }
    @Override public void onClose() {
        if (busy) { request++; if (!host) TailcatMod.stop(); busy = false; }
        minecraft.setScreen(parent);
    }
}
