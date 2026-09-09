package africa.sendbyte.internal;

import africa.sendbyte.exception.SerializationException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

final class JacksonJsonCodec {
    private final ObjectMapper mapper =
            JsonMapper.builder()
                    .defaultPropertyInclusion(
                            JsonInclude.Value.construct(
                                    JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS))
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .build();

    byte[] write(final Object value) {
        try {
            return this.mapper.writeValueAsBytes(value);
        } catch (final Exception exception) {
            throw new SerializationException(
                    "Could not encode the SendByte request body", exception);
        }
    }

    <T> T read(final byte[] value, final Class<T> type) {
        try {
            return this.mapper.readValue(value, type);
        } catch (final Exception exception) {
            throw new SerializationException(
                    "Could not decode the SendByte response body", exception);
        }
    }
}
