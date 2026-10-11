/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.editor.api.SessionCreated;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * The server's open editor session, kept in a file so it survives a restart. One session at a time.
 */
public final class EditorSessionFile {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private final Path file;

    /**
     * @param file where the session is kept
     */
    public EditorSessionFile(Path file) {
        this.file = file;
    }

    /**
     * @return the open session, if there is one
     */
    public Optional<SessionCreated> load() {
        if (!Files.exists(file)) return Optional.empty();
        try {
            return Optional.of(MAPPER.readValue(Files.readString(file), SessionCreated.class));
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    public void save(SessionCreated session) {
        try {
            Files.createDirectories(Objects.requireNonNull(file.toAbsolutePath().getParent()));
            Files.writeString(file, MAPPER.writeValueAsString(session));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void clear() {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
