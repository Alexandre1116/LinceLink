package pt.tailcatmod.transport;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.regex.*;

/** Owns one official Tailcat CLI session. Never executes a shell. */
public final class Tunnel implements AutoCloseable {
    private static final Pattern ADDRESS = Pattern.compile("new address: (tc[A-Za-z0-9_-]+)");
    private static final Pattern FORWARD = Pattern.compile("# forwarding 127\\.0\\.0\\.1:(\\d+) ->");
    private Process process;
    private Path keyDirectory;
    private boolean started, closed;
    private volatile String clientPublicKey;
    private volatile Consumer<String> statusListener = json -> {};
    private final CompletableFuture<String> ready = new CompletableFuture<>();

    public String clientPublicKey() { return clientPublicKey; }
    public void onStatus(Consumer<String> listener) { statusListener = listener; }

    public synchronized CompletableFuture<String> start(Path gameDir, boolean host, String argument, int port) throws IOException {
        if (started || closed) throw new IllegalStateException("Tunnel already used");
        started = true;
        background(() -> {
            try {
                Path installed = TailcatInstaller.findInstalled(gameDir);
                if (installed == null) throw new IOException("Tailcat não está instalado. Instala-o em " + TailcatInstaller.destination(gameDir));
                String executable = installed.toString();
                Path clientKey = host ? null : createClientIdentity(executable);
                ProcessBuilder builder = host
                        ? new ProcessBuilder(executable, "--key=new", "serve", Integer.toString(port))
                        : new ProcessBuilder(executable, "--key=" + clientKey, "forward", argument, "0:" + port);
                if (host) builder.environment().put("TAILCAT_STATUS_LOOP", "1");
                Process child = launch(builder);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Matcher match = (host ? ADDRESS : FORWARD).matcher(line);
                        if (match.find()) ready.complete(match.group(1));
                        int marker = host ? line.indexOf("status = ") : -1;
                        if (marker >= 0) {
                            try { statusListener.accept(line.substring(marker + 9)); }
                            catch (RuntimeException ignored) { /* Diagnostics must not interrupt the tunnel. */ }
                        }
                    }
                    ready.completeExceptionally(new IOException("Tailcat terminou antes de abrir a ligação (código " + child.waitFor() + ")."));
                }
            } catch (Exception e) { ready.completeExceptionally(e); }
            finally { close(); }
        });
        background(() -> {
            try { ready.get(60, TimeUnit.SECONDS); }
            catch (TimeoutException e) { ready.completeExceptionally(e); close(); }
            catch (Exception e) { close(); }
        });
        return ready;
    }

    private Path createClientIdentity(String executable) throws Exception {
        Path directory;
        synchronized (this) {
            if (closed) throw new CancellationException();
            directory = keyDirectory = Files.createTempDirectory("tailcat-minecraft-");
        }
        directory.toFile().deleteOnExit();
        Path key = directory.resolve("client.private.json");
        key.toFile().deleteOnExit();
        Process child = launch(new ProcessBuilder(executable, "genkey", "--client", "--key=" + key));
        if (!child.waitFor(10, TimeUnit.SECONDS)) { child.destroyForcibly(); throw new IOException("Tempo esgotado ao criar identidade Tailcat."); }
        String publicKey = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) if (line.matches("nodekey:[0-9a-f]{64}")) publicKey = line;
        }
        if (child.exitValue() != 0 || publicKey == null || !publicKey.matches("nodekey:[0-9a-f]{64}")) throw new IOException("Não foi possível criar a identidade Tailcat.");
        clientPublicKey = publicKey;
        return key;
    }

    private synchronized Process launch(ProcessBuilder builder) throws IOException {
        if (closed) throw new CancellationException();
        return process = builder.redirectErrorStream(true).start();
    }
    private static void background(Runnable task) {
        Thread thread = new Thread(task, "tailcat-worker"); thread.setDaemon(true); thread.start();
    }
    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        ready.completeExceptionally(new CancellationException("Ligação cancelada"));
        Process child = process;
        Path directory = keyDirectory;
        process = null;
        if (child != null) child.destroy();
        background(() -> {
            if (child != null) {
                try { if (!child.waitFor(2, TimeUnit.SECONDS)) { child.destroyForcibly(); child.waitFor(2, TimeUnit.SECONDS); } }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); child.destroyForcibly(); }
            }
            if (directory != null) {
                try { Files.deleteIfExists(directory.resolve("client.private.json")); Files.deleteIfExists(directory); }
                catch (IOException ignored) { /* Do not turn shutdown into a game crash. */ }
            }
        });
    }
    public synchronized boolean alive() { return process != null && process.isAlive(); }
}
