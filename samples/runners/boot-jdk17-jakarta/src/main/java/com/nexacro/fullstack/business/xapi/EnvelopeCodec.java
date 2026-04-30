package com.nexacro.fullstack.business.xapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * JSON encode/decode for {@link NexacroEnvelope}.
 *
 * <p>Uses a single shared {@link ObjectMapper} instance configured to:
 * <ul>
 *   <li>Ignore unknown JSON properties (forward-compatible with new Nexacro fields)</li>
 *   <li>Not fail on empty beans</li>
 *   <li>NOT write dates as timestamps</li>
 * </ul>
 *
 * <p>All methods are static; this class is never instantiated.
 * Callers that need the same {@code ObjectMapper} for Spring MVC message
 * converters should use {@link #objectMapper()}.
 */
public final class EnvelopeCodec {

    /** Shared, thread-safe ObjectMapper — configured once at class load. */
    private static final ObjectMapper MAPPER = buildMapper();

    private EnvelopeCodec() { /* utility class */ }

    // -----------------------------------------------------------------------
    // ObjectMapper factory
    // -----------------------------------------------------------------------

    private static ObjectMapper buildMapper() {
        return new ObjectMapper()
                // Unknown fields in the JSON stream are silently ignored.
                // This keeps us forward-compatible if Nexacro adds new envelope keys.
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Don't throw on empty POJO (e.g. ColumnInfo with no ConstColumn).
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Return the shared {@link ObjectMapper} so that runners can register it
     * as a Spring MVC message converter to ensure consistent envelope serde.
     *
     * @return shared, thread-safe ObjectMapper
     */
    public static ObjectMapper objectMapper() {
        return MAPPER;
    }

    // -----------------------------------------------------------------------
    // Decode
    // -----------------------------------------------------------------------

    /**
     * Deserialise a Nexacro JSON envelope from an {@link InputStream}.
     *
     * <p>The stream is NOT closed by this method — the caller owns the lifecycle.
     *
     * @param in JSON input stream (UTF-8 or BOM-free UTF-16)
     * @return parsed {@link NexacroEnvelope}; never {@code null}
     * @throws UncheckedIOException if JSON is malformed or IO fails
     */
    public static NexacroEnvelope decode(InputStream in) {
        try {
            return MAPPER.readValue(in, NexacroEnvelope.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to decode Nexacro envelope", e);
        }
    }

    // -----------------------------------------------------------------------
    // Encode
    // -----------------------------------------------------------------------

    /**
     * Serialise a {@link NexacroEnvelope} to an {@link OutputStream} as UTF-8 JSON.
     *
     * <p>The stream is NOT closed or flushed by this method — the caller owns
     * the lifecycle.
     *
     * @param envelope the envelope to serialise; must not be {@code null}
     * @param out      destination stream
     * @throws UncheckedIOException if serialisation or IO fails
     */
    public static void encode(NexacroEnvelope envelope, OutputStream out) {
        try {
            MAPPER.writeValue(out, envelope);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to encode Nexacro envelope", e);
        }
    }

    /**
     * Convenience: serialise an envelope to a UTF-8 JSON byte array.
     *
     * @param envelope the envelope to serialise; must not be {@code null}
     * @return UTF-8 encoded JSON bytes
     * @throws UncheckedIOException if serialisation fails
     */
    public static byte[] encodeToBytes(NexacroEnvelope envelope) {
        try {
            return MAPPER.writeValueAsBytes(envelope);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to encode Nexacro envelope to bytes", e);
        }
    }
}
