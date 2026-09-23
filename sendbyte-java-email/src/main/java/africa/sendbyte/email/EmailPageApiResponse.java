package africa.sendbyte.email;

import africa.sendbyte.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class EmailPageApiResponse {
    @JsonProperty("data")
    public List<EmailApiResponse> data;

    @JsonProperty("has_more")
    public boolean hasMore;

    Page<Email> toPage() {
        if (this.data == null) {
            return new Page<>(Collections.emptyList(), this.hasMore);
        }
        final List<Email> emails = new ArrayList<>(this.data.size());
        for (final EmailApiResponse email : this.data) {
            emails.add(email.toEmail());
        }
        return new Page<>(emails, this.hasMore);
    }
}
