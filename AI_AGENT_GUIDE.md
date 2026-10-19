# Using the SendByte Java SDK with a coding agent

This guide gives coding agents the project facts they need when adding SendByte to a Java application. It describes the SDK consumer surface, not how to contribute to this repository.

## Check release availability first

This repository currently builds version `0.1.0-SNAPSHOT`; it is not a stable release. Before adding a dependency, check the project's release page or ask the application owner for the exact published version and repository. Do not invent a version or assume the snapshot is available from Maven Central.

The artifact group is `io.github.sendbyte-4j.sendbyte`. Add the core and email modules at the same release version:

```kotlin
dependencies {
    implementation("io.github.sendbyte-4j.sendbyte:sendbyte-java-core:<released-version>")
    implementation("io.github.sendbyte-4j.sendbyte:sendbyte-java-email:<released-version>")
}
```

The core and product modules target Java 11. Spring Boot and Quarkus integrations target Java 17. Use a framework integration only when its artifact is listed as available for the version being used.

## Current supported API example

The documented, implemented vertical slice is sending email. Do not infer that every operation in the OpenAPI document or every module is available in a published release.

Configure the API key and base URI from the application environment or secret manager. The OpenAPI snapshot uses `https://api.example.test` as a placeholder; it is not a production endpoint. Obtain the actual API base URI from the application owner or current SendByte documentation.

```java
import africa.sendbyte.SendByteClient;
import africa.sendbyte.email.Emails;
import africa.sendbyte.email.request.SendEmailRequest;
import java.net.URI;

String apiKey = System.getenv("SENDBYTE_API_KEY");
String baseUri = System.getenv("SENDBYTE_BASE_URI");

try (SendByteClient client = SendByteClient.builder()
        .apiKey(apiKey)
        .baseUri(URI.create(baseUri))
        .build()) {
    SendEmailRequest request = SendEmailRequest.builder()
            .from("updates@example.com")
            .to("person@example.net")
            .subject("Your update")
            .text("The update is ready.")
            .build();

    new Emails(client).send(request);
}
```

Keep API keys out of source code, logs, generated examples, and exception reports. Reuse a client for the lifetime of the application component that owns it, and close it when that component shuts down. The client is thread-safe.

For asynchronous sending, use `Emails.sendAsync(request)` and keep the client open until the returned `CompletableFuture` has completed or been cancelled.

## Instructions for coding agents

When working in an application that uses this SDK:

1. Confirm the released SDK version and available artifacts before editing the build file.
2. Use only documented public APIs from that exact version. If the needed operation is not available, explain the gap instead of constructing raw HTTP calls or guessing a method name.
3. Ask the application owner for missing API base URI, credential configuration, or product requirements. Never request or print a production API key.
4. Keep the API key in the application's established secret mechanism and keep message content and recipient data out of logs.
5. Follow the application's Java version, dependency management, and lifecycle conventions.
6. Add or update application tests using a mock transport or HTTP server. Do not send real messages as part of automated tests.

## Reference material

- [Project README](README.md) for the current implementation and build status.
- [Versioned OpenAPI snapshot](spec/openapi.yaml) for the wire contract. A contract operation is not evidence that a corresponding SDK method is implemented or released.
- [Security policy](SECURITY.md) for handling credentials and customer data.
