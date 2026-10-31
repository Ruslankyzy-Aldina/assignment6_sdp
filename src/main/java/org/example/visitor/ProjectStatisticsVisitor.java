package org.example.visitor;

import org.example.element.ConfigurationFile;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;
import org.example.report.ProjectStatisticsReport;

public class ProjectStatisticsVisitor implements ProjectVisitor {
    private long totalFiles;
    private long sourceFiles;
    private long testFiles;
    private long linesOfCode;
    private long numberOfTests;
    private long dependencies;
    private long totalComplexity;

    @Override
    public void visit(SourceCodeFile file) {
        totalFiles++;
        sourceFiles++;
        linesOfCode += file.linesOfCode();
        totalComplexity += file.complexity();
    }

    @Override
    public void visit(TestFile file) {
        totalFiles++;
        testFiles++;
        linesOfCode += file.linesOfCode();
        numberOfTests += file.numberOfTests();
    }

    @Override
    public void visit(ConfigurationFile file) {
        totalFiles++;
    }

    @Override
    public void visit(DependencyFile file) {
        totalFiles++;
        dependencies += file.dependencies().size();
    }

    @Override
    public void visit(Directory directory) {
    }

    public ProjectStatisticsReport getReport() {
        return new ProjectStatisticsReport(totalFiles, sourceFiles, testFiles,
                linesOfCode, numberOfTests, dependencies, totalComplexity);
    }
}
