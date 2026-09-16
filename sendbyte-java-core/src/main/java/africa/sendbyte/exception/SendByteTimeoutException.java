package africa.sendbyte.exception;

import africa.sendbyte.RequestDiagnostics;

/** The SDK request exhausted its configured total deadline. */
public final class SendByteTimeoutException extends SendByteException {
    private static final long serialVersionUID = 1L;

    public SendByteTimeoutException(final String message, final RequestDiagnostics diagnostics) {
        super(message, diagnostics);
    }

    public SendByteTimeoutException(
            final String message, final Throwable cause, final RequestDiagnostics diagnostics) {
        super(message, cause, diagnostics);
    }
}
