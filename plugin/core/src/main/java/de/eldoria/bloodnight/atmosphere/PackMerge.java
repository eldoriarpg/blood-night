/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.mob.meta.MobName;
import de.eldoria.bloodnight.web.api.Language;
import tools.jackson.core.type.TypeReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Puts BloodNight's resource pack (bundled in the jar under {@value #ROOT}) where pack plugins merge outside packs
 * into the one pack they send, so players get one pack instead of two: Nexo and Oraxen take a zip, ItemsAdder a
 * folder. Files are only written when their content changed, so the pack plugins rebuild only when needed. The zip is
 * built the same way every time: sorted entries, fixed times.
 */
public final class PackMerge {
    static final String ROOT = "resourcepack";
    private static final long FIXED_TIME = 0;
    private static final String LANG = "assets/bloodnight/lang/";
    private static final TypeReference<Map<String, String>> LANG_FILE = new TypeReference<>() {};

    /**
     * A pack plugin and where it takes outside packs.
     *
     * @param plugin the plugin's name
     * @param path   where the pack goes, below the plugin's data folder
     * @param zipped whether it takes a zip rather than a folder
     * @param reload what rebuilds its pack
     */
    public record Target(String plugin, String path, boolean zipped, String reload) {}

    /**
     * The pack plugins BloodNight knows.
     */
    public static final List<Target> TARGETS = List.of(
            new Target("Nexo", "pack/external_packs/BloodNight.zip", true, "/nexo reload pack"),
            new Target("Oraxen", "pack/uploads/bloodnight.zip", true, "/oraxen reload pack"),
            new Target("ItemsAdder", "contents/bloodnight/resourcepack", false, "/iazip"));

    private final SortedMap<String, byte[]> files;

    private PackMerge(SortedMap<String, byte[]> files) {
        this.files = files;
    }

    /**
     * @return the pack as bundled in the plugin
     * @throws UncheckedIOException when the plugin's own files cannot be read
     */
    public static PackMerge bundled() {
        URL meta = PackMerge.class.getClassLoader().getResource(ROOT + "/pack.mcmeta");
        if (meta == null) throw new UncheckedIOException(new IOException("The plugin holds no resource pack"));
        try {
            URI uri = meta.toURI();
            if (!uri.getScheme().equals("jar"))
                return new PackMerge(read(Path.of(uri).resolveSibling(".").normalize()));
            try (FileSystem jar = FileSystems.newFileSystem(uri, Map.of())) {
                return new PackMerge(read(jar.getPath(ROOT)));
            } catch (FileSystemAlreadyExistsException e) {
                return new PackMerge(read(FileSystems.getFileSystem(uri).getPath(ROOT)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param files the pack's files by path, e.g. {@code pack.mcmeta}
     * @return a pack of these files
     */
    static PackMerge of(Map<String, byte[]> files) {
        return new PackMerge(new TreeMap<>(files));
    }

    /**
     * The pack with mob names added to its language files, so name tags show them in each player's language: each
     * name's text goes under {@code bloodnight.mob.<key>} into the file of its language, next to what the file holds.
     *
     * @param names names by their key below {@code bloodnight.mob.}, e.g. a mob's id
     * @return a pack with those names; this pack stays as it is
     */
    public PackMerge withMobNames(Map<String, MobName> names) {
        Map<String, SortedMap<String, String>> byFile = new TreeMap<>();
        names.forEach((key, name) -> name.texts().forEach((language, text) -> byFile.computeIfAbsent(
                        LANG + minecraftLocale(language) + ".json", ignored -> new TreeMap<>())
                .put(MobName.translationKey(key), text)));
        SortedMap<String, byte[]> merged = new TreeMap<>(files);
        byFile.forEach((file, texts) -> {
            SortedMap<String, String> content = new TreeMap<>();
            byte[] present = merged.get(file);
            if (present != null) content.putAll(EditorPayload.MAPPER.readValue(present, LANG_FILE));
            content.putAll(texts);
            merged.put(
                    file,
                    (EditorPayload.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(content) + "\n")
                            .getBytes(StandardCharsets.UTF_8));
        });
        return new PackMerge(merged);
    }

    /**
     * @param language a language code of a mob name, e.g. {@code de} or {@code pt_br}
     * @return the name of Minecraft's language file for it, e.g. {@code de_de} or {@code pt_br}
     */
    static String minecraftLocale(String language) {
        for (Language shipped : Language.values()) {
            if (shipped.language().equals(language)) return shipped.minecraft();
        }
        return language.contains("_") ? language : language + "_" + language;
    }

    /**
     * @param present whether a plugin, by name, is installed
     * @return the installed pack plugins
     */
    public static List<Target> installed(Predicate<String> present) {
        return TARGETS.stream().filter(target -> present.test(target.plugin())).toList();
    }

    /**
     * Puts the pack into a pack plugin's data folder.
     *
     * @param plugins the server's plugin folder
     * @return whether anything was written
     */
    public boolean install(Path plugins, Target target) throws IOException {
        Path destination = plugins.resolve(target.plugin()).resolve(target.path());
        return target.zipped() ? writeZip(destination) : writeFolder(destination);
    }

    /**
     * Writes the pack as {@code bloodnight.zip} and as the folder {@code bloodnight} into a directory, to merge by hand.
     */
    public void export(Path directory) throws IOException {
        writeZip(directory.resolve("bloodnight.zip"));
        writeFolder(directory.resolve("bloodnight"));
    }

    /**
     * @return the pack as a zip, the same bytes for the same files
     */
    byte[] zip() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (var file : files.entrySet()) {
                ZipEntry entry = new ZipEntry(file.getKey());
                entry.setTime(FIXED_TIME);
                zip.putNextEntry(entry);
                zip.write(file.getValue());
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private boolean writeZip(Path destination) throws IOException {
        return write(destination, zip());
    }

    private boolean writeFolder(Path destination) throws IOException {
        boolean changed = false;
        for (var file : files.entrySet()) {
            if (file.getKey().equals("pack.mcmeta")) continue;
            changed |= write(destination.resolve(file.getKey()), file.getValue());
        }
        return changed;
    }

    private static boolean write(Path destination, byte[] content) throws IOException {
        if (Files.exists(destination) && Arrays.equals(Files.readAllBytes(destination), content)) return false;
        Files.createDirectories(destination.toAbsolutePath().resolveSibling(".").normalize());
        Files.write(destination, content);
        return true;
    }

    private static SortedMap<String, byte[]> read(Path root) throws IOException {
        SortedMap<String, byte[]> files = new TreeMap<>();
        List<Path> paths = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile).forEach(paths::add);
        }
        for (Path path : paths) {
            try (InputStream stream = Files.newInputStream(path)) {
                files.put(root.relativize(path).toString().replace('\\', '/'), stream.readAllBytes());
            }
        }
        return files;
    }
}
