package org.example.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.ComplianceReport;
import org.example.report.ComplianceViolation;

import static org.example.report.ComplianceViolation.Rule.*;

public class ComplianceVisitor implements ProjectVisitor {
    private final List<ComplianceViolation> violations = new ArrayList<>();
    private final List<String> productionSources = new ArrayList<>();
    private final Set<String> testedSources = new HashSet<>();

    @Override
    public void visit(SourceCodeFile file) {
        if (file.hardcodedCredentials()) {
            violations.add(new ComplianceViolation(EXPOSED_CREDENTIALS, file.name(), "Hardcoded credentials"));
        }
        if (file.production()) {
            productionSources.add(file.name());
        }
    }

    @Override
    public void visit(TestFile file) {
        if (file.numberOfTests() > 0) {
            testedSources.add(file.testedSourceFile());
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.exposedCredentials()) {
            violations.add(new ComplianceViolation(EXPOSED_CREDENTIALS, file.name(), "Exposed credentials"));
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.dependencies()) {
            if (dependency.version().isBlank()) {
                violations.add(new ComplianceViolation(MISSING_DEPENDENCY_VERSION, file.name(),
                        "Dependency version is missing: " + dependency.name()));
            }
            if (dependency.prohibited()) {
                violations.add(new ComplianceViolation(PROHIBITED_DEPENDENCY, file.name(),
                        "Prohibited dependency: " + dependency.name()));
            }
        }
    }

    @Override
    public void visit(Directory directory) {
    }

    public ComplianceReport getReport() {
        List<ComplianceViolation> findings = new ArrayList<>(violations);
        for (String source : productionSources) {
            if (!testedSources.contains(source)) {
                findings.add(new ComplianceViolation(MISSING_TESTS, source, "Production source has no tests"));
            }
        }
        return new ComplianceReport(findings);
    }
}
