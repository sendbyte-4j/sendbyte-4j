package africa.sendbyte.exception;

/** Request encoding or response decoding failed. */
public final class SerializationException extends SendByteException {
    private static final long serialVersionUID = 1L;

    public SerializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
