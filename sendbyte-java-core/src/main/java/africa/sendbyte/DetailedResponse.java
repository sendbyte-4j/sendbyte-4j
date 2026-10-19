package africa.sendbyte;

import java.util.Objects;

/** A typed SDK value plus portable response metadata and request diagnostics. */
public final class DetailedResponse<T> {
    private final T value;
    private final ResponseMetadata metadata;
    private final RequestDiagnostics diagnostics;

    public DetailedResponse(
            final T value, final ResponseMetadata metadata, final RequestDiagnostics diagnostics) {
        this.value = value;
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
    }

    public T value() {
        return value;
    }

    public ResponseMetadata metadata() {
        return metadata;
    }

    public RequestDiagnostics diagnostics() {
        return diagnostics;
    }
}
