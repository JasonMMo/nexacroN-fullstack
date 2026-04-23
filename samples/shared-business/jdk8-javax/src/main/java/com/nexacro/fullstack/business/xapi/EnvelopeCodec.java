package com.nexacro.fullstack.business.xapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * JSON encode/decode for {@link NexacroEnvelope}.
 *
 * <p>Uses a single shared {@link ObjectMapper} instance configured to:
 * <ul>
 *   <li>Ignore unknown JSON properties (forward-compatible with new Nexacro fields)</li>
 *   <li>Not fail on empty beans</li>
 * </ul>
 *
 * <p>All methods are static; this class is never instantiated.
 * Callers that need the same {@code ObjectMapper} for Spring MVC message
 * converters should use {@link #mapper()}.
 */
public final class EnvelopeCodec {

    /** Shared, thread-safe ObjectMapper — configured once at class load. */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private EnvelopeCodec() { /* utility class */ }

    /**
     * Return the shared {@link ObjectMapper} so that runners can register it
     * as a Spring MVC message converter to ensure consistent envelope serde.
     *
     * @return shared, thread-safe ObjectMapper
     */
    public static ObjectMapper mapper() {
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
     * @throws IOException if JSON is malformed or IO fails
     */
    public static NexacroEnvelope decode(InputStream in) throws IOException {
        return MAPPER.readValue(in, NexacroEnvelope.class);
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
     * @throws IOException if serialisation or IO fails
     */
    public static void encode(NexacroEnvelope envelope, OutputStream out) throws IOException {
        MAPPER.writeValue(out, envelope);
    }
}
