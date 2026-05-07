package com.nexacro.uiadapter.service.impl;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.mapper.BoardMapper;
import com.nexacro.uiadapter.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default {@link BoardService} backed by {@link BoardMapper}.
 *
 * <p>{@link #processRows(DataSet)} dispatches per-row using the Nexacro
 * {@code _RowType_} flag (see {@link DataSet#ROW_TYPE_INSERTED} et al.):
 * INSERTED → insert, UPDATED → update, DELETED/REMOVED → softDelete.
 * NORMAL rows are skipped — they represent unchanged client state.
 *
 * <p>DataSet column names follow the canonical TB_BOARD schema:
 * POST_ID / TITLE / CONTENTS / REG_ID / COMMUNITY_ID / HIDDEN_INFO /
 * HIT_COUNT / IS_NOTICE.
 */
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;

    @Override
    public List<Board> selectList(Board search) {
        return boardMapper.selectList(search);
    }

    @Override
    public Board selectById(Integer postId) {
        return boardMapper.selectById(postId);
    }

    @Override
    @Transactional
    public int processRows(DataSet input) {
        if (input == null || input.getRowCount() == 0) {
            return 0;
        }
        int affected = 0;
        int rowCount = input.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int rowType = input.getRowType(i);
            switch (rowType) {
                case DataSet.ROW_TYPE_INSERTED:
                    affected += boardMapper.insert(toBoard(input, i));
                    break;
                case DataSet.ROW_TYPE_UPDATED:
                    affected += boardMapper.update(toBoard(input, i));
                    break;
                case DataSet.ROW_TYPE_DELETED:
                    affected += boardMapper.softDelete(input.getInt(i, "POST_ID"));
                    break;
                default:
                    break;
            }
        }
        return affected;
    }

    private Board toBoard(DataSet ds, int row) {
        Board b = new Board();
        if (ds.getColumn("POST_ID") != null && ds.getInt(row, "POST_ID") != 0) {
            b.setPostId(ds.getInt(row, "POST_ID"));
        }
        b.setTitle(ds.getString(row, "TITLE"));
        b.setContents(ds.getString(row, "CONTENTS"));
        b.setRegId(ds.getString(row, "REG_ID"));
        b.setCommunityId(ds.getString(row, "COMMUNITY_ID"));
        b.setHiddenInfo(ds.getString(row, "HIDDEN_INFO"));
        if (ds.getColumn("HIT_COUNT") != null) {
            b.setHitCount(ds.getInt(row, "HIT_COUNT"));
        }
        if (ds.getColumn("IS_NOTICE") != null) {
            b.setIsNotice(Boolean.valueOf(ds.getString(row, "IS_NOTICE")));
        }
        return b;
    }
}
