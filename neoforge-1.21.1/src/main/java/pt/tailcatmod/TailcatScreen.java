package pt.tailcatmod;

import net.minecraft.SharedConstants;
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
    private final Screen parent;
    private final boolean host;
    private EditBox input;
    private String status = "";
    private Button action;
    private boolean busy;
    private int request;
    TailcatScreen(Screen parent, boolean host) {
        super(Component.literal(host ? "Abrir para LinceLink" : "Conectar com LinceLink"));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new EditBox(font, x, y, 300, 20, Component.literal("Convite LinceLink"));
        input.setMaxLength(8192);
        if (host && TailcatMod.invite != null) input.setValue(TailcatMod.invite);
        input.setEditable(!host);
        addRenderableWidget(input);
        action = addRenderableWidget(Button.builder(Component.literal(host ? "Abrir mundo" : "Conectar"), b -> start()).bounds(x, y + 28, 145, 20).build());
        action.active = !busy && !(host && TailcatMod.invite != null);
        addRenderableWidget(Button.builder(Component.literal(host ? "Copiar convite" : "Colar convite"), b -> {
            if (host) { if (TailcatMod.invite != null) minecraft.keyboardHandler.setClipboard(TailcatMod.invite); }
            else input.setValue(minecraft.keyboardHandler.getClipboard());
        }).bounds(x + 155, y + 28, 145, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Parar LinceLink"), b -> {
            request++; TailcatMod.stop(); busy = false; input.setValue(""); status = "Ligação LinceLink terminada."; action.active = true;
        }).bounds(x, y + 56, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"), b -> onClose()).bounds(x, y + 84, 300, 20).build());
    }
    private void start() {
        if (busy) return;
        try {
            if (!host) Invite.parse(input.getValue(), SharedConstants.getCurrentVersion().getName());
        } catch (Exception e) { status = "Falha: " + e.getMessage(); return; }
        busy = true; action.active = false; status = "A preparar Tailcat…";
        int token = ++request;
        TailcatInstaller.ensure(minecraft.gameDirectory.toPath()).whenComplete((path, error) -> minecraft.execute(() -> {
            if (request != token) return;
            if (error != null) { busy = false; action.active = true; status = "Falha: " + error.getMessage(); }
            else openTunnel();
        }));
    }
    private void openTunnel() {
        try {
            String argument;
            int port;
            if (host) {
                var server = minecraft.getSingleplayerServer();
                if (server == null) throw new IllegalStateException("Abre primeiro um mundo singleplayer.");
                if (!server.isPublished() && !server.publishServer(server.getDefaultGameType(), false, HttpUtil.getAvailablePort()))
                    throw new IllegalStateException("Não foi possível abrir o servidor LAN.");
                port = server.getPort();
                argument = "";
            } else {
                Invite invite = Invite.parse(input.getValue(), SharedConstants.getCurrentVersion().getName());
                argument = invite.address(); port = invite.port();
            }
            TailcatMod.stop();
            Tunnel session = new Tunnel();
            TailcatMod.tunnel = session; TailcatMod.hosting = host;
            busy = true; action.active = false; status = "A iniciar LinceLink…";
            session.start(minecraft.gameDirectory.toPath(), host, argument, port).whenComplete((value, error) -> minecraft.execute(() -> {
                if (TailcatMod.tunnel != session) return;
                busy = false;
                if (error != null) {
                    TailcatMod.stop(); action.active = true;
                    status = "Falha: " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
                } else if (host) {
                    TailcatMod.invite = new Invite(SharedConstants.getCurrentVersion().getName(), value, port).encode();
                    input.setValue(TailcatMod.invite); status = "Mundo aberto. Copia o convite e envia-o ao teu amigo.";
                } else {
                    String local = "127.0.0.1:" + value;
                    ConnectScreen.startConnecting(parent, minecraft, ServerAddress.parseString(local), new ServerData("LinceLink", local, ServerData.Type.OTHER), false, null);
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = "Falha: " + e.getMessage();
        }
    }
    @Override public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(font, title, width / 2, height / 2 - 70, 0xFFFFFF);
        context.drawWordWrap(font, Component.literal(status), width / 2 - 150, height / 2 + 72, 300, 0xFFFFFF);
    }
    @Override public void onClose() {
        if (busy) { request++; if (!host) TailcatMod.stop(); busy = false; }
        minecraft.setScreen(parent);
    }
}
