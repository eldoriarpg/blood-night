/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The address a request comes from. Behind proxies, such as Traefik, Cloudflare or the frontend's server, the socket
 * address is the last proxy's, and each trusted proxy appends the address it saw to {@code X-Forwarded-For}. The
 * client is the nearest address in that chain that is no trusted proxy, read from the right, so entries a client
 * writes into the header itself never count. Nothing is trusted unless configured.
 */
public final class ClientAddress {
    private final List<Range> trusted;

    private ClientAddress(List<Range> trusted) {
        this.trusted = trusted;
    }

    /**
     * @param proxies the trusted proxies as addresses or ranges, e.g. {@code 172.16.0.0/12}
     * @return the resolver trusting those proxies
     * @throws IllegalArgumentException when an entry is no address or range
     */
    public static ClientAddress trusting(List<String> proxies) {
        return new ClientAddress(proxies.stream().map(Range::parse).toList());
    }

    /**
     * @param remote    the socket address of the request
     * @param forwarded the request's {@code X-Forwarded-For} header, if any
     * @return the address to count the request for
     */
    public String of(String remote, @Nullable String forwarded) {
        List<String> chain = new ArrayList<>();
        if (forwarded != null) {
            Arrays.stream(forwarded.split(","))
                    .map(String::strip)
                    .filter(entry -> !entry.isEmpty())
                    .forEach(chain::add);
        }
        chain.add(remote);
        for (int i = chain.size() - 1; i > 0; i--) {
            if (!trusts(chain.get(i))) return chain.get(i);
        }
        return chain.getFirst();
    }

    private boolean trusts(String address) {
        return trusted.stream().anyMatch(range -> range.contains(address));
    }

    /**
     * A block of addresses: the leading bits its members share, of addresses of one length in bytes.
     */
    private record Range(BigInteger prefix, int length, int bits) {
        static Range parse(String text) {
            String[] parts = text.strip().split("/", 2);
            byte[] address = address(parts[0])
                    .orElseThrow(() -> new IllegalArgumentException("Not an address: " + text))
                    .getAddress();
            int bits = parts.length == 2 ? Integer.parseInt(parts[1]) : address.length * 8;
            if (bits < 0 || bits > address.length * 8) throw new IllegalArgumentException("Not a range: " + text);
            return new Range(prefix(address, bits), address.length, bits);
        }

        boolean contains(String text) {
            return address(text)
                    .map(InetAddress::getAddress)
                    .filter(address -> address.length == length)
                    .map(address -> prefix(address, bits).equals(prefix))
                    .orElse(false);
        }

        private static BigInteger prefix(byte[] address, int bits) {
            return new BigInteger(1, address).shiftRight(address.length * 8 - bits);
        }

        private static Optional<InetAddress> address(String text) {
            String literal = text.strip();
            if (literal.isEmpty()
                    || !literal.chars().allMatch(c -> Character.digit(c, 16) >= 0 || c == '.' || c == ':')) {
                return Optional.empty();
            }
            try {
                return Optional.of(InetAddress.getByName(literal));
            } catch (UnknownHostException e) {
                return Optional.empty();
            }
        }
    }
}
