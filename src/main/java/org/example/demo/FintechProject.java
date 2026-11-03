package org.example.demo;

import java.util.List;
import org.example.element.ConfigurationFile;
import org.example.element.Dependency;
import org.example.element.DependencyFile;
import org.example.element.Directory;
import org.example.element.SourceCodeFile;
import org.example.element.TestFile;

public final class FintechProject {
    private FintechProject() {
    }

    public static Directory create() {
        return new Directory("fintech-project", List.of(
                new Directory("src", List.of(
                        new SourceCodeFile("PaymentService.java", 180, 12, true, true, true),
                        new SourceCodeFile("UserService.java", 120, 6, false, false, true))),
                new Directory("tests", List.of(
                        new TestFile("PaymentServiceTest.java", 90, 6, "PaymentService.java"))),
                new Directory("config", List.of(
                        new ConfigurationFile("application.properties", true, true))),
                new DependencyFile("pom.xml", List.of(
                        new Dependency("demo-payment-sdk", "1.0", true, false),
                        new Dependency("demo-legacy-crypto", "", false, true)))));
    }
}
