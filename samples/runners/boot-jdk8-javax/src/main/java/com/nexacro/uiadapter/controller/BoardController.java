package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.spring.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.spring.core.annotation.ParamVariable;
import com.nexacro.uiadapter.spring.core.data.NexacroResult;
import com.nexacro.uiadapter.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Board endpoints (GitLab canonical uiadapter pattern, javax lane).
 */
@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /** Single-row read; client xfdl declares {@code outData="dsList=output1"}. */
    @RequestMapping("/select_data_single.do")
    public NexacroResult selectDataSingle(
            @ParamDataSet(name = "dsSearch", required = false) Map<String, String> dsSearch) {
        NexacroResult result = new NexacroResult();
        Map<String, Object> board = boardService.selectDataSingle(dsSearch);
        result.addDataSet("output1", board == null ? Collections.<Map<String, Object>>emptyList()
                : Collections.<Map<String, Object>>singletonList(board));
        return result;
    }

    /** List read; client xfdl declares {@code outData="dsList=output1"}. */
    @RequestMapping("/select_datalist.do")
    public NexacroResult selectDataList(
            @ParamDataSet(name = "ds_search", required = false) Board search) {
        NexacroResult result = new NexacroResult();
        result.addDataSet("output1", boardService.selectList(search));
        return result;
    }

    /**
     * Mutating CRUD — input dataset's {@code _RowType_} drives I / U / D
     * dispatch. Returns affected row count via {@code affectedRows} variable.
     */
    @RequestMapping("/update_datalist_map.do")
    public NexacroResult updateDataListMap(
            @ParamDataSet(name = "input1") List<Map<String, Object>> input1) {
        int affected = boardService.updateDatalistMap(input1);
        NexacroResult result = new NexacroResult();
        result.addVariable("affectedRows", affected);
        return result;
    }

    @RequestMapping("/select_datalist_map.do")
    public NexacroResult selectDatalistMap(
            @ParamDataSet(name = "dsSearch", required = false) Map<String, String> dsSearch) {
        NexacroResult result = new NexacroResult();
        result.addDataSet("output1", boardService.selectDatalistMap(dsSearch));
        return result;
    }

    @RequestMapping("/update_datalist.do")
    public NexacroResult updateDatalist(
            @ParamDataSet(name = "input1") List<Board> input1) {
        int affected = boardService.updateDatalist(input1);
        NexacroResult result = new NexacroResult();
        result.addVariable("affectedRows", affected);
        return result;
    }

    @RequestMapping("/test.do")
    public NexacroResult test(
            @ParamVariable(name = "message", required = false) String message) {
        NexacroResult result = new NexacroResult();
        result.addVariable("echo", message == null ? "ok" : message);
        return result;
    }
}
