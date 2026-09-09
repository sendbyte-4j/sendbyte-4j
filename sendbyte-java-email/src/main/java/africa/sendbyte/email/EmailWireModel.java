package africa.sendbyte.email;

import africa.sendbyte.exception.SerializationException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

final class EmailWireModel {
    public String id;
    public String from;
    public List<String> to;
    public String subject;
    public String status;
    public boolean sandbox;
    public List<String> tags;
    public String messageId;
    public int openCount;
    public int clickCount;
    public String firstOpenedAt;
    public String firstClickedAt;
    public String scheduledAt;
    public String createdAt;

    Email toEmail() {
        try {
            return new Email(id, from, to, subject, status, sandbox, tags, messageId, openCount, clickCount,
                    parse(firstOpenedAt), parse(firstClickedAt), parse(scheduledAt), parse(createdAt));
        } catch (final DateTimeParseException exception) {
            throw new SerializationException("Could not decode an email timestamp", exception);
        }
    }

    private static OffsetDateTime parse(final String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }
}
