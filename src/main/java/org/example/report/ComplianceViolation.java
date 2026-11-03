package org.example.report;

import java.util.Objects;

public record ComplianceViolation(Rule rule, String fileName, String message) {
    public enum Rule {
        EXPOSED_CREDENTIALS, MISSING_DEPENDENCY_VERSION, PROHIBITED_DEPENDENCY, MISSING_TESTS
    }

    public ComplianceViolation {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(message, "message");
        if (fileName.isBlank() || message.isBlank()) {
            throw new IllegalArgumentException("File name and message must be nonblank");
        }
    }
}
