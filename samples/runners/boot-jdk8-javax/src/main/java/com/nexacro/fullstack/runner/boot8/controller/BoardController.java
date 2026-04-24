package com.nexacro.fullstack.runner.boot8.controller;

import com.nexacro.fullstack.business.domain.board.BoardService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
public class BoardController extends NexacroController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping("/select")
    public NexacroEnvelope select(@RequestBody(required = false) NexacroEnvelope req) {
        return NexacroResponseBuilder.ok(boardService.selectAll());
    }

    @PostMapping({"/insert", "/update", "/delete"})
    public NexacroEnvelope mutate(@RequestBody NexacroEnvelope req) {
        NexacroDataset input = datasetById(req, "input");
        int affected = boardService.processRows(input);
        NexacroEnvelope out = NexacroResponseBuilder.ok();
        out.getParameters().add(new NexacroEnvelope.Parameter("affectedRows", affected, "int"));
        return out;
    }
}
