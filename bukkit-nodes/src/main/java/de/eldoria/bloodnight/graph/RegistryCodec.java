/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.types.LiteralCodec;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Config values of registry-backed types, written as namespaced keys ({@code minecraft:zombie}) and resolved through
 * Paper's registries when generated code runs.
 * <p>
 * Used for every type that has a registry, including types that are still enums, so graphs survive the move of
 * those enums to registries. Keys without namespace get {@code minecraft:}.
 * <p>
 * The known keys come from the running server's registry. Without a server, e.g. when the editor catalog is built,
 * they come from Paper's generated key class ({@code MobEffectKeys} and the like) or, for enum-backed registries,
 * from the enum constants; values are checked against those as well.
 *
 * @param type     the registered type
 * @param key      its registry
 * @param keyField name of the {@link RegistryKey} constant for {@code key}, used in generated code
 * @param keys     Paper's generated key class for the registry, or {@code null} for enum-backed registries
 * @param <T>      the registered type
 */
public record RegistryCodec<T extends Keyed>(
        Class<T> type,
        RegistryKey<T> key,
        String keyField,
        @Nullable Class<?> keys) implements LiteralCodec<NamespacedKey> {
    private static final Pattern KEY = Pattern.compile("([a-z0-9_.-]+:)?[a-z0-9_./-]+");

    @Override
    public NamespacedKey read(JsonNode node) {
        if (node == null || !node.isString() || !KEY.matcher(node.stringValue()).matches()) {
            throw new IllegalArgumentException("Not a valid %s key: %s".formatted(type.getSimpleName(), node));
        }
        NamespacedKey namespaced = NamespacedKey.fromString(node.stringValue());
        if (namespaced == null)
            throw new IllegalArgumentException("Not a valid %s key: %s".formatted(type.getSimpleName(), node));
        List<String> known = values();
        if (!known.isEmpty() && !known.contains(namespaced.toString())) {
            throw new IllegalArgumentException("Unknown %s %s".formatted(type.getSimpleName(), namespaced));
        }
        return namespaced;
    }

    @Override
    public CodeBlock emit(NamespacedKey value) {
        return CodeBlock.of(
                "$T.registryAccess().getRegistry($T.$N).getOrThrow($T.fromString($S))",
                RegistryAccess.class,
                RegistryKey.class,
                keyField,
                NamespacedKey.class,
                value.toString());
    }

    @Override
    public List<String> values() {
        if (Bukkit.getServer() != null) {
            return RegistryAccess.registryAccess()
                    .getRegistry(key)
                    .keyStream()
                    .map(NamespacedKey::toString)
                    .sorted()
                    .toList();
        }
        return keys == null ? enumKeys() : generatedKeys();
    }

    private List<String> generatedKeys() {
        List<String> result = new ArrayList<>();
        for (Field field : keys.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !TypedKey.class.isAssignableFrom(field.getType())) continue;
            try {
                result.add(((TypedKey<?>) field.get(null)).asString());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot read " + field, e);
            }
        }
        return result.stream().sorted().toList();
    }

    private List<String> enumKeys() {
        if (!type.isEnum()) return List.of();
        List<String> result = new ArrayList<>();
        for (T constant : type.getEnumConstants()) {
            try {
                result.add(constant.getKey().toString());
            } catch (RuntimeException e) {
                continue;
            }
        }
        return result.stream().sorted().toList();
    }
}
