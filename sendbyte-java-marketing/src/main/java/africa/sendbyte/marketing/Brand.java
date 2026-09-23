package africa.sendbyte.marketing;

import java.time.OffsetDateTime;

/** Marketing brand metadata. */
public final class Brand {
    private final String id;
    private final String name;
    private final String publicName;
    private final String logoUrl;
    private final String fromAddress;
    private final String postalAddress;
    private final String timezone;
    private final OffsetDateTime createdAt;

    Brand(
            final String id,
            final String name,
            final String publicName,
            final String logoUrl,
            final String fromAddress,
            final String postalAddress,
            final String timezone,
            final OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.publicName = publicName;
        this.logoUrl = logoUrl;
        this.fromAddress = fromAddress;
        this.postalAddress = postalAddress;
        this.timezone = timezone;
        this.createdAt = createdAt;
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String publicName() {
        return this.publicName;
    }

    public String logoUrl() {
        return this.logoUrl;
    }

    public String fromAddress() {
        return this.fromAddress;
    }

    public String postalAddress() {
        return this.postalAddress;
    }

    public String timezone() {
        return this.timezone;
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }
}
