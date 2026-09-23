package africa.sendbyte.email;

/** Result returned after deleting a saved template. */
public final class TemplateDeletion {
    private final String id;
    private final boolean deleted;

    TemplateDeletion(final String id, final boolean deleted) {
        this.id = id;
        this.deleted = deleted;
    }

    public String id() {
        return this.id;
    }

    public boolean deleted() {
        return this.deleted;
    }
}
