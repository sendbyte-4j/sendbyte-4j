package africa.sendbyte.webhooks;

/** Replay request accepted by the Webhooks API. */
public final class WebhookReplay {
    private final String id;
    private final String replayOf;
    private final String status;

    WebhookReplay(final String id, final String replayOf, final String status) {
        this.id = id;
        this.replayOf = replayOf;
        this.status = status;
    }

    public String id() {
        return this.id;
    }

    public String replayOf() {
        return this.replayOf;
    }

    public String status() {
        return this.status;
    }
}
