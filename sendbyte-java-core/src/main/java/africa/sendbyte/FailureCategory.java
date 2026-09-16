package africa.sendbyte;

/** Stable, low-cardinality failure categories used by diagnostics and exceptions. */
public enum FailureCategory {
    API,
    CANCELLATION,
    CONNECTION,
    OBSERVER,
    RESPONSE_TOO_LARGE,
    SERIALIZATION,
    TIMEOUT,
    UNKNOWN
}
