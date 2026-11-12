package pt.tailcatmod;

import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.junit.*;
import static org.junit.Assert.*;
import pt.tailcatmod.transport.*;

public class UiTextTest {
    private Language original;
    @Before public void saveLanguage() { original = Language.getInstance(); }
    @After public void restoreLanguage() { Language.inject(original); }
    private void use(Map<String, String> strings, boolean rtl) {
        Language.inject(new Language() {
            @Override public String getOrDefault(String key, String fallback) { return strings.getOrDefault(key, original.getOrDefault(key, fallback)); }
            @Override public boolean has(String key) { return strings.containsKey(key) || original.has(key); }
            @Override public boolean isDefaultRightToLeft() { return rtl; }
            @Override public FormattedCharSequence getVisualOrder(FormattedText text) { return FormattedCharSequence.forward(text.getString(), Style.EMPTY); }
        });
    }
    @Test public void existingComponentsFollowLanguageChangesInsteadOfCachingPortuguese() {
        Component direct = UiText.route(PeerRoutes.Route.DIRECT);
        Component title = UiText.title(true);
        use(Map.of("selectServer.direct", "Connexion directe", "menu.shareToLan", "Ouvrir au LAN"), false);
        assertEquals("Connexion directe", direct.getString());
        assertEquals("LinceLink · Ouvrir au LAN", title.getString());
        use(Map.of("selectServer.direct", "直接连接", "menu.shareToLan", "对局域网开放"), false);
        assertEquals("直接连接", direct.getString());
        assertEquals("LinceLink · 对局域网开放", title.getString());
        use(Map.of("selectServer.direct", "اتصال مباشر"), true);
        assertEquals("اتصال مباشر", direct.getString());
        assertEquals("DERP", UiText.route(PeerRoutes.Route.DERP).getString());
    }
    @Test public void translatedErrorsPreservePathsPercentSignsAndUnicode() {
        use(Map.of("mco.errorMessage.generic", "Erreur : "), false);
        String path = "C:\\Users\\中文\\100%\\tailcat.exe";
        Component failure = UiText.failure(new TransportFailure("mco.errorMessage.generic", "Diagnostic only", path));
        assertEquals("Erreur : " + path, failure.getString());
        assertFalse(failure.getString().contains("Diagnostic only"));
    }
    @Test public void mismatchedVersionUsesLocalizedMessageAndCompleteVersionArgument() {
        use(Map.of("mco.errorMessage.generic", "Erreur : ", "multiplayer.disconnect.incompatible", "Version requise : %s"), false);
        String invite = new Invite("1.21.1", "tc" + "a".repeat(40), 25565).encode();
        try { Invite.parse(invite, "1.20.1"); fail(); }
        catch (Invite.Invalid error) {
            assertEquals("Erreur : Version requise : 1.21.1", UiText.failure(error).getString());
        }
    }
}
