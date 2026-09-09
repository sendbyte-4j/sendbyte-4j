# 04 — Webhooks

## Goal

Provide secure webhook verification, contract-backed endpoint management, and application test support without forcing installation of unrelated products.

## Deliverables

### D04.1 Signature verification hardening

Acceptance criteria:

- [ ] Verification uses the exact raw request bytes and the contract-defined header grammar.
- [ ] Timestamp tolerance, future timestamps, malformed values, multiple signatures, and secret rotation are covered.
- [ ] MAC comparison is constant-time for comparable values.
- [ ] Empty/null/oversized inputs have explicit behavior.
- [ ] Official cross-language test vectors, if available, pass.
- [ ] Verification can be used with only core/webhook dependencies.

### D04.2 Event envelopes and payloads

Acceptance criteria:

- [ ] Envelope fields, event IDs, timestamps, API/schema versions, and account context match the contract.
- [ ] Unknown event types and newer payload fields remain safely accessible or ignorable.
- [ ] Product payload adapters do not require every product module.
- [ ] Event models do not expose serializer-specific types in the primary API.

### D04.3 Endpoint and delivery management

Acceptance criteria:

- [ ] Create/list/update-or-disable/delivery-list/replay operations match the approved contract.
- [ ] Pagination and event-subscription validation are complete.
- [ ] One-time signing secrets are redacted from logs, exceptions, and object string forms.
- [ ] Replay and other unsafe operations follow the idempotency/retry policy.

### D04.4 Consumer test support

Acceptance criteria:

- [ ] Deterministic helpers generate signed webhook fixtures with caller-controlled time/body/event.
- [ ] Helpers can generate invalid/expired fixtures for negative tests without real credentials.
- [ ] Servlet/Spring and Quarkus raw-body examples compile in their relevant test suites.
- [ ] Security guidance requires verification before parsing or acting on an event.
