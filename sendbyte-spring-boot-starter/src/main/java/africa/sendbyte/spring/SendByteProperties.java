package africa.sendbyte.spring;

import africa.sendbyte.SendByteClient;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for the SendByte Spring Boot starter. */
@ConfigurationProperties("sendbyte")
public final class SendByteProperties {
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

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public URI getBaseUri() {
        return this.baseUri;
    }

    public void setBaseUri(final URI baseUri) {
        this.baseUri = baseUri;
    }

    public Duration getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setConnectTimeout(final Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getRequestTimeout() {
        return this.requestTimeout;
    }

    public void setRequestTimeout(final Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    public long getMaxResponseBodyBytes() {
        return this.maxResponseBodyBytes;
    }

    public void setMaxResponseBodyBytes(final long maxResponseBodyBytes) {
        this.maxResponseBodyBytes = maxResponseBodyBytes;
    }

    public Retry getRetry() {
        return this.retry;
    }

    public Diagnostics getDiagnostics() {
        return this.diagnostics;
    }

    public Product getEmail() {
        return this.email;
    }

    public Product getAdmin() {
        return this.admin;
    }

    public Product getMarketing() {
        return this.marketing;
    }

    public Product getWebhooks() {
        return this.webhooks;
    }

    /** Retry settings applied to the shared client runtime. */
    public static final class Retry {
        private int maxAttempts = 3;
        private Duration initialBackoff = Duration.ofMillis(100);
        private Duration maxBackoff = Duration.ofSeconds(2);
        private double jitterRatio = 0.2d;

        public int getMaxAttempts() {
            return this.maxAttempts;
        }

        public void setMaxAttempts(final int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getInitialBackoff() {
            return this.initialBackoff;
        }

        public void setInitialBackoff(final Duration initialBackoff) {
            this.initialBackoff = initialBackoff;
        }

        public Duration getMaxBackoff() {
            return this.maxBackoff;
        }

        public void setMaxBackoff(final Duration maxBackoff) {
            this.maxBackoff = maxBackoff;
        }

        public double getJitterRatio() {
            return this.jitterRatio;
        }

        public void setJitterRatio(final double jitterRatio) {
            this.jitterRatio = jitterRatio;
        }
    }

    /** Diagnostics integration settings. */
    public static final class Diagnostics {
        private boolean enabled = true;

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    /** Product facade auto-configuration toggle. */
    public static final class Product {
        private boolean enabled = true;

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
}
