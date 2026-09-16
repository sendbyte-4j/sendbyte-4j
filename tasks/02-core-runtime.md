# 02 — Core Runtime

## Goal

Deliver one framework-neutral, deterministic, production-safe execution pipeline used by every product and integration.

## Deliverables

### D02.1 Client, configuration, and credentials

- Immutable client builder and validated configuration.
- Fixed credentials plus approved provider/rotation behavior.
- Explicit ownership rules for transports, executors, schedulers, and other injected components.

Acceptance criteria:

- [x] Null/blank keys, malformed base URLs, and invalid durations fail before any request.
- [x] Contradictory client options fail before any request as those option combinations are introduced.
- [x] Client and configuration objects are thread-safe and have no global mutable state.
- [x] Closing the client is idempotent and closes only resources it owns.
- [x] Concurrent clients cannot leak credentials or configuration into one another.
- [x] Secrets are redacted from `toString`, exceptions, diagnostics, and logs.

### D02.2 Transport and serialization contracts

- SendByte-owned contracts with built-in defaults.
- Correct URL/path/query/header/body handling.
- Typed serialization and connection failures.

Acceptance criteria:

- [x] Primary public APIs expose no OkHttp or Jackson type.
- [x] Unknown response fields and future enum values do not break deserialization in the implemented email slice.
- [x] Path segments and queries follow the approved encoding rules, including Unicode and reserved-character tests.
- [x] Header handling is case-insensitive and supports required multi-value semantics.
- [x] Default transport tests use a real local mock server for sync and async requests.
- [x] Response-body buffering/size and attachment behavior are documented and tested.

### D02.3 Request options, deadlines, retries, and idempotency

- Client defaults and request-level overrides.
- Total deadline across attempts.
- Conservative retry policy with deterministic test seams.

Acceptance criteria:

- [x] Retryable conditions and safe operations match the approved contract.
- [x] Backoff is capped exponential with testable jitter and respects integer/date `Retry-After`.
- [x] Unsafe operations are never retried without approved idempotency protection.
- [x] A total deadline stops in-flight work and retry delays.
- [x] Attempt count and accumulated delay are exact in tests.
- [x] Tests cover application retry multiplication guidance.

### D02.4 Async and cancellation

Acceptance criteria:

- [x] Sync and async paths produce equivalent values, errors, retry behavior, and diagnostics.
- [x] Canceling the returned async handle cancels the underlying transport call in the implemented pipeline.
- [x] Cancellation before dispatch, during I/O, during backoff, and after completion is deterministic in tests.
- [x] User continuations never execute on an undocumented internal thread by surprise.

### D02.5 Errors, responses, and diagnostics

- Stable error hierarchy, detailed response wrapper, immutable request diagnostics, and observers.

Acceptance criteria:

- [x] Every documented API error maps to a typed or explicitly generic SDK exception.
- [x] Connection, timeout, serialization, cancellation, and observer failures are distinguishable.
- [x] Failures preserve request ID, safe error fields, status, retryability, and diagnostics.
- [x] Detailed success responses expose approved headers/metadata without raw third-party types.
- [x] Observer failures are contained and cannot fail a successful API operation.
- [x] Diagnostics use stable low-cardinality operation names and retain no request history.
- [x] Sanitization tests cover keys, auth headers, bodies, recipient data, phone numbers, identifiers, and URLs.

### D02.6 Core test kit

Acceptance criteria:

- [x] Fake transport, clock, scheduler/sleeper, and randomness allow tests without sleeps or network access.
- [x] Request capture asserts method, URL, headers, and serialized body for the email-send slice.
- [x] Test-support API stability is either guaranteed or clearly marked internal.
- [x] Concurrency and resource-leak tests run reliably in CI.
