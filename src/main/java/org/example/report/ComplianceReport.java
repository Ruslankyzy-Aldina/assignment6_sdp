package org.example.report;

import java.util.List;

public record ComplianceReport(List<ComplianceViolation> violations) {
    public ComplianceReport {
        violations = List.copyOf(violations);
    }

    public boolean compliant() {
        return violations.isEmpty();
    }

    public String format() {
        StringBuilder result = new StringBuilder("Compliance check\n");
        result.append("Violations: ").append(violations.size()).append('\n');
        for (ComplianceViolation violation : violations) {
            result.append('[').append(violation.rule()).append("] ")
                    .append(violation.fileName()).append(": ")
                    .append(violation.message()).append('\n');
        }
        return result.append("FINAL RESULT: ").append(compliant() ? "PASS" : "FAIL")
                .append('\n').toString();
    }
}
