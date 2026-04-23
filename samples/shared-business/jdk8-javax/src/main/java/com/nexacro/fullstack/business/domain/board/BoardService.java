package com.nexacro.fullstack.business.domain.board;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.RowType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoardService {

    private final BoardDao dao;

    public BoardService(BoardDao dao) { this.dao = dao; }

    /**
     * Returns a NexacroDataset named "board" with all non-deleted rows,
     * with CLOB values normalised to String so Jackson can serialise them.
     */
    public NexacroDataset selectAll() {
        List<Map<String, Object>> raw = dao.selectAll();
        List<Map<String, Object>> normalised = normalizeClobRows(raw);

        NexacroDataset ds = new NexacroDataset();
        ds.setId("board");

        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        List<NexacroDataset.Column> cols = new ArrayList<NexacroDataset.Column>();
        cols.add(new NexacroDataset.Column("BOARD_ID", "INT", "10"));
        cols.add(new NexacroDataset.Column("TITLE", "STRING", "200"));
        cols.add(new NexacroDataset.Column("CONTENT", "STRING", "4000"));
        cols.add(new NexacroDataset.Column("AUTHOR_ID", "STRING", "32"));
        cols.add(new NexacroDataset.Column("VIEW_COUNT", "INT", "10"));
        cols.add(new NexacroDataset.Column("CREATED_AT", "DATETIME", "19"));
        cols.add(new NexacroDataset.Column("UPDATED_AT", "DATETIME", "19"));
        ci.setColumn(cols);
        ds.setColumnInfo(ci);

        ds.setRows(normalised);
        return ds;
    }

    /**
     * Dispatch rows by _RowType_. Returns number of affected rows.
     * Uses classic switch + break — REQUIRED for Java 8 target (no arrow expressions).
     */
    @Transactional
    public int processRows(NexacroDataset input) {
        if (input == null || input.getRows() == null) return 0;
        int affected = 0;
        for (Map<String, Object> row : input.getRows()) {
            Object rtVal = row.get("_RowType_");
            RowType rt = RowType.fromString(rtVal == null ? null : String.valueOf(rtVal));
            switch (rt) {
                case I:
                    affected += dao.insert(row);
                    break;
                case U:
                    affected += dao.update(row);
                    break;
                case D: {
                    Object idObj = row.get("BOARD_ID");
                    int id;
                    if (idObj instanceof Number) {
                        id = ((Number) idObj).intValue();
                    } else if (idObj != null) {
                        id = Integer.parseInt(String.valueOf(idObj));
                    } else {
                        break; // skip, no id
                    }
                    affected += dao.softDelete(id);
                    break;
                }
                case N:
                case O:
                default:
                    // skip — new placeholder / original / unknown
                    break;
            }
        }
        return affected;
    }

    /**
     * Walk rows and convert any java.sql.Clob value to its String contents.
     * HSQL returns JDBCClobClient instances for CLOB columns — Jackson cannot
     * serialise those. Mutates a copy of each row (does not touch originals).
     */
    private List<Map<String, Object>> normalizeClobRows(List<Map<String, Object>> rows) {
        if (rows == null) return new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> copy = new HashMap<String, Object>(row);
            for (Map.Entry<String, Object> e : row.entrySet()) {
                Object v = e.getValue();
                if (v instanceof Clob) {
                    Clob c = (Clob) v;
                    copy.put(e.getKey(), clobToString(c));
                }
            }
            out.add(copy);
        }
        return out;
    }

    private String clobToString(Clob clob) {
        Reader reader = null;
        try {
            reader = clob.getCharacterStream();
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        } catch (SQLException | IOException ex) {
            throw new RuntimeException("Failed to read CLOB", ex);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) { /* no-op */ }
            }
        }
    }
}
