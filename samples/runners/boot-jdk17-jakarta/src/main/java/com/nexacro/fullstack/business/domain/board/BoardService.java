package com.nexacro.fullstack.business.domain.board;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.RowType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoardService {

    private final BoardDao boardDao;

    public BoardService(BoardDao boardDao) {
        this.boardDao = boardDao;
    }

    /** Build output dataset with all active board rows. */
    public NexacroDataset selectAll() {
        List<Map<String, Object>> rows = boardDao.selectAll();
        NexacroDataset ds = new NexacroDataset();
        ds.setId("board");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        ci.setColumns(List.of(
            new NexacroDataset.Column("BOARD_ID",   "int",      "10"),
            new NexacroDataset.Column("TITLE",      "string",   "200"),
            new NexacroDataset.Column("CONTENT",    "string",   "4000"),
            new NexacroDataset.Column("AUTHOR_ID",  "string",   "32"),
            new NexacroDataset.Column("VIEW_COUNT", "int",      "10"),
            new NexacroDataset.Column("CREATED_AT", "datetime", "20"),
            new NexacroDataset.Column("UPDATED_AT", "datetime", "20")
        ));
        ds.setColumnInfo(ci);
        ds.setRows(rows == null ? List.of() : normalizeClobRows(rows));
        return ds;
    }

    /**
     * HSQLDB returns CLOB columns as {@code java.sql.Clob} objects which
     * Jackson cannot serialize. Convert any Clob values to String.
     * Builds a new map per row to avoid ConcurrentModificationException.
     */
    private List<Map<String, Object>> normalizeClobRows(List<Map<String, Object>> rows) {
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> normalized = new HashMap<>(row.size());
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Object val = entry.getValue();
                if (val instanceof Clob clob) {
                    try {
                        long len = clob.length();
                        val = (len == 0) ? "" : clob.getSubString(1, (int) len);
                    } catch (SQLException e) {
                        val = "";
                    }
                }
                normalized.put(entry.getKey(), val);
            }
            result.add(normalized);
        }
        return result;
    }

    /**
     * Iterates the input dataset's rows and dispatches by _RowType_:
     *   N or O → skip (new-blank or original-unchanged)
     *   I → insert
     *   U → update
     *   D → softDelete (uses BOARD_ID from the row map)
     * Returns count of rows actually affected (sum of DAO return values).
     */
    @Transactional
    public int processRows(NexacroDataset input) {
        if (input == null || input.getRows() == null) return 0;
        int affected = 0;
        for (Map<String, Object> row : input.getRows()) {
            Object typeRaw = row.get("_RowType_");
            RowType rt = RowType.fromString(typeRaw == null ? null : typeRaw.toString());
            switch (rt) {
                case I -> affected += boardDao.insert(row);
                case U -> affected += boardDao.update(row);
                case D -> {
                    Object id = row.get("BOARD_ID");
                    if (id != null) {
                        Integer boardId = id instanceof Integer ? (Integer) id : Integer.valueOf(id.toString());
                        affected += boardDao.softDelete(boardId);
                    }
                }
                case N, O -> { /* skip */ }
            }
        }
        return affected;
    }
}
