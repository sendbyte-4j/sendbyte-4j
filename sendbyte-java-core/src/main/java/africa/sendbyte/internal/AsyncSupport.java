package africa.sendbyte.internal;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Helpers for deriving an asynchronous result from a runtime operation.
 *
 * <p>A plain {@code thenApply} would break cancellation: cancelling the derived future does not
 * cancel its source, so the transport call and any scheduled retry would keep running for a result
 * nobody is waiting for. Product modules map through this helper instead so that cancellation
 * always reaches the shared request pipeline.
 *
 * <p>This type is internal SDK plumbing: it is not a stable application API and may change between
 * SDK versions.
 */
public final class AsyncSupport {
    private AsyncSupport() {}

    /**
     * Maps a completed operation to another value while keeping cancellation propagation.
     *
     * <p>The mapper runs on the thread that completed {@code source}, which is the runtime executor
     * for operations that complete asynchronously.
     */
    public static <T, R> CompletableFuture<R> map(
            final CompletableFuture<T> source, final Function<T, R> mapper) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(mapper, "mapper");

        final CompletableFuture<R> result = new CompletableFuture<>();
        source.whenComplete(
                (value, failure) -> {
                    if (failure != null) {
                        result.completeExceptionally(unwrap(failure));
                    } else {
                        try {
                            result.complete(mapper.apply(value));
                        } catch (final RuntimeException mappingFailure) {
                            result.completeExceptionally(mappingFailure);
                        }
                    }
                });
        result.whenComplete(
                (ignored, failure) -> {
                    if (result.isCancelled()) {
                        source.cancel(true);
                    }
                });
        return result;
    }

    /**
     * Strips the {@link CompletionException} and {@link ExecutionException} wrappers added by the
     * JDK.
     */
    public static Throwable unwrap(final Throwable failure) {
        Throwable current = failure;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
