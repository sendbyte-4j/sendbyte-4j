package africa.sendbyte.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Open/closed state shared by a client and its runtime, plus the asynchronous operations that must
 * be failed when the client closes.
 *
 * <p>Closing publishes the closed flag before sweeping in-flight operations, so an operation racing
 * with close either observes the closed client and fails immediately or is registered before the
 * sweep and failed by it.
 */
public final class ClientLifecycle {
    private static final String CLOSED_MESSAGE = "SendByteClient is closed";

    private final AtomicBoolean closed = new AtomicBoolean();
    private final Set<AsyncOperation<?>> inFlight =
            Collections.newSetFromMap(new ConcurrentHashMap<AsyncOperation<?>, Boolean>());

    public boolean isClosed() {
        return this.closed.get();
    }

    public void ensureOpen() {
        if (this.closed.get()) {
            throw new IllegalStateException(CLOSED_MESSAGE);
        }
    }

    <T> AsyncOperation<T> register(final AsyncOperation<T> operation) {
        this.inFlight.add(operation);
        if (this.closed.get() && this.inFlight.remove(operation)) {
            operation.abort(new IllegalStateException(CLOSED_MESSAGE));
        }
        return operation;
    }

    void deregister(final AsyncOperation<?> operation) {
        this.inFlight.remove(operation);
    }

    /**
     * Fails every in-flight asynchronous operation so that no caller is left waiting on a future
     * that can never complete.
     *
     * @return {@code true} when this call performed the open-to-closed transition
     */
    public boolean shutdown() {
        if (!this.closed.compareAndSet(false, true)) {
            return false;
        }
        for (final AsyncOperation<?> operation : this.inFlight) {
            operation.abort(new IllegalStateException(CLOSED_MESSAGE));
        }
        this.inFlight.clear();
        return true;
    }
}
