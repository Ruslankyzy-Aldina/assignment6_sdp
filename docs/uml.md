# UML diagrams

These Mermaid diagrams describe the implementation through stage 7. Concrete elements and reports are Java records. Fields are abbreviated to focus on Visitor relationships. GitHub renders the diagrams directly.

## Class diagram

```mermaid
classDiagram
    class ProjectElement {
        <<interface>>
        +accept(ProjectVisitor visitor) void
    }
    class ProjectVisitor {
        <<interface>>
        +visit(SourceCodeFile file) void
        +visit(TestFile file) void
        +visit(ConfigurationFile file) void
        +visit(DependencyFile file) void
        +visit(Directory directory) void
    }
    class SourceCodeFile {
        +accept(ProjectVisitor visitor) void
    }
    class TestFile {
        +accept(ProjectVisitor visitor) void
    }
    class ConfigurationFile {
        +accept(ProjectVisitor visitor) void
    }
    class DependencyFile {
        +accept(ProjectVisitor visitor) void
    }
    class Directory {
        +accept(ProjectVisitor visitor) void
    }
    class Dependency
    class SecurityAuditVisitor {
        +getReport() SecurityReport
    }
    class ProjectStatisticsVisitor {
        +getReport() ProjectStatisticsReport
    }
    class QualityGateVisitor {
        +getReport() QualityGateReport
    }
    class ComplianceVisitor {
        +getReport() ComplianceReport
    }
    ProjectElement <|.. SourceCodeFile
    ProjectElement <|.. TestFile
    ProjectElement <|.. ConfigurationFile
    ProjectElement <|.. DependencyFile
    ProjectElement <|.. Directory
    Directory "1" o-- "0..*" ProjectElement : children
    DependencyFile "1" o-- "0..*" Dependency : dependencies
    ProjectElement ..> ProjectVisitor : accept argument
    SourceCodeFile ..> ProjectVisitor : visit(this)
    TestFile ..> ProjectVisitor : visit(this)
    ConfigurationFile ..> ProjectVisitor : visit(this)
    DependencyFile ..> ProjectVisitor : visit(this)
    Directory ..> ProjectVisitor : visit(this), then children.accept
    ProjectVisitor <|.. SecurityAuditVisitor
    ProjectVisitor <|.. ProjectStatisticsVisitor
    ProjectVisitor <|.. QualityGateVisitor
    ProjectVisitor <|.. ComplianceVisitor
    ProjectVisitor ..> SourceCodeFile : visit parameter
    ProjectVisitor ..> TestFile : visit parameter
    ProjectVisitor ..> ConfigurationFile : visit parameter
    ProjectVisitor ..> DependencyFile : visit parameter
    ProjectVisitor ..> Directory : visit parameter
    QualityGateVisitor *-- SecurityAuditVisitor : security
    QualityGateVisitor *-- ProjectStatisticsVisitor : statistics
    SecurityAuditVisitor ..> SecurityReport : creates
    ProjectStatisticsVisitor ..> ProjectStatisticsReport : creates
    QualityGateVisitor ..> QualityGateReport : creates
    ComplianceVisitor ..> ComplianceReport : creates
    SecurityReport o-- SecurityIssue : issues
    ComplianceReport o-- ComplianceViolation : violations
    QualityGateReport --> SecurityReport : security
    QualityGateReport --> ProjectStatisticsReport : statistics
```

Each concrete visitor implements all five `visit` overloads declared by `ProjectVisitor`. `Directory.accept` owns traversal; visitor directory methods do not traverse children. Aggregation shows that immutable elements and dependencies may be shared between trees.

## Sequence diagram

This shows the security audit of `PaymentService.java` in the demo. Other children are omitted for readability.

```mermaid
sequenceDiagram
    participant Main
    participant Root as fintech-project:Directory
    participant Src as src:Directory
    participant File as PaymentService.java:SourceCodeFile
    participant Audit as security:SecurityAuditVisitor
    Main->>Root: accept(security)
    Root->>Audit: visit(this Directory)
    Audit-->>Root: return
    Root->>Src: accept(security)
    Src->>Audit: visit(this Directory)
    Audit-->>Src: return
    Src->>File: accept(security)
    File->>Audit: visit(this SourceCodeFile)
    Audit->>Audit: record CRITICAL hardcoded credentials
    Audit->>Audit: record HIGH source risk
    Audit-->>File: return
    File-->>Src: return
    Src-->>Root: return
    Note over Root,Audit: Remaining children are visited in list order
    Root-->>Main: return
    Main->>Audit: getReport()
    Audit-->>Main: immutable SecurityReport snapshot
    Note over Main: Print report.format()
```

The first dispatch selects the concrete element's `accept` method. Inside it, `this` has a concrete type, selecting the matching `visit` overload. Runtime dispatch selects that method on the concrete visitor.
