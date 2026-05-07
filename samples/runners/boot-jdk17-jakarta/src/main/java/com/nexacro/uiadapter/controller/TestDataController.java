package com.nexacro.uiadapter.controller;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.uiadapter.jakarta.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import com.nexacro.uiadapter.service.TestDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TestData all-types demo endpoints.
 *
 * <p>{@code /sampleTestData.do} loads every column type from TB_TEST_DATA_TYPE.
 * {@code /check_testDataTypeList.do} echoes the inbound dataset back unchanged
 * (column-type round-trip validation).
 */
@Controller
@RequiredArgsConstructor
public class TestDataController {

    private final TestDataService testDataService;

    @RequestMapping("/sampleTestData.do")
    public NexacroResult selectAll() {
        NexacroResult result = new NexacroResult();
        result.addDataSet("ds_testData", testDataService.selectAll());
        return result;
    }

    @RequestMapping("/check_testDataTypeList.do")
    public NexacroResult check(
            @ParamDataSet(name = "ds_list", required = false) DataSet input) {
        NexacroResult result = new NexacroResult();
        if (input != null) {
            result.addDataSet("ds_testData", input);
        }
        return result;
    }
}
