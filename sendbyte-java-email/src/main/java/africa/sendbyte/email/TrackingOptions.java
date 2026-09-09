package africa.sendbyte.email;

/** Per-message open and click tracking overrides. */
public final class TrackingOptions {
    private final Boolean opens;
    private final Boolean clicks;

    private TrackingOptions(final Boolean opens, final Boolean clicks) {
        if (opens == null && clicks == null) {
            throw new IllegalArgumentException("At least one tracking option must be specified");
        }
        this.opens = opens;
        this.clicks = clicks;
    }

    public static TrackingOptions opens(final boolean enabled) {
        return new TrackingOptions(enabled, null);
    }

    public static TrackingOptions clicks(final boolean enabled) {
        return new TrackingOptions(null, enabled);
    }

    public static TrackingOptions of(final Boolean opens, final Boolean clicks) {
        return new TrackingOptions(opens, clicks);
    }

    public Boolean getOpens() {
        return this.opens;
    }

    public Boolean getClicks() {
        return this.clicks;
    }
}
