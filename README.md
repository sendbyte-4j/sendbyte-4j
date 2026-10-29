# SendByte Java SDK

The SendByte Java SDK lets Java applications call the SendByte API with typed request and response
objects. It includes a framework-neutral client, product modules, and a Spring Boot integration.

## Requirements

- Java 11 or newer for the core client and product modules.
- Java 17 or newer for the Spring Boot and Quarkus integrations.
- A SendByte API key. The SDK defaults to `https://api.sendbyte.africa`.

## Install

Use a version listed on the [GitHub Releases page](https://github.com/sendbyte-4j/sendbyte-4j/releases).
The version in this source repository may not have been published yet.

Gradle:

```kotlin
dependencies {
    implementation("io.github.sendbyte-4j.sendbyte:sendbyte-java-email:<version>")
}
```

Maven:

```xml
<dependency>
  <groupId>io.github.sendbyte-4j.sendbyte</groupId>
  <artifactId>sendbyte-java-email</artifactId>
  <version>REPLACE_WITH_RELEASED_VERSION</version>
</dependency>
```

The email module includes the core client as a dependency. Other product modules are available as
`sendbyte-java-marketing`, `sendbyte-java-webhooks`, and `sendbyte-java-admin`. Use
`sendbyte-java-all` if you want all product modules.

## Send an email

Set `SENDBYTE_API_KEY` to your API key. Test keys (`sk_test_`) use SendByte's sandbox, while live
keys (`sk_live_`) send real email. The SDK uses `https://api.sendbyte.africa` by default. You can
override the host with `SENDBYTE_BASE_URI` for local testing or an alternate deployment. Keep the
key in an environment variable or secret manager, not in source code.

```java
import africa.sendbyte.SendByteClient;
import africa.sendbyte.email.Email;
import africa.sendbyte.email.Emails;
import africa.sendbyte.email.request.SendEmailRequest;

public final class SendUpdate {
    public static void main(final String[] args) {
        final String apiKey = System.getenv("SENDBYTE_API_KEY");

        try (SendByteClient client = SendByteClient.builder()
                .apiKey(apiKey)
                .build()) {
            final Emails emails = new Emails(client);
            final SendEmailRequest request = SendEmailRequest.builder()
                    .from("updates@example.com")
                    .to("person@example.net")
                    .subject("Your update")
                    .text("The update is ready.")
                    .build();

            final Email sent = emails.send(request);
            System.out.println("Accepted email with id: " + sent.id());
        }
    }
}
```

`SendByteClient` is thread-safe. Create one per application component, reuse it, and close it when
that component shuts down. For asynchronous requests, use `emails.sendAsync(request)` and keep the
client open until the returned `CompletableFuture` completes or is cancelled.

## Spring Boot

Add the starter and the product module you use at the same version. For email support, add:

```kotlin
dependencies {
    implementation("io.github.sendbyte-4j.sendbyte:sendbyte-spring-boot-starter:<version>")
    implementation("io.github.sendbyte-4j.sendbyte:sendbyte-java-email:<version>")
}
```

Then configure the API key in `application.yml`. The API host defaults to
`https://api.sendbyte.africa`; set `base-uri` only when you need to override it:

```yaml
sendbyte:
  api-key: ${SENDBYTE_API_KEY}
```

Inject `africa.sendbyte.email.Emails` into your service to send messages. The starter creates and
closes the shared client with the Spring application context.

## Errors and diagnostics

SDK request failures are reported as `SendByteException` subclasses, including authentication,
authorization, rate-limit, timeout, and connection failures. Catch a specific exception when your
application needs different handling for that case. Exceptions expose request diagnostics when
available; avoid logging API keys, message bodies, or recipient data.

## API coverage

The SDK is being developed in modules. Check the Java API in the version you install for available
operations. The [OpenAPI snapshot](spec/openapi.yaml) describes the service contract; an operation
in that file does not necessarily mean the matching SDK method is included in your version.

For help using the SDK, open a [GitHub issue](https://github.com/sendbyte-4j/sendbyte-4j/issues).
For vulnerability reports, follow the [security policy](SECURITY.md).
