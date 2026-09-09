# ADR 0001: Build a New SDK and Treat the Existing Java SDK as Reference Material

- Status: accepted
- Date: 2026-09-09

## Context

`sendbyte-4j` is a new Gradle project. `SendByte-Java-main` contains useful Java implementation ideas, but it is a separate Maven project with its own public API, coordinates, dependencies, mutable models, and incomplete coverage of the current SendByte contract.

## Decision

Build the SDK in `sendbyte-4j` as a new public contract. Use `SendByte-Java-main` only as reviewed reference material. There is no source- or binary-compatibility requirement with its `0.1.0` API.

Code may be adapted when it:

1. Matches the vendored OpenAPI contract.
2. Fits the new module and dependency rules.
3. Meets current immutability, cancellation, diagnostics, security, and testing requirements.
4. Retains applicable MIT license attribution.

## Consequences

- Package names, artifact coordinates, models, and client APIs can be designed deliberately.
- There is no migration bridge or old-API deprecation plan.
- Reference tests and implementation can accelerate work but cannot prove current contract conformance.
- The project needs its own semantic-versioning policy beginning with its first published release.
