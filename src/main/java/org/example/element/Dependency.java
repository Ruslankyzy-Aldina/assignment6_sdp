package org.example.element;

import java.util.Objects;

public record Dependency(String name, String version, boolean vulnerable, boolean prohibited) {
    public Dependency {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(version, "version");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must be nonblank");
        }
    }
}
