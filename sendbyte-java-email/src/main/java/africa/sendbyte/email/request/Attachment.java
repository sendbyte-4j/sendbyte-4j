package africa.sendbyte.email.request;

import java.util.Base64;
import java.util.Objects;

/** Immutable email attachment encoded for the SendByte API. */
public final class Attachment {
    private final String filename;
    private final String content;
    private final String contentType;
    private final String contentId;
    private final Disposition disposition;

    private Attachment(final Builder builder) {
        this.filename = requireLength(builder.filename, "filename", 1, 255);
        this.content = Objects.requireNonNull(builder.content, "content");
        this.contentType = requireLength(builder.contentType, "contentType", 3, 255);
        this.contentId = optionalMax(builder.contentId, "contentId", 200);
        this.disposition = builder.disposition;
    }

    public static Builder builder(
            final String filename, final String contentType, final byte[] content) {
        Objects.requireNonNull(content, "content");
        return new Builder(filename, contentType, Base64.getEncoder().encodeToString(content));
    }

    public static Builder encodedBuilder(
            final String filename, final String contentType, final String base64Content) {
        return new Builder(
                filename, contentType, Objects.requireNonNull(base64Content, "base64Content"));
    }

    public String getFilename() {
        return this.filename;
    }

    public String getContent() {
        return this.content;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getContentId() {
        return this.contentId;
    }

    public String getDisposition() {
        return this.disposition == null ? null : this.disposition.wireValue;
    }

    public enum Disposition {
        ATTACHMENT("attachment"),
        INLINE("inline");

        private final String wireValue;

        Disposition(final String wireValue) {
            this.wireValue = wireValue;
        }
    }

    public static final class Builder {
        private final String filename;
        private final String contentType;
        private final String content;
        private String contentId;
        private Disposition disposition;

        private Builder(final String filename, final String contentType, final String content) {
            this.filename = filename;
            this.contentType = contentType;
            this.content = content;
        }

        public Builder contentId(final String contentId) {
            this.contentId = contentId;
            return this;
        }

        public Builder disposition(final Disposition disposition) {
            this.disposition = disposition;
            return this;
        }

        public Attachment build() {
            return new Attachment(this);
        }
    }

    static String requireLength(
            final String value, final String name, final int minimum, final int maximum) {
        Objects.requireNonNull(value, name);
        if (value.length() < minimum || value.length() > maximum) {
            throw new IllegalArgumentException(
                    name + " must contain between " + minimum + " and " + maximum + " characters");
        }
        return value;
    }

    static String optionalMax(final String value, final String name, final int maximum) {
        if (value != null && value.length() > maximum) {
            throw new IllegalArgumentException(
                    name + " must contain at most " + maximum + " characters");
        }
        return value;
    }
}
