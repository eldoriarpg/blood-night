package de.eldoria.bloodnight;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.bloodnight.compiler.CompileClasspath;
import de.eldoria.bloodnight.compiler.HardcodedBehaviour;
import de.eldoria.graph.compiler.CompilationException;
import de.eldoria.graph.compiler.CompilationResult;
import de.eldoria.graph.compiler.RuntimeCompiler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.logging.Level;

/**
 * Plugin entry point.
 * <p>
 * Compiles {@link HardcodedBehaviour} off the main thread on enable, then drives it from the tick scheduler and from an
 * {@link EntityDamageEvent} executor registered without a listener class. {@code /bloodnight compile [runs]} measures
 * repeated compilation, {@code /bloodnight source} prints the generated source.
 */
public final class BloodNight extends JavaPlugin {
    private final Listener dispatchOwner = new Listener() {
    };
    private RuntimeCompiler compiler;
    private BukkitTask tickTask;

    @Override
    public void onEnable() {
        List<Path> classpath = CompileClasspath.discover(getClassLoader());
        getLogger().info("Compile classpath: %d entries".formatted(classpath.size()));
        classpath.forEach(path -> getLogger().fine(path.toString()));
        compiler = new RuntimeCompiler(classpath, getClassLoader());

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                CompilationResult result = compiler.compile(HardcodedBehaviour.source());
                getLogger().info("Compiled %s in %d ms".formatted(HardcodedBehaviour.BINARY_NAME, result.took().toMillis()));
                CompiledBehaviour behaviour = result.instantiate(HardcodedBehaviour.BINARY_NAME, CompiledBehaviour.class, getLogger());
                getServer().getScheduler().runTask(this, () -> install(behaviour));
            } catch (CompilationException | RuntimeException e) {
                getLogger().log(Level.SEVERE, "Could not compile generated behaviour", e);
            }
        });
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(dispatchOwner);
        if (tickTask != null) tickTask.cancel();
    }

    private void install(CompiledBehaviour behaviour) {
        getServer().getPluginManager().registerEvent(EntityDamageEvent.class, dispatchOwner, EventPriority.NORMAL,
                (listener, event) -> {
                    if (event instanceof EntityDamageEvent damage) behaviour.onEntityDamage(damage);
                }, this, true);
        tickTask = getServer().getScheduler().runTaskTimer(this, behaviour::onTick, 1, 1);
        getLogger().info("Generated behaviour installed: %s loaded by %s".formatted(
                behaviour.getClass().getName(), behaviour.getClass().getClassLoader()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return false;
        switch (args[0]) {
            case "compile" -> benchmark(sender, args.length > 1 ? Integer.parseInt(args[1]) : 20);
            case "source" -> sender.sendMessage(HardcodedBehaviour.source().toString());
            default -> {
                return false;
            }
        }
        return true;
    }

    private void benchmark(CommandSender sender, int runs) {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            LongSummaryStatistics stats = new LongSummaryStatistics();
            try {
                for (int i = 0; i < runs; i++) {
                    Duration took = compiler.compile(HardcodedBehaviour.source()).took();
                    stats.accept(took.toNanos() / 1000);
                }
            } catch (CompilationException e) {
                getLogger().log(Level.SEVERE, "Benchmark failed", e);
                return;
            }
            sender.sendMessage("Compiled %d times: min %.1f ms, avg %.1f ms, max %.1f ms".formatted(
                    runs, stats.getMin() / 1000.0, stats.getAverage() / 1000.0, stats.getMax() / 1000.0));
        });
    }
}
