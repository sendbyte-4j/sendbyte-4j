package africa.sendbyte;

/** Supplies the API key used for a request. Implementations must be thread-safe. */
@FunctionalInterface
public interface CredentialsProvider {
    String apiKey();
}
