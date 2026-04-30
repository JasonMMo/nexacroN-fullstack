package com.nexacro.fullstack.business.domain.dept;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DeptService {

    private final DeptDao dao;

    public DeptService(DeptDao dao) { this.dao = dao; }

    public NexacroDataset listAll() {
        return buildDataset("dept", dao.listAll(), false);
    }

    public NexacroDataset tree() {
        return buildDataset("deptTree", dao.tree(), true);
    }

    private NexacroDataset buildDataset(String id, List<Map<String, Object>> rows, boolean includePath) {
        NexacroDataset ds = new NexacroDataset();
        ds.setId(id);
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        List<NexacroDataset.Column> cols = new ArrayList<NexacroDataset.Column>();
        cols.add(new NexacroDataset.Column("DEPT_ID", "STRING", "20"));
        cols.add(new NexacroDataset.Column("DEPT_NAME", "STRING", "100"));
        cols.add(new NexacroDataset.Column("PARENT_ID", "STRING", "20"));
        cols.add(new NexacroDataset.Column("LEVEL_NO", "INT", "10"));
        cols.add(new NexacroDataset.Column("SORT_ORDER", "INT", "10"));
        if (includePath) cols.add(new NexacroDataset.Column("PATH", "STRING", "500"));
        ci.setColumns(cols);
        ds.setColumnInfo(ci);
        ds.setRows(rows != null ? rows : new ArrayList<Map<String, Object>>());
        return ds;
    }
}
