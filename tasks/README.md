# SendByte Java SDK Delivery Plan

This directory is the execution backlog for completing the new SDK described in [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md). `../SendByte-Java-main` is reference material only. Tasks are ordered by dependency, not estimated duration.

## Status convention

Each deliverable should use one status in its pull request or tracker: `blocked`, `ready`, `in progress`, or `done`. A task is `done` only when every acceptance criterion is met or an explicitly recorded decision removes that criterion from scope.

## Definition of complete

The SDK is release-complete when:

- The supported SendByte API surface is contract-tested and documented.
- Stable modules can be installed individually and through the aggregate artifact/BOM.
- Core behavior for auth, validation, serialization, transport, errors, retries, idempotency, deadlines, cancellation, diagnostics, and shutdown is specified and tested.
- Every claimed JDK, Spring Boot, Quarkus, and native-image combination passes CI.
- Public APIs have stability checks, JavaDoc, and compiled examples.
- Security/privacy checks show that secrets and message/recipient data do not leak through errors, logs, or diagnostics.
- A signed staging release can be consumed by clean Maven and Gradle sample applications.
- Remaining limitations and experimental products are explicitly documented.

## Dependency map

```text
00 Contract and decisions
├── 01 Build and modularization
│   ├── 02 Core runtime
│   │   ├── 03 Mail and shared resources
│   │   ├── 04 Webhooks
│   │   ├── 06 Spring Boot
│   │   └── 07 Quarkus
│   └── 05 New products (also contract-gated)
├── 08 Quality, compatibility, and security (continuous)
└── 09 Documentation and release (final gate)
```

## Work packages

| ID | Work package | Outcome | Depends on |
| --- | --- | --- | --- |
| 00 | [Contract and architecture decisions](00-contract-and-decisions.md) | Authoritative scope and compatibility rules | None |
| 01 | [Build and modularization](01-build-and-modularization.md) | Publishable module graph, aggregate, and BOM | 00 |
| 02 | [Core runtime](02-core-runtime.md) | Production-safe shared request pipeline | 00, 01 |
| 03 | [Mail and existing resources](03-mail-and-existing-resources.md) | Existing API surface migrated and hardened | 02 |
| 04 | [Webhooks](04-webhooks.md) | Cross-product webhook management and verification | 00, 02 |
| 05 | [Marketing and future products](05-new-product-modules.md) | Contract-backed optional product modules | 00, 02 |
| 06 | [Spring Boot integration](06-spring-boot.md) | First-class starter with optional telemetry | 01, 02 |
| 07 | [Quarkus integration](07-quarkus.md) | First-class JVM/native extension | 01, 02 |
| 08 | [Quality, compatibility, and security](08-quality-compatibility-security.md) | Evidence for all reliability claims | Starts at 00; gates release |
| 09 | [Documentation and release](09-documentation-and-release.md) | Consumable, signed, supportable release | All stable-scope work |

## Milestones

1. **M0 — Scope frozen:** Work package 00 accepted.
2. **M1 — Architecture proven:** One email-send vertical slice passes core wire, error, retry, cancellation, diagnostics, and sandbox tests.
3. **M2 — Initial SDK complete:** Mail/shared resources and webhooks are implemented with contract evidence.
4. **M3 — Framework ready:** Supported Spring Boot and Quarkus matrices pass, including claimed native builds.
5. **M4 — Product complete:** Every contract-approved stable product module passes its contract suite.
6. **M5 — Release candidate:** Quality, docs, artifact-consumer, signing, and staging gates all pass.

## Scope controls

- Do not implement an endpoint from README prose alone; use the approved API contract.
- Do not mark SMS, WhatsApp, native image, telemetry, or a framework version stable until its dedicated criteria pass.
- Do not treat packages, coordinates, dependencies, or public API from `SendByte-Java-main` as requirements. Reuse an idea only after checking it against the current OpenAPI contract and architecture.
- Record architecture decisions in `docs/adr/` as they are made. This task plan may then link to, but should not duplicate, those decisions.
