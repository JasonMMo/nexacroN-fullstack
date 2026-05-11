package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler;

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

    /**
     * Streaming list read. Rows are flushed to the client in fixed-size
     * chunks via {@link NexacroFirstRowHandler#sendDataSet} rather than
     * accumulated in a single {@code List<Board>}. Used by the canonical
     * {@code /select_datalist_firstrow.do} endpoint.
     *
     * @param search filter criteria (same shape as {@link #selectList(Board)})
     * @param firstRowHandler partial-response handler obtained from
     *        {@code NexacroContextHolder.getNexacroContext().getFirstRowHandler()}
     * @param chunkSize rows per flush (canonical default = 100)
     * @param dataSetName DataSet name on the wire (matches client xfdl
     *        {@code outData="dsList=output1"})
     */
    void selectListFirstRow(Board search,
                            NexacroFirstRowHandler firstRowHandler,
                            int chunkSize,
                            String dataSetName);

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
