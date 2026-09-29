# Maven Central and SonarQube Cloud Requirements

Status: deferred until release preparation

Last reviewed: 2026-09-09

This document records the external setup and repository work required before publishing the SDK. It is intentionally not an implementation blocker while the SDK is incomplete.

## Maven Central account and ownership

- Create a Sonatype Central Portal account.
- Verify the `io.github.sendbyte-4j` publishing namespace for the `sendbyte-4j`
  GitHub organization/account before the first release.
- Generate a Central Portal user token and store it only in the release environment or repository secrets.

## Project identity

- Confirm the final Git repository and public project URL.
- Confirm the group ID, stable artifact IDs, and initial release version.
- Decide which completed modules are stable enough to publish; do not release empty or experimental modules accidentally.
- Replace the placeholders in `AUTHORS.md` with approved maintainer information.
- Provide developer name, developer ID, public email if desired, and profile/organization URL for Maven POM metadata.
- Select a project license and add the complete `LICENSE` file.
- Preserve the reference project's MIT copyright attribution wherever substantially reused code requires it.
- Define the semantic-versioning and deprecation policy.

## Gradle publication configuration

- Apply and configure `maven-publish` for each releasable Java module and the BOM.
- Apply and configure Gradle's `signing` plugin.
- Generate a valid POM for every publication with:
  - project name and description;
  - project URL;
  - license name and URL;
  - developer information;
  - SCM connection, developer connection, and repository URL;
  - correct published dependency scopes.
- Publish the primary artifact and, where required, matching sources and JavaDoc JARs.
- Generate the required checksums and detached PGP signatures.
- Use a Central Publisher Portal-compatible release mechanism. Legacy OSSRH deployment is not the target workflow.
- Keep snapshot publication separate from immutable final releases.

## Signing

- Create or select a PGP signing key owned by the release maintainer or organization.
- Publish the public key to an appropriate public key server.
- Store the private key and passphrase only in protected CI/release secrets.
- Never commit or print Portal credentials, private signing keys, or passphrases.
- Verify signatures as part of the release dry run.

## Release validation

- Publish all candidate artifacts to a local or isolated staging repository first.
- Validate generated POMs, Gradle module metadata, signatures, checksums, sources, and JavaDoc artifacts.
- Use clean Maven and Gradle fixture projects to consume selective modules, the aggregate, and the BOM from staging.
- Inspect dependency scopes and confirm that framework or unrelated product dependencies do not leak transitively.
- Inspect JAR contents for secrets, personal test data, duplicate classes, and unintended build metadata.
- Run the full supported JDK and framework compatibility matrices against the exact release commit.
- Perform a user-managed Central Portal deployment first, validate it, and require explicit approval before publishing.
- Tag and publish each final version once; Maven Central releases are immutable.
- Document recovery for validation failures and partial releases.

## CI secrets expected later

Exact names will be chosen with the release workflow. The workflow will need protected equivalents of:

- Central Portal token username;
- Central Portal token password;
- armored private PGP key;
- PGP key passphrase;
- optional SonarQube Cloud analysis token.

## SonarQube Cloud, if adopted

SonarQube Cloud analysis is separate from Maven Central publication.

- Host the project in its final GitHub organization or account.
- Create/import the project and select its SonarQube Cloud organization and project key.
- Add the Sonar Gradle plugin and JaCoCo XML coverage reporting.
- Store `SONAR_TOKEN` as a protected repository secret.
- Run compiled-code analysis in CI with complete Git history where required for accurate issue attribution.
- Configure source/test scope for every Gradle module.
- Choose a new-code definition and an enforceable quality gate.
- Decide whether analysis is required on pull requests, `dev`, release branches, and tags.

## Inputs still required from project ownership

- Verified Maven namespace.
- Final Git repository URL.
- Approved maintainer/developer metadata.
- Approved license.
- First stable module set and release version.
- Release approval and credential ownership policy.
- Decision on SonarQube Cloud adoption and organization.

## References

- [Sonatype Central Portal registration](https://central.sonatype.org/register/central-portal/)
- [Sonatype namespace registration](https://central.sonatype.org/register/namespace/)
- [Sonatype publishing requirements](https://central.sonatype.org/publish/requirements/)
- [Sonatype Gradle publishing guidance](https://central.sonatype.org/publish/publish-portal-gradle/)
- [Sonatype Portal token generation](https://central.sonatype.org/publish/generate-portal-token/)
- [Gradle Maven Publish plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [Gradle Signing plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)
- [SonarScanner for Gradle](https://docs.sonarsource.com/sonarqube-cloud/analyzing-source-code/scanners/sonarscanner-for-gradle)
