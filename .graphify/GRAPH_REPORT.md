# Graph Report - .  (2026-11-08)

## Corpus Check
- Corpus is ~4,346 words - fits in a single context window. You may not need a graph.

## Summary
- 113 nodes · 113 edges · 18 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output
- Edge kinds: method: 67 · calls: 21 · contains: 19 · inherits: 4 · imports: 2


## Input Scope
- Requested: auto
- Resolved: all (source: default-auto)
- Included files: 23 · Candidates: recursive
- Excluded: 0 untracked · 0 ignored · 21 sensitive · 0 missing committed
## God Nodes (most connected - your core abstractions)
1. `JwtService` - 10 edges
2. `JwtServiceTest` - 10 edges
3. `AuthServiceTest` - 9 edges
4. `GlobalExceptionHandler` - 8 edges
5. `AuthControllerTest` - 7 edges
6. `ProcessedEventRepository` - 6 edges
7. `AuthService` - 6 edges
8. `ReactiveAdapter` - 6 edges
9. `AuthController` - 5 edges
10. `AuthServiceTest` - 5 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities

### Community 0 - "Community 0"
Cohesion: 0.24
Nodes (1): JwtService

### Community 1 - "Community 1"
Cohesion: 0.29
Nodes (1): JwtServiceTest

### Community 2 - "Community 2"
Cohesion: 0.29
Nodes (1): AuthServiceTest

### Community 3 - "Community 3"
Cohesion: 0.39
Nodes (1): GlobalExceptionHandler

### Community 4 - "Community 4"
Cohesion: 0.25
Nodes (1): AuthControllerTest

### Community 5 - "Community 5"
Cohesion: 0.29
Nodes (1): ReactiveAdapter

### Community 6 - "Community 6"
Cohesion: 0.38
Nodes (4): ProcessedEvent, ReactiveMongoRepository, ProcessedEventRepository, String

### Community 7 - "Community 7"
Cohesion: 0.33
Nodes (1): AuthService

### Community 8 - "Community 8"
Cohesion: 0.40
Nodes (1): AuthController

### Community 9 - "Community 9"
Cohesion: 0.33
Nodes (1): AuthServiceTest

### Community 10 - "Community 10"
Cohesion: 0.40
Nodes (1): GlobalExceptionHandlerTest

### Community 11 - "Community 11"
Cohesion: 0.40
Nodes (2): BusinessException, RuntimeException

### Community 12 - "Community 12"
Cohesion: 0.67
Nodes (1): AuthServiceApplication

### Community 13 - "Community 13"
Cohesion: 0.67
Nodes (1): SecurityConfig

### Community 14 - "Community 14"
Cohesion: 0.67
Nodes (1): Topics

### Community 15 - "Community 15"
Cohesion: 1.00
Nodes (1): CacheProperties

### Community 16 - "Community 16"
Cohesion: 1.00
Nodes (1): JwtProperties

### Community 18 - "Community 18"
Cohesion: 1.00
Nodes (1): ProcessedEvent

## Knowledge Gaps
- **3 isolated node(s):** `ProcessedEvent`, `CacheProperties`, `JwtProperties`
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 0`** (1 nodes): `JwtService`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 1`** (1 nodes): `JwtServiceTest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 2`** (1 nodes): `AuthServiceTest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 3`** (1 nodes): `GlobalExceptionHandler`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 4`** (1 nodes): `AuthControllerTest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 5`** (1 nodes): `ReactiveAdapter`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 7`** (1 nodes): `AuthService`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 8`** (1 nodes): `AuthController`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 9`** (1 nodes): `AuthServiceTest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 10`** (1 nodes): `GlobalExceptionHandlerTest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 11`** (2 nodes): `BusinessException`, `RuntimeException`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 12`** (1 nodes): `AuthServiceApplication`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 13`** (1 nodes): `SecurityConfig`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 14`** (1 nodes): `Topics`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 15`** (1 nodes): `CacheProperties`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 16`** (1 nodes): `JwtProperties`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 18`** (1 nodes): `ProcessedEvent`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `ProcessedEvent`, `CacheProperties`, `JwtProperties` to the rest of the system?**
  _3 weakly-connected nodes found - possible documentation gaps or missing edges._