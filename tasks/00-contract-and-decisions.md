# 00 — Contract and Architecture Decisions

## Goal

Replace assumptions with an authoritative server contract, product scope, and initial public-API policy before implementing artifacts.

## Deliverables

### D00.1 Canonical API contract

- Version the authoritative OpenAPI description published by SendByte.
- Reconcile it with production behavior and the documented sandbox.
- Capture auth, base URLs, API versioning, pagination, idempotency, error envelopes, request IDs, rate-limit headers, webhook signatures, limits, and timestamp formats.

Acceptance criteria:

- [x] The official OpenAPI 1.0.0 snapshot is versioned under `spec/` with its source and checksum recorded.
- [x] Every reference-project operation maps to the contract, and missing contract operations are inventoried.
- [ ] At least one sanitized request/response fixture exists for every success and documented error shape.
- [ ] Known production deviations are listed with owner and disposition.
- [x] A scheduled/manual CI check reports upstream contract drift without overwriting the reviewed snapshot.

### D00.2 Product and release scope

- Define which resources belong to email, marketing, webhooks, shared administration, SMS, or WhatsApp.
- Classify every product/module as stable, preview, experimental, or out of scope for the next release.

Acceptance criteria:

- [x] Every current contract operation has exactly one proposed owning module.
- [x] Initial stable aggregate contents are explicit.
- [x] Uncontracted products are excluded from the initial artifacts.
- [x] SMS and WhatsApp remain future work because the official API contract has no operations for them.

### D00.3 Reference implementation assessment

- Inventory useful patterns and limitations in `SendByte-Java-main`.
- Compare its supported operations and wire assumptions with the official contract.
- Record what may be adapted, redesigned, or omitted. No source/binary compatibility is required.

Acceptance criteria:

- [x] Reference capabilities and contract gaps are documented.
- [x] Reference limitations that must not be copied are documented.
- [x] The new project is explicitly free to define its own API and package layout.
- [ ] Initial Maven coordinates and semantic-versioning policy are approved.

### D00.4 Platform support ADRs

Record decisions for minimum Java, tested JDKs, Gradle structure, Spring Boot lines, Quarkus lines, native-image scope, async return type, default transport/serializer placement, retry defaults, credential rotation, and multi-tenancy.

Acceptance criteria:

- [ ] Each decision has context, choice, alternatives, consequences, and owner/date.
- [ ] The selected Java baseline can support every target module.
- [ ] CI matrices exactly match the advertised support matrix.
- [ ] No downstream work package remains blocked by an unowned architecture choice.
