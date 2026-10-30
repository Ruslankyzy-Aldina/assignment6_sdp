package org.example.element;

import java.util.Objects;
import org.example.visitor.ProjectVisitor;

public record ConfigurationFile(String name, boolean exposedCredentials,
                                boolean insecureConfiguration) implements ProjectElement {
    public ConfigurationFile {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must be nonblank");
        }
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
