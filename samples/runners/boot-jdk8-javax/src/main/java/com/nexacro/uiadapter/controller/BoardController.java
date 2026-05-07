package com.nexacro.uiadapter.controller;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.spring.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.spring.core.annotation.ParamVariable;
import com.nexacro.uiadapter.spring.core.data.NexacroResult;
import com.nexacro.uiadapter.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

/**
 * Board endpoints (GitLab canonical uiadapter pattern, javax lane).
 */
@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @RequestMapping("/select_data_single.do")
    public NexacroResult selectDataSingle(
            @ParamVariable(name = "boardId", required = false) Integer boardId) {
        NexacroResult result = new NexacroResult();
        Board board = (boardId == null) ? null : boardService.selectById(boardId);
        result.addDataSet("ds_board", board == null ? Collections.emptyList() : Collections.singletonList(board));
        return result;
    }

    @RequestMapping("/select_datalist.do")
    public NexacroResult selectDataList(
            @ParamDataSet(name = "ds_search", required = false) Board search) {
        NexacroResult result = new NexacroResult();
        result.addDataSet("ds_board", boardService.selectList(search));
        return result;
    }

    @RequestMapping("/update_datalist_map.do")
    public NexacroResult updateDataListMap(
            @ParamDataSet(name = "ds_board") DataSet input) {
        int affected = boardService.processRows(input);
        NexacroResult result = new NexacroResult();
        result.addVariable("affectedRows", affected);
        return result;
    }
}
