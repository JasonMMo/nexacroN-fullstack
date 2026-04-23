package com.nexacro.fullstack.runner.boot17.controller;

import com.nexacro.fullstack.business.domain.large.LargeDataService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/large")
public class LargeController extends NexacroController {

    private final LargeDataService largeService;

    public LargeController(LargeDataService largeService) {
        this.largeService = largeService;
    }

    @PostMapping("/page.do")
    public NexacroEnvelope page(@RequestBody(required = false) NexacroEnvelope req) {
        int pageNum  = asInt(parameterById(req, "page"),     1);
        int pageSize = asInt(parameterById(req, "pageSize"), 50);
        String category = asString(parameterById(req, "category"));
        NexacroDataset data = largeService.page(pageNum, pageSize, category);
        int total = largeService.count(category);
        NexacroEnvelope out = NexacroResponseBuilder.ok(data);
        out.getParameters().add(new NexacroEnvelope.Parameter("totalCount", total, "int"));
        return out;
    }

    private static int asInt(Object v, int def) {
        if (v == null) return def;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return def; }
    }

    private static String asString(Object v) {
        return v == null ? null : v.toString();
    }
}
