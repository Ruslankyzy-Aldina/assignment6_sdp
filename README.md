# Smart Code Inspection and Compliance Engine

Assignment 6: a Java application that will use the Visitor pattern to inspect software projects for security, quality, and compliance issues.

## Current status

Stages 1 and 2 are implemented: Java 17 Maven setup, a runnable entry point, five project element types, the visitor interface, directory traversal, and eight JUnit 5 tests. Concrete inspection visitors, reports, and UML diagrams will be added in subsequent stages.

## Requirements

- JDK 17 or newer
- Maven 3.9 or newer

## Build and run

```shell
mvn clean package
java -cp target/classes org.example.Main
```

Expected output:

```text
Smart Code Inspection and Compliance Engine
```

## Testing

```shell
mvn test
```

Eight JUnit 5 tests cover typed dispatch, nested and empty directories, defensive collection copies, missing dependency versions, invalid metrics, and null children. Later stages will extend coverage to at least 12 meaningful tests including the concrete visitors.

## Design

`org.example.element.ProjectElement` declares `accept(ProjectVisitor visitor)`. Five immutable records implement it: `SourceCodeFile`, `TestFile`, `ConfigurationFile`, `DependencyFile`, and `Directory`. Source files store code metrics and security flags; test files store line and test counts and their target source file; configuration files store security flags. `Dependency` stores a dependency name, version, and vulnerability/prohibition flags. An empty version represents a missing version for future compliance checks.

`org.example.visitor.ProjectVisitor` declares a typed `visit` method for each concrete element. Each element calls `visitor.visit(this)`. Runtime dispatch selects the element implementation, which selects the matching visit overload; runtime dispatch then selects the concrete visitor implementation. This is double dispatch without type checks. `Directory.accept` visits the directory first, then recursively visits its children in list order. Visitors must not traverse children themselves. Lists are defensively copied, names must be nonblank, and metrics must be nonnegative. Separate visitors will audit security, collect statistics, evaluate release quality gates, and check compliance. A technical debt visitor will be introduced later without changing the existing elements.

## Development milestones

1. Initialize Maven, the application entry point, and README.
2. Add the element hierarchy, visitor interface, and directory traversal.
3. Implement security auditing and its tests.
4. Implement project statistics and its tests.
5. Implement quality gates with passing and failing tests.
6. Implement compliance checks and their tests.
7. Add the fintech demo, UML diagrams, and design explanations; complete core test coverage.
8. Add `TechnicalDebtVisitor` and its tests in a separate commit, then update the demo and documentation.

The final documentation will explain double dispatch, Visitor tradeoffs, adding `DatabaseMigrationFile`, the difference from Strategy, and the applicable SOLID principles.
