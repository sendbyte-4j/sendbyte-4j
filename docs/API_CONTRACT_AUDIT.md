# SendByte API Contract Audit

Status: initial audit complete; production fixtures and owner sign-off pending
Audit date: 2026-09-09

## Sources

- Canonical contract: production contract URL pending
- Contract version: OpenAPI 3.1.0, API document version `1.0.0`
- Vendored snapshot: [`../spec/openapi.yaml`](../spec/openapi.yaml)
- Snapshot SHA-256: `de37a5be7fee93eebbec0955f5f3ceff7aed856ab2f3da61fd1dadc3c28815c9`
- Documentation index: production documentation URL pending
- Reference implementation: `../../SendByte-Java-main/`

The vendored snapshot is the build-time design input. Online documentation is useful context, but generated SDK behavior must be derived from the versioned contract plus reviewed deviations.

## Contract summary

The contract contains 41 operations across 27 paths. It defines bearer authentication, placeholder server `https://api.example.test`, a `/v1` path prefix, an error envelope, 24 machine-readable error codes, `x-request-id` on responses, and integer-seconds `Retry-After` for rate limiting.

Current documented channel status is:

- Transactional email: live.
- Marketing email: live.
- SMS: coming soon; absent from OpenAPI 1.0.0.
- WhatsApp: coming soon; absent from OpenAPI 1.0.0.

## Proposed module ownership

| Module | Contract areas | Operations |
| --- | --- | ---: |
| `sendbyte-java-email` | Emails, domains, templates, suppressions | 23 |
| `sendbyte-java-marketing` | Brands, subscriber lists, subscribers, imports | 7 |
| `sendbyte-java-webhooks` | Endpoints, enable/disable, rotation, deliveries, replay | 7 |
| `sendbyte-java-admin` | API-key create/list/rename/revoke | 4 |
| **Total** |  | **41** |

`sendbyte-java-core` owns no endpoint. It owns shared authentication, transport, serialization, request execution, pagination, errors, retries, cancellation, deadlines, and diagnostics.

## Endpoint inventory

### Email — 23 operations

- Emails: `sendEmail`, `listEmails`, `exportEmails`, `getEmail`.
- Domains: `createDomain`, `listDomains`, `getDomain`, `deleteDomain`, `verifyDomain`, `setDomainTracking`.
- Templates: `createTemplate`, `listTemplates`, `getTemplate`, `updateTemplate`, `deleteTemplate`, `renderTemplate`, `previewTemplate`.
- Suppressions: `listSuppressions`, `getSuppression`, `requestSuppressionRemoval`, `listSuppressionRemovals`, `getSuppressionRemoval`, `cancelSuppressionRemoval`.

### Marketing — 7 operations

- Brands: `listBrands`, `createBrand`.
- Subscriber lists: `listSubscriberLists`, `createSubscriberList`.
- Subscribers: `listSubscribers`, `addSubscriber`, `importSubscribers`.

The public documentation also describes campaigns, but OpenAPI 1.0.0 has no campaign path. Campaign operations remain excluded until added to the canonical contract or recorded as an approved deviation.

### Webhooks — 7 operations

- `createWebhook`, `listWebhooks`, `disableWebhook`, `enableWebhook`, `rotateWebhookSecret`, `listWebhookDeliveries`, `replayWebhookDelivery`.

### Administration — 4 operations

- `createApiKey`, `listApiKeys`, `renameApiKey`, `revokeApiKey`.

## Reference implementation assessment

The reference project implements 22 of the 41 operations:

- All three original email JSON operations: send, retrieve, and list.
- Domain create, retrieve, list, and verify.
- All seven template operations.
- API-key create, list, and revoke.
- Webhook create, list, disable, delivery list, and replay.

The reference does not implement 19 current contract operations:

- Email CSV export.
- Domain delete and tracking configuration.
- Marketing brands, lists, subscribers, and imports (7 operations).
- API-key rename.
- Webhook enable and signing-secret rotation.
- Suppressions and removal flows (6 operations).

### Reusable ideas

- Resource-oriented root client.
- Central request executor.
- Transport abstraction and fake transport tests.
- Typed builders and resource models.
- Sync/async method pairing.
- Unknown-enum fallback pattern.
- Typed error subclasses and request-ID propagation.
- Raw-byte webhook signature verification.

### Redesign before reuse

- Do not copy the Maven build into the Gradle project.
- Do not preserve `io.github.dev-hills` coordinates or `africa.sendbyte` packages merely for compatibility.
- Do not expose Jackson/OkHttp implementation types through primary APIs.
- Replace mutable response beans and mutable collection exposure with immutable models.
- Add cancellation propagation, deadlines, retry policy, diagnostics, and explicit resource ownership.
- Expand typed error coverage from the reference's 9 mappings to the contract's 24 codes or an approved categorized model.
- Validate all models and wire behavior against `spec/openapi.yaml`; README examples are not a contract.

## Contract questions and deviations

These items need product/API-owner confirmation or contract updates:

1. Public marketing documentation includes campaigns, but the OpenAPI snapshot contains only brands, lists, subscribers, and imports.
2. The contract declares a default rate limit but does not fully specify all rate-limit response headers.
3. Retry safety must be classified per operation; email send explicitly supports idempotency, while other unsafe operations need confirmation.
4. Webhook verification rules live primarily in prose documentation and should be represented by versioned test vectors.
5. Production parity still needs sandbox fixtures for every response and error shape.
6. API version change policy and OpenAPI compatibility expectations are not defined in the snapshot.
7. Email identifiers and pagination cursors are marked as UUID-formatted in parts of OpenAPI, while official examples use prefixed values such as `em_...`; the Java SDK therefore keeps these identifiers as strings pending clarification.

## Next contract actions

- Add a CI task that downloads the official contract and reports checksum/schema/operation changes without silently overwriting the vendored snapshot.
- Add reviewed, sanitized fixtures per operation and documented error shape.
- Add webhook signing test vectors.
- Obtain explicit product/API-owner sign-off for the snapshot, proposed module ownership, and listed deviations.
