package africa.sendbyte.email;

import africa.sendbyte.exception.SerializationException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

final class EmailApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("from")
    public String from;

    @JsonProperty("to")
    public List<String> to;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("status")
    public String status;

    @JsonProperty("sandbox")
    public boolean sandbox;

    @JsonProperty("tags")
    public List<String> tags;

    @JsonProperty("message_id")
    public String messageId;

    @JsonProperty("open_count")
    public int openCount;

    @JsonProperty("click_count")
    public int clickCount;

    @JsonProperty("first_opened_at")
    public String firstOpenedAt;

    @JsonProperty("first_clicked_at")
    public String firstClickedAt;

    @JsonProperty("scheduled_at")
    public String scheduledAt;

    @JsonProperty("created_at")
    public String createdAt;

    Email toEmail() {
        try {
            return new Email(
                    this.id,
                    this.from,
                    this.to,
                    this.subject,
                    this.status,
                    this.sandbox,
                    this.tags,
                    this.messageId,
                    this.openCount,
                    this.clickCount,
                    parse(this.firstOpenedAt),
                    parse(this.firstClickedAt),
                    parse(this.scheduledAt),
                    parse(this.createdAt));
        } catch (final DateTimeParseException exception) {
            throw new SerializationException("Could not decode an email timestamp", exception);
        }
    }

    private static OffsetDateTime parse(final String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }
}
