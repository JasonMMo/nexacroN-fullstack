package com.nexacro.fullstack.runner.boot17.controller;

import com.nexacro.fullstack.business.domain.dept.DeptService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeptController extends NexacroController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    // spec #6: dept list/tree fetch (strategy a: repurposed from /dept/list.do).
    // Returns flat dept list; /dept/tree.do logic retained as private helper below.
    @PostMapping("/uiadapter/update_deptlist_map.do")
    public NexacroEnvelope updateDeptlistMap() {
        return NexacroResponseBuilder.ok(deptService.listAll());
    }

    // Private helper — kept for internal use; not in spec §2.
    // TODO Plan8: /dept/tree.do (tree fetch) is not in spec §2 — flag for Opus
    private NexacroEnvelope treeHelper() {
        return NexacroResponseBuilder.ok(deptService.tree());
    }
}
