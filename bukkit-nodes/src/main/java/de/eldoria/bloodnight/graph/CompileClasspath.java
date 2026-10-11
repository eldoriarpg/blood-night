package de.eldoria.bloodnight.graph;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.graph.std.runtime.Cooldown;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Finds the jars generated code compiles against: the server API, its libraries and the plugin itself.
 * <p>
 * The compiler cannot read classes out of foreign class loaders, so the jars behind them are collected instead: the
 * URLs of every {@link URLClassLoader} from the plugin's loader up to the root, the JVM classpath, and the code
 * sources of a few anchor classes in case one of those loaders hides its URLs.
 */
public final class CompileClasspath {
    private static final List<Class<?>> ANCHORS = List.of(Bukkit.class, Component.class, CompiledBehaviour.class, Cooldown.class);

    private CompileClasspath() {
    }

    /**
     * Discovers the classpath as seen from a class loader.
     *
     * @param loader the plugin's class loader
     * @return existing jars and directories, in discovery order
     */
    public static List<Path> discover(ClassLoader loader) {
        Set<Path> paths = new LinkedHashSet<>();
        for (ClassLoader current = loader; current != null; current = current.getParent()) {
            if (current instanceof URLClassLoader urlLoader) {
                for (URL url : urlLoader.getURLs()) toPath(url).ifPresent(paths::add);
            }
        }
        for (String entry : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
            if (!entry.isBlank()) paths.add(Path.of(entry).toAbsolutePath());
        }
        for (Class<?> anchor : ANCHORS) {
            CodeSource source = anchor.getProtectionDomain().getCodeSource();
            if (source != null) toPath(source.getLocation()).ifPresent(paths::add);
        }
        return new ArrayList<>(paths.stream().filter(Files::exists).toList());
    }

    private static Optional<Path> toPath(URL url) {
        if (url == null || !"file".equals(url.getProtocol())) return Optional.empty();
        try {
            return Optional.of(Path.of(url.toURI()).toAbsolutePath());
        } catch (URISyntaxException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
