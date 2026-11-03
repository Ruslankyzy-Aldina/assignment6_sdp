package org.example.visitor;

import java.util.ArrayList;
import java.util.List;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.TechnicalDebtItem;
import org.example.report.TechnicalDebtReport;

public class TechnicalDebtVisitor implements ProjectVisitor {
    private final List<TechnicalDebtItem> items = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        if (file.complexity() > 10) {
            items.add(new TechnicalDebtItem(file.name(), "Reduce complexity above 10",
                    (file.complexity() - 10L) * 30));
        }
        if (file.highRisk()) {
            items.add(new TechnicalDebtItem(file.name(), "Review high-risk source", 60));
        }
    }

    @Override
    public void visit(TestFile file) {
        if (file.numberOfTests() == 0) {
            items.add(new TechnicalDebtItem(file.name(), "Implement tests in empty test file", 30));
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.insecureConfiguration()) {
            items.add(new TechnicalDebtItem(file.name(), "Harden insecure configuration", 30));
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.dependencies()) {
            if (dependency.vulnerable()) {
                items.add(new TechnicalDebtItem(file.name(), "Upgrade vulnerable dependency: " + dependency.name(), 120));
            }
            if (dependency.version().isBlank()) {
                items.add(new TechnicalDebtItem(file.name(), "Specify dependency version: " + dependency.name(), 30));
            }
            if (dependency.prohibited()) {
                items.add(new TechnicalDebtItem(file.name(), "Replace prohibited dependency: " + dependency.name(), 60));
            }
        }
    }

    @Override
    public void visit(Directory directory) {
    }

    public TechnicalDebtReport getReport() {
        return new TechnicalDebtReport(items);
    }
}
