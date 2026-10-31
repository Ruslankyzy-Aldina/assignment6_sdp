package org.example.visitor;

import java.util.List;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.SecurityIssue;
import org.example.report.SecurityReport;
import org.junit.jupiter.api.Test;

import static org.example.report.SecurityIssue.Severity.CRITICAL;
import static org.example.report.SecurityIssue.Severity.HIGH;
import static org.junit.jupiter.api.Assertions.*;

class SecurityAuditVisitorTest {
    @Test
    void detectsBothSourceSecurityFlags() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new SourceCodeFile("Payment.java", 100, 4, true, true, true).accept(visitor);

        assertEquals(List.of(
                new SecurityIssue(CRITICAL, "Payment.java", "Hardcoded credentials"),
                new SecurityIssue(HIGH, "Payment.java", "High-risk source file")),
                visitor.getReport().issues());
        assertEquals(1, visitor.getReport().criticalIssueCount());
    }

    @Test
    void detectsBothConfigurationSecurityFlags() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new ConfigurationFile("app.properties", true, true).accept(visitor);

        assertEquals(List.of(
                new SecurityIssue(CRITICAL, "app.properties", "Exposed credentials"),
                new SecurityIssue(HIGH, "app.properties", "Insecure configuration")),
                visitor.getReport().issues());
    }

    @Test
    void reportsOnlyVulnerableDependencies() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new DependencyFile("pom.xml", List.of(
                new Dependency("safe", "2.0", false, false),
                new Dependency("unsafe", "1.0", true, false),
                new Dependency("restricted", "3.0", false, true))).accept(visitor);

        assertEquals(List.of(new SecurityIssue(CRITICAL, "pom.xml",
                "Vulnerable dependency: unsafe (version: 1.0)")), visitor.getReport().issues());
    }

    @Test
    void handlesVulnerableDependencyWithoutVersion() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new DependencyFile("pom.xml", List.of(new Dependency("unsafe", "", true, false))).accept(visitor);

        assertEquals("Vulnerable dependency: unsafe (version: unspecified)",
                visitor.getReport().issues().get(0).message());
    }

    @Test
    void cleanProjectHasNoFindings() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new Directory("project", List.of(
                new SourceCodeFile("A.java", 100, 3, false, false, true),
                new TestFile("ATest.java", 30, 3, "A.java"),
                new ConfigurationFile("app.properties", false, false),
                new DependencyFile("pom.xml", List.of(new Dependency("safe", "1", false, false)))))
                .accept(visitor);

        assertTrue(visitor.getReport().issues().isEmpty());
        assertEquals(0, visitor.getReport().criticalIssueCount());
        assertTrue(visitor.getReport().format().contains("No security issues found."));
    }

    @Test
    void nestedDirectoriesDoNotDuplicateFindings() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new Directory("root", List.of(new Directory("config", List.of(
                new ConfigurationFile("app.properties", true, false))))).accept(visitor);

        assertEquals(1, visitor.getReport().issues().size());
        assertEquals(1, visitor.getReport().criticalIssueCount());
    }

    @Test
    void emptyDirectoryProducesEmptyReport() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new Directory("empty", List.of()).accept(visitor);

        assertEquals("Security audit\nTotal issues: 0\nCritical issues: 0\nNo security issues found.\n",
                visitor.getReport().format());
    }

    @Test
    void reportIsAnImmutableSnapshot() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new ConfigurationFile("first.properties", true, false).accept(visitor);
        SecurityReport snapshot = visitor.getReport();
        new ConfigurationFile("second.properties", true, false).accept(visitor);

        assertEquals(1, snapshot.issues().size());
        assertEquals(2, visitor.getReport().issues().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.issues().clear());
        assertTrue(snapshot.format().contains("[CRITICAL] first.properties: Exposed credentials"));
        assertFalse(snapshot.format().contains("second.properties"));
    }
}
