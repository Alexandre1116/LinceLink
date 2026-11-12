package pt.tailcatmod.transport;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.zip.*;

/** Explicit integration test: downloads the pinned official Linux release. */
public final class InstallerSmoke {
    private static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        check(TailcatInstaller.release("Windows 11", "amd64")[0].endsWith("windows_amd64.zip"));
        check(TailcatInstaller.release("Windows 11", "aarch64")[0].endsWith("windows_arm64.zip"));
        try { TailcatInstaller.release("Mac OS X", "aarch64"); throw new AssertionError(); } catch (IOException expected) { }
        Path temp = Files.createTempDirectory("tailcat-installer-test-");
        ByteArrayOutputStream malicious = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(malicious)) { z.putNextEntry(new ZipEntry("../escape")); z.write(42); z.closeEntry(); }
        try { TailcatInstaller.extract(new ByteArrayInputStream(malicious.toByteArray()), true, temp); throw new AssertionError(); }
        catch (IOException expected) { check(!Files.exists(temp.getParent().resolve("escape"))); }
        Path bad = temp.resolve("bad"); Files.write(bad, new byte[]{1,2,3});
        try { TailcatInstaller.verify(bad, "0000"); throw new AssertionError(); } catch (IOException expected) { }
        Path windows = Files.createDirectory(temp.resolve("windows"));
        Path archive = Paths.get(args[0]);
        TailcatInstaller.verify(archive, TailcatInstaller.release("Windows", "amd64")[1]);
        try (InputStream input = Files.newInputStream(archive)) { TailcatInstaller.extract(input, true, windows); }
        byte[] header = new byte[2];
        try (DataInputStream input = new DataInputStream(Files.newInputStream(windows.resolve("tailcat.exe")))) { input.readFully(header); }
        check(header[0] == 'M' && header[1] == 'Z'); check(Files.exists(windows.resolve("LICENSE")));
        Path game = Files.createDirectory(temp.resolve("game"));
        System.setProperty("tailcat.executable", temp.resolve("missing").toString());
        try { TailcatInstaller.ensure(game).get(5, TimeUnit.SECONDS); throw new AssertionError(); } catch (ExecutionException expected) { }
        System.clearProperty("tailcat.executable");
        CompletableFuture<Path> first = TailcatInstaller.ensure(game);
        check(first == TailcatInstaller.ensure(game));
        Path binary = first.get(180, TimeUnit.SECONDS);
        check(binary.equals(TailcatInstaller.destination(game))); check(Files.isExecutable(binary));
        Process child = new ProcessBuilder(binary.toString(), "--version").redirectErrorStream(true).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream()))) { check("v0.7.0".equals(reader.readLine())); }
        check(child.waitFor(5, TimeUnit.SECONDS) && child.exitValue() == 0);
        long modified = Files.getLastModifiedTime(binary).toMillis();
        check(TailcatInstaller.ensure(game).get().equals(binary)); check(modified == Files.getLastModifiedTime(binary).toMillis());
        try (java.util.stream.Stream<Path> files = Files.walk(temp)) {
            files.sorted(java.util.Comparator.reverseOrder()).forEach(path -> { try { Files.delete(path); } catch (IOException e) { throw new UncheckedIOException(e); } });
        }
        System.out.println("PASS: Windows extraction, platform selection, path traversal rejection, checksum rejection, retry, concurrent requests, real Linux download/install/run and reuse (Java 8).");
    }
}
