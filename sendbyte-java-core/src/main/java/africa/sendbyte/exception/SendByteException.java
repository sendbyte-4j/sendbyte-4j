package africa.sendbyte.exception;

import africa.sendbyte.RequestDiagnostics;

/** Base class for failures produced by the SendByte SDK. */
public class SendByteException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final RequestDiagnostics diagnostics;

    public SendByteException(final String message) {
        super(message);
        this.diagnostics = null;
    }

    public SendByteException(final String message, final Throwable cause) {
        super(message, cause);
        this.diagnostics = null;
    }

    public SendByteException(final String message, final RequestDiagnostics diagnostics) {
        super(message);
        this.diagnostics = diagnostics;
    }

    public SendByteException(
            final String message, final Throwable cause, final RequestDiagnostics diagnostics) {
        super(message, cause);
        this.diagnostics = diagnostics;
    }

    public RequestDiagnostics diagnostics() {
        return this.diagnostics;
    }
}
