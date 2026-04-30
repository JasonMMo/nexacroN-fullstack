package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.jakarta.core.annotation.ParamVariable;
import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import com.nexacro.uiadapter.service.WideColumnsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * WideColumns 50-column search endpoint.
 *
 * <p>Optional {@code keyId} variable filter; returns {@code ds_wide}.
 */
@Controller
@RequiredArgsConstructor
public class WideController {

    private final WideColumnsService wideColumnsService;

    @RequestMapping("/sampleWideColumns.do")
    public NexacroResult search(
            @ParamVariable(name = "keyId", required = false) String keyId) {
        NexacroResult result = new NexacroResult();
        result.addDataSet("ds_wide", wideColumnsService.selectList(keyId));
        return result;
    }
}
