package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.jakarta.core.annotation.ParamVariable;
import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import com.nexacro.uiadapter.service.LargeDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Large-data paging endpoint.
 *
 * <p>Returns {@code ds_large} populated with the requested page and
 * a {@code totalCount} variable for the client's pager.
 */
@Controller
@RequiredArgsConstructor
public class LargeController {

    private final LargeDataService largeDataService;

    @RequestMapping("/sampleLargeData.do")
    public NexacroResult page(
            @ParamVariable(name = "page", required = false) Integer page,
            @ParamVariable(name = "pageSize", required = false) Integer pageSize,
            @ParamVariable(name = "category", required = false) String category) {
        int p = (page == null) ? 1 : page;
        int ps = (pageSize == null) ? 50 : pageSize;
        NexacroResult result = new NexacroResult();
        result.addDataSet("ds_large", largeDataService.page(p, ps, category));
        result.addVariable("totalCount", largeDataService.count(category));
        return result;
    }
}
