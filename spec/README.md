# API Contract Snapshot

`openapi.yaml` is a vendored snapshot of SendByte's canonical API contract.

- Source: <https://docs.sendbyte.africa/api-reference/openapi.yaml>
- OpenAPI: 3.1.0
- API document version: 1.0.0
- Retrieved: 2026-09-09
- SHA-256: `01c89ddb9d553a9738b02a4c76162a8435c1f5ae0482a384aba4b68d94155b72`

Do not edit the snapshot by hand. When the upstream contract changes:

1. Download it to a temporary location.
2. Review operation, schema, and compatibility changes.
3. Update the snapshot, checksum, contract audit, fixtures, and implementation together.
4. Record breaking or ambiguous changes in an ADR before merging.

The normal `check` lifecycle validates the local checksum. Run `./gradlew checkOpenApiDrift` to compare it with the published contract; a scheduled/manual CI workflow runs the same check without automatically replacing the reviewed snapshot.
