package com.nexacro.fullstack.business.domain.dept;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeptService {

    private final DeptDao deptDao;

    public DeptService(DeptDao deptDao) {
        this.deptDao = deptDao;
    }

    /** Returns a flat list of all enabled departments ordered by LEVEL_NO, SORT_ORDER. */
    public NexacroDataset listAll() {
        List<Map<String, Object>> rows = deptDao.listAll();
        NexacroDataset ds = new NexacroDataset();
        ds.setId("dept");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        ci.setColumns(List.of(
            new NexacroDataset.Column("DEPT_ID",    "string", "20"),
            new NexacroDataset.Column("DEPT_NAME",  "string", "100"),
            new NexacroDataset.Column("PARENT_ID",  "string", "20"),
            new NexacroDataset.Column("SORT_ORDER", "int",    "10"),
            new NexacroDataset.Column("LEVEL_NO",   "int",    "10")
        ));
        ds.setColumnInfo(ci);
        ds.setRows(rows == null ? List.of() : rows);
        return ds;
    }

    /** Returns the department hierarchy via recursive CTE, ordered by PATH. */
    public NexacroDataset tree() {
        List<Map<String, Object>> rows = deptDao.tree();
        NexacroDataset ds = new NexacroDataset();
        ds.setId("deptTree");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        ci.setColumns(List.of(
            new NexacroDataset.Column("DEPT_ID",    "string", "20"),
            new NexacroDataset.Column("DEPT_NAME",  "string", "100"),
            new NexacroDataset.Column("PARENT_ID",  "string", "20"),
            new NexacroDataset.Column("SORT_ORDER", "int",    "10"),
            new NexacroDataset.Column("LEVEL_NO",   "int",    "10"),
            new NexacroDataset.Column("DEPTH",      "int",    "10"),
            new NexacroDataset.Column("PATH",       "string", "500")
        ));
        ds.setColumnInfo(ci);
        ds.setRows(rows == null ? List.of() : rows);
        return ds;
    }
}
