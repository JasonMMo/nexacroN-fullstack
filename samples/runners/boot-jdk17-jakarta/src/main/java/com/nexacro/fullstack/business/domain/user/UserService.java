package com.nexacro.fullstack.business.domain.user;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Stub login: compares PASSWORD_HASH to "stub$" + userId (seed-data convention).
     * Returns a single-row dataset (id="output") with USER_ID, USER_NAME, ROLE.
     * Throws IllegalArgumentException on failure (caller wraps via NexacroResponseBuilder).
     */
    public NexacroDataset login(String userId, String password) {
        if (userId == null || password == null) {
            throw new IllegalArgumentException("userId/password required");
        }

        Map<String, Object> row = userDao.findById(userId);
        if (row == null) {
            throw new IllegalArgumentException("user not found");
        }

        Object stored = row.get("PASSWORD_HASH");
        String expected = "stub$" + userId;
        if (stored == null || !expected.equals(stored.toString())) {
            throw new IllegalArgumentException("invalid credentials");
        }

        userDao.updateLastLogin(userId);

        // Build output dataset
        NexacroDataset ds = new NexacroDataset();
        ds.setId("output");

        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        ci.setColumns(List.of(
            new NexacroDataset.Column("USER_ID", "string", "32"),
            new NexacroDataset.Column("USER_NAME", "string", "100"),
            new NexacroDataset.Column("ROLE", "string", "20")
        ));
        ds.setColumnInfo(ci);

        Map<String, Object> outRow = new LinkedHashMap<>();
        outRow.put("USER_ID", row.get("USER_ID"));
        outRow.put("USER_NAME", row.get("USER_NAME"));
        outRow.put("ROLE", row.get("ROLE"));
        ds.setRows(List.of(outRow));

        return ds;
    }
}
