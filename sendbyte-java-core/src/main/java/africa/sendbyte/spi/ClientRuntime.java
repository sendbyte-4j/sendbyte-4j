package africa.sendbyte.spi;

import africa.sendbyte.DetailedResponse;
import java.util.concurrent.CompletableFuture;

/** Shared execution contract consumed by SendByte product modules. */
public interface ClientRuntime {
    <T> T execute(final ClientRequest request, final Class<T> responseType);

    <T> DetailedResponse<T> executeDetailed(
            final ClientRequest request, final Class<T> responseType);

    /**
     * Executes asynchronously using the SDK runtime executor. Dependent stages registered without
     * an explicit executor may run on that documented SDK runtime executor when the returned future
     * is completed.
     */
    <T> CompletableFuture<T> executeAsync(final ClientRequest request, final Class<T> responseType);

    /**
     * Executes asynchronously using the SDK runtime executor and returns detailed metadata.
     * Dependent stages registered without an explicit executor may run on that documented SDK
     * runtime executor when the returned future is completed.
     */
    <T> CompletableFuture<DetailedResponse<T>> executeDetailedAsync(
            final ClientRequest request, final Class<T> responseType);
}
