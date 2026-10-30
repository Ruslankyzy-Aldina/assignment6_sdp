package org.example.element;

import java.util.Objects;
import org.example.visitor.ProjectVisitor;

public record TestFile(String name, int linesOfCode, int numberOfTests,
                       String testedSourceFile) implements ProjectElement {
    public TestFile {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(testedSourceFile, "testedSourceFile");
        if (name.isBlank() || testedSourceFile.isBlank() || linesOfCode < 0 || numberOfTests < 0) {
            throw new IllegalArgumentException("Names must be nonblank and metrics nonnegative");
        }
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
