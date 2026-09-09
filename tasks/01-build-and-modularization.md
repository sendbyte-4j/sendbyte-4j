# 01 — Build and Modularization

## Goal

Establish a coherent, reproducible Gradle module graph for the new SDK.

## Deliverables

### D01.1 Build baseline

- Keep the Gradle Wrapper versioned and pin plugins.
- Make clean builds reproducible from a fresh checkout.
- Add formatting, static analysis, compiler warnings, dependency convergence, license, and JavaDoc checks appropriate to the approved Java baseline.

Acceptance criteria:

- [x] `./gradlew clean check` succeeds from a clean checkout without pre-populated Gradle caches, given network access.
- [x] The same Gradle command is documented and used by CI.
- [ ] Build output contains no unexplained warnings.
- [ ] Generated/build artifacts are ignored and source artifacts are reproducible.

### D01.2 Module graph

- Create core, email, marketing, webhooks, admin, framework, aggregate, and platform/BOM modules in `settings.gradle.kts`.
- Enforce dependency direction and keep optional integrations out of core/product runtime classpaths.

Acceptance criteria:

- [x] `./gradlew :sendbyte-java-core:dependencies` contains no framework dependency.
- [x] A mail-only consumer does not resolve SMS, WhatsApp, Spring, or Quarkus.
- [x] Framework artifacts do not enter standalone consumers transitively.
- [x] The current module graph is acyclic and Gradle rejects cyclic project dependencies.
- [x] Each current Java module produces sources and JavaDoc artifacts.

### D01.3 Aggregate artifact and BOM

Acceptance criteria:

- [x] The aggregate installs every currently planned stable product and has no implementation code of its own.
- [x] The BOM constrains every currently publishable SDK module to one compatible version.
- [ ] Maven and Gradle fixtures successfully consume selective modules, the aggregate, and the BOM.
- [ ] Experimental modules are excluded from the stable aggregate unless explicitly approved.

### D01.4 Reference-code reuse controls

Acceptance criteria:

- [ ] Reused code has been checked against the current OpenAPI contract and new architecture.
- [ ] Package names, dependencies, mutable models, and accidental public internals are not copied by default.
- [ ] Reused MIT-licensed code retains required attribution and license notices.
- [ ] Reference examples are accepted only after they compile against the new SDK and match current wire behavior.
