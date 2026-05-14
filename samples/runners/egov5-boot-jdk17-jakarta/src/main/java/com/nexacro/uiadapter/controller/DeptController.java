package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import com.nexacro.uiadapter.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Dept endpoints.
 *
 * <p>{@code /update_deptlist_map.do} returns the flat list (legacy spec #6
 * naming retained).
 * Routes have no {@code /uiadapter} prefix — the servlet context-path adds it.
 */
@Controller
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @RequestMapping("/update_deptlist_map.do")
    public NexacroResult deptList() {
        NexacroResult result = new NexacroResult();
        result.addDataSet("output1", deptService.selectList());
        return result;
    }
}
