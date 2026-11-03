package org.example;

import org.example.demo.FintechProject;
import org.example.element.Directory;
import org.example.visitor.ComplianceVisitor;
import org.example.visitor.ProjectStatisticsVisitor;
import org.example.visitor.QualityGateVisitor;
import org.example.visitor.SecurityAuditVisitor;

public class Main {
    public static void main(String[] args) {
        Directory project = FintechProject.create();
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        QualityGateVisitor quality = new QualityGateVisitor();
        ComplianceVisitor compliance = new ComplianceVisitor();

        project.accept(security);
        project.accept(statistics);
        project.accept(quality);
        project.accept(compliance);

        System.out.println("Smart Code Inspection and Compliance Engine");
        System.out.println("Project: " + project.name());
        System.out.println();
        System.out.println(security.getReport().format());
        System.out.println(statistics.getReport().format());
        System.out.println("Release quality gate");
        System.out.println(quality.getReport().format());
        System.out.print(compliance.getReport().format());
    }
}
