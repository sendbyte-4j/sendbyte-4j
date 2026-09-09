package africa.sendbyte.exception;

/** Base class for failures produced by the SendByte SDK. */
public class SendByteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SendByteException(final String message) {
        super(message);
    }

    public SendByteException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
