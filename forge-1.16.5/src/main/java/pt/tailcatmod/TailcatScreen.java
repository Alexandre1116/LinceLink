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
    TailcatScreen(Screen parent, boolean host) {
        super(new StringTextComponent(host ? "Abrir para LinceLink" : "Conectar com LinceLink"));
        this.parent = parent; this.host = host;
    }
    @Override protected void init() {
        int x = width / 2 - 150, y = height / 2 - 40;
        input = new TextFieldWidget(font, x, y, 300, 20, new StringTextComponent("Convite LinceLink"));
        input.setMaxStringLength(8192);
        if (host && TailcatMod.invite != null) input.setText(TailcatMod.invite);
        input.setEnabled(!host);
        addButton(input);
        action = addButton(new Button(x, y + 28, 145, 20, new StringTextComponent(host ? "Abrir mundo" : "Conectar"), b -> start()));
        action.active = !busy && !(host && TailcatMod.invite != null);
        addButton(new Button(x + 155, y + 28, 145, 20, new StringTextComponent(host ? "Copiar convite" : "Colar convite"), b -> {
            if (host) { if (TailcatMod.invite != null) minecraft.keyboardListener.setClipboardString(TailcatMod.invite); }
            else input.setText(minecraft.keyboardListener.getClipboardString());
        }));
        addButton(new Button(x, y + 56, 300, 20, new StringTextComponent("Parar LinceLink"), b -> {
            TailcatMod.stop(); busy = false; input.setText(""); status = "Ligação LinceLink terminada."; action.active = true;
        }));
        addButton(new Button(x, y + 84, 300, 20, new StringTextComponent("Voltar"), b -> closeScreen()));
    }
    private static int freePort() throws java.io.IOException {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) { return socket.getLocalPort(); }
    }
    private void start() {
        try {
            String argument;
            int port;
            if (host) {
                net.minecraft.server.integrated.IntegratedServer server = minecraft.getIntegratedServer();
                if (server == null) throw new IllegalStateException("Abre primeiro um mundo singleplayer.");
                if (!server.getPublic() && !server.shareToLAN(server.getGameType(), false, freePort()))
                    throw new IllegalStateException("Não foi possível abrir o servidor LAN.");
                port = server.getServerPort();
                argument = "";
            } else {
                Invite invite = Invite.parse(input.getText(), "1.16.5");
                argument = invite.address(); port = invite.port();
            }
            TailcatMod.stop();
            Tunnel session = new Tunnel();
            TailcatMod.tunnel = session; TailcatMod.hosting = host;
            busy = true; action.active = false; status = "A iniciar LinceLink…";
            session.start(minecraft.gameDir.toPath(), host, argument, port).whenComplete((value, error) -> minecraft.execute(() -> {
                if (TailcatMod.tunnel != session) return;
                busy = false;
                if (error != null) {
                    TailcatMod.stop(); action.active = true;
                    status = "Falha: " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
                } else if (host) {
                    TailcatMod.invite = new Invite("1.16.5", value, port).encode();
                    input.setText(TailcatMod.invite); status = "Mundo aberto. Copia o convite e envia-o ao teu amigo.";
                } else {
                    String local = "127.0.0.1:" + value;
                    minecraft.displayGuiScreen(new ConnectingScreen(parent, minecraft, new ServerData("LinceLink", local, false)));
                }
            }));
        } catch (Exception e) {
            TailcatMod.stop(); busy = false; action.active = true;
            status = "Falha: " + e.getMessage();
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
        if (busy && !host) { TailcatMod.stop(); busy = false; }
        minecraft.displayGuiScreen(parent);
    }
}
