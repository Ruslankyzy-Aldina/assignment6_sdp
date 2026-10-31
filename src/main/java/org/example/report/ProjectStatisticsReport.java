package org.example.report;

import java.util.Locale;

public record ProjectStatisticsReport(long totalFiles, long sourceFiles, long testFiles,
                                      long linesOfCode, long numberOfTests,
                                      long dependencies, long totalComplexity) {
    public ProjectStatisticsReport {
        if (totalFiles < 0 || sourceFiles < 0 || testFiles < 0 || linesOfCode < 0
                || numberOfTests < 0 || dependencies < 0 || totalComplexity < 0) {
            throw new IllegalArgumentException("Statistics must be nonnegative");
        }
        if (sourceFiles > totalFiles || testFiles > totalFiles - sourceFiles) {
            throw new IllegalArgumentException("Source and test files cannot exceed total files");
        }
        if (sourceFiles == 0 && totalComplexity != 0) {
            throw new IllegalArgumentException("Complexity requires source files");
        }
    }

    public double averageComplexity() {
        return sourceFiles == 0 ? 0.0 : (double) totalComplexity / sourceFiles;
    }

    public double testSourceRatio() {
        return sourceFiles == 0 ? 0.0 : (double) testFiles / sourceFiles;
    }

    public String format() {
        return String.format(Locale.ROOT,
                "Project statistics\nTotal files: %d\nSource files: %d\nTest files: %d\n"
                        + "Lines of code: %d\nTests: %d\nDependencies: %d\n"
                        + "Average complexity: %.2f\nTest/source ratio: %.2f\n",
                totalFiles, sourceFiles, testFiles, linesOfCode, numberOfTests,
                dependencies, averageComplexity(), testSourceRatio());
    }
}
