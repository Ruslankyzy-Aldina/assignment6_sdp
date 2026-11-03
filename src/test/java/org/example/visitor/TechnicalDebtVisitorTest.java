package org.example.visitor;

import java.util.List;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.TechnicalDebtItem;
import org.example.report.TechnicalDebtReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalDebtVisitorTest {
    @Test
    void estimatesComplexityAndRiskSeparately() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new SourceCodeFile("A.java", 100, 12, false, true, true).accept(visitor);
        assertEquals(List.of(
                new TechnicalDebtItem("A.java", "Reduce complexity above 10", 60),
                new TechnicalDebtItem("A.java", "Review high-risk source", 60)), visitor.getReport().items());
        assertEquals(120, visitor.getReport().totalMinutes());
    }

    @Test
    void complexityAtThresholdHasNoDebt() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new SourceCodeFile("A.java", 100, 10, false, false, true).accept(visitor);
        new SourceCodeFile("B.java", 0, 0, false, false, false).accept(visitor);
        assertTrue(visitor.getReport().items().isEmpty());
    }

    @Test
    void countsEmptyTestFilesOnly() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new TestFile("EmptyTest.java", 10, 0, "A.java").accept(visitor);
        new TestFile("ATest.java", 20, 1, "A.java").accept(visitor);
        assertEquals(List.of(new TechnicalDebtItem("EmptyTest.java",
                "Implement tests in empty test file", 30)), visitor.getReport().items());
    }

    @Test
    void countsInsecureConfigurationOnly() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new ConfigurationFile("safe.properties", false, false).accept(visitor);
        new ConfigurationFile("unsafe.properties", false, true).accept(visitor);
        assertEquals(List.of(new TechnicalDebtItem("unsafe.properties",
                "Harden insecure configuration", 30)), visitor.getReport().items());
    }

    @Test
    void dependencyRulesAccumulateIndependently() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new DependencyFile("pom.xml", List.of(
                new Dependency("safe", "1", false, false),
                new Dependency("legacy", " ", true, true))).accept(visitor);
        assertEquals(List.of(
                new TechnicalDebtItem("pom.xml", "Upgrade vulnerable dependency: legacy", 120),
                new TechnicalDebtItem("pom.xml", "Specify dependency version: legacy", 30),
                new TechnicalDebtItem("pom.xml", "Replace prohibited dependency: legacy", 60)),
                visitor.getReport().items());
        assertEquals(210, visitor.getReport().totalMinutes());
    }

    @Test
    void handlesEmptyAndNestedDirectoriesWithoutDuplicateEstimates() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new Directory("empty", List.of()).accept(visitor);
        assertEquals("Technical debt estimate\nItems: 0\nEstimated effort (minutes): 0\n",
                visitor.getReport().format());
        new Directory("root", List.of(new Directory("config", List.of(
                new ConfigurationFile("app.properties", false, true))))).accept(visitor);
        assertEquals(1, visitor.getReport().items().size());
        assertEquals(30, visitor.getReport().totalMinutes());
    }

    @Test
    void reportIsAnImmutableSnapshot() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new ConfigurationFile("app.properties", false, true).accept(visitor);
        TechnicalDebtReport snapshot = visitor.getReport();
        new TestFile("ATest.java", 10, 0, "A.java").accept(visitor);
        assertEquals(30, snapshot.totalMinutes());
        assertEquals(60, visitor.getReport().totalMinutes());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.items().clear());
        assertTrue(snapshot.format().contains("app.properties: Harden insecure configuration (+30 min)"));
    }

    @Test
    void complexityEstimateDoesNotOverflowIntegerRange() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new SourceCodeFile("A.java", 100, Integer.MAX_VALUE, false, false, true).accept(visitor);
        assertEquals((Integer.MAX_VALUE - 10L) * 30, visitor.getReport().totalMinutes());
    }
}
