package com.nexacro.fullstack.runner.boot17.controller;

import com.nexacro.fullstack.business.domain.board.BoardService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BoardController extends NexacroController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // spec #3: single-row select, spec #4: list select — same handler, both paths bound
    @PostMapping({"/uiadapter/select_data_single.do", "/uiadapter/select_datalist.do"})
    public NexacroEnvelope select(@RequestBody(required = false) NexacroEnvelope req) {
        return NexacroResponseBuilder.ok(boardService.selectAll());
    }

    // spec #5: insert/update/delete via _RowType_ flag
    @PostMapping("/uiadapter/update_datalist_map.do")
    public NexacroEnvelope mutate(@RequestBody NexacroEnvelope req) {
        NexacroDataset input = datasetById(req, "input");
        int affected = boardService.processRows(input);
        NexacroEnvelope out = NexacroResponseBuilder.ok();
        out.getParameters().add(new NexacroEnvelope.Parameter("affectedRows", affected, "int"));
        return out;
    }
}
