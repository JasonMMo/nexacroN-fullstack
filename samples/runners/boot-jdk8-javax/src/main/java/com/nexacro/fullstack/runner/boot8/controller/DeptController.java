package com.nexacro.fullstack.runner.boot8.controller;

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

    // spec #6: dept list/tree fetch
    @PostMapping("/uiadapter/update_deptlist_map.do")
    public NexacroEnvelope updateDeptlistMap() {
        return NexacroResponseBuilder.ok(deptService.listAll());
    }

    // Private helper — kept for internal use; not in spec §2.
    private NexacroEnvelope treeHelper() {
        return NexacroResponseBuilder.ok(deptService.tree());
    }
}
