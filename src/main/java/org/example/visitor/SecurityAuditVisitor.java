package org.example.visitor;

import java.util.ArrayList;
import java.util.List;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.SecurityIssue;
import org.example.report.SecurityReport;

import static org.example.report.SecurityIssue.Severity.CRITICAL;
import static org.example.report.SecurityIssue.Severity.HIGH;

public class SecurityAuditVisitor implements ProjectVisitor {
    private final List<SecurityIssue> issues = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        if (file.hardcodedCredentials()) {
            issues.add(new SecurityIssue(CRITICAL, file.name(), "Hardcoded credentials"));
        }
        if (file.highRisk()) {
            issues.add(new SecurityIssue(HIGH, file.name(), "High-risk source file"));
        }
    }

    @Override
    public void visit(TestFile file) {
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.exposedCredentials()) {
            issues.add(new SecurityIssue(CRITICAL, file.name(), "Exposed credentials"));
        }
        if (file.insecureConfiguration()) {
            issues.add(new SecurityIssue(HIGH, file.name(), "Insecure configuration"));
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.dependencies()) {
            if (dependency.vulnerable()) {
                String version = dependency.version().isBlank() ? "unspecified" : dependency.version();
                issues.add(new SecurityIssue(CRITICAL, file.name(),
                        "Vulnerable dependency: " + dependency.name() + " (version: " + version + ")"));
            }
        }
    }

    @Override
    public void visit(Directory directory) {
    }

    public SecurityReport getReport() {
        return new SecurityReport(issues);
    }
}
