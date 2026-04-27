package com.nexacro.fullstack.runner.boot8.controller;

import com.nexacro.fullstack.business.domain.wide.WideColumnsService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Uiadapter board-search controller — contract endpoint #14 (wide-columns search).
 *
 * Ported from <mvc-jdk8-javax-legacy>/<UidapterBoardController.java>@69cdaf1a9d on 2026-04-24.
 * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
 * Extends shared {@link NexacroController}; NexacroResult/addDataSet replaced by
 * {@link NexacroEnvelope} + {@link NexacroResponseBuilder}; delegates to {@link WideColumnsService}
 * backed by MyBatis @Mapper DAO.
 */
@RestController
@RequestMapping("/uiadapter")
public class UiadapterBoardController extends NexacroController {

    private final WideColumnsService wideColumnsService;

    public UiadapterBoardController(WideColumnsService wideColumnsService) {
        this.wideColumnsService = wideColumnsService;
    }

    /**
     * Endpoint #14 — Wide-columns search.
     *
     * Ported from mvc-jdk8-javax-legacy/src/main/java/com/nexacro/example/web/UidapterBoardController.search_manyColumn_data@69cdaf1a9d on 2026-04-24.
     * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
     * NexacroResult/addDataSet replaced by NexacroEnvelope + NexacroResponseBuilder;
     * dsSearch.KEY_ID optional filter instead of legacy count PlatformVariable + row-seeding;
     * uses MyBatis @Mapper DAO.
     */
    @PostMapping("/search_manyColumn_data.do")
    public NexacroEnvelope searchManyColumn(@RequestBody(required = false) NexacroEnvelope req) {
        String keyId = null;
        NexacroDataset dsSearch = datasetById(req, "dsSearch");
        if (dsSearch != null && dsSearch.getRows() != null && !dsSearch.getRows().isEmpty()) {
            Map<String, Object> row = dsSearch.getRows().get(0);
            Object v = row.get("KEY_ID");
            if (v != null) {
                String s = v.toString();
                if (!s.isEmpty()) keyId = s;
            }
        }
        NexacroDataset out = wideColumnsService.search(keyId);
        return NexacroResponseBuilder.ok(out);
    }
}
