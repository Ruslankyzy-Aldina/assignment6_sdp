package org.example.report;

import java.util.Objects;

public record QualityGateReport(SecurityReport security, ProjectStatisticsReport statistics) {
    public static final double MAX_AVERAGE_COMPLEXITY = 10.0;
    public static final double MIN_TEST_SOURCE_RATIO = 0.40;

    public QualityGateReport {
        Objects.requireNonNull(security, "security");
        Objects.requireNonNull(statistics, "statistics");
    }

    public boolean securityPassed() {
        return security.criticalIssueCount() == 0;
    }

    public boolean complexityPassed() {
        return statistics.averageComplexity() <= MAX_AVERAGE_COMPLEXITY;
    }

    public boolean testsPassed() {
        return statistics.testSourceRatio() >= MIN_TEST_SOURCE_RATIO;
    }

    public boolean passed() {
        return securityPassed() && complexityPassed() && testsPassed();
    }

    public String format() {
        return "Security: " + status(securityPassed())
                + "\nComplexity: " + status(complexityPassed())
                + "\nTests: " + status(testsPassed())
                + "\n\nFINAL RESULT: " + (passed() ? "PASSED" : "FAILED") + "\n";
    }

    private static String status(boolean passed) {
        return passed ? "PASS" : "FAIL";
    }
}
