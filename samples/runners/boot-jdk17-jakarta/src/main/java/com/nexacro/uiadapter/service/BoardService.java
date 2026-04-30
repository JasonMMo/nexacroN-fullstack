package com.nexacro.uiadapter.service;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.uiadapter.domain.Board;

import java.util.List;

/**
 * Board domain service contract.
 *
 * <p>Read endpoints map straight onto {@link #selectList(Board)} /
 * {@link #selectById(Integer)}; mutation endpoints accept a Nexacro
 * {@link DataSet} so per-row {@code _RowType_} dispatch (I/U/D/N/O) can be
 * preserved end-to-end.
 */
public interface BoardService {

    /** All active boards, optionally filtered by search criteria. */
    List<Board> selectList(Board search);

    /** Single board by primary key. */
    Board selectById(Integer boardId);

    /** Process a Nexacro DataSet by per-row RowType; returns affected count. */
    int processRows(DataSet input);
}
