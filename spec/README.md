# API Contract Snapshot

`openapi.yaml` is a vendored snapshot of SendByte's canonical API contract.

- Source: production contract URL pending
- OpenAPI: 3.1.0
- API document version: 1.0.0
- Retrieved: 2026-09-09
- SHA-256: `de37a5be7fee93eebbec0955f5f3ceff7aed856ab2f3da61fd1dadc3c28815c9`

Do not edit the snapshot by hand. When the upstream contract changes:

1. Download it to a temporary location.
2. Review operation, schema, and compatibility changes.
3. Update the snapshot, checksum, contract audit, fixtures, and implementation together.
4. Record breaking or ambiguous changes in an ADR before merging.

The normal `check` lifecycle validates the local checksum. Run `./gradlew checkOpenApiDrift` to compare it with the published contract; a scheduled/manual CI workflow runs the same check without automatically replacing the reviewed snapshot.
