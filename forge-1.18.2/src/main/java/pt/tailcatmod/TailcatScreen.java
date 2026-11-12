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
    TailcatScreen(Screen parent, boolean host) {
        super(new TextComponent(host ? "Abrir para Tailcat" : "Conectar com Tailcat"));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new EditBox(font, x, y, 300, 20, new TextComponent("Convite Tailcat"));
        input.setMaxLength(8192);
        if (host && TailcatMod.invite != null) input.setValue(TailcatMod.invite);
        input.setEditable(!host);
        addRenderableWidget(input);
        action = addRenderableWidget(new Button(x, y + 28, 145, 20, new TextComponent(host ? "Abrir mundo" : "Conectar"), b -> start()));
        action.active = !busy && !(host && TailcatMod.invite != null);
        addRenderableWidget(new Button(x + 155, y + 28, 145, 20, new TextComponent(host ? "Copiar convite" : "Colar convite"), b -> {
            if (host) { if (TailcatMod.invite != null) minecraft.keyboardHandler.setClipboard(TailcatMod.invite); }
            else input.setValue(minecraft.keyboardHandler.getClipboard());
        }));
        addRenderableWidget(new Button(x, y + 56, 300, 20, new TextComponent("Parar Tailcat"), b -> {
            TailcatMod.stop(); busy = false; input.setValue(""); status = "Ligação Tailcat terminada."; action.active = true;
        }));
        addRenderableWidget(new Button(x, y + 84, 300, 20, new TextComponent("Voltar"), b -> onClose()));
    }
    private void start() {
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
            busy = true; action.active = false; status = "A iniciar Tailcat…";
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
                    ConnectScreen.startConnecting(parent, minecraft, ServerAddress.parseString(local), new ServerData("Tailcat", local, false));
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = "Falha: " + e.getMessage();
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
        if (busy && !host) { TailcatMod.stop(); busy = false; }
        minecraft.setScreen(parent);
    }
}
