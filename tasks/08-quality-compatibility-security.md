# 08 — Quality, Compatibility, and Security

## Goal

Provide repeatable evidence for reliability, compatibility, performance, and privacy claims. This work runs continuously and gates release.

## Deliverables

### D08.1 Test pyramid and CI

Acceptance criteria:

- [ ] Unit tests are deterministic and do not use real sleep or uncontrolled network access.
- [ ] Default-transport integration tests run against a local mock HTTP server.
- [ ] Credential-gated sandbox tests are isolated, retry-safe, and never run destructive operations unexpectedly.
- [ ] CI runs all advertised JDK/framework matrices with no silent exclusions.
- [ ] Flaky tests have an owner and are fixed or quarantined with an expiry; they are not blindly retried.
- [ ] Coverage thresholds protect core error/retry/cancellation/security paths.

### D08.2 Compatibility enforcement

Acceptance criteria:

- [ ] Binary/source API checks compare against the latest applicable stable release.
- [ ] Wire fixtures detect serialization and endpoint regressions.
- [ ] Unknown fields/enums and additive API evolution have dedicated tests.
- [ ] Deprecation/removal policy is enforced during release review.
- [ ] JPMS/automatic-module-name behavior is decided and tested if claimed.

### D08.3 Security and privacy

Acceptance criteria:

- [ ] Dependency vulnerability and license checks run in CI with a documented triage process.
- [ ] Secret scanning covers source, fixtures, logs, and published source artifacts.
- [ ] Redaction tests cover auth, API keys, webhook secrets, content, recipients, phone numbers, customer IDs, and query strings.
- [ ] Webhook cryptography and replay handling receive focused review.
- [ ] Redirect behavior cannot forward authorization to an untrusted host.
- [ ] TLS/proxy/custom-endpoint behavior and trust boundaries are documented.
- [ ] A vulnerability reporting policy and supported-version policy exist.

### D08.4 Concurrency, lifecycle, and performance

Acceptance criteria:

- [ ] Concurrent use tests cover all shared components and credential rotation if supported.
- [ ] Cancellation and close operations do not leak calls, threads, connections, or scheduled work.
- [ ] Load tests establish defensible overhead and memory baselines for sync/async sends.
- [ ] Large responses and attachments obey documented memory/size bounds.
- [ ] Performance regressions have measured thresholds rather than subjective review.

### D08.5 Release-candidate audit

Acceptance criteria:

- [ ] Published POMs have correct dependency scopes and no unintended optional/framework dependencies.
- [ ] JARs contain no secrets, test fixtures with personal data, duplicate classes, or unintended build metadata.
- [ ] Sources/JavaDoc/signatures/checksums are present for every required artifact.
- [ ] A clean-room consumer test resolves artifacts from staging rather than the reactor/local classes.
