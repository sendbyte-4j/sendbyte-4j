package africa.sendbyte.exception;

import africa.sendbyte.RequestDiagnostics;

/** Request encoding or response decoding failed. */
public final class SerializationException extends SendByteException {
    private static final long serialVersionUID = 1L;

    public SerializationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SerializationException(
            final String message, final Throwable cause, final RequestDiagnostics diagnostics) {
        super(message, cause, diagnostics);
    }
}
