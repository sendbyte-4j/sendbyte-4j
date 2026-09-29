# Reference Reuse Audit

## Scope

This audit covers the implementation present through Task 01. The reference project is
`../SendByte-Java-main`, licensed under MIT with copyright held by Hilary Emujede. The
reviewed wire contract is `spec/openapi.yaml`.

## Findings

- The reference client, transport, error types, email models, and examples were compared
  with the current OpenAPI contract and modular architecture.
- The new implementation adopts only general SDK concepts: a root client, resource
  services, a replaceable transport boundary, typed API failures, and immutable models.
- No reference source file or example was copied verbatim or in substantial part. The
  current implementation was written against the OpenAPI contract, so no MIT-derived
  source currently requires an embedded attribution notice.
- The shared `africa.sendbyte` Java package namespace identifies the SendByte product.
  Reference internal package layouts and plural `emails` models were not preserved by default.
- The reference's OkHttp transport, exposed Jackson helper, mutable response shapes, and
  accidental public internals were not copied. Core instead uses the JDK HTTP client and
  keeps JSON implementation details internal.
- No reference example has been admitted as a new-SDK example. An example may be accepted
  only after it compiles against the locally published modules and its payload and response
  behavior are checked against the current contract.

## Future reuse rule

Any later reuse must record the reference path, destination path, contract differences,
and validation performed. Copies or substantial derivatives must retain the reference
project's MIT copyright and license notice.
