# ADR 0002: Initial Platform Baseline

- Status: accepted
- Date: 2026-09-09

## Context

The core must support a built-in HTTP implementation, modern Spring Boot and Quarkus integrations, asynchronous cancellation, and native-image testing without maintaining separate execution pipelines. This ADR establishes the baseline implemented by the Gradle build and compatibility matrix.

## Decision

- Minimum Java runtime and compilation release for core/product artifacts: Java 11.
- Minimum Java runtime for Spring Boot and Quarkus integrations: Java 17.
- Tested JDKs: core/products on 11, 17, 21, and 25; framework modules on supported combinations starting at 17.
- Build: Gradle Kotlin DSL with the checked-in wrapper and version catalogs/conventions as the module graph grows.
- Blocking and asynchronous APIs: blocking methods plus `CompletableFuture` methods over one pipeline.
- Default HTTP transport: JDK `java.net.http.HttpClient`, behind a SendByte-owned contract.
- Default JSON: Jackson behind a SendByte-owned serialization contract; no Jackson type in the primary API.
- Credentials: fixed API key plus a thread-safe provider for rotation; no request-scoped multi-tenant credentials in the initial release.
- Retries: opt-in until per-operation safety is approved; idempotent email sends may use a conservative default once test vectors exist.
- Native image: required for the Quarkus integration before that integration is labeled stable.

## Rationale

Java 11 gives standalone applications the standard HTTP client without a third-party transport dependency. Java 17 is the practical baseline for modern Spring Boot and Quarkus integrations. Gradle runs on a Java 17 toolchain while core/product compilation uses `--release 11`. Jackson is mature, but isolating it preserves API freedom and permits future alternatives. Opt-in retries prevent duplicate side effects while operation safety is still being classified.

## Consequences

- Java 8 applications will not be supported by the new SDK.
- Core/product modules must avoid APIs and bytecode newer than Java 11.
- Framework integrations may use Java 17 APIs but may not raise the core/product baseline.
- The Gradle build runs with a Java 17 toolchain and compiles core/product code with `--release 11` semantics.
- Framework compatibility lines still need dedicated ADRs based on supported Spring Boot and Quarkus releases.
- CI must verify the Java 11 bytecode/runtime floor rather than relying only on a newer build JDK.
