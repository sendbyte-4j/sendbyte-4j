package africa.sendbyte.email;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable summary returned after an email is accepted or replayed. */
public final class Email {
    private final String id;
    private final String from;
    private final List<String> to;
    private final String subject;
    private final EmailStatus status;
    private final String rawStatus;
    private final boolean sandbox;
    private final List<String> tags;
    private final String messageId;
    private final int openCount;
    private final int clickCount;
    private final OffsetDateTime firstOpenedAt;
    private final OffsetDateTime firstClickedAt;
    private final OffsetDateTime scheduledAt;
    private final OffsetDateTime createdAt;

    Email(final String id, final String from, final List<String> to, final String subject,
          final String rawStatus, final boolean sandbox, final List<String> tags, final String messageId,
          final int openCount, final int clickCount, final OffsetDateTime firstOpenedAt,
          final OffsetDateTime firstClickedAt, final OffsetDateTime scheduledAt,
          final OffsetDateTime createdAt) {
        this.id = id;
        this.from = from;
        this.to = immutable(to);
        this.subject = subject;
        this.status = EmailStatus.fromWire(rawStatus);
        this.rawStatus = rawStatus;
        this.sandbox = sandbox;
        this.tags = immutable(tags);
        this.messageId = messageId;
        this.openCount = openCount;
        this.clickCount = clickCount;
        this.firstOpenedAt = firstOpenedAt;
        this.firstClickedAt = firstClickedAt;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
    }

    public String id() {
        return this.id;
    }

    public String from() {
        return this.from;
    }

    public List<String> to() {
        return this.to;
    }

    public String subject() {
        return this.subject;
    }

    public EmailStatus status() {
        return this.status;
    }

    public String rawStatus() {
        return this.rawStatus;
    }

    public boolean sandbox() {
        return this.sandbox;
    }

    public List<String> tags() {
        return this.tags;
    }

    public String messageId() {
        return this.messageId;
    }

    public int openCount() {
        return this.openCount;
    }

    public int clickCount() {
        return this.clickCount;
    }

    public OffsetDateTime firstOpenedAt() {
        return this.firstOpenedAt;
    }

    public OffsetDateTime firstClickedAt() {
        return this.firstClickedAt;
    }

    public OffsetDateTime scheduledAt() {
        return this.scheduledAt;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    private static <T> List<T> immutable(final List<T> value) {
        return value == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(value));
    }
}
