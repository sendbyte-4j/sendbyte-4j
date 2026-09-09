# 09 — Documentation and Release

Deferred external publication prerequisites are recorded in [`../docs/PUBLISHING_REQUIREMENTS.md`](../docs/PUBLISHING_REQUIREMENTS.md).

## Goal

Ship discoverable, accurate documentation and a signed release that clean applications can consume.

## Deliverables

### D09.1 Documentation set

- Root overview and module selector.
- Per-product installation and quickstarts.
- Standalone, Spring Boot, and Quarkus guides.
- Configuration reference.
- Error, retry/idempotency, timeout/deadline, cancellation, diagnostics, security, webhook, testing, and troubleshooting guides.

Acceptance criteria:

- [ ] The first successful sandbox request requires only documented steps and no repository knowledge.
- [ ] Maven and Gradle snippets use released coordinates and supported versions.
- [ ] All Java examples compile; critical quickstarts execute against mock fixtures in CI.
- [ ] Defaults, thread safety, ownership/close behavior, retry multiplication, and sensitive-data rules are explicit.
- [ ] Each product's limitations and stability are visible from its landing page.
- [ ] README claims are backed by tests or qualified accurately.

### D09.2 API reference and adoption guide

Acceptance criteria:

- [ ] Public APIs have complete JavaDoc with nullability, exceptions, thread safety, and since/deprecation tags where applicable.
- [ ] Adoption guide explains module selection, client construction, sync/async use, configuration, and lifecycle.
- [ ] Copy/paste examples compile.
- [ ] Changelog follows the approved version policy and links breaking changes to upgrade instructions after the first stable release.

### D09.3 Release engineering

- Automated versioning, signing, staging/publishing, provenance/checksums where supported, and GitHub release creation.

Acceptance criteria:

- [ ] Release workflow builds from a tag/commit exactly once and publishes the tested artifacts.
- [ ] Maven Central metadata, sources, JavaDoc, signatures, licenses, SCM, and developer information validate.
- [ ] A staging/dry-run release completes without manual artifact modification.
- [ ] Clean Maven and Gradle consumers resolve and run selective, aggregate, Spring, and Quarkus samples from staging.
- [ ] Failed/partial releases have a documented recovery procedure.
- [ ] `RELEASING.md` documents credentials, key rotation, rollback, and post-release verification without embedding secrets.

### D09.4 Operational handoff

Acceptance criteria:

- [ ] Ownership exists for releases, dependency/security updates, API contract drift, and user support.
- [ ] Issue templates request SDK/API versions and sanitized request IDs without secrets or payloads.
- [ ] Supported versions, deprecation timelines, and vulnerability contact are public.
- [ ] Post-release smoke checks verify Maven Central resolution, documentation links, badges, and a sandbox request.
- [ ] Deferred and experimental work remains tracked with explicit non-goals for the released version.
