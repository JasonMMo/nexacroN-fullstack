package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.jakarta.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.jakarta.core.annotation.ParamVariable;
import com.nexacro.uiadapter.jakarta.core.context.NexacroContext;
import com.nexacro.uiadapter.jakarta.core.context.NexacroContextHolder;
import com.nexacro.uiadapter.jakarta.core.data.NexacroFirstRowHandler;
import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import com.nexacro.uiadapter.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

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

    /** Single-row read; client xfdl declares {@code outData="dsList=output1"}. */
    @RequestMapping("/select_data_single.do")
    public NexacroResult selectDataSingle(
            @ParamDataSet(name = "dsSearch", required = false) Map<String, String> dsSearch) {
        NexacroResult result = new NexacroResult();
        Map<String, Object> board = boardService.selectDataSingle(dsSearch);
        result.addDataSet("output1", board == null ? List.of() : List.of(board));
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

    /**
     * Canonical streaming list read. Rows are chunked via uiadapter's
     * {@code MybatisRowHandler} and pushed through
     * {@link NexacroFirstRowHandler#sendDataSet} as soon as each chunk
     * fills — the response body is committed before the full result set
     * is materialised, matching the GitLab canonical
     * {@code /select_datalist_firstrow.do} endpoint.
     */
    @RequestMapping("/select_datalist_firstrow.do")
    public void selectDataListFirstRow(
            @ParamDataSet(name = "ds_search", required = false) Board search,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        NexacroContext ctx = NexacroContextHolder.getNexacroContext(request, response);
        NexacroFirstRowHandler firstRowHandler = ctx.getFirstRowHandler();
        boardService.selectListFirstRow(
                search == null ? new Board() : search,
                firstRowHandler,
                100,
                "output1");
    }

    @RequestMapping("/test.do")
    public NexacroResult test(
            @ParamVariable(name = "message", required = false) String message) {
        NexacroResult result = new NexacroResult();
        result.addVariable("echo", message == null ? "ok" : message);
        return result;
    }
}
