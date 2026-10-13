# SendByte Java SDK Architecture Notes

Status: working architecture direction

Last updated: 2026-09-09

Implementation repository: `sendbyte-4j/`

Reference implementation: `../SendByte-Java-main/`

This document records the intended architecture for the new SendByte Java SDK. `SendByte-Java-main` is reference material whose useful ideas may be adapted; this is not a migration or compatibility project. The delivery backlog is tracked in [GitHub Issues](https://github.com/sendbyte-4j/sendbyte-4j/issues).

Handwritten Java code follows [`CODING_CONVENTIONS.md`](CODING_CONVENTIONS.md).

## 1. Product goal

Build a modular, framework-friendly Java SDK that works with sensible defaults, integrates with existing application infrastructure, minimizes dependency conflicts, and explains each request well without becoming an observability platform.

The shorter north star is:

> Easy to adopt, difficult to misuse, and flexible without becoming complicated.

## 2. Verified baseline

### Target project

`sendbyte-4j` is the new multi-module Gradle Kotlin DSL implementation. Its core and product artifacts use the `io.github.sendbyte-4j.sendbyte` group, currently carry the development version `0.1.0-SNAPSHOT`, compile to Java 11 bytecode, and are tested with JUnit 5. Framework integrations will require Java 17. The first implemented vertical slice provides the shared client/runtime plus synchronous and asynchronous email sending.

### Reference project

`SendByte-Java-main` is a separate Maven implementation used only to accelerate design and implementation. Nothing in its package names, API shapes, dependencies, version, or release history is automatically part of the new SDK's contract.

| Area | Reference state |
| --- | --- |
| Build | Single Maven `jar`, version `0.1.0` |
| Coordinates | `io.github.dev-hills:sendbyte-java` |
| Java | Source/target 8; CI declares JDK 8, 11, and 17 |
| Default HTTP | OkHttp 4.12.0 |
| JSON | Jackson Databind 2.17.2 |
| Client | `SendByteClient`, `AutoCloseable`, with resource accessors |
| Products/resources | Emails, domains, templates, webhooks, and API keys |
| Execution styles | Synchronous and `CompletableFuture` methods |
| Errors | Typed exceptions for known API error codes and a connection exception |
| Webhook security | HMAC-SHA256 verification, timestamp tolerance, constant-time comparison |
| Tests | Stub-transport unit tests for current resources and webhook signatures |
| Automation | CI and Maven Central release workflows |

The reference project's README and changelog describe a completed `0.1.0` release. That history does not create compatibility requirements for `sendbyte-4j`.

### Reference ideas worth evaluating

- A small, discoverable root client.
- A single request executor used by product resources.
- A transport contract that makes unit testing possible without network calls.
- Defensive mapping of unknown enum values where implemented.
- Typed request builders and typed response models.
- Async methods that use the transport's native asynchronous execution.
- Request ID and `Retry-After` extraction on API failures.
- Webhook verification against raw bytes.

### Reference limitations not to reproduce

- Its build is monolithic; consumers always receive all resources and runtime dependencies.
- Public types under `africa.sendbyte.http` are documented as internal but remain part of the exported API.
- `Json.mapper()` exposes Jackson's `ObjectMapper`, and `SendEmailRequest` carries a public Jackson annotation.
- Response models are mutable and expose mutable collections.
- The request pipeline has no retries, total deadline, per-request options, diagnostics, or observers.
- Canceling an async future does not cancel the underlying OkHttp call.
- Serialization failures surface as generic `IllegalStateException`.
- Only integer-seconds `Retry-After` is parsed; HTTP-date is not handled.
- Path segments are concatenated without an explicit path-encoding policy.
- Client options do not comprehensively validate URLs, durations, or ownership of injected resources.
- The custom transport constructor does not carry a clear ownership/close contract.
- Tests are primarily unit tests over canned responses; no mock-server, sandbox, compatibility, concurrency, or native-image suites exist.
- No Spring Boot or Quarkus integration exists.
- No SMS or WhatsApp implementation exists in the reference repository.
- The release workflow exists, but release reproducibility and actual publication have not been proven by repository evidence.

## 3. Design principles

### Framework-neutral core

Core and product modules must not depend on Spring, Quarkus, Reactor, or Mutiny. Framework integrations adapt the same client and request pipeline.

### No third-party types in the primary API

Normal application-facing APIs should expose JDK types and SendByte-owned types, not Jackson, Gson, OkHttp, Apache HTTP Client, Reactor, Mutiny, Spring, or Quarkus types. Optional adapter APIs may expose the library they explicitly integrate with.

### Modular by capability

Applications should install only the products they use. An aggregate artifact remains available for applications that want every stable product.

### One execution pipeline

Authentication, serialization, transport, retries, idempotency, errors, diagnostics, deadlines, and sanitization must be implemented once. Blocking, async, and optional reactive adapters must delegate to that pipeline.

### Safe defaults and explicit policy

Behavior must be documented for authentication, timeouts, retries, idempotency, cancellation, API versions, sensitive data, resource ownership, and thread safety.

### Stateless diagnostics

The SDK reports immutable facts about each request. It does not retain request history, calculate application-wide summaries, or act as a monitoring database.

### Compatibility before novelty

Public API compatibility, wire compatibility, Java baseline, and framework support ranges must be explicit and tested. New API surface is added only when backed by an authoritative server contract.

## 4. Target module architecture

The initial artifact names and `io.github.sendbyte-4j.sendbyte` group are approved for implementation. Release versioning and final publication metadata remain release gates.

```text
sendbyte-4j (Gradle root)
├── sendbyte-java-core
├── sendbyte-java-email
├── sendbyte-java-marketing
├── sendbyte-java-webhooks
├── sendbyte-java-admin
├── sendbyte-java-all
├── sendbyte-java-bom
├── sendbyte-spring-boot-starter
├── sendbyte-spring-boot-starter-all  (optional convenience artifact)
├── sendbyte-quarkus-runtime
├── sendbyte-quarkus-deployment
└── sendbyte-quarkus                  (user-facing extension artifact)

Future, not part of the initial module graph:
├── sendbyte-java-sms                 (no API contract yet)
└── sendbyte-java-whatsapp            (no API contract yet)
```

The initial ownership proposal follows the official OpenAPI 1.0.0 contract: email owns emails, domains, templates, and suppressions; marketing owns brands, subscriber lists, subscribers, and imports; admin owns API keys; webhooks owns endpoint and delivery management. This can change through an ADR if the server's product taxonomy changes.

### Core

The core owns:

- Client construction and immutable configuration.
- Credentials providers and authentication application.
- SendByte-owned transport and serialization contracts.
- Default transport and serialization implementations, either internal or isolated in implementation modules.
- Request execution, deadlines, retry policy, idempotency, cancellation, and resource ownership.
- Common response metadata, diagnostics, observer contracts, and error hierarchy.
- Shared pagination and request-option types.
- Test-support contracts where they are stable enough for consumers.

It contains no product endpoints or framework types.

### Product modules

Each product module owns its services, requests, responses, validation, event payload types, tests, examples, and product documentation. A product module depends on core and not on another product unless the API taxonomy proves that dependency is necessary.

### Aggregate and BOM

`sendbyte-java-all` contains little or no runtime code and aggregates every stable product module. The BOM manages compatible versions for selective installation. Experimental products must not silently enter the stable aggregate.

### Framework integrations

Spring Boot and Quarkus modules own configuration mapping, dependency injection, lifecycle, conditional activation, test support, and optional framework-native telemetry/health adapters. They do not reimplement SendByte operations.

## 5. Client and public API

The common path should remain compact:

```java
SendByteClient client = SendByteClient.builder()
    .apiKey(System.getenv("SENDBYTE_API_KEY"))
    .baseUri(URI.create(System.getenv("SENDBYTE_BASE_URI")))
    .build();
```

Advanced construction should use SendByte-owned contracts:

```java
SendByteClient client = SendByteClient.builder()
    .credentialsProvider(credentialsProvider)
    .baseUri(baseUri)
    .httpTransport(httpTransport)
    .serialization(serialization)
    .retryPolicy(retryPolicy)
    .requestObserver(observer)
    .build();
```

The client must be immutable after construction, thread-safe, reusable, free of global mutable state, and explicit about whether it owns and closes injected components.

Product APIs remain resource-oriented, using names confirmed by the API contract. Every operation should provide typed inputs and outputs, consistent validation, structured errors, request options where useful, and documented sync/async behavior.

Models should be immutable where practical. Deserialization must tolerate unknown response fields and preserve forward compatibility for evolving enum values.

Public models are not serialized or deserialized directly. Product modules translate them to package-private API DTOs whose property names are mapped explicitly to the reviewed OpenAPI contract. A global JSON naming strategy is not used because it can silently rewrite exceptional or newly introduced field names. This boundary allows the public Java API and the platform wire contract to evolve independently.

## 6. Transport, serialization, and dependencies

The SDK must work immediately with built-in defaults. "Self-contained" does not mean one shaded JAR containing multiple frameworks and HTTP stacks.

The transport contract must support:

- Synchronous and asynchronous execution through the same semantics.
- Cancellation propagation.
- Request and total-operation deadlines.
- Multi-value, case-insensitive response headers.
- Streaming or bounded buffering decisions for bodies and attachments.
- Clear ownership and shutdown behavior.
- Test doubles without third-party dependencies.

The serialization contract must support consistent SDK behavior, unknown fields, forward-compatible enums, useful typed failures, native-image constraints, and controlled customization. Jackson may remain an internal default, but it must not leak through the primary public API.

Shading is not a default. Any shaded dependency requires a documented decision covering security updates, vulnerability scanning, artifact size, debugging, JPMS, and native-image behavior.

## 7. Request execution semantics

### Timeouts and deadlines

Connect, read, and write timeouts must be validated. A total operation deadline spans every attempt and retry delay. Request-level overrides may shorten or otherwise override client defaults according to a documented rule.

### Retries and idempotency

Retries are conservative and policy-driven. Retry candidates may include selected connection failures, timeouts, HTTP 429, and selected 5xx responses, subject to the API contract and operation safety.

The implementation must:

- Respect both forms of `Retry-After` supported by HTTP.
- Use capped exponential backoff with jitter.
- Honor the remaining total deadline.
- Avoid retrying unsafe operations without idempotency protection.
- Surface attempt count and accumulated retry delay.
- Document interaction with application, framework, proxy, and service-mesh retries.

### Cancellation

Canceling an asynchronous SDK operation must attempt to cancel its underlying transport work and scheduled retries. Cancellation behavior before dispatch, in flight, during backoff, and after completion must be tested.

### Credentials

A fixed key is the default. A credentials-provider abstraction may support rotation and secret managers. Request-scoped credentials or account context are added only if confirmed by the authentication and tenancy model. Credentials never appear in diagnostics, exceptions, or logs.

## 8. Responses, errors, and diagnostics

Normal methods return convenient typed values. An explicit detailed-response form should expose response metadata and request diagnostics without forcing that complexity on all callers.

Portable diagnostics include, when available:

- Stable operation name.
- Request ID.
- Final status code or failure category.
- Total duration.
- Attempt count and retry delay.
- Rate-limit metadata.
- Sanitized byte counts.

Transport phase timings such as DNS, TCP, TLS, and time-to-first-byte are optional and must not be presented as portable guarantees.

Failures preserve the same diagnostic context. The error hierarchy must distinguish API, authentication/authorization, validation, conflict, not-found, rate-limit, connection, timeout, serialization, cancellation, and unknown failures as supported by the real API.

Observers receive immutable, sanitized, low-cardinality facts after an attempt or operation. Observer failures must never turn a successful API call into a failed application call. The SDK stores no request history.

Diagnostics and errors must not contain API keys, authorization headers, bodies, message content, recipient addresses, phone numbers, customer identifiers, or unfiltered payloads/URLs.

## 9. Webhooks

Webhook signature verification is a cross-product capability and must remain usable without installing every product module. The module should cover, as supported by the server contract:

- Raw-body signature verification and constant-time comparison.
- Timestamp tolerance and replay-window checks.
- Signing-secret rotation.
- Typed event envelopes and safe unknown-event handling.
- Schema-version compatibility.
- Product payload adapters without hard coupling all products together.
- Deterministic fixture generation for application tests.

Endpoint management and webhook verification may share an artifact only if that remains coherent and lightweight after modularization.

## 10. Spring Boot integration

The starter should provide type-safe properties, auto-configuration, a configured client bean, user-bean backoff, customizers, lifecycle handling, conditional product activation, and framework test support. Micrometer, OpenTelemetry, and Actuator integration must be optional and classpath-conditional.

Supported Spring Boot generations and the Java baseline must be published as a compatibility matrix. Configuration metadata and context-runner tests are required.

## 11. Quarkus integration

The extension should provide CDI injection, Quarkus configuration mapping, lifecycle handling, conditional product activation, test overrides, and optional health/telemetry integrations. Runtime and deployment concerns must be separated according to Quarkus extension conventions.

Native-image support is a design input, not a late patch. Reflection must be avoided where practical; necessary metadata must be tested by an actual native build for each claimed support line.

Mutiny types, if offered, stay in the Quarkus adapter and delegate to the shared asynchronous request pipeline.

## 12. Testing and release quality

Testing must cover typed results and wire behavior: method, path and query encoding, headers, body shape, absent versus null fields, error decoding, retries, idempotency, deadlines, cancellation, concurrency, resource ownership, and sensitive-data sanitization.

The quality stack should include:

- Fast unit tests with deterministic clock, sleeper/scheduler, randomness, and fake transport.
- Mock HTTP server integration tests for the default transport.
- Sandbox contract tests gated by credentials.
- Binary/source compatibility checks for public releases.
- Dependency convergence and vulnerability review.
- JDK and framework compatibility matrices.
- Spring application-context tests.
- Quarkus JVM and native tests.
- Compiled documentation examples.
- Reproducible signed release validation.

## 13. Decisions recorded

1. `SendByte-Java-main` is reference material only; `sendbyte-4j` defines a new SDK contract.
2. The core remains framework-neutral.
3. Product capabilities become independently installable.
4. A stable aggregate artifact and BOM provide convenient installation.
5. Spring Boot and Quarkus are first-class integrations.
6. Native-image constraints influence core design early.
7. Third-party implementation types do not belong in the primary public API.
8. One request pipeline owns sync, async, and adapter behavior.
9. Diagnostics are per-request, sanitized, and stateless.
10. An in-memory insights store and monitoring dashboard are out of scope.
11. Default transport and serialization implementations are included.
12. Mutable response models and accidental internal public APIs from the reference project do not define the new contract.
13. The initial stable scope is email, marketing, webhooks, and API-key administration from the official OpenAPI 1.0.0 contract. SMS and WhatsApp are excluded until authoritative operations exist.
14. Compatibility with the reference `0.1.0` API is not a goal; familiar naming may still be retained where it improves usability.
15. Core and product artifacts target Java 11; Spring Boot and Quarkus integrations target Java 17.
16. Gradle Kotlin DSL, JDK `HttpClient`, internally isolated Jackson, and `CompletableFuture` are the initial platform choices.
17. Credentials providers are supported, retries default to conservative behavior, and native-image compatibility will be validated rather than assumed.
18. Initial implementation coordinates use the `io.github.sendbyte-4j.sendbyte` group and the module names in section 4.

## 14. Decisions still required

These questions still require product ownership, operational evidence, or release policy:

- Production parity and change-management policy for the canonical OpenAPI specification.
- Release train, deprecation window, and semantic-versioning policy.
- Supported Spring Boot and Quarkus generations.
- Exact retry-safe operation classification as the endpoint surface is implemented.
- Confirmed multi-tenant/account-context requirements beyond rotating credentials.

Until resolved, these are task gates—not invitations to guess.

## 15. Delivery sequence

The work should proceed in dependency order:

1. Freeze the API contract, compatibility policy, and release scope.
2. Establish a trustworthy build and repository baseline.
3. Prove the target architecture with one email-send vertical slice.
4. Implement and harden the remaining stable resources, adapting useful reference code deliberately.
5. Complete webhook functionality and test support.
6. Add diagnostics and optional telemetry adapters.
7. Add the contract-approved marketing module; add future products only when their contracts become available.
8. Add Spring Boot and Quarkus integrations.
9. Complete compatibility, security, documentation, and release gates.

The detailed deliverables and acceptance criteria are maintained in [GitHub Issues](https://github.com/sendbyte-4j/sendbyte-4j/issues).
