package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.spring.core.data.NexacroResult;
import com.nexacro.uiadapter.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @RequestMapping("/dept_tree.do")
    public NexacroResult deptTree() {
        NexacroResult result = new NexacroResult();
        result.addDataSet("output1", deptService.selectTree());
        return result;
    }
}
