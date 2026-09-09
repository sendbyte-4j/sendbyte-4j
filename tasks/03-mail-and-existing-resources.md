# 03 — Mail and Existing Resources

## Goal

Implement emails, domains, templates, suppressions, and API keys in their approved modules, adapting useful reference code only where it matches the current contract.

## Deliverables

### D03.1 Email operations

- Send, retrieve, and list operations with all contract-approved fields.
- Immutable requests/responses, pagination, scheduling, templates, attachments, tags, headers, and idempotency.

Acceptance criteria:

- [ ] Every email contract operation has sync/async parity and wire tests.
- [ ] Required fields, recipient/attachment limits, mutually exclusive fields, formats, and maximum sizes match the server contract.
- [ ] Collections are defensively copied and cannot mutate built requests/responses.
- [ ] Cursor pagination handles empty pages, final pages, filters, and encoded cursors.
- [ ] Attachment memory/size behavior is documented and tested.
- [ ] One email-send vertical slice passes against the sandbox, including one typed failure.

### D03.2 Domains

Acceptance criteria:

- [ ] Create/get/list/verify behavior and DNS record models match the contract.
- [ ] Unknown statuses and record types remain readable.
- [ ] Domain IDs are safely path-encoded and blank IDs fail locally.
- [ ] Verification lifecycle examples and fixtures are contract-tested.

### D03.3 Templates

Acceptance criteria:

- [ ] Create/get/list/update/delete/render/preview match approved methods, paths, and schemas.
- [ ] HTML, text, MJML, subject, and variable validation matches the server.
- [ ] Null-versus-absent update semantics are explicit and tested.
- [ ] Saved and inline rendering examples compile and pass contract fixtures.

### D03.4 API keys

Acceptance criteria:

- [ ] Create/list/revoke operations and scopes/modes match the auth model.
- [ ] Secret key material is available only where the server returns it and is redacted everywhere else.
- [ ] Self-revocation and permission behavior are documented from the contract, not assumed.
- [ ] Unknown future scopes/modes have an explicit forward-compatibility behavior.

### D03.5 Contract coverage and documentation

Acceptance criteria:

- [ ] Every in-scope OpenAPI operation has a contract test and reference-only gaps are not mistaken for product scope.
- [ ] All public types and methods have JavaDoc with nullability, thread-safety, failure, and async behavior where relevant.
- [ ] Product examples are compiled in CI.
- [ ] Product dependency tree contains only core and approved runtime dependencies.
