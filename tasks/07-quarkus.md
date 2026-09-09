# 07 — Quarkus Integration

## Goal

Provide idiomatic Quarkus JVM and native-image integration over the same core client and execution pipeline.

## Deliverables

### D07.1 Extension structure and configuration

- Runtime/deployment modules, user-facing extension metadata, config mapping, CDI producers, and lifecycle handling.

Acceptance criteria:

- [ ] A minimal application injects a configured client/resource using standard CDI.
- [ ] Build-time and runtime configuration are separated correctly.
- [ ] Missing/invalid configuration fails with actionable, redacted messages.
- [ ] Product beans activate only for present modules/configuration.
- [ ] User-provided beans and test overrides have documented precedence.
- [ ] Shutdown closes only extension-owned resources.

### D07.2 Native-image readiness

Acceptance criteria:

- [ ] Reflection/resource/proxy requirements are eliminated or registered explicitly.
- [ ] Serialization works in native mode for every stable model.
- [ ] A real native executable performs a mock-server request in CI for each claimed support line.
- [ ] Native failures are not hidden behind JVM-only tests.
- [ ] Native artifact size/startup claims, if published, are reproducibly measured.

### D07.3 Optional Quarkus integrations

Acceptance criteria:

- [ ] Health, Micrometer, OpenTelemetry, and Mutiny support is independently optional.
- [ ] Any Mutiny API adapts the shared async execution and preserves cancellation/errors/diagnostics.
- [ ] Telemetry is sanitized and low-cardinality, and failures are isolated.
- [ ] No Quarkus or Mutiny type leaks into core/product public APIs.

### D07.4 Compatibility and test experience

Acceptance criteria:

- [ ] JVM and native tests pass for every advertised Quarkus/Java combination.
- [ ] `@QuarkusTest` consumers can override credentials and transport/base URL without network calls.
- [ ] Dev mode and test mode do not leak threads/resources across reloads.
- [ ] A clean Maven/Gradle sample consumes the published extension coordinates.
