package africa.sendbyte;

/** Optional observer for sanitized operation diagnostics. */
@FunctionalInterface
public interface RequestObserver {
    RequestObserver NONE = diagnostics -> {};

    void observe(RequestDiagnostics diagnostics);
}
