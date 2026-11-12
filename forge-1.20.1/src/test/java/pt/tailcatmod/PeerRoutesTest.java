package pt.tailcatmod;

import org.junit.Test;
import static org.junit.Assert.*;

public class PeerRoutesTest {
    private static final String TRAFFIC = "\"Active\":true,\"InMagicSock\":true,\"InEngine\":true,\"RxBytes\":1024,\"TxBytes\":2048";
    private static final String A = "nodekey:" + "a".repeat(64), B = "nodekey:" + "b".repeat(64);
    @Test public void separatesPeersAndPrefersActualDirectEndpointOverConfiguredRelay() {
        PeerRoutes peers = new PeerRoutes();
        peers.accept("{\"Peer\":{\"" + A + "\":{\"CurAddr\":\"192.0.2.1:1234\",\"Relay\":\"sfo\"},\"" + B + "\":{\"Relay\":\"sfo\"," + TRAFFIC + "}}}", 1000);
        assertEquals(PeerRoutes.Route.DIRECT, peers.route(A, 1001));
        assertEquals(PeerRoutes.Route.DERP, peers.route(B, 1001));
    }
    @Test public void unknownAndStaleDataNeverBecomeDerp() {
        PeerRoutes peers = new PeerRoutes();
        assertEquals(PeerRoutes.Route.UNKNOWN, peers.route(A, 1000));
        peers.accept("{\"Peer\":{\"" + A + "\":{\"Relay\":\"sfo\"," + TRAFFIC + "}}}", 1000);
        peers.accept("not-json", 2000);
        assertEquals(PeerRoutes.Route.DERP, peers.route(A, 2001));
        assertEquals(PeerRoutes.Route.UNKNOWN, peers.route(A, 17000));
        assertEquals(PeerRoutes.Route.UNKNOWN, peers.route(B, 2001));
    }
    @Test public void removedPeerAndEmptyEndpointClearPriorDirectRoute() {
        PeerRoutes peers = new PeerRoutes();
        peers.accept("{\"Peer\":{\"" + A + "\":{\"CurAddr\":\"192.0.2.1:1234\"}}}", 1000);
        peers.accept("{\"Peer\":{\"" + A + "\":{\"CurAddr\":null,\"Relay\":\"\"}}}", 2000);
        assertEquals(PeerRoutes.Route.UNKNOWN, peers.route(A, 2001));
        peers.accept("{\"Peer\":null}", 3000);
        assertEquals(PeerRoutes.Route.UNKNOWN, peers.route(A, 3001));
    }
    @Test public void relayCodeAloneIsNotEvidenceOfRelayedTraffic() {
        PeerRoutes peers = new PeerRoutes();
        peers.accept("{\"Peer\":{\"" + A + "\":{\"Relay\":\"sfo\"}}}", 1000);
        assertEquals(PeerRoutes.Route.UNKNOWN, peers.route(A, 1001));
    }
    @Test public void realRelayDoesNotRequireRegionCodeAndPeerRelayIsNotDerp() {
        PeerRoutes peers = new PeerRoutes();
        peers.accept("{\"Peer\":{\"" + A + "\":{\"Relay\":\"\"," + TRAFFIC + "}}}", 1000);
        assertEquals(PeerRoutes.Route.DERP, peers.route(A, 1001));
        peers.accept("{\"Peer\":{\"" + A + "\":{\"PeerRelay\":\"192.0.2.2:1234\"," + TRAFFIC + "}}}", 2000);
        assertEquals(PeerRoutes.Route.UNKNOWN, peers.route(A, 2001));
    }
}
