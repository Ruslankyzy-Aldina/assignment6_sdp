package org.example.element;

import java.util.ArrayList;
import java.util.List;
import org.example.visitor.ProjectVisitor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectElementTest {
    @Test
    void dispatchesEachElementToItsTypedMethod() {
        List<ProjectElement> elements = List.of(
                new SourceCodeFile("Payment.java", 100, 4, false, false, true),
                new TestFile("PaymentTest.java", 30, 3, "Payment.java"),
                new ConfigurationFile("app.properties", false, false),
                new DependencyFile("pom.xml", List.of()),
                new Directory("empty", List.of()));
        RecordingVisitor visitor = new RecordingVisitor();
        elements.forEach(element -> element.accept(visitor));
        assertEquals(List.of("source:Payment.java", "test:PaymentTest.java",
                "config:app.properties", "dependencies:pom.xml", "directory:empty"), visitor.visits);
    }

    @Test
    void traversesNestedDirectoriesInPreorder() {
        Directory root = new Directory("root", List.of(
                new Directory("src", List.of(new Directory("payment", List.of(
                        new SourceCodeFile("Payment.java", 100, 4, false, false, true))))),
                new ConfigurationFile("app.properties", false, false)));
        RecordingVisitor visitor = new RecordingVisitor();
        root.accept(visitor);
        assertEquals(List.of("directory:root", "directory:src", "directory:payment",
                "source:Payment.java", "config:app.properties"), visitor.visits);
    }

    @Test
    void visitsEmptyDirectoryOnce() {
        RecordingVisitor visitor = new RecordingVisitor();
        new Directory("empty", List.of()).accept(visitor);
        assertEquals(List.of("directory:empty"), visitor.visits);
    }

    @Test
    void directoryDefensivelyCopiesChildren() {
        List<ProjectElement> children = new ArrayList<>();
        children.add(new ConfigurationFile("app.properties", false, false));
        Directory directory = new Directory("config", children);
        children.clear();
        assertEquals(1, directory.children().size());
        assertThrows(UnsupportedOperationException.class, () -> directory.children().clear());
    }

    @Test
    void dependencyFileDefensivelyCopiesDependencies() {
        Dependency dependency = new Dependency("library", "1.0", true, false);
        List<Dependency> dependencies = new ArrayList<>(List.of(dependency));
        DependencyFile file = new DependencyFile("pom.xml", dependencies);
        dependencies.clear();
        assertEquals(List.of(dependency), file.dependencies());
        assertThrows(UnsupportedOperationException.class, () -> file.dependencies().clear());
    }

    @Test
    void allowsMissingVersionForComplianceChecks() {
        Dependency dependency = new Dependency("library", "", false, true);
        assertEquals("", dependency.version());
        assertTrue(dependency.prohibited());
    }

    @Test
    void rejectsInvalidNamesAndMetrics() {
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCodeFile(" ", 0, 0, false, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCodeFile("A.java", -1, 0, false, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCodeFile("A.java", 0, -1, false, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new TestFile("ATest.java", 0, -1, "A.java"));
        assertThrows(IllegalArgumentException.class,
                () -> new TestFile("ATest.java", 0, 0, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new ConfigurationFile("", false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new DependencyFile("", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Directory("", List.of()));
    }

    @Test
    void rejectsNullChildren() {
        List<ProjectElement> children = new ArrayList<>();
        children.add(null);
        assertThrows(NullPointerException.class, () -> new Directory("root", children));
    }

    private static class RecordingVisitor implements ProjectVisitor {
        private final List<String> visits = new ArrayList<>();

        @Override
        public void visit(SourceCodeFile file) {
            visits.add("source:" + file.name());
        }

        @Override
        public void visit(TestFile file) {
            visits.add("test:" + file.name());
        }

        @Override
        public void visit(ConfigurationFile file) {
            visits.add("config:" + file.name());
        }

        @Override
        public void visit(DependencyFile file) {
            visits.add("dependencies:" + file.name());
        }

        @Override
        public void visit(Directory directory) {
            visits.add("directory:" + directory.name());
        }
    }
}
