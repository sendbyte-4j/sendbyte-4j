package africa.sendbyte.email;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Full email record including body content and event timeline. */
public final class EmailDetail {
    private final Email email;
    private final String html;
    private final String text;
    private final List<EmailEvent> events;

    EmailDetail(
            final Email email,
            final String html,
            final String text,
            final List<EmailEvent> events) {
        this.email = email;
        this.html = html;
        this.text = text;
        this.events =
                events == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(events));
    }

    public Email email() {
        return this.email;
    }

    public String id() {
        return this.email.id();
    }

    public String html() {
        return this.html;
    }

    public String text() {
        return this.text;
    }

    public List<EmailEvent> events() {
        return this.events;
    }

    public OffsetDateTime createdAt() {
        return this.email.createdAt();
    }
}
