# Smart Code Inspection and Compliance Engine

Assignment 6: a Java application that will use the Visitor pattern to inspect software projects for security, quality, and compliance issues.

## Current status

Stages 1 through 6 are implemented: Java 17 Maven setup, five project element types, directory traversal, security auditing, project statistics, quality gates, compliance checks, immutable reports, and 40 JUnit 5 tests. The demo, UML diagrams, and additional visitor will follow in subsequent stages.

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

Forty JUnit 5 tests cover element dispatch, traversal, validation, security findings, statistics, immutable snapshots, empty projects, large totals, and quality gate PASS/FAIL cases. Gate tests include threshold boundaries, critical findings, noncritical warnings, and consistency with standalone visitors. Compliance tests cover credentials, dependency rules, production test requirements, traversal order, empty projects, and immutable reports. Later stages will add tests for the additional visitor.

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

Test files and directories have no direct security rules. Directory traversal remains in `Directory.accept`. Prohibited dependencies and missing versions are checked by the compliance visitor.

```java
SecurityAuditVisitor audit = new SecurityAuditVisitor();
project.accept(audit);
SecurityReport report = audit.getReport();
System.out.print(report.format());
```

Use `org.example.visitor.SecurityAuditVisitor` and `org.example.report.SecurityReport`; `project` is any `ProjectElement`. Each visitor accumulates findings across visits, so create a new visitor for each independent audit. `getReport()` returns an immutable snapshot with file names, severity, descriptions, and a critical issue count for the future quality gate. The entry point remains a placeholder until the demo stage.

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
