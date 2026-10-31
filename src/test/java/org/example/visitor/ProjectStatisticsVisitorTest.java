package org.example.visitor;

import java.util.List;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.ProjectStatisticsReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectStatisticsVisitorTest {
    @Test
    void aggregatesAllFileTypesAcrossNestedDirectories() {
        Directory project = new Directory("project", List.of(
                new Directory("src", List.of(
                        new SourceCodeFile("A.java", 100, 4, false, false, true),
                        new SourceCodeFile("B.java", 200, 9, false, false, true))),
                new Directory("tests", List.of(new TestFile("ATest.java", 50, 7, "A.java"))),
                new ConfigurationFile("app.properties", false, false),
                new DependencyFile("pom.xml", List.of(
                        new Dependency("first", "1", false, false),
                        new Dependency("second", "2", true, false)))));
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        project.accept(visitor);
        ProjectStatisticsReport report = visitor.getReport();

        assertEquals(new ProjectStatisticsReport(5, 2, 1, 350, 7, 2, 13), report);
        assertEquals(6.5, report.averageComplexity(), 0.0001);
        assertEquals(0.5, report.testSourceRatio(), 0.0001);
    }

    @Test
    void emptyDirectoriesHaveZeroStatistics() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new Directory("root", List.of(new Directory("empty", List.of()))).accept(visitor);

        assertEquals(new ProjectStatisticsReport(0, 0, 0, 0, 0, 0, 0), visitor.getReport());
        assertEquals(0.0, visitor.getReport().averageComplexity());
        assertEquals(0.0, visitor.getReport().testSourceRatio());
    }

    @Test
    void testOnlyProjectAvoidsDivisionByZero() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new TestFile("ATest.java", 30, 4, "A.java").accept(visitor);

        assertEquals(new ProjectStatisticsReport(1, 0, 1, 30, 4, 0, 0), visitor.getReport());
        assertEquals(0.0, visitor.getReport().testSourceRatio());
        assertEquals(0.0, visitor.getReport().averageComplexity());
    }

    @Test
    void sourceWithoutTestsHasZeroRatio() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new SourceCodeFile("A.java", 0, 0, false, false, false).accept(visitor);

        assertEquals(1, visitor.getReport().sourceFiles());
        assertEquals(0, visitor.getReport().numberOfTests());
        assertEquals(0.0, visitor.getReport().testSourceRatio());
    }

    @Test
    void countsDependencyDeclarationsAcrossFiles() {
        Dependency dependency = new Dependency("library", "1", false, false);
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new DependencyFile("root/pom.xml", List.of(dependency)).accept(visitor);
        new DependencyFile("module/pom.xml", List.of(dependency)).accept(visitor);

        assertEquals(2, visitor.getReport().dependencies());
        assertEquals(2, visitor.getReport().totalFiles());
        assertEquals(0, visitor.getReport().linesOfCode());
    }

    @Test
    void totalsDoNotOverflowIntegerRange() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new SourceCodeFile("A.java", Integer.MAX_VALUE, Integer.MAX_VALUE, false, false, true)
                .accept(visitor);
        new SourceCodeFile("B.java", Integer.MAX_VALUE, Integer.MAX_VALUE, false, false, true)
                .accept(visitor);

        assertEquals(2L * Integer.MAX_VALUE, visitor.getReport().linesOfCode());
        assertEquals(2L * Integer.MAX_VALUE, visitor.getReport().totalComplexity());
        assertEquals((double) Integer.MAX_VALUE, visitor.getReport().averageComplexity());
    }

    @Test
    void reportRemainsASnapshotAfterFurtherVisits() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new SourceCodeFile("A.java", 100, 5, false, false, true).accept(visitor);
        ProjectStatisticsReport snapshot = visitor.getReport();
        new TestFile("ATest.java", 30, 4, "A.java").accept(visitor);

        assertEquals(new ProjectStatisticsReport(1, 1, 0, 100, 0, 0, 5), snapshot);
        assertEquals(2, visitor.getReport().totalFiles());
        assertEquals(130, visitor.getReport().linesOfCode());
    }

    @Test
    void formatsAllStatistics() {
        ProjectStatisticsReport report = new ProjectStatisticsReport(5, 2, 1, 350, 7, 2, 13);
        assertEquals("Project statistics\nTotal files: 5\nSource files: 2\nTest files: 1\n"
                + "Lines of code: 350\nTests: 7\nDependencies: 2\n"
                + "Average complexity: 6.50\nTest/source ratio: 0.50\n", report.format());
    }
}
