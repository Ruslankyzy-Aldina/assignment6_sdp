package org.example.demo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.example.Main;
import org.example.element.Directory;
import org.example.report.ProjectStatisticsReport;
import org.example.visitor.ComplianceVisitor;
import org.example.visitor.ProjectStatisticsVisitor;
import org.example.visitor.QualityGateVisitor;
import org.example.visitor.SecurityAuditVisitor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FintechProjectTest {
    @Test
    void allVisitorsProduceConsistentReportsForTheSameProject() {
        Directory project = FintechProject.create();
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        QualityGateVisitor quality = new QualityGateVisitor();
        ComplianceVisitor compliance = new ComplianceVisitor();

        project.accept(security);
        project.accept(statistics);
        project.accept(quality);
        project.accept(compliance);

        assertEquals("fintech-project", project.name());
        assertEquals(new ProjectStatisticsReport(5, 2, 1, 390, 6, 2, 18), statistics.getReport());
        assertEquals(5, security.getReport().issues().size());
        assertEquals(3, security.getReport().criticalIssueCount());
        assertEquals(security.getReport(), quality.getReport().security());
        assertEquals(statistics.getReport(), quality.getReport().statistics());
        assertFalse(quality.getReport().securityPassed());
        assertTrue(quality.getReport().complexityPassed());
        assertTrue(quality.getReport().testsPassed());
        assertFalse(quality.getReport().passed());
        assertEquals(5, compliance.getReport().violations().size());
        assertFalse(compliance.getReport().compliant());
        assertTrue(compliance.getReport().format().contains("[MISSING_TESTS] UserService.java"));
    }

    @Test
    void entryPointPrintsAllFourReports() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            Main.main(new String[0]);
        } finally {
            System.setOut(original);
        }
        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Project: fintech-project"));
        assertTrue(text.contains("Security audit\nTotal issues: 5\nCritical issues: 3"));
        assertTrue(text.contains("Project statistics\nTotal files: 5"));
        assertTrue(text.contains("Security: FAIL\nComplexity: PASS\nTests: PASS\n\nFINAL RESULT: FAILED"));
        assertTrue(text.contains("Compliance check\nViolations: 5"));
        assertTrue(text.endsWith("FINAL RESULT: FAIL\n"));
    }
}
