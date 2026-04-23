package com.nexacro.fullstack.business.domain.user;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserDao dao;

    public UserService(UserDao dao) { this.dao = dao; }

    public NexacroDataset login(String userId, String password) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }
        Map<String, Object> user = dao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Unknown user: " + userId);
        }
        String expected = "stub$" + userId;
        if (!expected.equals(user.get("PASSWORD_HASH"))) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        dao.updateLastLogin(userId);

        // Build single-row output dataset with USER_ID + USER_NAME + ROLE
        NexacroDataset ds = new NexacroDataset();
        ds.setId("output");

        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        List<NexacroDataset.Column> cols = new ArrayList<NexacroDataset.Column>();
        cols.add(new NexacroDataset.Column("USER_ID", "STRING", "32"));
        cols.add(new NexacroDataset.Column("USER_NAME", "STRING", "100"));
        cols.add(new NexacroDataset.Column("ROLE", "STRING", "20"));
        ci.setColumn(cols);
        ds.setColumnInfo(ci);

        Map<String, Object> row = new HashMap<String, Object>();
        row.put("USER_ID", user.get("USER_ID"));
        row.put("USER_NAME", user.get("USER_NAME"));
        row.put("ROLE", user.get("ROLE"));
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(row);
        ds.setRows(rows);

        return ds;
    }
}
