package africa.sendbyte.email;

import com.fasterxml.jackson.annotation.JsonProperty;

final class TemplateDeletionApiResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("deleted")
    public boolean deleted;

    TemplateDeletion toTemplateDeletion() {
        return new TemplateDeletion(this.id, this.deleted);
    }
}
