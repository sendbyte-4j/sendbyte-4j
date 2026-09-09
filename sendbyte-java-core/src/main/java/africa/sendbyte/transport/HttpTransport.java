package africa.sendbyte.transport;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/** SendByte-owned HTTP extension point. */
public interface HttpTransport extends AutoCloseable {
    TransportResponse execute(final TransportRequest request)
            throws IOException, InterruptedException;

    CompletableFuture<TransportResponse> executeAsync(final TransportRequest request);

    @Override
    default void close() {}
}
