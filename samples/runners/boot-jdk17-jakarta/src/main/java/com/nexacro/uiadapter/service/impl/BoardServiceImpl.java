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
    public Board selectById(Integer boardId) {
        return boardMapper.selectById(boardId);
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
                    // ROW_TYPE_REMOVED shares the same int value as ROW_TYPE_DELETED in this xapi build,
                    // so a single case covers both client-side delete states.
                    affected += boardMapper.softDelete(input.getInt(i, "BOARD_ID"));
                    break;
                default:
                    // ROW_TYPE_NORMAL — unchanged, skip
                    break;
            }
        }
        return affected;
    }

    private Board toBoard(DataSet ds, int row) {
        Board b = new Board();
        b.setBoardId(ds.getColumn("BOARD_ID") != null && ds.getInt(row, "BOARD_ID") != 0
                ? Integer.valueOf(ds.getInt(row, "BOARD_ID")) : null);
        b.setTitle(ds.getString(row, "TITLE"));
        b.setContent(ds.getString(row, "CONTENT"));
        b.setAuthorId(ds.getString(row, "AUTHOR_ID"));
        if (ds.getColumn("VIEW_COUNT") != null) {
            b.setViewCount(ds.getInt(row, "VIEW_COUNT"));
        }
        return b;
    }
}
