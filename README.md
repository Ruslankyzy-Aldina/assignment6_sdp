# Smart Code Inspection and Compliance Engine

Assignment 6: a Java application that will use the Visitor pattern to inspect software projects for security, quality, and compliance issues.

## Current status

All eight implementation stages are complete: five project element types, five visitors, immutable reports, a runnable fintech demo, UML diagrams, design explanations, and 50 JUnit 5 tests. Stage 8 adds TechnicalDebtVisitor as a separate change without modifying existing element classes or ProjectVisitor.

## Requirements

- JDK 17 or newer
- Maven 3.9 or newer

## Build and run

```shell
mvn clean package
java -cp target/classes org.example.Main
```

The application runs all five visitors on the same in-memory project and prints their reports, beginning with:

```text
Smart Code Inspection and Compliance Engine
Project: fintech-project
```

## Testing

```shell
mvn test
```

Fifty JUnit 5 tests cover element dispatch, traversal, validation, security findings, statistics, immutable snapshots, empty projects, large totals, and quality gate PASS/FAIL cases. Gate tests include threshold boundaries, critical findings, noncritical warnings, and consistency with standalone visitors. Compliance tests cover credentials, dependency rules, production test requirements, traversal order, empty projects, and immutable reports. Two integration tests verify the demo totals, consistency between visitors, and all five reports printed by Main. Eight technical debt tests cover rule weights, thresholds, empty test files, dependency combinations, traversal, immutable reports, and arithmetic beyond the integer range.

## Design

`org.example.element.ProjectElement` declares `accept(ProjectVisitor visitor)`. Five immutable records implement it: `SourceCodeFile`, `TestFile`, `ConfigurationFile`, `DependencyFile`, and `Directory`. Source files store code metrics and security flags; test files store line and test counts and their target source file; configuration files store security flags. `Dependency` stores a dependency name, version, and vulnerability/prohibition flags. An empty version represents a missing version for future compliance checks.

`org.example.visitor.ProjectVisitor` declares a typed `visit` method for each concrete element. Each element calls `visitor.visit(this)`. Runtime dispatch selects the element implementation, which selects the matching visit overload; runtime dispatch then selects the concrete visitor implementation. This is double dispatch without type checks. `Directory.accept` visits the directory first, then recursively visits its children in list order. Visitors must not traverse children themselves. Lists are defensively copied, names must be nonblank, and metrics must be nonnegative. Separate visitors audit security, collect statistics, evaluate release quality gates, and check compliance. TechnicalDebtVisitor adds effort estimates without changing the existing elements or visitor interface.

## Security audit

`SecurityAuditVisitor` evaluates the flags stored in project elements; it does not parse real source files or query vulnerability databases.

| Rule | Severity |
| --- | --- |
| Hardcoded source credentials | CRITICAL |
| Exposed configuration credentials | CRITICAL |
| Vulnerable dependency | CRITICAL |
| High-risk source file | HIGH |
| Insecure configuration | HIGH |

Test files and directories have no direct security rules. Directory traversal remains in `Directory.accept`. Prohibited dependencies and missing versions are checked by the compliance visitor.

```java
SecurityAuditVisitor audit = new SecurityAuditVisitor();
project.accept(audit);
SecurityReport report = audit.getReport();
System.out.print(report.format());
```

Use `org.example.visitor.SecurityAuditVisitor` and `org.example.report.SecurityReport`; `project` is any `ProjectElement`. Each visitor accumulates findings across visits, so create a new visitor for each independent audit. `getReport()` returns an immutable snapshot with file names, severity, descriptions, and a critical issue count used by the quality gate. Main demonstrates all five visitors on the same project.

## Project statistics

`ProjectStatisticsVisitor` accumulates metrics from the same element tree and returns an immutable `ProjectStatisticsReport` snapshot through `getReport()`.

| Metric | Definition |
| --- | --- |
| Total files | Source, test, configuration, and dependency files; directories are excluded |
| Lines of code | Sum of source and test lines; other element types do not store line counts |
| Tests | Sum of `TestFile.numberOfTests`, separate from the number of test files |
| Dependencies | Number of dependency declarations across all dependency files, including repeated declarations |
| Average complexity | Sum of source complexity divided by source file count |
| Test/source ratio | Test file count divided by source file count; this is not code coverage |

Both derived metrics return `0.0` when no source files exist. All source files contribute, including non-production files. Totals use `long`; derived values use floating-point division. Reports format decimal values with a dot regardless of the system locale.

```java
ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
project.accept(statistics);
System.out.print(statistics.getReport().format());
```

Import `org.example.visitor.ProjectStatisticsVisitor`. Create a new visitor for each independent analysis; repeated visits intentionally accumulate totals. Elements and directory traversal are unchanged.

## Release quality gate

`QualityGateVisitor` forwards each typed visit to its own security and statistics visitors. The directory performs traversal once; the gate does not recursively revisit children. Existing elements and visitors remain unchanged.

| Check | PASS condition |
| --- | --- |
| Security | Zero CRITICAL issues |
| Complexity | Average source complexity <= 10 |
| Tests | Test file count / source file count >= 0.40 |

All three checks must pass. HIGH severity findings remain visible in the security report but do not block this gate. The ratio measures file counts, not test case counts or coverage. Empty and test-only projects fail the test check because their ratio is defined as zero. These checks are the assignment's release criteria, not a complete production readiness assessment.

```java
QualityGateVisitor gate = new QualityGateVisitor();
project.accept(gate);
System.out.print(gate.getReport().format());
```

Import `org.example.visitor.QualityGateVisitor`. Create a fresh visitor for each independent project. `getReport()` returns a `QualityGateReport` snapshot containing both underlying reports, individual check results, and the combined `passed()` result. Evaluation uses unrounded metrics.

Example output for a project with insufficient test files:

```text
Security: PASS
Complexity: PASS
Tests: FAIL

FINAL RESULT: FAILED
```

## Compliance checks

`ComplianceVisitor` checks four rules:

- Source and configuration files must not expose credentials.
- Every dependency must have a nonblank version.
- Dependencies marked as prohibited are not allowed.
- Each production source must have a matching test file containing at least one test.

The last rule matches `TestFile.testedSourceFile` to `SourceCodeFile.name` exactly. Use unique source identifiers across the project, such as project-relative paths when modules have identical file names, and the same identifier in the test. Directory names are not automatically prefixed. Non-production sources do not require tests.

Production test checks are evaluated in `getReport()` after collecting visited sources and tests. Tests can appear before or after sources, including in other nested directories. Read the final report after the complete traversal. Reports are immutable snapshots; repeated report reads do not add violations or change visitor state.

```java
ComplianceVisitor compliance = new ComplianceVisitor();
project.accept(compliance);
System.out.print(compliance.getReport().format());
```

Import `org.example.visitor.ComplianceVisitor`. Create a fresh visitor for each independent analysis. `ComplianceReport.compliant()` is true only when there are no violations. An empty project passes these rules, although it fails the quality gate's test ratio requirement. Security flags for vulnerable dependencies, high-risk sources, and insecure configuration remain the responsibility of the security audit; compliance and release quality reports are separate.

## Technical debt estimate

`TechnicalDebtVisitor` implements the existing five visit methods. No element classes, traversal logic, or ProjectVisitor methods were changed to add it. It returns an immutable `TechnicalDebtReport` with per-file reasons and estimated minutes.

| Condition | Estimated effort |
| --- | --- |
| Source complexity above 10 | 30 minutes per excess complexity point |
| High-risk source flag | 60 minutes |
| Test file with zero test cases | 30 minutes |
| Insecure configuration | 30 minutes |
| Vulnerable dependency | 120 minutes per declaration |
| Missing dependency version | 30 minutes per declaration |
| Prohibited dependency | 60 minutes per declaration |

Rules are additive, including multiple findings on the same file or dependency. Directories add no effort. Complexity at or below 10 adds no complexity debt. Totals use `long` to avoid overflowing the integer range when multiplying source complexity.

These are fixed educational heuristics, not measured delivery estimates. They do not cover every security or compliance finding: exposed credentials and missing production test files remain visible in the corresponding reports. Debt does not affect the release gate.

```java
TechnicalDebtVisitor debt = new TechnicalDebtVisitor();
project.accept(debt);
System.out.print(debt.getReport().format());
```

Import `org.example.visitor.TechnicalDebtVisitor`. Use a fresh visitor for each independent analysis. The demo estimates 120 minutes for PaymentService, 30 for configuration, and 210 for dependencies: 360 minutes in total.

## Demo project

`org.example.demo.FintechProject.create()` builds this structure in memory. It models file metadata; it does not create or scan these files on disk. Dependency names and security flags are fictional demonstration data.

```text
fintech-project/
├── src/
│   ├── PaymentService.java
│   └── UserService.java
├── tests/
│   └── PaymentServiceTest.java
├── config/
│   └── application.properties
└── pom.xml
```

The payment source has hardcoded credentials and a high-risk flag. Configuration exposes credentials and is insecure. One dependency is marked vulnerable; another is prohibited and lacks a version. UserService has no matching test file. No real credentials are stored.

| Report | Expected result |
| --- | --- |
| Security | 5 issues, including 3 CRITICAL |
| Statistics | 5 files, 390 lines, 6 tests, 2 dependency declarations |
| Derived metrics | Average complexity 9.00; test/source ratio 0.50 |
| Quality gate | Security FAIL, Complexity PASS, Tests PASS; final FAILED |
| Compliance | 5 violations; final FAIL |
| Technical debt | 6 items; estimated effort 360 minutes |

The gate passes the file ratio even though UserService has no tests. Compliance catches that separate per-source requirement. The demo exits normally after printing failed inspection results; it is not a CI command with failure exit codes.

## UML

See [class and sequence diagrams](docs/uml.md). The class diagram shows element and visitor implementations, accept/visit relationships, directory children, report types, and quality gate delegation. The sequence diagram follows the security visitor through a directory to PaymentService.java.

## Design questions

### What problem does Visitor solve here?

The same project tree needs several independent inspections. Visitor separates those operations from the data model so a new inspection does not require adding another operation to every element.

### Where are Element and Visitor?

`org.example.element.ProjectElement` is the Element interface. SourceCodeFile, TestFile, ConfigurationFile, DependencyFile, and Directory implement it. `org.example.visitor.ProjectVisitor` is the Visitor interface; the five inspection visitors implement its five overloads.

### What is double dispatch?

Calling `element.accept(visitor)` first dispatches to the runtime element implementation. Its `visitor.visit(this)` selects the overload for that concrete element type, then dispatches to the runtime visitor implementation. Both element type and visitor type determine the operation. Removing `accept` would lose that concrete-type handoff when elements are held as ProjectElement references.

### Why not put all operations inside the elements?

Methods such as checkSecurity and checkCompliance would mix data representation with unrelated rules. Every new analysis would require edits across the element hierarchy. Here each visitor owns one analysis while the elements hold metadata and provide traversal/dispatch.

### Is adding a Visitor or an Element easier?

Adding a visitor is easier: implement the existing visit methods and use it with the existing tree. No element classes need to change. Adding an element changes the visitor contract and requires every visitor to handle the new type.

### What happens when DatabaseMigrationFile is added?

Add a DatabaseMigrationFile class implementing ProjectElement with its data and `accept` calling `visitor.visit(this)`. Add `visit(DatabaseMigrationFile file)` to ProjectVisitor. Implement that overload in SecurityAuditVisitor, ProjectStatisticsVisitor, QualityGateVisitor, ComplianceVisitor, and TechnicalDebtVisitor. QualityGateVisitor must forward the new type to its delegated visitors. Test visitors such as RecordingVisitor must also implement the overload.

Without the interface overload, the new element's accept call does not compile. After adding the overload, concrete visitors fail to compile until they implement it. Existing element classes and Directory traversal do not need changes. Add migration fixtures and tests, update the UML, and extend reports only if the new rules need additional metrics. This is Visitor's tradeoff: adding operations is easy, but expanding the element hierarchy affects all visitors.

### When would Visitor be a bad choice?

Visitor is a poor fit when element types change frequently, when there is only one simple operation, or when operations need private details that should not be exposed. In those cases it can add coupling and boilerplate without much benefit.

### How is Visitor different from Strategy?

Strategy provides interchangeable implementations of one algorithm behind a common interface, such as several complexity scoring algorithms. Visitor provides an operation over a heterogeneous structure with a separate typed method for each element. Strategy does not inherently provide double dispatch across element types.

### Which SOLID principles apply?

- Single Responsibility: elements store metadata, visitors implement inspections, and report records format their results.
- Open/Closed: new visitors extend behavior without changing existing elements; this advantage does not apply when introducing a new element type.
- Liskov Substitution: all elements support the ProjectElement accept contract, so Directory can traverse them uniformly.
- Dependency Inversion: elements depend on the ProjectVisitor interface rather than concrete inspection classes. QualityGateVisitor deliberately composes two concrete visitors to reuse their rules.
- Interface Segregation is a tradeoff: ProjectVisitor forces each visitor to support every element, so some methods are empty. The design does not fully avoid unused methods.

## Development milestones

1. Initialize Maven, the application entry point, and README.
2. Add the element hierarchy, visitor interface, and directory traversal.
3. Implement security auditing and its tests.
4. Implement project statistics and its tests.
5. Implement quality gates with passing and failing tests.
6. Implement compliance checks and their tests.
7. Add the fintech demo, UML diagrams, and design explanations; complete core test coverage.
8. Add `TechnicalDebtVisitor` and its tests in a separate commit, then update the demo and documentation.

TechnicalDebtVisitor implements the existing visitor contract. Its separate development stage demonstrates adding an operation without changing the element hierarchy.
