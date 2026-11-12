package pt.tailcatmod;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import pt.tailcatmod.transport.*;

/** Native Minecraft keys cover its complete language selector, including special locales. */
final class UiText {
    private UiText() {}
    static Component branded(String key) { return Component.literal("LinceLink · ").append(Component.translatable(key)); }
    static Component title(boolean host) { return branded(host ? "menu.shareToLan" : "selectServer.select"); }
    static Component paste() { return Component.literal(Minecraft.ON_OSX ? "⌘V" : "Ctrl+V"); }
    static Component progress() {
        TailcatInstaller.Progress progress = TailcatInstaller.progress();
        if (progress.stage == TailcatInstaller.Stage.FAILED) return failure(progress.failure);
        String key = switch (progress.stage) {
            case PREPARING -> "mco.download.preparing";
            case DOWNLOADING -> "mco.download.downloading";
            case EXTRACTING -> "mco.download.extracting";
            case READY -> "gui.done";
            case FAILED -> throw new AssertionError();
        };
        return branded(key).copy().append(progress.detail.isEmpty() ? "" : " · " + progress.detail);
    }
    static Component reportFailure(Throwable error) {
        LogUtils.getLogger().warn("LinceLink operation failed", error);
        return failure(error);
    }
    static Component failure(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof DisplayIssue issue) {
                Component reason = Component.translatable(issue.translationKey(), issue.translationArguments());
                Component prefix = issue.translationKey().equals("mco.errorMessage.generic") ? Component.empty()
                        : Component.translatable("mco.errorMessage.generic");
                return prefix.copy().append(reason).append(issue.detail().isEmpty() ? ""
                        : (issue.translationKey().equals("mco.errorMessage.generic") ? "" : " · ") + issue.detail());
            }
            if (cause instanceof TimeoutException) return branded("disconnect.timeout");
            if (cause instanceof CancellationException) return branded("multiplayer.status.cancelled");
        }
        return branded("connect.failed");
    }
    static Component route(PeerRoutes.Route route) {
        return switch (route) {
            case DIRECT -> Component.translatable("selectServer.direct");
            case DERP -> Component.literal("DERP");
            case LOCAL -> Component.translatable("pack.source.local");
            case NORMAL -> Component.literal("LinceLink: ").append(Component.translatable("gui.no"));
            case UNKNOWN -> Component.translatable("selectWorld.versionUnknown");
        };
    }
}
