package de.eldoria.bloodnight.mob.meta;

import org.jetbrains.annotations.Nullable;

public record Equipment(@Nullable String head, @Nullable String chest, @Nullable String legs, @Nullable String feet,
                        @Nullable String mainHand, @Nullable String offHand) {
}
