package pt.tailcatmod;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.NetworkUtils;
import pt.tailcatmod.transport.*;

final class TailcatScreen extends Screen {
    private final Screen parent;
    private final boolean host;
    private TextFieldWidget input;
    private String status = "";
    private ButtonWidget action;
    private boolean busy;
    TailcatScreen(Screen parent, boolean host) {
        super(Text.literal(host ? "Abrir para LinceLink" : "Conectar com LinceLink"));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new TextFieldWidget(textRenderer, x, y, 300, 20, Text.literal("Convite LinceLink"));
        input.setMaxLength(8192);
        if (host && TailcatMod.invite != null) input.setText(TailcatMod.invite);
        input.setEditable(!host);
        addDrawableChild(input);
        action = addDrawableChild(ButtonWidget.builder(Text.literal(host ? "Abrir mundo" : "Conectar"), b -> start()).dimensions(x, y + 28, 145, 20).build());
        action.active = !busy && !(host && TailcatMod.invite != null);
        addDrawableChild(ButtonWidget.builder(Text.literal(host ? "Copiar convite" : "Colar convite"), b -> {
            if (host) { if (TailcatMod.invite != null) client.keyboard.setClipboard(TailcatMod.invite); }
            else input.setText(client.keyboard.getClipboard());
        }).dimensions(x + 155, y + 28, 145, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Parar LinceLink"), b -> {
            TailcatMod.stop(); busy = false; input.setText(""); status = "Ligação LinceLink terminada."; action.active = true;
        }).dimensions(x, y + 56, 300, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar"), b -> close()).dimensions(x, y + 84, 300, 20).build());
    }
    private void start() {
        try {
            String argument;
            int port;
            if (host) {
                var server = client.getServer();
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
                    ConnectScreen.connect(parent, client, ServerAddress.parse(local), new ServerInfo("LinceLink", local, ServerInfo.ServerType.OTHER), false, null);
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = "Falha: " + e.getMessage();
        }
    }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 70, 0xFFFFFF);
        context.drawTextWrapped(textRenderer, Text.literal(status), width / 2 - 150, height / 2 + 72, 300, 0xFFFFFF);
    }
    @Override public void close() {
        if (busy && !host) { TailcatMod.stop(); busy = false; }
        client.setScreen(parent);
    }
}
