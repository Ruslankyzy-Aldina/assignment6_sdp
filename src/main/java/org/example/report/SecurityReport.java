package org.example.report;

import java.util.List;

public record SecurityReport(List<SecurityIssue> issues) {
    public SecurityReport {
        issues = List.copyOf(issues);
    }

    public long criticalIssueCount() {
        return issues.stream()
                .filter(issue -> issue.severity() == SecurityIssue.Severity.CRITICAL)
                .count();
    }

    public String format() {
        StringBuilder result = new StringBuilder("Security audit\n");
        result.append("Total issues: ").append(issues.size()).append('\n');
        result.append("Critical issues: ").append(criticalIssueCount()).append('\n');
        if (issues.isEmpty()) {
            result.append("No security issues found.\n");
        }
        for (SecurityIssue issue : issues) {
            result.append('[').append(issue.severity()).append("] ")
                    .append(issue.fileName()).append(": ")
                    .append(issue.message()).append('\n');
        }
        return result.toString();
    }
}
