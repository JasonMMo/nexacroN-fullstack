package com.nexacro.fullstack.business.xapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level Nexacro JSON envelope.
 *
 * <p>Wire JSON shape:
 * <pre>{@code
 * {
 *   "version": "1.0",
 *   "Parameters": [
 *     {"id":"ErrorCode","value":0},
 *     {"id":"ErrorMsg","value":""},
 *     {"id":"param1","value":"0","type":"string"}
 *   ],
 *   "Datasets": [
 *     { ... NexacroDataset ... }
 *   ]
 * }
 * }</pre>
 *
 * <p>The Nexacro protocol mandates capital-case keys ({@code Parameters},
 * {@code Datasets}) which are mapped via {@link JsonProperty}.
 *
 * <p>Error convention: on success set {@code ErrorCode=0, ErrorMsg=""};
 * on error set {@code ErrorCode=-1, ErrorMsg="<message>"}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NexacroEnvelope {

    /** Protocol version — always {@code "1.0"} for current Nexacro N. */
    private String version = "1.0";

    @JsonProperty("Parameters")
    private List<Parameter> parameters = new ArrayList<Parameter>();

    @JsonProperty("Datasets")
    private List<NexacroDataset> datasets = new ArrayList<NexacroDataset>();

    // -----------------------------------------------------------------------
    // Convenience constructors
    // -----------------------------------------------------------------------

    /** Construct an envelope with only parameters (e.g. for error responses). */
    public NexacroEnvelope(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    // -----------------------------------------------------------------------
    // Lookup helpers
    // -----------------------------------------------------------------------

    /**
     * Find a dataset by its {@code id} field.
     *
     * @param id dataset identifier
     * @return the first matching dataset, or {@code null} if not found
     */
    public NexacroDataset dataset(String id) {
        if (id == null || datasets == null) return null;
        for (NexacroDataset d : datasets) {
            if (id.equals(d.getId())) {
                return d;
            }
        }
        return null;
    }

    /**
     * Find a parameter value by its {@code id} field.
     *
     * @param id parameter identifier
     * @return the raw value object, or {@code null} if not found
     */
    public Object parameterValue(String id) {
        if (id == null || parameters == null) return null;
        for (Parameter p : parameters) {
            if (id.equals(p.getId())) {
                return p.getValue();
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Nested: Parameter
    // -----------------------------------------------------------------------

    /**
     * A single key/value parameter in the {@code Parameters} array.
     *
     * <p>The {@code value} field is {@code Object} to accommodate mixed types
     * (numeric error codes as integers, string messages, etc.) without losing
     * type fidelity during round-trips.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String id;
        /** Mixed-type value — Integer, String, Double, Boolean, etc. */
        private Object value;
        /** Optional Nexacro type hint (e.g. {@code "string"}). May be null. */
        private String type;

        /** Convenience constructor when type hint is not needed. */
        public Parameter(String id, Object value) {
            this.id = id;
            this.value = value;
        }
    }
}
