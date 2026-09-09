# 06 — Spring Boot Integration

## Goal

Provide a native Spring Boot developer experience while keeping the SDK core independent and product-selective.

## Deliverables

### D06.1 Starter and configuration

- Type-safe properties for credentials, endpoint, timeouts/deadline, retries, diagnostics, and approved product settings.
- Auto-configuration for the core client and present product modules.

Acceptance criteria:

- [ ] A minimal application creates a usable client from `sendbyte.api-key`.
- [ ] Missing/invalid required configuration fails with actionable, redacted messages.
- [ ] Auto-configuration backs off for user-provided client and supported component beans.
- [ ] Product beans activate only when their modules/configuration are present.
- [ ] Configuration metadata is generated and verified.
- [ ] Shutdown closes owned resources exactly once.

### D06.2 Customization and test support

Acceptance criteria:

- [ ] Ordered customizers cover approved advanced options without requiring users to rebuild the client.
- [ ] Context-runner tests cover defaults, overrides, backoff, conditional classes, and invalid properties.
- [ ] Test overrides support fake transport/base URL without reaching the network.
- [ ] No Spring type leaks into core or product modules.

### D06.3 Optional diagnostics integrations

Acceptance criteria:

- [ ] Logging, Micrometer, OpenTelemetry, and Actuator behavior is individually classpath-conditional.
- [ ] Installing the starter alone does not force optional telemetry/Actuator libraries.
- [ ] Metric/span names and tags are stable, sanitized, and low-cardinality.
- [ ] Telemetry failures do not change request results.
- [ ] Health behavior avoids billable/destructive API operations and documents what is actually measured.

### D06.4 Compatibility matrix

Acceptance criteria:

- [ ] Every advertised Spring Boot/Java combination builds and passes application-context tests in CI.
- [ ] Maven and Gradle sample applications package and start for each supported line.
- [ ] Unsupported combinations fail clearly or are documented.
- [ ] The optional all-products starter, if approved, contains no duplicate runtime pipeline.
