# Contributing

Thanks for helping improve the SendByte Java SDK. User-facing installation and usage instructions
are in the [README](README.md).

## Requirements for development

- Run Gradle on JDK 17 or newer.
- Core and product modules compile for Java 11; framework integrations compile for Java 17.
- Follow [coding conventions](CODING_CONVENTIONS.md) for Java changes.

## Verify a change

```shell
./gradlew clean check
```

`check` validates formatting, PMD rules, compiler warnings, dependency version conflicts,
dependency licenses, JavaDoc syntax, the reviewed OpenAPI snapshot, and stable module membership.
It does not publish artifacts.

For changes to published artifacts, consumer fixtures, or release gates, also run:

```shell
./gradlew verifyRelease
```

To run tests using a particular installed JDK:

```shell
./gradlew test -PtestJavaVersion=11
```

## Release process

The `Release` GitHub Actions workflow is manually started from the `release` branch with a version
matching the build. It runs `verifyRelease`, waits for the protected GitHub `release` environment,
checks publishing credentials, creates or reuses the matching `v<version>` tag, and publishes to
Maven Central. A rerun reuses a tag only when it points to the verified commit.

Configure these secrets in the GitHub `release` environment:

- `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`: the Central Portal user token pair.
- `GPG_SIGNING_KEY` and `GPG_SIGNING_KEY_PASSWORD`: the ASCII-armored private signing key and its
  passphrase. Publish the matching public key to a key server accepted by Central.

The project is licensed under the MIT License. Every published module includes the corresponding
license information in its Maven POM.
