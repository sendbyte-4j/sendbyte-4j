# Project instructions

Follow [`docs/CODING_CONVENTIONS.md`](docs/CODING_CONVENTIONS.md) for all Java changes in this project.

This is the active SDK implementation. `../SendByte-Java-main/` is reference material, not a migration target or a source of implicit compatibility requirements.

Preserve the Java 11 baseline for core and product modules and the Java 17 baseline for framework integrations. Verify implementation changes with the narrowest relevant tests and run `./gradlew clean check` for completed work packages. `check` is hermetic; run `./gradlew verifyRelease` as well when a change affects published artifacts, consumer fixtures, or the release gates.
