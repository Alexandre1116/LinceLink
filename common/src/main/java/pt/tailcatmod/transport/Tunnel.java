package pt.tailcatmod.transport;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.regex.*;

/** Owns one official Tailcat CLI process. Never executes a shell. */
public final class Tunnel implements AutoCloseable {
    private static final Pattern ADDRESS = Pattern.compile("new address: (tc[A-Za-z0-9_-]+)");
    private static final Pattern FORWARD = Pattern.compile("# forwarding 127\\.0\\.0\\.1:(\\d+) ->");
    private Process process;
    private final CompletableFuture<String> ready = new CompletableFuture<>();

    public synchronized CompletableFuture<String> start(Path gameDir, boolean host, String argument, int port) throws IOException {
        if (process != null || ready.isDone()) throw new IllegalStateException("Tunnel already used");
        String executable = System.getProperty("tailcat.executable");
        if (executable == null || executable.trim().isEmpty()) {
            Path local = gameDir.resolve("tailcat").resolve(System.getProperty("os.name").startsWith("Windows") ? "tailcat.exe" : "tailcat");
            executable = Files.isRegularFile(local) ? local.toAbsolutePath().toString() : "tailcat";
        }
        ProcessBuilder builder = host
                ? new ProcessBuilder(executable, "serve", Integer.toString(port))
                : new ProcessBuilder(executable, "forward", argument, "0:" + port);
        try {
            process = builder.redirectErrorStream(true).start();
        } catch (IOException e) {
            Path expected = gameDir.resolve("tailcat").resolve(System.getProperty("os.name").startsWith("Windows") ? "tailcat.exe" : "tailcat").toAbsolutePath();
            throw new IOException("Não foi possível iniciar Tailcat. Instala o executável oficial v0.7.0 em " + expected + ". Se já existe, verifica permissões e compatibilidade com o sistema.", e);
        }
        Process child = process;
        background(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher match = (host ? ADDRESS : FORWARD).matcher(line);
                    if (match.find()) ready.complete(match.group(1));
                }
                ready.completeExceptionally(new IOException("Tailcat terminou antes de abrir a ligação (código " + child.waitFor() + "). Verifica a versão e a ligação à Internet."));
            } catch (Exception e) { ready.completeExceptionally(e); }
        });
        background(() -> {
            try { ready.get(60, TimeUnit.SECONDS); }
            catch (TimeoutException e) { ready.completeExceptionally(e); close(); }
            catch (Exception e) { close(); }
        });
        return ready;
    }

    private static void background(Runnable task) {
        Thread thread = new Thread(task, "tailcat-worker"); thread.setDaemon(true); thread.start();
    }

    @Override public synchronized void close() {
        ready.completeExceptionally(new CancellationException("Ligação cancelada"));
        if (process != null) {
            Process child = process;
            child.destroy();
            background(() -> {
                try { if (!child.waitFor(2, TimeUnit.SECONDS)) child.destroyForcibly(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); child.destroyForcibly(); }
            });
            process = null;
        }
    }
    public synchronized boolean alive() { return process != null && process.isAlive(); }
}
