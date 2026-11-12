package pt.tailcatmod;

import com.google.gson.*;
import java.util.*;

/** Parses the data-plane status of the actual game tunnel, not a separate probe. */
final class PeerRoutes {
    enum Route { UNKNOWN, DIRECT, DERP, LOCAL, NORMAL }
    private Map<String, Route> peers = Map.of();
    private long sampledAt;

    synchronized void accept(String json, long now) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            Map<String, Route> next = new HashMap<>();
            JsonElement raw = root.get("Peer");
            if (raw != null && raw.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : raw.getAsJsonObject().entrySet()) {
                    JsonObject peer = entry.getValue().getAsJsonObject();
                    String key = text(peer, "PublicKey");
                    if (key.isEmpty()) key = entry.getKey();
                    if (!key.matches("nodekey:[0-9a-f]{64}")) continue;
                    // Tailcat's DERP map may omit region codes, leaving Relay empty even
                    // during real relayed traffic. magicsock only sets CurAddr when the
                    // current send path is exclusively direct UDP. PeerRelay is separate.
                    boolean traffic = flag(peer, "Active") && flag(peer, "InMagicSock") && flag(peer, "InEngine")
                            && number(peer, "RxBytes") > 0 && number(peer, "TxBytes") > 0;
                    Route route = !text(peer, "CurAddr").isEmpty() ? Route.DIRECT
                            : text(peer, "PeerRelay").isEmpty() && traffic ? Route.DERP : Route.UNKNOWN;
                    next.put(key, route);
                }
            }
            peers = Map.copyOf(next); sampledAt = now;
        } catch (RuntimeException ignored) { /* Keep last sample briefly; malformed output never means DERP. */ }
    }
    synchronized Route route(String key, long now) {
        if (now - sampledAt > 15000 || now < sampledAt) return Route.UNKNOWN;
        return peers.getOrDefault(key, Route.UNKNOWN);
    }
    private static boolean flag(JsonObject object, String field) {
        JsonElement value = object.get(field);
        return value != null && !value.isJsonNull() && value.getAsBoolean();
    }
    private static long number(JsonObject object, String field) {
        JsonElement value = object.get(field);
        return value == null || value.isJsonNull() ? 0 : value.getAsLong();
    }
    private static String text(JsonObject object, String field) {
        JsonElement value = object.get(field);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }
}
