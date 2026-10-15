/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.web.api.Language;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * The plugin's chat texts: one sender that puts the prefix in front of every chat message and styles it, and the
 * localizer that picks each player's language. There is one set of texts per server, reached through {@link #sender()}
 * from wherever a text is sent.
 * <p>
 * Texts are MiniMessage in {@code messages_<locale>.properties}. The bundled files are copied into
 * {@code plugins/BloodNight/messages/} and completed with new keys on every start, so server owners can change them. A
 * text is sent by its key; the player's client language picks the file, the closest one of the same language when
 * there is no exact match, the fallback language otherwise.
 * <p>
 * Texts use the plugin's styles rather than colours, so every text looks alike and translators need not pick colours:
 * <ul>
 *     <li>{@code <accent>} names and values</li>
 *     <li>{@code <muted>} side notes</li>
 *     <li>{@code <success>}, {@code <warn>}, {@code <error>} outcomes</li>
 *     <li>{@code <cmd:'/bloodnight editor apply'>} a command, clicked to put it into the chat</li>
 *     <li>{@code <url:'https://...'>label</url>} a link</li>
 * </ul>
 */
public final class Texts {
    /**
     * The bundled languages.
     */
    public static final List<String> LOCALES =
            Arrays.stream(Language.values()).map(Language::name).toList();

    static final NamedTextColor TEXT = NamedTextColor.GRAY;
    static final NamedTextColor ACCENT = NamedTextColor.GOLD;
    static final NamedTextColor MUTED = NamedTextColor.DARK_GRAY;
    static final NamedTextColor SUCCESS = NamedTextColor.GREEN;
    static final NamedTextColor WARN = NamedTextColor.YELLOW;
    static final NamedTextColor ERROR = NamedTextColor.RED;
    static final NamedTextColor CLICKABLE = NamedTextColor.AQUA;

    private static volatile MessageSender current;

    private final Plugin plugin;
    private final Localizer localizer;

    private Texts(Plugin plugin, Localizer localizer) {
        this.plugin = plugin;
        this.localizer = localizer;
    }

    /**
     * Writes the text files into the plugin's folder, loads them and sets up the sender, which from then on is the one
     * {@link #sender()} gives. Called again, e.g. on a reload, it reads the files anew.
     *
     * @param prefix   MiniMessage put before every chat message
     * @param fallback the language for players whose language has no file, e.g. {@code en_US}
     */
    public static Texts create(Plugin plugin, String prefix, String fallback) {
        Localizer localizer = Localizer.builder(plugin, fallback)
                .setIncludedLocales(LOCALES.toArray(String[]::new))
                .setUserLocale(player -> player.locale().toString())
                .build();
        Texts texts = new Texts(plugin, localizer);
        texts.configure(prefix, fallback);
        return texts;
    }

    private void configure(String prefix, String fallback) {
        localizer.setDefaultLocale(fallback);
        current = MessageSender.builder(plugin)
                .localizer(localizer)
                .prefix(prefix.strip())
                .messageColor(TEXT)
                .errorColor(ERROR)
                .addTag(Texts::theme)
                .register();
    }

    /**
     * @return the sender of the plugin's texts
     */
    public static MessageSender sender() {
        MessageSender sender = current;
        if (sender == null) throw new IllegalStateException("The texts are not set up yet");
        return sender;
    }

    /**
     * Renders a text for its receiver, without the prefix, e.g. for a title or to put into another text.
     *
     * @param key       the text's key
     * @param arguments its placeholders
     */
    public static Component render(CommandSender receiver, String key, TagResolver... arguments) {
        return sender().serializeMessage(receiver, key, arguments);
    }

    /**
     * @return a command as the {@code <cmd>} style shows it, for a command known only in code
     */
    public static Component command(String command) {
        return command(command, command);
    }

    /**
     * @param shown     what the text shows, e.g. the command with its arguments
     * @param suggested what a click puts into the chat
     * @return a command as the {@code <cmd>} style shows it
     */
    public static Component command(String shown, String suggested) {
        return Component.text(shown, CLICKABLE)
                .clickEvent(ClickEvent.suggestCommand(suggested))
                .hoverEvent(HoverEvent.showText(Component.text(suggested, CLICKABLE)));
    }

    static void theme(TagResolver.Builder tags) {
        tags.tag("accent", Tag.styling(ACCENT))
                .tag("muted", Tag.styling(MUTED))
                .tag("success", Tag.styling(SUCCESS))
                .tag("warn", Tag.styling(WARN))
                .tag("error", Tag.styling(ERROR))
                .tag(
                        "cmd",
                        (arguments, context) -> Tag.selfClosingInserting(
                                command(arguments.popOr("cmd needs a command").value())))
                .tag("url", (arguments, context) -> {
                    String url = arguments.popOr("url needs an address").value();
                    return Tag.styling(
                            CLICKABLE,
                            TextDecoration.UNDERLINED,
                            ClickEvent.openUrl(url),
                            HoverEvent.showText(Component.text(url, CLICKABLE)));
                });
    }
}
