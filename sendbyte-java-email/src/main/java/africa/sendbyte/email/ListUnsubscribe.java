package africa.sendbyte.email;

import java.net.URI;

/** List-unsubscribe URL and/or mail address included with an email. */
public final class ListUnsubscribe {
    private final String url;
    private final String mailto;

    private ListUnsubscribe(final String url, final String mailto) {
        if ((url == null || url.isEmpty()) && (mailto == null || mailto.trim().isEmpty())) {
            throw new IllegalArgumentException("At least one unsubscribe URL or mailto value is required");
        }
        if (url != null) {
            final URI parsed = URI.create(url);
            if (!parsed.isAbsolute() || url.length() > 2048) {
                throw new IllegalArgumentException("Unsubscribe URL must be absolute and at most 2048 characters");
            }
        }
        this.url = url;
        this.mailto = mailto;
    }

    public static ListUnsubscribe of(final String url, final String mailto) {
        return new ListUnsubscribe(url, mailto);
    }

    public String getUrl() {
        return this.url;
    }

    public String getMailto() {
        return this.mailto;
    }
}
