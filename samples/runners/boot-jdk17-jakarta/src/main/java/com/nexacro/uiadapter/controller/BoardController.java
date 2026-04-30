package com.nexacro.uiadapter.controller;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.jakarta.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.jakarta.core.annotation.ParamVariable;
import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import com.nexacro.uiadapter.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Board endpoints (GitLab canonical uiadapter pattern).
 *
 * <p>Routes are mapped <b>without</b> the {@code /uiadapter} prefix; the
 * Spring servlet context-path supplies it (see {@code application.yml} —
 * {@code server.servlet.context-path: /uiadapter}). The
 * {@link com.nexacro.uiadapter.jakarta.core.resolve.NexacroRequestMappingHandlerAdapter}
 * registered in {@link com.nexacro.uiadapter.config.UiadapterWebMvcConfig}
 * unwraps the inbound Nexacro envelope into the
 * {@link ParamDataSet} / {@link ParamVariable} arguments and frames the
 * returned {@link NexacroResult} on the way out.
 */
@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /** Single-row read; client expects {@code ds_board} with one row. */
    @RequestMapping("/select_data_single.do")
    public NexacroResult selectDataSingle(
            @ParamVariable(name = "boardId", required = false) Integer boardId) {
        NexacroResult result = new NexacroResult();
        Board board = (boardId == null) ? null : boardService.selectById(boardId);
        result.addDataSet("ds_board", board == null ? List.of() : List.of(board));
        return result;
    }

    /** List read; client expects {@code ds_board} populated. */
    @RequestMapping("/select_datalist.do")
    public NexacroResult selectDataList(
            @ParamDataSet(name = "ds_search", required = false) Board search) {
        NexacroResult result = new NexacroResult();
        result.addDataSet("ds_board", boardService.selectList(search));
        return result;
    }

    /**
     * Mutating CRUD — input dataset's {@code _RowType_} drives I / U / D
     * dispatch. Returns affected row count via {@code affectedRows} variable.
     */
    @RequestMapping("/update_datalist_map.do")
    public NexacroResult updateDataListMap(
            @ParamDataSet(name = "ds_board") DataSet input) {
        int affected = boardService.processRows(input);
        NexacroResult result = new NexacroResult();
        result.addVariable("affectedRows", affected);
        return result;
    }
}
