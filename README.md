# Smart Code Inspection and Compliance Engine

Assignment 6: a Java application that will use the Visitor pattern to inspect software projects for security, quality, and compliance issues.

## Current status

Stages 1 through 3 are implemented: Java 17 Maven setup, five project element types, directory traversal, SecurityAuditVisitor, immutable security reports, and 16 JUnit 5 tests. Statistics, quality gates, compliance, the demo, and UML diagrams will follow in subsequent stages.

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

Sixteen JUnit 5 tests cover element dispatch, directory traversal, validation, immutable collections, security findings, clean projects, missing dependency versions, and report snapshots. Later stages will add tests for the remaining visitors.

## Design

`org.example.element.ProjectElement` declares `accept(ProjectVisitor visitor)`. Five immutable records implement it: `SourceCodeFile`, `TestFile`, `ConfigurationFile`, `DependencyFile`, and `Directory`. Source files store code metrics and security flags; test files store line and test counts and their target source file; configuration files store security flags. `Dependency` stores a dependency name, version, and vulnerability/prohibition flags. An empty version represents a missing version for future compliance checks.

`org.example.visitor.ProjectVisitor` declares a typed `visit` method for each concrete element. Each element calls `visitor.visit(this)`. Runtime dispatch selects the element implementation, which selects the matching visit overload; runtime dispatch then selects the concrete visitor implementation. This is double dispatch without type checks. `Directory.accept` visits the directory first, then recursively visits its children in list order. Visitors must not traverse children themselves. Lists are defensively copied, names must be nonblank, and metrics must be nonnegative. Separate visitors will audit security, collect statistics, evaluate release quality gates, and check compliance. A technical debt visitor will be introduced later without changing the existing elements.

## Security audit

`SecurityAuditVisitor` evaluates the flags stored in project elements; it does not parse real source files or query vulnerability databases.

| Rule | Severity |
| --- | --- |
| Hardcoded source credentials | CRITICAL |
| Exposed configuration credentials | CRITICAL |
| Vulnerable dependency | CRITICAL |
| High-risk source file | HIGH |
| Insecure configuration | HIGH |

Test files and directories have no direct security rules. Directory traversal remains in `Directory.accept`. Prohibited dependencies and missing versions are reserved for the compliance visitor.

```java
SecurityAuditVisitor audit = new SecurityAuditVisitor();
project.accept(audit);
SecurityReport report = audit.getReport();
System.out.print(report.format());
```

Use `org.example.visitor.SecurityAuditVisitor` and `org.example.report.SecurityReport`; `project` is any `ProjectElement`. Each visitor accumulates findings across visits, so create a new visitor for each independent audit. `getReport()` returns an immutable snapshot with file names, severity, descriptions, and a critical issue count for the future quality gate. The entry point remains a placeholder until the demo stage.

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
