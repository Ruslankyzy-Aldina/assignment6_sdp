package org.example.element;

import java.util.List;
import java.util.Objects;
import org.example.visitor.ProjectVisitor;

public record DependencyFile(String name, List<Dependency> dependencies) implements ProjectElement {
    public DependencyFile {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must be nonblank");
        }
        dependencies = List.copyOf(dependencies);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
