# SendByte Java SDK

`sendbyte-4j` is the new modular Java SDK for the SendByte API. It uses the official OpenAPI snapshot in [`spec/openapi.yaml`](spec/openapi.yaml) as its wire contract; `../SendByte-Java-main` is reference material only and is not a migration target.

## Current implementation

- Java 11 core and product runtime baseline.
- Framework-neutral client with fixed or provider-backed credentials.
- JDK `HttpClient` transport with synchronous/asynchronous execution and cancellation propagation.
- Internally isolated Jackson serialization.
- Typed API errors with request-ID and rate-limit metadata.
- First email-send vertical slice with immutable request/response models.

Marketing, webhooks, administration, remaining email resources, Spring Boot, and Quarkus are represented in the module plan but are not yet complete.

## Build

Run Gradle itself on JDK 17 or newer. Core and product source is compiled with `--release 11`.

```shell
./gradlew clean check
```

The `check` lifecycle verifies formatting, PMD rules, compiler warnings, dependency
version conflicts, dependency licenses, JavaDoc syntax, the reviewed OpenAPI snapshot,
stable module membership, and Gradle/Maven consumer fixtures. Use
`./gradlew spotlessApply` to format supported source and repository files.

To exercise tests on a specific installed JDK:

```shell
./gradlew test -PtestJavaVersion=11
```

CI runs the test suite on Java 11, 17, 21, and 25 and checks that repeated builds produce identical archives. The delivery sequence and acceptance criteria are tracked in [`tasks/`](tasks/README.md), while architectural decisions are recorded in [`docs/`](docs/ARCHITECTURE.md).

Java contributions follow the repository's [`coding conventions`](docs/CODING_CONVENTIONS.md). Coding agents also receive these rules through the checked-in `AGENTS.md` files so they persist across sessions.
