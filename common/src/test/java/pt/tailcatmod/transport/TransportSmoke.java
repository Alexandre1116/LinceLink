package pt.tailcatmod.transport;

import java.net.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Run explicitly: uses the Internet and official Tailcat binary. */
public final class TransportSmoke {
    private static byte[] readFive(Socket socket) throws java.io.IOException {
        byte[] data = new byte[5]; new java.io.DataInputStream(socket.getInputStream()).readFully(data); return data;
    }
    public static void main(String[] args) throws Exception {
        Invite sample = new Invite("1.21.1", "tc" + String.join("", java.util.Collections.nCopies(40, "a")), 25565);
        if (!Invite.parse(sample.encode(), "1.21.1").encode().equals(sample.encode())) throw new AssertionError();
        try { Invite.parse(sample.encode(), "1.20.1"); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
        try { Invite.parse("tailcat-mc:1:1.21.1:--help", "1.21.1"); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
        try (ServerSocket echo = new ServerSocket(0, 8, InetAddress.getLoopbackAddress()); Tunnel host = new Tunnel(); Tunnel guest = new Tunnel()) {
            Thread echoThread = new Thread(() -> {
                try (Socket socket = echo.accept()) { socket.getOutputStream().write(readFive(socket)); }
                catch (Exception e) { if (!echo.isClosed()) throw new RuntimeException(e); }
            });
            echoThread.setDaemon(true); echoThread.start();
            String address = host.start(java.nio.file.Paths.get("."), true, "", echo.getLocalPort()).get(65, TimeUnit.SECONDS);
            String port = guest.start(java.nio.file.Paths.get("."), false, address, echo.getLocalPort()).get(65, TimeUnit.SECONDS);
            try (Socket socket = new Socket("127.0.0.1", Integer.parseInt(port))) {
                socket.setSoTimeout(30000);
                socket.getOutputStream().write("hello".getBytes());
                if (!new String(readFive(socket)).equals("hello")) throw new AssertionError("TCP mismatch");
            }
            System.out.println("PASS: invite validation and TCP round trip through two real Tailcat processes");
        }
    }
}
