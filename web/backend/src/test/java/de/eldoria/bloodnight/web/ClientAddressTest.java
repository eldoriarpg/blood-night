/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientAddressTest {
    private final ClientAddress behindProxies =
            ClientAddress.trusting(List.of("172.16.0.0/12", "173.245.48.0/20", "::1"));

    @Test
    void takesTheNearestAddressThatIsNoTrustedProxy() {
        assertEquals("203.0.113.7", behindProxies.of("172.18.0.5", "203.0.113.7, 173.245.48.9"));
        assertEquals("198.51.100.2", behindProxies.of("172.18.0.5", "198.51.100.2"));
        assertEquals("203.0.113.9", behindProxies.of("::1", "203.0.113.9"));
    }

    @Test
    void ignoresWhatAClientWritesIntoTheHeader() {
        assertEquals("198.51.100.2", behindProxies.of("172.18.0.5", "1.2.3.4, 198.51.100.2"));
        assertEquals("198.51.100.4", behindProxies.of("198.51.100.4", "1.2.3.4"));
        assertEquals("10.0.0.1", ClientAddress.trusting(List.of()).of("10.0.0.1", "1.2.3.4"));
    }

    @Test
    void keepsTheSocketAddressWithoutForwarding() {
        assertEquals("172.18.0.5", behindProxies.of("172.18.0.5", null));
    }

    @Test
    void refusesWhatIsNoRange() {
        assertThrows(IllegalArgumentException.class, () -> ClientAddress.trusting(List.of("example.com")));
        assertThrows(IllegalArgumentException.class, () -> ClientAddress.trusting(List.of("10.0.0.0/33")));
    }
}
