package africa.sendbyte;

import africa.sendbyte.internal.DefaultClientRuntime;
import africa.sendbyte.spi.ClientRuntime;
import africa.sendbyte.transport.HttpTransport;
import africa.sendbyte.transport.JdkHttpTransport;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Framework-neutral entry point for SendByte product clients.
 *
 * <p>Instances are immutable and thread-safe. Create one and reuse it. Product modules adapt the
 * shared {@link #runtime()} rather than implementing their own authentication, HTTP, serialization,
 * or error handling.
 *
 * <p>The default transport buffers response bodies in memory and the runtime rejects responses
 * above {@link #DEFAULT_MAX_RESPONSE_BODY_BYTES} unless overridden. Request payloads, including
 * attachments represented by product modules, are serialized in memory by the core runtime.
 */
public final class SendByteClient implements AutoCloseable {
    public static final URI DEFAULT_BASE_URI = URI.create("https://api.sendbyte.africa");
    public static final long DEFAULT_MAX_RESPONSE_BODY_BYTES = 16L * 1024L * 1024L;

    private final ClientRuntime runtime;
    private final HttpTransport transport;
    private final ResourceOwnership transportOwnership;
    private final ScheduledExecutorService retryExecutor;
    private final AtomicBoolean closed = new AtomicBoolean();

    private SendByteClient(final Builder builder) {
        final CredentialsProvider credentials =
                Objects.requireNonNull(builder.credentialsProvider, "credentialsProvider");
        validateCredentials(credentials.apiKey());

        this.transport =
                builder.transport != null
                        ? builder.transport
                        : new JdkHttpTransport(builder.connectTimeout);

        this.transportOwnership =
                builder.transport != null ? builder.transportOwnership : ResourceOwnership.CLIENT;
        this.retryExecutor = newRetryExecutor();

        this.runtime =
                new DefaultClientRuntime(
                        builder.baseUri,
                        credentials,
                        this.transport,
                        builder.requestTimeout,
                        builder.userAgent,
                        builder.retryPolicy,
                        builder.requestObserver,
                        builder.maxResponseBodyBytes,
                        this.retryExecutor,
                        this.closed);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Shared runtime intended for SendByte product modules and advanced adapters. Application code
     * normally passes this client to a product facade instead.
     */
    public ClientRuntime runtime() {
        this.ensureOpen();
        return this.runtime;
    }

    public boolean isClosed() {
        return this.closed.get();
    }

    private void ensureOpen() {
        if (this.closed.get()) {
            throw new IllegalStateException("SendByteClient is closed");
        }
    }

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)
                && this.transportOwnership == ResourceOwnership.CLIENT) {
            this.transport.close();
        }
        if (this.closed.get()) {
            this.retryExecutor.shutdownNow();
        }
    }

    private static void validateCredentials(final String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("A non-blank SendByte API key is required");
        }
        if (containsControlCharacter(apiKey)) {
            throw new IllegalArgumentException(
                    "The SendByte API key contains an invalid control character");
        }
    }

    private static boolean containsControlCharacter(final String value) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.isISOControl(value.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    public static final class Builder {
        private URI baseUri = DEFAULT_BASE_URI;
        private CredentialsProvider credentialsProvider;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration requestTimeout = Duration.ofSeconds(30);
        private String userAgent = "sendbyte-java/0.1.0";
        private RetryPolicy retryPolicy = RetryPolicy.DEFAULT;
        private RequestObserver requestObserver = RequestObserver.NONE;
        private long maxResponseBodyBytes = DEFAULT_MAX_RESPONSE_BODY_BYTES;
        private HttpTransport transport;
        private ResourceOwnership transportOwnership = ResourceOwnership.CALLER;

        private Builder() {}

        public Builder apiKey(final String apiKey) {
            this.credentialsProvider = () -> apiKey;
            return this;
        }

        public Builder credentialsProvider(final CredentialsProvider credentialsProvider) {
            this.credentialsProvider =
                    Objects.requireNonNull(credentialsProvider, "credentialsProvider");
            return this;
        }

        public Builder baseUri(final URI baseUri) {
            this.baseUri = validateBaseUri(baseUri);
            return this;
        }

        public Builder connectTimeout(final Duration connectTimeout) {
            this.connectTimeout = positive(connectTimeout, "connectTimeout");
            return this;
        }

        public Builder requestTimeout(final Duration requestTimeout) {
            this.requestTimeout = positive(requestTimeout, "requestTimeout");
            return this;
        }

        public Builder retryPolicy(final RetryPolicy retryPolicy) {
            this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
            return this;
        }

        public Builder requestObserver(final RequestObserver requestObserver) {
            this.requestObserver = Objects.requireNonNull(requestObserver, "requestObserver");
            return this;
        }

        /** Sets the maximum buffered response body size accepted by the runtime before decoding. */
        public Builder maxResponseBodyBytes(final long maxResponseBodyBytes) {
            if (maxResponseBodyBytes < 1L) {
                throw new IllegalArgumentException("maxResponseBodyBytes must be positive");
            }
            this.maxResponseBodyBytes = maxResponseBodyBytes;
            return this;
        }

        public Builder userAgent(final String userAgent) {
            if (userAgent == null
                    || userAgent.trim().isEmpty()
                    || containsControlCharacter(userAgent)) {
                throw new IllegalArgumentException(
                        "userAgent must be non-blank and contain no control characters");
            }
            this.userAgent = userAgent;
            return this;
        }

        public Builder transport(final HttpTransport transport, final ResourceOwnership ownership) {
            this.transport = Objects.requireNonNull(transport, "transport");
            this.transportOwnership = Objects.requireNonNull(ownership, "ownership");
            return this;
        }

        public SendByteClient build() {
            validateBaseUri(this.baseUri);
            positive(this.connectTimeout, "connectTimeout");
            positive(this.requestTimeout, "requestTimeout");
            if (this.transport == null && this.connectTimeout.compareTo(this.requestTimeout) > 0) {
                throw new IllegalArgumentException(
                        "connectTimeout must not exceed requestTimeout for the default transport");
            }

            return new SendByteClient(this);
        }

        private static URI validateBaseUri(final URI value) {
            Objects.requireNonNull(value, "baseUri");
            final String scheme = value.getScheme();

            if (!value.isAbsolute()
                    || value.getHost() == null
                    || value.getQuery() != null
                    || value.getFragment() != null) {
                throw new IllegalArgumentException(
                        "baseUri must be an absolute HTTP(S) URI without query or fragment");
            }

            final boolean secure = "https".equalsIgnoreCase(scheme);
            final boolean loopback =
                    "http".equalsIgnoreCase(scheme)
                            && ("localhost".equalsIgnoreCase(value.getHost())
                                    || "127.0.0.1".equals(value.getHost())
                                    || "::1".equals(value.getHost()));
            if (!secure && !loopback) {
                throw new IllegalArgumentException(
                        "baseUri must use HTTPS; HTTP is permitted only for loopback tests");
            }

            final String path = value.getPath();
            if (path != null && !path.isEmpty() && !"/".equals(path)) {
                throw new IllegalArgumentException("baseUri must not include a path");
            }

            return URI.create(value.toString().replaceAll("/$", ""));
        }

        private static Duration positive(final Duration value, final String name) {
            Objects.requireNonNull(value, name);

            if (value.isZero() || value.isNegative()) {
                throw new IllegalArgumentException(name + " must be positive");
            }

            return value;
        }
    }

    private static ScheduledExecutorService newRetryExecutor() {
        final AtomicInteger count = new AtomicInteger();
        final ThreadFactory threadFactory =
                task -> {
                    final Thread thread =
                            new Thread(task, "sendbyte-runtime-" + count.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                };
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
}
