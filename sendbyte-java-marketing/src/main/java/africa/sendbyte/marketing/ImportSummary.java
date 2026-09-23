package africa.sendbyte.marketing;

import java.util.List;

/** Summary returned after importing subscribers. */
public final class ImportSummary {
    private final int imported;
    private final int updated;
    private final int invalid;
    private final int skipped;
    private final int overCap;
    private final Integer cap;
    private final List<RejectedSubscriber> rejected;

    ImportSummary(
            final int imported,
            final int updated,
            final int invalid,
            final int skipped,
            final int overCap,
            final Integer cap,
            final List<RejectedSubscriber> rejected) {
        this.imported = imported;
        this.updated = updated;
        this.invalid = invalid;
        this.skipped = skipped;
        this.overCap = overCap;
        this.cap = cap;
        this.rejected = WireSupport.immutable(rejected);
    }

    public int imported() {
        return this.imported;
    }

    public int updated() {
        return this.updated;
    }

    public int invalid() {
        return this.invalid;
    }

    public int skipped() {
        return this.skipped;
    }

    public int overCap() {
        return this.overCap;
    }

    public Integer cap() {
        return this.cap;
    }

    public List<RejectedSubscriber> rejected() {
        return this.rejected;
    }
}
