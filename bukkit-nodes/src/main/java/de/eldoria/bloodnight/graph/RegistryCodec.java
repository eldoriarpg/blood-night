package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.types.LiteralCodec;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Config values of registry-backed types, written as namespaced keys ({@code minecraft:zombie}) and resolved through
 * Paper's registries when generated code runs.
 * <p>
 * Used for every type that has a registry, including types that are still enums, so graphs survive the move of
 * those enums to registries. Keys without namespace get {@code minecraft:}. While a server is running, keys are also
 * checked against the registry; without one, only their syntax is.
 *
 * @param type     the registered type
 * @param key      its registry
 * @param keyField name of the {@link RegistryKey} constant for {@code key}, used in generated code
 * @param <T>      the registered type
 */
public record RegistryCodec<T extends Keyed>(Class<T> type, RegistryKey<T> key, String keyField) implements LiteralCodec<NamespacedKey> {
    private static final Pattern KEY = Pattern.compile("([a-z0-9_.-]+:)?[a-z0-9_./-]+");

    @Override
    public NamespacedKey read(JsonNode node) {
        if (node == null || !node.isString() || !KEY.matcher(node.stringValue()).matches()) {
            throw new IllegalArgumentException("Not a valid %s key: %s".formatted(type.getSimpleName(), node));
        }
        NamespacedKey namespaced = NamespacedKey.fromString(node.stringValue());
        if (namespaced == null) throw new IllegalArgumentException("Not a valid %s key: %s".formatted(type.getSimpleName(), node));
        registry().ifPresent(registry -> {
            if (registry.get(namespaced) == null) throw new IllegalArgumentException("Unknown %s %s".formatted(type.getSimpleName(), namespaced));
        });
        return namespaced;
    }

    @Override
    public CodeBlock emit(NamespacedKey value) {
        return CodeBlock.of("$T.registryAccess().getRegistry($T.$N).getOrThrow($T.fromString($S))",
                RegistryAccess.class, RegistryKey.class, keyField, NamespacedKey.class, value.toString());
    }

    @Override
    public List<String> values() {
        return registry().map(registry -> registry.keyStream().map(NamespacedKey::toString).sorted().toList()).orElse(List.of());
    }

    private Optional<Registry<T>> registry() {
        if (Bukkit.getServer() == null) return Optional.empty();
        return Optional.of(RegistryAccess.registryAccess().getRegistry(key));
    }
}
