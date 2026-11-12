package pt.tailcatmod;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;
import pt.tailcatmod.transport.Tunnel;

/** Explicit integration test: uses two official Tailcat processes and a live TCP stream. */
public final class RouteSmoke {
    public static void main(String[] args) throws Exception {
        PeerRoutes peers = new PeerRoutes();
        PeerRoutes.Route expected = PeerRoutes.Route.valueOf(args[0]);
        try (ServerSocket echo = new ServerSocket(0, 8, InetAddress.getLoopbackAddress()); Tunnel host = new Tunnel(); Tunnel guest = new Tunnel()) {
            host.onStatus(json -> {
                peers.accept(json, System.currentTimeMillis());
                String capture = System.getProperty("tailcat.route.capture");
                if (capture != null) try { Files.writeString(Paths.get(capture), json); } catch (IOException e) { throw new UncheckedIOException(e); }
            });
            Thread server = new Thread(() -> {
                try (Socket socket = echo.accept()) {
                    socket.setSoTimeout(30000);
                    byte[] buffer = new byte[256]; int n;
                    while ((n = socket.getInputStream().read(buffer)) != -1) socket.getOutputStream().write(buffer, 0, n);
                } catch (IOException e) { if (!echo.isClosed()) throw new UncheckedIOException(e); }
            });
            server.setDaemon(true); server.start();
            String address = host.start(Paths.get("."), true, "", echo.getLocalPort()).get(65, TimeUnit.SECONDS);
            int port = Integer.parseInt(guest.start(Paths.get("."), false, address, echo.getLocalPort()).get(65, TimeUnit.SECONDS));
            if (guest.clientPublicKey() == null) throw new AssertionError("Missing guest identity");
            try (Socket socket = new Socket("127.0.0.1", port)) {
                socket.setSoTimeout(30000);
                socket.getOutputStream().write(42);
                if (socket.getInputStream().read() != 42) throw new AssertionError("TCP failed");
                long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(25);
                PeerRoutes.Route actual;
                do {
                    actual = peers.route(guest.clientPublicKey(), System.currentTimeMillis());
                    if (actual == expected) break;
                    Thread.sleep(100);
                } while (System.nanoTime() < until);
                if (actual != expected) throw new AssertionError("Expected " + expected + ", got " + actual);
                System.out.println("PASS: real TCP stream, guest public key matched to host status, route " + actual);
            }
        }
    }
}
