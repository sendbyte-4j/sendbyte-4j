package africa.sendbyte.exception;

import africa.sendbyte.RequestDiagnostics;

/** The SDK operation was canceled. */
public final class SendByteCancellationException extends SendByteException {
    private static final long serialVersionUID = 1L;

    public SendByteCancellationException(
            final String message, final RequestDiagnostics diagnostics) {
        super(message, diagnostics);
    }
}
