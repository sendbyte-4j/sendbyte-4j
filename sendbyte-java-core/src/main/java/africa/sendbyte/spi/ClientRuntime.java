package africa.sendbyte.spi;

import java.util.concurrent.CompletableFuture;

/** Shared execution contract consumed by SendByte product modules. */
public interface ClientRuntime {
    <T> T execute(final ClientRequest request, final Class<T> responseType);

    <T> CompletableFuture<T> executeAsync(final ClientRequest request, final Class<T> responseType);
}
