package pt.tailcatmod;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.util.HttpUtil;
import pt.tailcatmod.transport.*;

final class TailcatScreen extends Screen {
    private static final ResourceLocation LOGO = new ResourceLocation("tailcatmod", "textures/gui/lincelink.png");
    private final Screen parent;
    private final boolean host;
    private EditBox input;
    private Component status = Component.empty();
    private Button action;
    private boolean busy;
    private int request;
    private boolean installing;
    TailcatScreen(Screen parent, boolean host) {
        super(UiText.title(host));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int panel = Math.min(440, width - 32), half = (panel - 10) / 2;
        int x = (width - panel) / 2, y = Math.max(76, height / 2 - 40);
        input = new EditBox(font, x, y, panel, 20, Component.translatable("addServer.enterIp"));
        input.setHint(Component.translatable("addServer.enterIp"));
        input.setMaxLength(8192);
        if (host && TailcatMod.invite != null) input.setValue(TailcatMod.invite);
        input.setEditable(!host);
        addRenderableWidget(input);
        action = addRenderableWidget(Button.builder(Component.translatable(host ? "lanServer.start" : "selectServer.select"), b -> start()).bounds(x, y + 28, half, 20).build());
        action.active = !busy && !(host && TailcatMod.invite != null);
        addRenderableWidget(Button.builder(host ? Component.translatable("chat.copy") : UiText.paste(), b -> {
            if (host) { if (TailcatMod.invite != null) minecraft.keyboardHandler.setClipboard(TailcatMod.invite); }
            else input.setValue(minecraft.keyboardHandler.getClipboard());
        }).bounds(x + half + 10, y + 28, half, 20).build());
        addRenderableWidget(Button.builder(UiText.branded("menu.disconnect"), b -> {
            request++; installing = false; TailcatMod.stop(); busy = false; input.setValue(""); status = UiText.branded("disconnect.closed"); action.active = true;
        }).bounds(x, y + 56, panel, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose()).bounds(x, y + 84, panel, 20).build());
    }
    private void start() {
        if (busy) return;
        try {
            if (!host) Invite.parse(input.getValue(), SharedConstants.getCurrentVersion().getName());
        } catch (Exception e) { status = UiText.reportFailure(e); return; }
        busy = true; installing = true; action.active = false;
        status = UiText.branded("mco.download.preparing");
        int token = ++request;
        TailcatInstaller.ensure(minecraft.gameDirectory.toPath()).whenComplete((path, error) -> minecraft.execute(() -> {
            if (request != token) return;
            installing = false;
            if (error != null) {
                busy = false; action.active = true; status = UiText.reportFailure(error);
            } else openTunnel();
        }));
    }
    private void openTunnel() {
        try {
            String argument;
            int port;
            if (host) {
                var server = minecraft.getSingleplayerServer();
                if (server == null) throw new TransportFailure("menu.singleplayer", "Open a singleplayer world first", "");
                if (!server.isPublished() && !server.publishServer(server.getDefaultGameType(), false, HttpUtil.getAvailablePort()))
                    throw new TransportFailure("lanServer.port.unavailable", "Could not publish the integrated LAN server", "");
                port = server.getPort();
                argument = "";
            } else {
                Invite invite = Invite.parse(input.getValue(), SharedConstants.getCurrentVersion().getName());
                argument = invite.address(); port = invite.port();
            }
            TailcatMod.stop();
            Tunnel session = new Tunnel();
            TailcatMod.tunnel = session; TailcatMod.hosting = host;
            if (host) RouteNetwork.startHost(session);
            busy = true; action.active = false; status = UiText.branded("connect.connecting");
            session.start(minecraft.gameDirectory.toPath(), host, argument, port).whenComplete((value, error) -> minecraft.execute(() -> {
                if (TailcatMod.tunnel != session) return;
                busy = false;
                if (error != null) {
                    TailcatMod.stop(); action.active = true;
                    status = UiText.reportFailure(error);
                } else if (host) {
                    TailcatMod.invite = new Invite(SharedConstants.getCurrentVersion().getName(), value, port).encode();
                    input.setValue(TailcatMod.invite); status = Component.translatable("commands.publish.started", port).append(" · ").append(Component.translatable("chat.copy.click"));
                } else {
                    String local = "127.0.0.1:" + value;
                    ConnectScreen.startConnecting(parent, minecraft, ServerAddress.parseString(local), new ServerData("LinceLink", local, false), false);
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = UiText.reportFailure(e);
        }
    }
    @Override public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int panel = Math.min(440, width - 32), y = Math.max(76, height / 2 - 40);
        context.pose().pushPose();
        context.pose().translate(width / 2.0F - 16, y - 68, 0);
        context.pose().scale(32.0F / 1254, 32.0F / 1254, 1);
        context.blit(LOGO, 0, 0, 0, 0, 1254, 1254, 1254, 1254);
        context.pose().popPose();
        context.drawCenteredString(font, title, width / 2, y - 28, 0xFFFFFF);
        Component message = installing || status.getString().isEmpty() && TailcatMod.invite == null ? UiText.progress() : status;
        context.drawWordWrap(font, message, (width - panel) / 2, y + 114, panel, 0xFFFFFF);
    }
    @Override public void onClose() {
        if (installing) { request++; installing = false; busy = false; }
        else if (busy && !host) { TailcatMod.stop(); busy = false; }
        minecraft.setScreen(parent);
    }
}
