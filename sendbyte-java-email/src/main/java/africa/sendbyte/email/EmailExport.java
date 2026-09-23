package africa.sendbyte.email;

import africa.sendbyte.ResponseMetadata;

/** CSV email export bytes and response metadata. */
public final class EmailExport {
    private final byte[] content;
    private final ResponseMetadata metadata;

    EmailExport(final byte[] content, final ResponseMetadata metadata) {
        this.content = content == null ? new byte[0] : content.clone();
        this.metadata = metadata;
    }

    public byte[] content() {
        return content.clone();
    }

    public ResponseMetadata metadata() {
        return this.metadata;
    }

    public String contentDisposition() {
        return metadata.firstHeader("content-disposition");
    }
}
