package org.example.report;

import java.util.Objects;

public record SecurityIssue(Severity severity, String fileName, String message) {
    public enum Severity {
        CRITICAL, HIGH
    }

    public SecurityIssue {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(message, "message");
        if (fileName.isBlank() || message.isBlank()) {
            throw new IllegalArgumentException("File name and message must be nonblank");
        }
    }
}
