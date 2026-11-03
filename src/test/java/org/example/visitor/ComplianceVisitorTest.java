package org.example.visitor;

import java.util.List;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.ComplianceReport;
import org.example.report.ComplianceViolation;
import org.junit.jupiter.api.Test;

import static org.example.report.ComplianceViolation.Rule.*;
import static org.junit.jupiter.api.Assertions.*;

class ComplianceVisitorTest {
    @Test
    void reportsCredentialsInSourceAndConfiguration() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new SourceCodeFile("A.java", 100, 5, true, false, false).accept(visitor);
        new ConfigurationFile("app.properties", true, false).accept(visitor);

        assertEquals(List.of(
                new ComplianceViolation(EXPOSED_CREDENTIALS, "A.java", "Hardcoded credentials"),
                new ComplianceViolation(EXPOSED_CREDENTIALS, "app.properties", "Exposed credentials")),
                visitor.getReport().violations());
        assertFalse(visitor.getReport().compliant());
    }

    @Test
    void reportsMissingVersionsAndProhibitedDependenciesIndependently() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new DependencyFile("pom.xml", List.of(
                new Dependency("safe", "1", false, false),
                new Dependency("restricted", " ", false, true),
                new Dependency("unversioned", "", false, false))).accept(visitor);

        assertEquals(List.of(
                new ComplianceViolation(MISSING_DEPENDENCY_VERSION, "pom.xml", "Dependency version is missing: restricted"),
                new ComplianceViolation(PROHIBITED_DEPENDENCY, "pom.xml", "Prohibited dependency: restricted"),
                new ComplianceViolation(MISSING_DEPENDENCY_VERSION, "pom.xml", "Dependency version is missing: unversioned")),
                visitor.getReport().violations());
    }

    @Test
    void requiresTestsOnlyForProductionSources() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new SourceCodeFile("Production.java", 100, 5, false, false, true).accept(visitor);
        new SourceCodeFile("Example.java", 100, 5, false, false, false).accept(visitor);

        assertEquals(List.of(new ComplianceViolation(MISSING_TESTS, "Production.java",
                "Production source has no tests")), visitor.getReport().violations());
    }

    @Test
    void emptyOrUnrelatedTestsDoNotSatisfyProductionRule() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new SourceCodeFile("A.java", 100, 5, false, false, true).accept(visitor);
        new TestFile("EmptyTest.java", 20, 0, "A.java").accept(visitor);
        new TestFile("OtherTest.java", 20, 10, "B.java").accept(visitor);

        assertEquals(1, visitor.getReport().violations().size());
        assertEquals(MISSING_TESTS, visitor.getReport().violations().get(0).rule());
    }

    @Test
    void findsTestsInNestedDirectoriesRegardlessOfVisitOrder() {
        SourceCodeFile source = new SourceCodeFile("A.java", 100, 5, false, false, true);
        Directory tests = new Directory("tests", List.of(
                new Directory("unit", List.of(new TestFile("ATest.java", 20, 2, "A.java")))));
        ComplianceVisitor first = new ComplianceVisitor();
        ComplianceVisitor second = new ComplianceVisitor();
        new Directory("root", List.of(source, tests)).accept(first);
        new Directory("root", List.of(tests, source)).accept(second);

        assertTrue(first.getReport().compliant());
        assertEquals(first.getReport(), second.getReport());
    }

    @Test
    void securityOnlyFlagsDoNotCreateComplianceViolations() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new SourceCodeFile("Example.java", 100, 50, false, true, false).accept(visitor);
        new ConfigurationFile("app.properties", false, true).accept(visitor);
        new DependencyFile("pom.xml", List.of(new Dependency("library", "1", true, false)))
                .accept(visitor);

        assertTrue(visitor.getReport().compliant());
    }

    @Test
    void emptyProjectPassesCompliance() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new Directory("empty", List.of()).accept(visitor);

        assertEquals("Compliance check\nViolations: 0\nFINAL RESULT: PASS\n", visitor.getReport().format());
    }

    @Test
    void reportsAreImmutableAndRepeatedReadsDoNotDuplicateFindings() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new SourceCodeFile("A.java", 100, 5, false, false, true).accept(visitor);
        ComplianceReport snapshot = visitor.getReport();
        assertEquals(snapshot, visitor.getReport());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.violations().clear());

        new TestFile("ATest.java", 20, 2, "A.java").accept(visitor);
        assertTrue(visitor.getReport().compliant());
        assertFalse(snapshot.compliant());
        assertEquals("Compliance check\nViolations: 1\n[MISSING_TESTS] A.java: Production source has no tests\n"
                + "FINAL RESULT: FAIL\n", snapshot.format());
    }
}
