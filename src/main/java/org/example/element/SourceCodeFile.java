package org.example.element;

import java.util.Objects;
import org.example.visitor.ProjectVisitor;

public record SourceCodeFile(String name, int linesOfCode, int complexity,
                             boolean hardcodedCredentials, boolean highRisk,
                             boolean production) implements ProjectElement {
    public SourceCodeFile {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || linesOfCode < 0 || complexity < 0) {
            throw new IllegalArgumentException("Name must be nonblank and metrics nonnegative");
        }
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
