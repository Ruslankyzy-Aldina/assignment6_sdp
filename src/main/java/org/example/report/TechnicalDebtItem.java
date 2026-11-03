package org.example.report;

import java.util.Objects;

public record TechnicalDebtItem(String fileName, String reason, long estimatedMinutes) {
    public TechnicalDebtItem {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(reason, "reason");
        if (fileName.isBlank() || reason.isBlank() || estimatedMinutes <= 0) {
            throw new IllegalArgumentException("Names must be nonblank and estimated minutes positive");
        }
    }
}
