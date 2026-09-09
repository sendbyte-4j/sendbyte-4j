# 05 — Marketing and Future Product Modules

## Goal

Implement the contract-backed marketing module and define a safe entry gate for SMS, WhatsApp, and later products.

## Deliverables

### D05.1 Marketing ownership and models

The OpenAPI 1.0.0 scope includes brands, subscriber lists, subscribers, and bulk imports. Campaign operations described in prose documentation are not yet in the canonical contract.

Acceptance criteria:

- [ ] `sendbyte-java-marketing` owns all seven contracted marketing operations.
- [ ] Brand, list, subscriber, import request, result, quota, and pagination models match the contract.
- [ ] Models are immutable, collections are defensively copied, and unknown additive fields remain compatible.
- [ ] Campaigns remain excluded until a canonical contract or approved deviation defines them.
- [ ] No email, admin, webhook, Spring, or Quarkus implementation is pulled into a marketing-only consumer.

### D05.2 Marketing operations

Acceptance criteria:

- [ ] List/create brand operations have exact method, path, body, response, and error tests.
- [ ] List/create subscriber-list operations safely encode `brandId` and implement contract pagination.
- [ ] List/add subscriber operations safely encode brand/list identifiers and validate required data.
- [ ] CSV subscriber import follows the contract's multipart/media/size behavior without buffering beyond documented limits.
- [ ] Every operation uses the core pipeline for auth, serialization, deadlines, cancellation, errors, and diagnostics.
- [ ] Sync/async behavior is consistent for every applicable operation.

### D05.3 Marketing contract and packaging proof

Acceptance criteria:

- [ ] All seven operations have success, validation, error, and wire-contract fixtures.
- [ ] At least one read and one write operation pass against the sandbox or an approved integration environment.
- [ ] Test data contains no real subscriber/customer information.
- [ ] A marketing-only Maven and Gradle consumer resolves no unrelated products/frameworks.
- [ ] Installation, quickstart, errors, diagnostics, testing, and framework examples compile.
- [ ] The stable module is included in the BOM and aggregate artifact.

### D05.4 Future product entry gate

SMS, WhatsApp, and any later channel remain outside the initial build. A new module may begin only when product/API owners publish or approve its operations, schemas, auth, limits, idempotency, errors, sandbox behavior, and stability.

Acceptance criteria for each future product:

- [ ] The canonical contract includes its operations or an approved versioned contract is committed.
- [ ] Every operation/resource has one owning module and no arbitrary cross-product dependency.
- [ ] Stable versus preview surface is visible in artifacts, documentation, and versioning.
- [ ] Requests/responses are typed and immutable, with exact wire and negative tests.
- [ ] A sandbox vertical slice passes before the module is labeled stable.
- [ ] Stable modules enter the BOM/aggregate; preview modules follow an explicit opt-in policy.
