# Smart Code Inspection and Compliance Engine

Assignment 6: a Java application that will use the Visitor pattern to inspect software projects for security, quality, and compliance issues.

## Current status

The initial setup includes a Java 17 Maven configuration, JUnit 5 dependencies, and a runnable entry point. Elements, visitors, reports, tests, and UML diagrams will be added in subsequent stages.

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

JUnit 5 and Maven Surefire are configured. This initial setup does not contain tests yet. Later stages will add at least 12 meaningful tests for dispatch, security, statistics, quality gates, compliance, nested and empty directories, the additional visitor, and edge cases.

## Planned design

`ProjectElement` will declare `accept(ProjectVisitor visitor)`. Concrete elements will represent source files, test files, configuration files, dependency files, and directories. Directories will contain other project elements, including nested directories.

`ProjectVisitor` will declare a typed `visit` method for each concrete element. Separate visitors will audit security, collect statistics, evaluate release quality gates, and check compliance. A technical debt visitor will be introduced later without changing the existing elements.

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
