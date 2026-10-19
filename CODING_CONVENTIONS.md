# Java Coding Conventions

Status: accepted

Last updated: 2026-09-09

These conventions apply to handwritten Java production code, tests, examples, and generated-code templates in `sendbyte-4j`. Vendored and generated output is not reformatted manually.

## Immutability and `final`

- Declare classes `final` unless inheritance is an intentional, documented extension point.
- Declare fields `final` whenever their reference or value does not change after construction.
- Declare method and constructor parameters `final` unless reassignment is required.
- Declare local variables `final` when they are assigned once and a descriptive name improves the code.
- Keep builder fields and other deliberately changing state non-final; make their produced value immutable.
- Do not force `final` onto serializer-owned mutable wire models or other cases where a framework contract requires mutation. Keep those exceptions internal.

`final` communicates ownership and prevents accidental reassignment. It can help the compiler and code analysis, but it is not treated as a garbage-collection optimization.

## Instance references

- Use `this.` to make same-class instance method calls explicit when it clarifies that the behavior is internal.
- Do not use `this.` as a blanket qualifier for fields or for methods invoked through collaborator objects.
- Qualify same-class static method calls with their declaring class (for example, `RetryPolicy.builder()`).
- Local variables and collaborator calls are not qualified with `this`.
- Constructor delegation continues to use the normal `this(...)` syntax.

## Layout

- Keep directly related statements together.
- Separate distinct validation, transformation, I/O, and result-handling stages with one blank line.
- Avoid multiple consecutive blank lines and avoid spacing that breaks a single logical operation apart.
- Prefer readable multi-line methods over compressing several statements onto one line.

## Comments

- Comment only logic whose reason, invariant, security property, protocol constraint, or non-obvious tradeoff is not clear from the code.
- Explain why the code exists, not what an immediately readable statement does.
- Do not add narration comments to straightforward assignments, getters, builders, or delegation.
- Use JavaDoc for public API contracts; ordinary implementation comments do not replace JavaDoc.

## Review checklist

Before considering Java work complete:

- Check that eligible classes, fields, parameters, and meaningful single-assignment locals use `final`.
- Check that instance references consistently use `this.`.
- Check that blank lines communicate logical grouping.
- Remove comments that merely restate the code and retain comments that explain non-obvious behavior.
- Run the relevant Gradle tests while preserving the module's Java baseline.
