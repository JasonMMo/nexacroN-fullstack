package com.nexacro.fullstack.business.xapi;

/**
 * Nexacro {@code _RowType_} flag that every Dataset row carries.
 *
 * <ul>
 *   <li>{@code N} — Normal / unchanged: ignore for DML</li>
 *   <li>{@code I} — Inserted on the client: server should INSERT</li>
 *   <li>{@code U} — Updated on the client: server should UPDATE</li>
 *   <li>{@code D} — Deleted on the client: server should DELETE</li>
 *   <li>{@code O} — Original snapshot of a U/D row (pre-image for optimistic lock)</li>
 * </ul>
 *
 * @see <a href="https://docs.tobesoft.com/advanced_development_guide_nexacro_n_v24_ko/c0e662b5844feb1b">
 *     Nexacro SSV format reference</a>
 */
public enum RowType {
    N, I, U, D, O;

    /**
     * Parse a single-character string into a {@link RowType}.
     * Returns {@link #O} for {@code null}, empty, or unrecognised values.
     *
     * @param value the raw {@code _RowType_} value from the wire (e.g. {@code "I"})
     * @return the matching enum constant, or {@link #O} if unrecognised
     */
    public static RowType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return O;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return O;
        }
    }
}
