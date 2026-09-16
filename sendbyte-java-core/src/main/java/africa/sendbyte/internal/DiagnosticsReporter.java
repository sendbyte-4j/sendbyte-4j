package africa.sendbyte.internal;

import africa.sendbyte.FailureCategory;
import africa.sendbyte.RequestDiagnostics;
import africa.sendbyte.RequestObserver;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.transport.TransportResponse;

final class DiagnosticsReporter {
    private final Ticker ticker;
    private final RequestObserver observer;

    DiagnosticsReporter(final Ticker ticker, final RequestObserver observer) {
        this.ticker = ticker;
        this.observer = observer;
    }

    RequestDiagnostics create(
            final ClientRequest request,
            final ExecutionState state,
            final Integer statusCode,
            final String requestId,
            final FailureCategory category,
            final boolean retryable) {
        return RequestDiagnostics.builder(request.operation())
                .requestId(requestId)
                .statusCode(statusCode)
                .failureCategory(category)
                .totalDuration(state.totalDuration(this.ticker))
                .attemptCount(Math.max(1, state.attemptCount()))
                .retryDelay(state.retryDelay())
                .retryable(retryable)
                .build();
    }

    RequestDiagnostics create(
            final ClientRequest request,
            final ExecutionState state,
            final TransportResponse response,
            final FailureCategory category,
            final boolean retryable) {
        return this.create(
                request,
                state,
                response == null ? null : response.statusCode(),
                response == null ? null : response.firstHeader("x-request-id"),
                category,
                retryable);
    }

    void observe(final RequestDiagnostics diagnostics) {
        try {
            this.observer.observe(diagnostics);
        } catch (final RuntimeException ignored) {
            // Observers are side-channel diagnostics and cannot affect API outcomes.
        }
    }
}
