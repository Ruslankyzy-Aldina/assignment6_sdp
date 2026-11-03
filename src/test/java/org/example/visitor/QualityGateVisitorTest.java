package org.example.visitor;

import java.util.ArrayList;
import java.util.List;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.ProjectElement;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.QualityGateReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QualityGateVisitorTest {
    @Test
    void passesAtBothThresholds() {
        QualityGateReport report = inspect(project(5, 2, 10));

        assertEquals(10.0, report.statistics().averageComplexity());
        assertEquals(0.4, report.statistics().testSourceRatio());
        assertTrue(report.passed());
        assertEquals("Security: PASS\nComplexity: PASS\nTests: PASS\n\nFINAL RESULT: PASSED\n",
                report.format());
    }

    @Test
    void failsWhenAverageComplexityExceedsLimit() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        project(1, 1, 10).accept(visitor);
        new SourceCodeFile("B.java", 50, 11, false, false, true).accept(visitor);
        QualityGateReport report = visitor.getReport();

        assertEquals(10.5, report.statistics().averageComplexity());
        assertTrue(report.securityPassed());
        assertFalse(report.complexityPassed());
        assertTrue(report.testsPassed());
        assertFalse(report.passed());
    }

    @Test
    void failsWhenTestFileRatioIsTooLow() {
        QualityGateReport report = inspect(project(3, 1, 5));

        assertTrue(report.securityPassed());
        assertTrue(report.complexityPassed());
        assertFalse(report.testsPassed());
        assertFalse(report.passed());
        assertEquals("Security: PASS\nComplexity: PASS\nTests: FAIL\n\nFINAL RESULT: FAILED\n",
                report.format());
    }

    @Test
    void criticalSourceIssueBlocksRelease() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new SourceCodeFile("A.java", 100, 5, true, false, true).accept(visitor);
        new TestFile("ATest.java", 20, 3, "A.java").accept(visitor);
        QualityGateReport report = visitor.getReport();

        assertEquals(1, report.security().criticalIssueCount());
        assertFalse(report.securityPassed());
        assertTrue(report.complexityPassed());
        assertTrue(report.testsPassed());
        assertFalse(report.passed());
    }

    @Test
    void configurationAndDependencyIssuesBlockRelease() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        project(1, 1, 5).accept(visitor);
        new ConfigurationFile("app.properties", true, false).accept(visitor);
        new DependencyFile("pom.xml", List.of(new Dependency("unsafe", "1", true, false)))
                .accept(visitor);

        assertEquals(2, visitor.getReport().security().criticalIssueCount());
        assertFalse(visitor.getReport().securityPassed());
        assertFalse(visitor.getReport().passed());
    }

    @Test
    void highSeverityWarningsDoNotFailCriticalIssueRule() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new SourceCodeFile("A.java", 100, 5, false, true, true).accept(visitor);
        new TestFile("ATest.java", 20, 3, "A.java").accept(visitor);
        new ConfigurationFile("app.properties", false, true).accept(visitor);

        assertEquals(2, visitor.getReport().security().issues().size());
        assertEquals(0, visitor.getReport().security().criticalIssueCount());
        assertTrue(visitor.getReport().passed());
    }

    @Test
    void emptyAndTestOnlyProjectsFailTestRatioRule() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new Directory("empty", List.of()).accept(visitor);
        assertFalse(visitor.getReport().testsPassed());
        assertFalse(visitor.getReport().passed());

        new TestFile("ATest.java", 20, 3, "A.java").accept(visitor);
        assertEquals(0.0, visitor.getReport().statistics().testSourceRatio());
        assertFalse(visitor.getReport().passed());
    }

    @Test
    void nestedTraversalMatchesStandaloneVisitorsAndPreservesSnapshots() {
        Directory project = new Directory("root", List.of(new Directory("module", List.of(
                project(2, 1, 5), new ConfigurationFile("app.properties", false, true)))));
        QualityGateVisitor gate = new QualityGateVisitor();
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        project.accept(gate);
        project.accept(security);
        project.accept(statistics);
        QualityGateReport snapshot = gate.getReport();

        assertEquals(security.getReport(), snapshot.security());
        assertEquals(statistics.getReport(), snapshot.statistics());
        assertEquals(4, snapshot.statistics().totalFiles());
        assertTrue(snapshot.passed());

        new ConfigurationFile("secret.properties", true, false).accept(gate);
        assertTrue(snapshot.passed());
        assertEquals(0, snapshot.security().criticalIssueCount());
        assertFalse(gate.getReport().passed());
    }

    private static QualityGateReport inspect(ProjectElement project) {
        QualityGateVisitor visitor = new QualityGateVisitor();
        project.accept(visitor);
        return visitor.getReport();
    }

    private static Directory project(int sourceCount, int testCount, int complexity) {
        List<ProjectElement> children = new ArrayList<>();
        for (int i = 0; i < sourceCount; i++) {
            children.add(new SourceCodeFile("Source" + i + ".java", 100, complexity, false, false, true));
        }
        for (int i = 0; i < testCount; i++) {
            children.add(new TestFile("Source" + i + "Test.java", 20, 10, "Source" + i + ".java"));
        }
        return new Directory("project", children);
    }
}
