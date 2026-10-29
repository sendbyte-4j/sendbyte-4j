package africa.sendbyte.quarkus;

import africa.sendbyte.SendByteClient;
import java.net.URI;
import java.time.Duration;

/** Runtime configuration consumed by the SendByte Quarkus integration. */
public final class SendByteQuarkusConfig {
    private boolean enabled = true;
    private String apiKey;
    private URI baseUri = SendByteClient.DEFAULT_BASE_URI;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration requestTimeout = Duration.ofSeconds(30);
    private String userAgent;
    private long maxResponseBodyBytes = SendByteClient.DEFAULT_MAX_RESPONSE_BODY_BYTES;
    private final Retry retry = new Retry();
    private final Diagnostics diagnostics = new Diagnostics();
    private final Product email = new Product();
    private final Product admin = new Product();
    private final Product marketing = new Product();
    private final Product webhooks = new Product();

    public boolean enabled() {
        return this.enabled;
    }

    public void enabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String apiKey() {
        return this.apiKey;
    }

    public void apiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public URI baseUri() {
        return this.baseUri;
    }

    public void baseUri(final URI baseUri) {
        this.baseUri = baseUri;
    }

    public Duration connectTimeout() {
        return this.connectTimeout;
    }

    public void connectTimeout(final Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration requestTimeout() {
        return this.requestTimeout;
    }

    public void requestTimeout(final Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String userAgent() {
        return this.userAgent;
    }

    public void userAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    public long maxResponseBodyBytes() {
        return this.maxResponseBodyBytes;
    }

    public void maxResponseBodyBytes(final long maxResponseBodyBytes) {
        this.maxResponseBodyBytes = maxResponseBodyBytes;
    }

    public Retry retry() {
        return this.retry;
    }

    public Diagnostics diagnostics() {
        return this.diagnostics;
    }

    public Product email() {
        return this.email;
    }

    public Product admin() {
        return this.admin;
    }

    public Product marketing() {
        return this.marketing;
    }

    public Product webhooks() {
        return this.webhooks;
    }

    /** Retry settings applied to the shared runtime. */
    public static final class Retry {
        private int maxAttempts = 3;
        private Duration initialBackoff = Duration.ofMillis(100);
        private Duration maxBackoff = Duration.ofSeconds(2);
        private double jitterRatio = 0.2d;

        public int maxAttempts() {
            return this.maxAttempts;
        }

        public void maxAttempts(final int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration initialBackoff() {
            return this.initialBackoff;
        }

        public void initialBackoff(final Duration initialBackoff) {
            this.initialBackoff = initialBackoff;
        }

        public Duration maxBackoff() {
            return this.maxBackoff;
        }

        public void maxBackoff(final Duration maxBackoff) {
            this.maxBackoff = maxBackoff;
        }

        public double jitterRatio() {
            return this.jitterRatio;
        }

        public void jitterRatio(final double jitterRatio) {
            this.jitterRatio = jitterRatio;
        }
    }

    /** Diagnostics integration settings. */
    public static final class Diagnostics {
        private boolean enabled = true;

        public boolean enabled() {
            return this.enabled;
        }

        public void enabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    /** Product facade activation setting. */
    public static final class Product {
        private boolean enabled = true;

        public boolean enabled() {
            return this.enabled;
        }

        public void enabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
}
