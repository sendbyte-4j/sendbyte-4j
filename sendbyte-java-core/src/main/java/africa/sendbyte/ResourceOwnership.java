package africa.sendbyte;

/** Defines whether a client closes an injected resource when the client closes. */
public enum ResourceOwnership {
    CLIENT,
    CALLER
}
