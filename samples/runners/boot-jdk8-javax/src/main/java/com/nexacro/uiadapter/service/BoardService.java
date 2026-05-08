package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.Board;

import java.util.List;
import java.util.Map;

/**
 * Board domain service contract.
 *
 * <p>Read endpoints map straight onto {@link #selectList(Board)} /
 * {@link #selectById(Integer)}; mutation endpoints accept either POJO lists or
 * Map-mode payloads so per-row {@code _RowType_} dispatch (I/U/D/N/O) can be
 * preserved end-to-end.
 */
public interface BoardService {

    /** All boards, optionally filtered by search criteria. */
    List<Board> selectList(Board search);

    /** Single board by primary key (TB_BOARD.POST_ID). */
    Board selectById(Integer postId);

    /** Single row lookup when client asks for canonical Map-mode response. */
    Map<String, Object> selectDataSingle(Map<String, String> dsSearch);

    /** Map-mode list read for canonical endpoint compatibility. */
    List<Map<String, Object>> selectDatalistMap(Map<String, String> dsSearch);

    /** POJO-mode bulk mutation driven by {@code _RowType_} per entry. */
    int updateDatalist(List<Board> input1);

    /** Map-mode bulk mutation driven by {@code _RowType_} per entry. */
    int updateDatalistMap(List<Map<String, Object>> input1);
}
