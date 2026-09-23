package africa.sendbyte.email;

import africa.sendbyte.exception.SerializationException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @JsonProperty("html")
    public String html;

    @JsonProperty("text")
    public String text;

    @JsonProperty("events")
    public List<EventApiResponse> events;

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

    EmailDetail toEmailDetail() {
        return new EmailDetail(this.toEmail(), this.html, this.text, this.toEvents());
    }

    private List<EmailEvent> toEvents() {
        if (this.events == null) {
            return Collections.emptyList();
        }
        final List<EmailEvent> mapped = new ArrayList<>(this.events.size());
        for (final EventApiResponse event : this.events) {
            mapped.add(event.toEmailEvent());
        }
        return Collections.unmodifiableList(mapped);
    }

    private static OffsetDateTime parse(final String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }

    static final class EventApiResponse {
        @JsonProperty("type")
        public String type;

        @JsonProperty("payload")
        public Map<String, Object> payload;

        @JsonProperty("created_at")
        public String createdAt;

        EmailEvent toEmailEvent() {
            try {
                return new EmailEvent(this.type, this.payload, parse(this.createdAt));
            } catch (final DateTimeParseException exception) {
                throw new SerializationException(
                        "Could not decode an email event timestamp", exception);
            }
        }
    }
}
