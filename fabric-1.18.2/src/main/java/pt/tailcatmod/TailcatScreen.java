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
    TailcatScreen(Screen parent, boolean host) {
        super(new net.minecraft.text.LiteralText(host ? "Abrir para LinceLink" : "Conectar com LinceLink"));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new TextFieldWidget(textRenderer, x, y, 300, 20, new net.minecraft.text.LiteralText("Convite LinceLink"));
        input.setMaxLength(8192);
        if (host && TailcatMod.invite != null) input.setText(TailcatMod.invite);
        input.setEditable(!host);
        addDrawableChild(input);
        action = addDrawableChild(new ButtonWidget(x, y + 28, 145, 20, new net.minecraft.text.LiteralText(host ? "Abrir mundo" : "Conectar"), b -> start()));
        action.active = !busy && !(host && TailcatMod.invite != null);
        addDrawableChild(new ButtonWidget(x + 155, y + 28, 145, 20, new net.minecraft.text.LiteralText(host ? "Copiar convite" : "Colar convite"), b -> {
            if (host) { if (TailcatMod.invite != null) client.keyboard.setClipboard(TailcatMod.invite); }
            else input.setText(client.keyboard.getClipboard());
        }));
        addDrawableChild(new ButtonWidget(x, y + 56, 300, 20, new net.minecraft.text.LiteralText("Parar LinceLink"), b -> {
            TailcatMod.stop(); busy = false; input.setText(""); status = "Ligação LinceLink terminada."; action.active = true;
        }));
        addDrawableChild(new ButtonWidget(x, y + 84, 300, 20, new net.minecraft.text.LiteralText("Voltar"), b -> close()));
    }
    private void start() {
        try {
            String argument;
            int port;
            if (host) {
                net.minecraft.server.integrated.IntegratedServer server = client.getServer();
                if (server == null) throw new IllegalStateException("Abre primeiro um mundo singleplayer.");
                if (!server.isRemote() && !server.openToLan(server.getDefaultGameMode(), false, NetworkUtils.findLocalPort()))
                    throw new IllegalStateException("Não foi possível abrir o servidor LAN.");
                port = server.getServerPort();
                argument = "";
            } else {
                Invite invite = Invite.parse(input.getText(), SharedConstants.getGameVersion().getName());
                argument = invite.address(); port = invite.port();
            }
            TailcatMod.stop();
            Tunnel session = new Tunnel();
            TailcatMod.tunnel = session; TailcatMod.hosting = host;
            busy = true; action.active = false; status = "A iniciar LinceLink…";
            session.start(client.runDirectory.toPath(), host, argument, port).whenComplete((value, error) -> client.execute(() -> {
                if (TailcatMod.tunnel != session) return;
                busy = false;
                if (error != null) {
                    TailcatMod.stop(); action.active = true;
                    status = "Falha: " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
                } else if (host) {
                    TailcatMod.invite = new Invite(SharedConstants.getGameVersion().getName(), value, port).encode();
                    input.setText(TailcatMod.invite); status = "Mundo aberto. Copia o convite e envia-o ao teu amigo.";
                } else {
                    String local = "127.0.0.1:" + value;
                    ConnectScreen.connect(parent, client, ServerAddress.parse(local), new ServerInfo("LinceLink", local, false));
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = "Falha: " + e.getMessage();
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
        if (busy && !host) { TailcatMod.stop(); busy = false; }
        client.setScreen(parent);
    }
}
