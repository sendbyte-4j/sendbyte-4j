package africa.sendbyte.internal;

import africa.sendbyte.DetailedResponse;
import africa.sendbyte.FailureCategory;
import africa.sendbyte.RequestDiagnostics;
import africa.sendbyte.ResponseMetadata;
import africa.sendbyte.exception.ResponseTooLargeException;
import africa.sendbyte.spi.ClientRequest;
import africa.sendbyte.transport.TransportResponse;

final class ResponseProcessor {
    private final JacksonJsonCodec json;
    private final long maxResponseBodyBytes;
    private final DiagnosticsReporter diagnosticsReporter;

    ResponseProcessor(
            final JacksonJsonCodec json,
            final long maxResponseBodyBytes,
            final DiagnosticsReporter diagnosticsReporter) {
        this.json = json;
        this.maxResponseBodyBytes = maxResponseBodyBytes;
        this.diagnosticsReporter = diagnosticsReporter;
    }

    <T> DetailedResponse<T> success(
            final ClientRequest request,
            final TransportResponse response,
            final Class<T> responseType,
            final ExecutionState state) {
        this.enforceBodyLimit(request, response, state);
        final T value = this.decode(response, responseType);
        final ResponseMetadata metadata =
                new ResponseMetadata(response.statusCode(), response.headers());
        final RequestDiagnostics diagnostics =
                this.diagnosticsReporter.create(
                        request, state, response.statusCode(), metadata.requestId(), null, false);
        this.diagnosticsReporter.observe(diagnostics);
        return new DetailedResponse<>(value, metadata, diagnostics);
    }

    void enforceBodyLimit(
            final ClientRequest request,
            final TransportResponse response,
            final ExecutionState state) {
        if (response.body().length <= this.maxResponseBodyBytes) {
            return;
        }

        final RequestDiagnostics diagnostics =
                this.diagnosticsReporter.create(
                        request,
                        state,
                        response.statusCode(),
                        response.firstHeader("x-request-id"),
                        FailureCategory.RESPONSE_TOO_LARGE,
                        false);
        this.diagnosticsReporter.observe(diagnostics);
        throw new ResponseTooLargeException(
                "The SendByte API response body exceeded the configured SDK buffer limit",
                diagnostics);
    }

    private <T> T decode(final TransportResponse response, final Class<T> responseType) {
        if (response.body().length == 0 || responseType == Void.class) {
            return null;
        }
        if (responseType == byte[].class) {
            return responseType.cast(response.body());
        }
        return json.read(response.body(), responseType);
    }
}
