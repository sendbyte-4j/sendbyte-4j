package africa.sendbyte.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class SendEmailApiRequest {
    @JsonProperty("from")
    private final String from;

    @JsonProperty("to")
    private final List<String> to;

    @JsonProperty("subject")
    private final String subject;

    @JsonProperty("html")
    private final String html;

    @JsonProperty("text")
    private final String text;

    @JsonProperty("template_id")
    private final String templateId;

    @JsonProperty("variables")
    private final Map<String, Object> variables;

    @JsonProperty("cc")
    private final List<String> cc;

    @JsonProperty("bcc")
    private final List<String> bcc;

    @JsonProperty("reply_to")
    private final List<String> replyTo;

    @JsonProperty("attachments")
    private final List<AttachmentApiRequest> attachments;

    @JsonProperty("tags")
    private final List<String> tags;

    @JsonProperty("headers")
    private final Map<String, String> headers;

    @JsonProperty("list_unsubscribe")
    private final ListUnsubscribeApiRequest listUnsubscribe;

    @JsonProperty("scheduled_at")
    private final String scheduledAt;

    @JsonProperty("idempotency_key")
    private final String idempotencyKey;

    @JsonProperty("tracking")
    private final TrackingApiRequest tracking;

    private SendEmailApiRequest(final SendEmailRequest request) {
        this.from = request.getFrom();
        this.to = request.getTo();
        this.subject = request.getSubject();
        this.html = request.getHtml();
        this.text = request.getText();
        this.templateId = request.getTemplateId();
        this.variables = request.getVariables();
        this.cc = request.getCc();
        this.bcc = request.getBcc();
        this.replyTo = request.getReplyTo();
        this.attachments = mapAttachments(request.getAttachments());
        this.tags = request.getTags();
        this.headers = request.getHeaders();
        this.listUnsubscribe = mapListUnsubscribe(request.getListUnsubscribe());
        this.scheduledAt = request.getScheduledAt();
        this.idempotencyKey = request.getIdempotencyKey();
        this.tracking = mapTracking(request.getTracking());
    }

    static SendEmailApiRequest from(final SendEmailRequest request) {
        return new SendEmailApiRequest(request);
    }

    private static List<AttachmentApiRequest> mapAttachments(final List<Attachment> attachments) {
        if (attachments == null) {
            return null;
        }

        final List<AttachmentApiRequest> wireAttachments = new ArrayList<>(attachments.size());
        for (final Attachment attachment : attachments) {
            wireAttachments.add(new AttachmentApiRequest(attachment));
        }
        return Collections.unmodifiableList(wireAttachments);
    }

    private static ListUnsubscribeApiRequest mapListUnsubscribe(
            final ListUnsubscribe listUnsubscribe) {
        return listUnsubscribe == null ? null : new ListUnsubscribeApiRequest(listUnsubscribe);
    }

    private static TrackingApiRequest mapTracking(final TrackingOptions tracking) {
        return tracking == null ? null : new TrackingApiRequest(tracking);
    }

    private static final class AttachmentApiRequest {
        @JsonProperty("filename")
        private final String filename;

        @JsonProperty("content")
        private final String content;

        @JsonProperty("content_type")
        private final String contentType;

        @JsonProperty("content_id")
        private final String contentId;

        @JsonProperty("disposition")
        private final String disposition;

        private AttachmentApiRequest(final Attachment attachment) {
            this.filename = attachment.getFilename();
            this.content = attachment.getContent();
            this.contentType = attachment.getContentType();
            this.contentId = attachment.getContentId();
            this.disposition = attachment.getDisposition();
        }
    }

    private static final class ListUnsubscribeApiRequest {
        @JsonProperty("url")
        private final String url;

        @JsonProperty("mailto")
        private final String mailto;

        private ListUnsubscribeApiRequest(final ListUnsubscribe listUnsubscribe) {
            this.url = listUnsubscribe.getUrl();
            this.mailto = listUnsubscribe.getMailto();
        }
    }

    private static final class TrackingApiRequest {
        @JsonProperty("opens")
        private final Boolean opens;

        @JsonProperty("clicks")
        private final Boolean clicks;

        private TrackingApiRequest(final TrackingOptions tracking) {
            this.opens = tracking.getOpens();
            this.clicks = tracking.getClicks();
        }
    }
}
