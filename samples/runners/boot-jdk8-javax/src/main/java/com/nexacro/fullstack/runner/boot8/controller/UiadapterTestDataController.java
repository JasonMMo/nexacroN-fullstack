package com.nexacro.fullstack.runner.boot8.controller;

import com.nexacro.fullstack.business.domain.testdata.TestDataService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroDataset;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Uiadapter test-data-type controller — contract endpoints #10 select, #11 check.
 *
 * Ported from <boot-jdk17-jakarta-legacy>/<ExampleDateTypeController.java>@e49a17791d on 2026-04-24.
 * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
 * Extends shared {@link NexacroController}; legacy NexacroResult/VO returns replaced by
 * {@link NexacroEnvelope} + {@link NexacroDataset} shim; delegates to {@link TestDataService}
 * backed by MyBatis @Mapper DAO.
 */
@RestController
@RequestMapping("/uiadapter")
public class UiadapterTestDataController extends NexacroController {

    private final TestDataService testDataService;

    public UiadapterTestDataController(TestDataService testDataService) {
        this.testDataService = testDataService;
    }

    /**
     * Endpoint #10 — Load the test-data-type list (exercises every Nexacro column type).
     *
     * Ported from boot-jdk17-jakarta-legacy/.../ExampleDateTypeController.select_testDataTypeList@e49a17791d on 2026-04-24.
     * Adaptations: DB-backed selectAll replaces legacy in-memory builder; response wrapped via
     * {@link NexacroResponseBuilder}.
     */
    // spec #12: test-data type list (renamed from select_testDataTypeList.do)
    @PostMapping("/sampleTestData.do")
    public NexacroEnvelope select(@RequestBody(required = false) NexacroEnvelope req) {
        return NexacroResponseBuilder.ok(testDataService.selectAll());
    }

    /**
     * Endpoint #11 — Echo/check a test-data-type list (round-trip column-type validation).
     *
     * Ported from boot-jdk17-jakarta-legacy/.../ExampleDateTypeController.check_testDataTypeList@e49a17791d on 2026-04-24.
     * Adaptations: legacy per-field logging dropped in favor of simple rewrap via
     * {@link TestDataService#echo(NexacroDataset)}; dsList lookup uses shared {@code datasetById}.
     */
    @PostMapping("/check_testDataTypeList.do")
    public NexacroEnvelope check(@RequestBody NexacroEnvelope req) {
        NexacroDataset input = datasetById(req, "dsList");
        return NexacroResponseBuilder.ok(testDataService.echo(input));
    }
}
