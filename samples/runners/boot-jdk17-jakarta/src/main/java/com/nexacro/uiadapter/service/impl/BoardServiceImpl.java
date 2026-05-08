package com.nexacro.uiadapter.service.impl;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.DataSetRowTypeAccessor;
import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.mapper.BoardMapper;
import com.nexacro.uiadapter.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Default {@link BoardService} backed by {@link BoardMapper}.
 *
 * <p>Supports both POJO-mode and Map-mode payloads so canonical Nexacro UI
 * adapters can share request contracts across Jakarta and Javax lanes.
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
    public Map<String, Object> selectDataSingle(Map<String, String> dsSearch) {
        return boardMapper.selectDataSingle(dsSearch);
    }

    @Override
    public List<Map<String, Object>> selectDatalistMap(Map<String, String> dsSearch) {
        return boardMapper.selectDatalistMap(dsSearch);
    }

    @Override
    @Transactional
    public int updateDatalist(List<Board> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int affected = 0;
        for (Board b : rows) {
            int rowType = (b instanceof DataSetRowTypeAccessor)
                    ? ((DataSetRowTypeAccessor) b).getRowType()
                    : DataSet.ROW_TYPE_NORMAL;
            switch (rowType) {
                case DataSet.ROW_TYPE_INSERTED:
                    affected += boardMapper.insertBoard(b);
                    break;
                case DataSet.ROW_TYPE_UPDATED:
                    affected += boardMapper.updateBoard(b);
                    break;
                case DataSet.ROW_TYPE_DELETED:
                    affected += boardMapper.deleteBoard(b.getPostId());
                    break;
                default:
                    break;
            }
        }
        return affected;
    }

    @Override
    @Transactional
    public int updateDatalistMap(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int affected = 0;
        for (Map<String, Object> row : rows) {
            Object rowTypeObj = row.get(DataSetRowTypeAccessor.NAME);
            int rowType = (rowTypeObj instanceof Integer)
                    ? (Integer) rowTypeObj
                    : DataSet.ROW_TYPE_NORMAL;
            switch (rowType) {
                case DataSet.ROW_TYPE_INSERTED:
                    affected += boardMapper.insertBoardMap(row);
                    break;
                case DataSet.ROW_TYPE_UPDATED:
                    affected += boardMapper.updateBoardMap(row);
                    break;
                case DataSet.ROW_TYPE_DELETED:
                    affected += boardMapper.deleteBoardMap(row);
                    break;
                default:
                    break;
            }
        }
        return affected;
    }
}
