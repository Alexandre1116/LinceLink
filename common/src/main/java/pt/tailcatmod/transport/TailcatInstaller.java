package pt.tailcatmod.transport;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;

/** Installs a pinned official release without executing archive contents. Java 8 compatible. */
public final class TailcatInstaller {
    private static final String BASE = "https://github.com/tailscale/tailcat/releases/download/v0.7.0/";
    private static final long LIMIT = 64L * 1024 * 1024;
    private static final Map<Path, CompletableFuture<Path>> INSTALLS = new HashMap<>();
    public enum Stage { PREPARING, DOWNLOADING, EXTRACTING, READY, FAILED }
    public static final class Progress {
        public final Stage stage;
        public final String detail;
        public final Throwable failure;
        private Progress(Stage stage, String detail, Throwable failure) { this.stage = stage; this.detail = detail; this.failure = failure; }
    }
    private static volatile Progress progress = new Progress(Stage.PREPARING, "", null);
    private TailcatInstaller() {}
    public static Progress progress() { return progress; }
    public static String status() { return progress.stage + " " + progress.detail; }
    private static void progress(Stage stage, String detail) { progress = new Progress(stage, detail, null); }
    public static Path destination(Path gameDir) {
        return gameDir.toAbsolutePath().resolve("tailcat").resolve(isWindows() ? "tailcat.exe" : "tailcat");
    }
    private static boolean isWindows() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows"); }
    static Path findInstalled(Path gameDir) throws IOException {
        String override = System.getProperty("tailcat.executable");
        if (override != null && !override.trim().isEmpty()) {
            Path custom = Paths.get(override);
            if (Files.isRegularFile(custom)) return custom.toAbsolutePath();
            Path fromPath = searchPath(override);
            if (fromPath != null) return fromPath;
            throw new TransportFailure("mco.errorMessage.generic", "Configured Tailcat executable not found", override);
        }
        Path local = destination(gameDir);
        if (Files.isRegularFile(local)) {
            if (!isWindows() && !Files.isExecutable(local)) throw new TransportFailure("mco.errorMessage.generic", "Tailcat executable permission missing", "chmod +x " + local);
            return local;
        }
        return searchPath(isWindows() ? "tailcat.exe" : "tailcat");
    }
    private static Path searchPath(String name) {
        String path = System.getenv("PATH");
        if (path == null) return null;
        for (String folder : path.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
            if (folder.isEmpty()) continue;
            try {
                if (folder.length() >= 2 && folder.charAt(0) == '"' && folder.charAt(folder.length() - 1) == '"') folder = folder.substring(1, folder.length() - 1);
                Path file = Paths.get(folder).resolve(name);
                if (Files.isRegularFile(file) && (isWindows() || Files.isExecutable(file))) return file.toAbsolutePath();
            } catch (InvalidPathException ignored) { /* Ignore malformed PATH entries. */ }
        }
        return null;
    }
    public static synchronized CompletableFuture<Path> ensure(Path gameDir) {
        Path key = gameDir.toAbsolutePath().normalize();
        CompletableFuture<Path> existing = INSTALLS.get(key);
        if (existing != null && !existing.isCompletedExceptionally()) {
            if (!existing.isDone() || Files.isRegularFile(existing.getNow(null))) return existing;
        }
        CompletableFuture<Path> future = new CompletableFuture<>();
        INSTALLS.put(key, future);
        progress(Stage.PREPARING, "");
        Thread worker = new Thread(() -> {
            try {
                Path found = findInstalled(key);
                if (found == null) found = install(key);
                progress(Stage.READY, ""); future.complete(found);
            } catch (Exception e) {
                progress = new Progress(Stage.FAILED, "", e);
                future.completeExceptionally(new IOException("Tailcat installation failed", e));
            }
        }, "tailcat-installer");
        worker.setDaemon(true); worker.start();
        return future;
    }
    static String[] release(String os, String arch) throws IOException {
        os = os.toLowerCase(Locale.ROOT); arch = arch.toLowerCase(Locale.ROOT);
        String cpu = arch.equals("amd64") || arch.equals("x86_64") ? "amd64" :
                arch.equals("aarch64") || arch.equals("arm64") ? "arm64" : "";
        if (os.startsWith("windows") && cpu.equals("amd64")) return new String[]{"tailcat_0.7.0_windows_amd64.zip", "f04cac07e3bf6c700b0b543df0d3315a3b312cd5a05ee76714e1cc848e9b2dff"};
        if (os.startsWith("windows") && cpu.equals("arm64")) return new String[]{"tailcat_0.7.0_windows_arm64.zip", "1c2d081375ade2365402f9fcf611e89dbb2c5c5ee06936aaff2b205dbe427578"};
        if (os.equals("linux") && cpu.equals("amd64")) return new String[]{"tailcat_0.7.0_linux_amd64.tar.gz", "23c0b1887a5ec422f0d18a9c52b4f5357815febdaae738a1eb54036d10bd9ee6"};
        if (os.equals("linux") && cpu.equals("arm64")) return new String[]{"tailcat_0.7.0_linux_arm64.tar.gz", "bbb1ab50f24f00effe1e1fd86d0501803fb80793a90785a2a16ff3428f03d8ef"};
        throw new TransportFailure("mco.download.failed", "Automatic Tailcat installation unavailable for this platform", os + "/" + arch + " · tailcat/");
    }
    private static Path install(Path gameDir) throws Exception {
        String[] release = release(System.getProperty("os.name"), System.getProperty("os.arch"));
        Path dest = destination(gameDir), parent = dest.getParent();
        Files.createDirectories(parent);
        Path temp = Files.createTempDirectory(parent, ".install-");
        try {
            Path archive = temp.resolve("download");
            progress(Stage.DOWNLOADING, "v0.7.0");
            download(new URL(BASE + release[0]), archive);
            verify(archive, release[1]);
            progress(Stage.EXTRACTING, "");
            try (InputStream input = Files.newInputStream(archive)) {
                extract(input, release[0].endsWith(".zip"), temp);
            }
            Path binary = temp.resolve(isWindows() ? "tailcat.exe" : "tailcat");
            if (!Files.isRegularFile(binary) || !Files.isRegularFile(temp.resolve("LICENSE"))) throw new TransportFailure("mco.download.failed", "Incomplete official archive", "Tailcat v0.7.0");
            if (!isWindows() && !binary.toFile().setExecutable(true, true)) throw new TransportFailure("mco.errorMessage.generic", "Cannot set executable permission", "chmod +x " + dest);
            for (String name : new String[]{"LICENSE", "README.md"}) {
                if (Files.exists(temp.resolve(name))) Files.move(temp.resolve(name), parent.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            }
            // Publish only the complete, verified executable. Never replace an existing installation.
            Files.move(binary, dest);
            return dest;
        } finally {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(temp)) {
                for (Path file : entries) Files.deleteIfExists(file);
            }
            Files.deleteIfExists(temp);
        }
    }
    private static void download(URL url, Path archive) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000); connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "Tailcat-Minecraft/0.1.2");
        try {
            if (connection.getResponseCode() != 200) throw new TransportFailure("mco.download.failed", "HTTP download failed", "HTTP " + connection.getResponseCode());
            long length = connection.getContentLengthLong(), total = 0;
            long deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(3);
            try (InputStream in = connection.getInputStream(); OutputStream out = Files.newOutputStream(archive)) {
                byte[] buffer = new byte[16384]; int n;
                while ((n = in.read(buffer)) != -1) {
                    total += n;
                    if (total > LIMIT || System.nanoTime() > deadline) throw new TransportFailure("mco.download.failed", "Download size/time limit exceeded", "64 MB / 180 s");
                    out.write(buffer, 0, n);
                    progress(Stage.DOWNLOADING, length > 0 ? (total * 100 / length) + "%" : (total / 1024) + " KB");
                }
            }
        } finally { connection.disconnect(); }
    }
    static void verify(Path archive, String expected) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(archive)) {
            byte[] buffer = new byte[16384]; int n;
            while ((n = in.read(buffer)) != -1) digest.update(buffer, 0, n);
        }
        StringBuilder hex = new StringBuilder();
        for (byte value : digest.digest()) hex.append(String.format("%02x", value & 255));
        if (!hex.toString().equals(expected)) throw new TransportFailure("mco.download.failed", "Invalid download checksum; executable not installed", "SHA-256");
    }
    static void extract(InputStream input, boolean zip, Path directory) throws IOException {
        if (zip) {
            try (ZipInputStream entries = new ZipInputStream(input)) {
                ZipEntry entry; int count = 0;
                while ((entry = entries.getNextEntry()) != null) {
                    if (++count > 32) throw new TransportFailure("mco.download.failed", "Too many archive entries", "ZIP/TAR");
                    if (!allowed(entry.getName()) || entry.isDirectory()) throw new TransportFailure("mco.download.failed", "Unexpected archive entry", "ZIP");
                    try (OutputStream out = Files.newOutputStream(directory.resolve(entry.getName()), StandardOpenOption.CREATE_NEW)) { copy(entries, out, -1); }
                }
            }
        } else {
            try (InputStream tar = new GZIPInputStream(input)) {
                byte[] header = new byte[512]; int count = 0;
                while (true) {
                    readFully(tar, header);
                    if (header[0] == 0) break;
                    if (++count > 32) throw new TransportFailure("mco.download.failed", "Too many archive entries", "ZIP/TAR");
                    String name = new String(header, 0, 100, java.nio.charset.StandardCharsets.US_ASCII).split("\u0000", 2)[0];
                    if (!allowed(name) || (header[156] != 0 && header[156] != '0')) throw new TransportFailure("mco.download.failed", "Unexpected archive entry", "TAR");
                    long size;
                    try { size = Long.parseLong(new String(header, 124, 12, java.nio.charset.StandardCharsets.US_ASCII).replace("\u0000", "").trim(), 8); }
                    catch (NumberFormatException e) { throw new TransportFailure("mco.download.failed", "Invalid TAR size", "TAR"); }
                    if (size < 0 || size > LIMIT) throw new TransportFailure("mco.download.failed", "Archive entry exceeds size limit", "64 MB");
                    try (OutputStream out = Files.newOutputStream(directory.resolve(name), StandardOpenOption.CREATE_NEW)) { copy(tar, out, size); }
                    long padding = (512 - size % 512) % 512;
                    while (padding-- > 0) if (tar.read() < 0) throw new EOFException();
                }
            }
        }
    }
    private static boolean allowed(String name) { return name.equals("tailcat") || name.equals("tailcat.exe") || name.equals("LICENSE") || name.equals("README.md"); }
    private static void copy(InputStream in, OutputStream out, long remaining) throws IOException {
        byte[] buffer = new byte[16384]; long total = 0;
        while (remaining != 0) {
            int n = in.read(buffer, 0, remaining < 0 ? buffer.length : (int)Math.min(buffer.length, remaining));
            if (n < 0) { if (remaining > 0) throw new EOFException(); break; }
            total += n; if (total > LIMIT) throw new TransportFailure("mco.download.failed", "Archive entry exceeds size limit", "64 MB");
            out.write(buffer, 0, n); if (remaining > 0) remaining -= n;
        }
    }
    private static void readFully(InputStream input, byte[] bytes) throws IOException { new DataInputStream(input).readFully(bytes); }
}
