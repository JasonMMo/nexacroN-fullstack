package com.nexacro.fullstack.business.domain.board;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.RowType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        ds.setRows(rows == null ? List.of() : rows);
        return ds;
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
