package africa.sendbyte.email;

/** Rendered subject and bodies for inline or saved template previews. */
public final class RenderedTemplate {
    private final String subject;
    private final String html;
    private final String text;

    RenderedTemplate(final String subject, final String html, final String text) {
        this.subject = subject;
        this.html = html;
        this.text = text;
    }

    public String subject() {
        return this.subject;
    }

    public String html() {
        return this.html;
    }

    public String text() {
        return this.text;
    }
}
