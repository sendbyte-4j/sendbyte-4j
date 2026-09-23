package africa.sendbyte.email;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Event recorded for a retrieved email. */
public final class EmailEvent {
    private final String type;
    private final Map<String, Object> payload;
    private final OffsetDateTime createdAt;

    EmailEvent(
            final String type, final Map<String, Object> payload, final OffsetDateTime createdAt) {
        this.type = type;
        this.payload =
                payload == null
                        ? Collections.emptyMap()
                        : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
        this.createdAt = createdAt;
    }

    public String type() {
        return this.type;
    }

    public Map<String, Object> payload() {
        return this.payload;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }
}
