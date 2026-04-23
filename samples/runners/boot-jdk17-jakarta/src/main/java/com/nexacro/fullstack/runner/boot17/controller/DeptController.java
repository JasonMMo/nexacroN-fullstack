package com.nexacro.fullstack.runner.boot17.controller;

import com.nexacro.fullstack.business.domain.dept.DeptService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dept")
public class DeptController extends NexacroController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @PostMapping("/list.do")
    public NexacroEnvelope list() {
        return NexacroResponseBuilder.ok(deptService.listAll());
    }

    @PostMapping("/tree.do")
    public NexacroEnvelope tree() {
        return NexacroResponseBuilder.ok(deptService.tree());
    }
}
