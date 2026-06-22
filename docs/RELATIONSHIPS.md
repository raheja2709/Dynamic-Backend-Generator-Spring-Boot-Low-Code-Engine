# Relationships and DTO Strategies

## Relationship Types

### MANY_TO_ONE

An entity holds a reference to one instance of another entity.

**JSON Input:**
```json
{
  "fieldName": "department",
  "relationshipType": "MANY_TO_ONE",
  "targetEntity": "Department",
  "fetchType": "LAZY",
  "nullable": false,
  "joinColumn": "department_id",
  "dtoStrategy": "ID_ONLY"
}
```

**Generated Entity Code:**
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```

### ONE_TO_MANY

An entity holds a collection of another entity (inverse side of MANY_TO_ONE).

**JSON Input:**
```json
{
  "fieldName": "employees",
  "relationshipType": "ONE_TO_MANY",
  "targetEntity": "Employee",
  "mappedBy": "department",
  "fetchType": "LAZY",
  "cascadeTypes": "ALL",
  "orphanRemoval": true,
  "dtoStrategy": "ID_ONLY"
}
```

**Generated Entity Code:**
```java
@JsonManagedReference("department-employees")
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private List<Employee> employees = new ArrayList<>();
```

### MANY_TO_MANY

Two entities share a many-to-many relationship via a join table.

**JSON Input:**
```json
{
  "fieldName": "tags",
  "relationshipType": "MANY_TO_MANY",
  "targetEntity": "Tag",
  "fetchType": "LAZY",
  "cascadeTypes": "PERSIST,MERGE",
  "joinTableName": "project_tags",
  "joinColumn": "project_id",
  "inverseJoinColumn": "tag_id",
  "dtoStrategy": "SUMMARY"
}
```

**Generated Entity Code:**
```java
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "project_tags",
    joinColumns = @JoinColumn(name = "project_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private List<Tag> tags = new ArrayList<>();
```

### ONE_TO_ONE

**JSON Input:**
```json
{
  "fieldName": "profile",
  "relationshipType": "ONE_TO_ONE",
  "targetEntity": "UserProfile",
  "fetchType": "LAZY",
  "cascadeTypes": "ALL",
  "orphanRemoval": true,
  "dtoStrategy": "NESTED"
}
```

## DTO Strategies

Each relationship can specify how it appears in Data Transfer Objects.

### ID_ONLY (default)

Only includes the foreign key ID. Lightest representation.

**Single (ManyToOne/OneToOne):**
```java
private Long departmentId;
```

**Collection (OneToMany/ManyToMany):**
```java
private List<Long> employeeIds;
```

### SUMMARY

Includes ID and a display label (first String field of the target entity).

**Single:**
```java
private DepartmentSummaryDto department;
```

**SummaryDto structure:**
```java
public class DepartmentSummaryDto {
    private Long id;
    private String name; // first String field
}
```

### NESTED

Embeds the full DetailDto of the target entity one level deep. Nested relationships within the embedded DTO default to ID_ONLY to prevent infinite recursion.

**Single:**
```java
private UserProfileDetailDto profile;
```

**Collection:**
```java
private List<OrderItemDetailDto> items;
```

### IGNORE

Omits the relationship field entirely from the DTO. Useful for internal or circular references that shouldn't be exposed via API.

## Generated DTO Types

For each entity, the generator produces:

| DTO | Purpose | Relationship Strategy |
|-----|---------|----------------------|
| `ListDto` | Collection endpoints | Always ID_ONLY |
| `DetailDto` | Single-entity detail | Per-relationship config |
| `CreateRequest` | POST body | FK IDs only |
| `UpdateRequest` | PUT body (partial) | FK IDs only |
| `SummaryDto` | Referenced by other entities | id + label |
| `Dto` (legacy) | Backward compatibility | Per-relationship config |

## Cascade Types

Valid values: `ALL`, `PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH`

- Single value: `cascade = CascadeType.ALL`
- Multiple values: `cascade = {CascadeType.PERSIST, CascadeType.MERGE}`
- Duplicates are automatically removed before generation

## Bidirectional JSON Handling

For bidirectional relationships, the generator automatically adds Jackson annotations to prevent infinite serialization loops:

- **Owning side** (has `@JoinColumn`): `@JsonBackReference`
- **Inverse side** (has `mappedBy`): `@JsonManagedReference`
- **Unidirectional**: No JSON annotations added

## Limits

- Maximum 50 relationships per entity
- Target entity must exist within the same project
- Cascade types validated against allowed set
