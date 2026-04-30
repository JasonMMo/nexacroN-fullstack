package com.nexacro.fullstack.business.xapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a single Nexacro Dataset inside a {@link NexacroEnvelope}.
 *
 * <p>Wire JSON shape (abridged):
 * <pre>{@code
 * {
 *   "id": "indata",
 *   "ColumnInfo": {
 *     "ConstColumn": [{"id":"market","type":"STRING","size":"10","value":"kse"}],
 *     "Column":      [{"id":"stockCode","type":"STRING","size":"5"}]
 *   },
 *   "Rows": [
 *     {"_RowType_":"I","stockCode":"10001"}
 *   ]
 * }
 * }</pre>
 *
 * <p>Rows are {@code List<Map<String,Object>>} so the special {@code _RowType_}
 * key fits naturally alongside regular column values. Use {@link RowType#fromString}
 * to interpret the {@code _RowType_} entry from each row map.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NexacroDataset {

    /** Dataset identifier matching the {@code id} attribute in the wire format. */
    private String id;

    /**
     * Column metadata container.  Jackson maps the capital-case "ColumnInfo"
     * key from the wire format automatically via this nested class.
     */
    @JsonProperty("ColumnInfo")
    private ColumnInfo columnInfo = new ColumnInfo();

    /**
     * Data rows.  Each map entry corresponds to one column value; the special
     * key {@code "_RowType_"} holds the {@link RowType} flag as a {@code String}.
     */
    @JsonProperty("Rows")
    private List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

    // -----------------------------------------------------------------------
    // Nested: ColumnInfo container
    // -----------------------------------------------------------------------

    /**
     * Container for {@link ConstColumn} and {@link Column} metadata.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnInfo {

        @JsonProperty("ConstColumn")
        private List<ConstColumn> constColumns = new ArrayList<ConstColumn>();

        @JsonProperty("Column")
        private List<Column> columns = new ArrayList<Column>();
    }

    // -----------------------------------------------------------------------
    // Nested: ConstColumn
    // -----------------------------------------------------------------------

    /**
     * A column whose value is constant across all rows (shipped once in
     * {@code ColumnInfo} rather than repeated in every row map).
     *
     * <p>The {@code value} field is {@code Object} because the Nexacro wire
     * format allows mixed types: numeric literals (no quotes) for INT/DECIMAL,
     * string literals for STRING.  Jackson deserialises these as
     * {@code Integer}/{@code Double}/{@code String} respectively.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConstColumn {
        private String id;
        private String type;
        private String size;
        /** Mixed-type constant value — may be String, Integer, Double, etc. */
        private Object value;
    }

    // -----------------------------------------------------------------------
    // Nested: Column
    // -----------------------------------------------------------------------

    /**
     * A regular variable-value column descriptor.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Column {
        private String id;
        private String type;
        private String size;
    }
}
