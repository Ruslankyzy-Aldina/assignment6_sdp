package org.example.element;

import java.util.List;
import java.util.Objects;
import org.example.visitor.ProjectVisitor;

public record Directory(String name, List<ProjectElement> children) implements ProjectElement {
    public Directory {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must be nonblank");
        }
        children = List.copyOf(children);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
        for (ProjectElement child : children) {
            child.accept(visitor);
        }
    }
}
