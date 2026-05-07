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
 * <p>Client xfdl ({@code pattern04-largeData.xfdl}) declares
 * {@code outData="dsList=ds_largeData"}, so the response dataset is named
 * {@code ds_largeData}. A {@code totalCount} variable backs the client pager.
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
        result.addDataSet("ds_largeData", largeDataService.page(p, ps, category));
        result.addVariable("totalCount", largeDataService.count(category));
        return result;
    }
}
