package africa.sendbyte.email;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable payload for the SendByte send-email operation. */
public final class SendEmailRequest {
    private final String from;
    private final List<String> to;
    private final String subject;
    private final String html;
    private final String text;
    private final String templateId;
    private final Map<String, Object> variables;
    private final List<String> cc;
    private final List<String> bcc;
    private final List<String> replyTo;
    private final List<Attachment> attachments;
    private final List<String> tags;
    private final Map<String, String> headers;
    private final ListUnsubscribe listUnsubscribe;
    private final String scheduledAt;
    private final String idempotencyKey;
    private final TrackingOptions tracking;

    private SendEmailRequest(final Builder builder) {
        this.from = Attachment.requireLength(builder.from, "from", 3, 320);
        this.to = addressList(builder.to, "to", true);
        this.subject = optionalNonBlank(builder.subject, "subject", 998);
        this.html = optionalNonBlank(builder.html, "html", 500_000);
        this.text = optionalNonBlank(builder.text, "text", 500_000);
        this.templateId = optionalNonBlank(builder.templateId, "templateId", 256);

        if (this.html == null && this.text == null && this.templateId == null) {
            throw new IllegalArgumentException("At least one of html, text, or templateId is required");
        }
        if (this.subject == null && this.templateId == null) {
            throw new IllegalArgumentException("subject is required unless templateId is provided");
        }

        this.variables = immutableMap(builder.variables);
        this.cc = addressList(builder.cc, "cc", false);
        this.bcc = addressList(builder.bcc, "bcc", false);
        this.replyTo = addressList(builder.replyTo, "replyTo", false);

        if (builder.attachments.size() > 10) {
            throw new IllegalArgumentException("attachments must contain at most 10 items");
        }
        this.attachments = immutableList(builder.attachments);

        if (builder.tags.size() > 10) {
            throw new IllegalArgumentException("tags must contain at most 10 items");
        }
        for (final String tag : builder.tags) {
            Attachment.requireLength(tag, "tag", 1, 64);
        }
        this.tags = immutableList(builder.tags);

        this.headers = validateHeaders(builder.headers);
        this.listUnsubscribe = builder.listUnsubscribe;
        this.scheduledAt = builder.scheduledAt;
        this.idempotencyKey = optionalNonBlank(builder.idempotencyKey, "idempotencyKey", 256);
        this.tracking = builder.tracking;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFrom() {
        return this.from;
    }

    public List<String> getTo() {
        return this.to;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getHtml() {
        return this.html;
    }

    public String getText() {
        return this.text;
    }

    public String getTemplateId() {
        return this.templateId;
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }

    public List<String> getCc() {
        return this.cc;
    }

    public List<String> getBcc() {
        return this.bcc;
    }

    public List<String> getReplyTo() {
        return this.replyTo;
    }

    public List<Attachment> getAttachments() {
        return this.attachments;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public ListUnsubscribe getListUnsubscribe() {
        return this.listUnsubscribe;
    }

    public String getScheduledAt() {
        return this.scheduledAt;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public TrackingOptions getTracking() {
        return this.tracking;
    }

    public static final class Builder {
        private String from;
        private final List<String> to = new ArrayList<>();
        private String subject;
        private String html;
        private String text;
        private String templateId;
        private final Map<String, Object> variables = new LinkedHashMap<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private final List<String> replyTo = new ArrayList<>();
        private final List<Attachment> attachments = new ArrayList<>();
        private final List<String> tags = new ArrayList<>();
        private final Map<String, String> headers = new LinkedHashMap<>();
        private ListUnsubscribe listUnsubscribe;
        private String scheduledAt;
        private String idempotencyKey;
        private TrackingOptions tracking;

        private Builder() {
        }

        public Builder from(final String from) {
            this.from = from;
            return this;
        }

        public Builder to(final String... addresses) {
            this.to.addAll(Arrays.asList(addresses));
            return this;
        }

        public Builder to(final List<String> addresses) {
            this.to.addAll(Objects.requireNonNull(addresses, "addresses"));
            return this;
        }

        public Builder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        public Builder html(final String html) {
            this.html = html;
            return this;
        }

        public Builder text(final String text) {
            this.text = text;
            return this;
        }

        public Builder templateId(final String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder variable(final String name, final Object value) {
            this.variables.put(Objects.requireNonNull(name, "name"), value);
            return this;
        }

        public Builder cc(final String... addresses) {
            this.cc.addAll(Arrays.asList(addresses));
            return this;
        }

        public Builder bcc(final String... addresses) {
            this.bcc.addAll(Arrays.asList(addresses));
            return this;
        }

        public Builder replyTo(final String... addresses) {
            this.replyTo.addAll(Arrays.asList(addresses));
            return this;
        }

        public Builder attachment(final Attachment attachment) {
            this.attachments.add(Objects.requireNonNull(attachment, "attachment"));
            return this;
        }

        public Builder tag(final String tag) {
            this.tags.add(tag);
            return this;
        }

        public Builder header(final String name, final String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder listUnsubscribe(final ListUnsubscribe value) {
            this.listUnsubscribe = value;
            return this;
        }

        public Builder scheduledAt(final OffsetDateTime value) {
            this.scheduledAt = value == null ? null : value.toString();
            return this;
        }

        public Builder idempotencyKey(final String value) {
            this.idempotencyKey = value;
            return this;
        }

        public Builder tracking(final TrackingOptions value) {
            this.tracking = value;
            return this;
        }

        public SendEmailRequest build() {
            return new SendEmailRequest(this);
        }
    }

    private static List<String> addressList(final List<String> values, final String name, final boolean required) {
        if (values.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(name + " must contain at least one address");
            }
            return null;
        }
        if (values.size() > 50) {
            throw new IllegalArgumentException(name + " must contain at most 50 addresses");
        }

        for (final String value : values) {
            Attachment.requireLength(value, name + " address", 3, 320);
        }

        return immutableList(values);
    }

    private static Map<String, String> validateHeaders(final Map<String, String> values) {
        if (values.isEmpty()) {
            return null;
        }

        final Map<String, String> copy = new LinkedHashMap<>();
        values.forEach((name, value) -> {
            Attachment.requireLength(name, "header name", 1, 256);
            Objects.requireNonNull(value, "header value");
            if (value.length() > 4096 || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
                throw new IllegalArgumentException("header values must be at most 4096 characters and contain no line breaks");
            }
            if ("message-id".equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Message-ID cannot be overridden");
            }

            copy.put(name, value);
        });

        return Collections.unmodifiableMap(copy);
    }

    private static String optionalNonBlank(final String value, final String name, final int maximum) {
        if (value == null) {
            return null;
        }

        return Attachment.requireLength(value, name, 1, maximum);
    }

    private static <T> List<T> immutableList(final List<T> values) {
        return values.isEmpty() ? null : Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static <K, V> Map<K, V> immutableMap(final Map<K, V> values) {
        return values.isEmpty() ? null : Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
