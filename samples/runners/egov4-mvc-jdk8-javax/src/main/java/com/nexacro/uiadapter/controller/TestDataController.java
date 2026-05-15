package com.nexacro.uiadapter.controller;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.DataSetList;
import com.nexacro.java.xapi.data.Variable;
import com.nexacro.java.xapi.data.VariableList;
import com.nexacro.java.xapi.tx.HttpPlatformRequest;
import com.nexacro.java.xapi.tx.HttpPlatformResponse;
import com.nexacro.uiadapter.domain.Board;
import com.nexacro.uiadapter.domain.TestDataType;
import com.nexacro.uiadapter.spring.core.NexacroException;
import com.nexacro.uiadapter.spring.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.spring.core.annotation.ParamVariable;
import com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler;
import com.nexacro.uiadapter.spring.core.data.NexacroResult;
import com.nexacro.uiadapter.service.TestDataService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Data-type sample controller (javax lane).
 *
 * <p>Mirrors the canonical Nexacro N reference at
 * {@code https://gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta/-/blob/master/src/main/java/example/nexacro/uiadapter/web/ExampleDateTypeController.java}
 * (canonical sample is jakarta — javax lane uses {@code com.nexacro.uiadapter.spring.core.*}
 * + {@code javax.servlet.*}, otherwise identical).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code /sampleTestData.do}            — existing aggregate load (kept for compat)</li>
 *   <li>{@code /check_testDataTypeList.do}    — round-trip POJO list, populate
 *       {@code bytesValue} with a random image for blob-type validation</li>
 *   <li>{@code /select_testDataTypeList.do}   — filtered POJO list + image bytes</li>
 *   <li>{@code /select_testDataTypeList_map.do} — filtered Map list</li>
 *   <li>{@code /update_testDataTypeList.do}   — upsert POJO list</li>
 *   <li>{@code /update_testDataTypeList_map.do} — upsert Map list</li>
 *   <li>{@code /checkArgsAnotation.do}        — uiadapter argument-type matrix demo</li>
 * </ul>
 *
 * <p>Image bytes are loaded from the classpath bundle
 * {@code /static/images/{example_7k.jpg, nexacro_5k.png}} (added in the
 * {@code feat/canonical-stream-controller} PR).
 */
@Controller
@RequiredArgsConstructor
public class TestDataController {

    private static final Logger log = LoggerFactory.getLogger(TestDataController.class);

    private final TestDataService testDataService;

    /** Existing aggregate load — kept for compatibility with current xfdl screens. */
    @RequestMapping("/sampleTestData.do")
    public NexacroResult selectAll() {
        NexacroResult result = new NexacroResult();
        result.addDataSet("ds_testData", testDataService.selectAll());
        return result;
    }

    /**
     * Echo the inbound dataset back as {@code output1}, populating
     * {@code bytesValue} with a random image so the client can validate
     * the blob round-trip.
     */
    @RequestMapping("/check_testDataTypeList.do")
    public NexacroResult check_testDataTypeList(
            @ParamDataSet(name = "dsList", required = true) List<TestDataType> dataList) throws NexacroException {

        if (log.isDebugEnabled()) log.debug("check_testDataTypeList size: {}", dataList.size());
        for (TestDataType item : dataList) {
            if (log.isTraceEnabled()) {
                log.trace("stringValue={} intValue={} booleanValue={} longValue={} floatValue={} doubleValue={} bigDecimalValue={} dateValue={} timeValue={} datetimeValue={}",
                        item.getStringValue(), item.getIntValue(), item.getBooleanValue(),
                        item.getLongValue(), item.getFloatValue(), item.getDoubleValue(),
                        item.getBigDecimalValue(), item.getDateValue(),
                        item.getTimeValue(), item.getDatetimeValue());
            }
            item.setBytesValue(getImageBytes());
        }

        NexacroResult result = new NexacroResult();
        result.addDataSet("output1", dataList);
        return result;
    }

    /** Filtered POJO list — image bytes attached to each row. */
    @RequestMapping("/select_testDataTypeList.do")
    public NexacroResult select_testDataTypeList(
            @ParamDataSet(name = "dsSearch", required = false) Map<String, String> searchMap) throws NexacroException {

        List<TestDataType> sampleList = testDataService.select_datalist(searchMap);
        for (TestDataType item : sampleList) {
            if (item != null) item.setBytesValue(getImageBytes());
        }
        NexacroResult result = new NexacroResult();
        result.addDataSet("output1", sampleList);
        return result;
    }

    /** Filtered Map list (column types come from result-set, not POJO). */
    @RequestMapping("/select_testDataTypeList_map.do")
    public NexacroResult select_testDataTypeList_map(
            @ParamDataSet(name = "dsSearch", required = false) Map<String, String> searchMap,
            HttpServletRequest request) throws NexacroException {

        if (log.isDebugEnabled()) log.debug("select_testDataTypeList_map filter: {}", searchMap);
        List<Map<String, Object>> sampleList = testDataService.select_datalist_map(searchMap);
        NexacroResult result = new NexacroResult();
        result.addDataSet("output1", sampleList);
        return result;
    }

    /** Upsert POJO list. id == null → INSERT, else UPDATE. */
    @RequestMapping("/update_testDataTypeList.do")
    public NexacroResult update_testDataTypeList(
            @ParamDataSet(name = "input1") List<TestDataType> dataList) throws NexacroException {

        testDataService.update_datalist(dataList);
        return new NexacroResult();
    }

    /** Upsert Map list. id == null → INSERT, else UPDATE. */
    @RequestMapping("/update_testDataTypeList_map.do")
    public NexacroResult update_testDataTypeList_map(
            @ParamDataSet(name = "input1") List<Map<String, Object>> dataList) throws NexacroException {

        testDataService.update_datalist_map(dataList);
        return new NexacroResult();
    }

    /**
     * Demonstrates every argument type the uiadapter argument-resolver supports.
     * Mirrors canonical {@code /checkArgsAnotation.do}.
     */
    @RequestMapping("/checkArgsAnotation.do")
    public NexacroResult checkArgsAnotation(
            @ParamDataSet(name = "dsUnit") List<Board> unitList,
            @ParamDataSet(name = "dsUnit") List<Map<String, Object>> unitMapList,
            @ParamDataSet(name = "dsUnit") DataSet dsUnit,

            @ParamVariable(name = "intValue") int intValue,
            @ParamVariable(name = "stringValue") String stringValue,
            @ParamVariable(name = "intValue") Variable intVariable,
            @ParamVariable(name = "stringValue") Variable stringVariable,

            DataSetList dataSetList,
            VariableList variableList,
            HttpPlatformRequest httpPlatformRequest,
            HttpPlatformResponse httpPlatformResponse,
            NexacroFirstRowHandler firstRowHandler) {

        if (log.isDebugEnabled()) {
            log.debug("checkArgsAnotation: dsUnit rows={} intValue={} stringValue={}",
                    unitList == null ? -1 : unitList.size(), intValue, stringValue);
        }
        NexacroResult result = new NexacroResult();
        result.addDataSet("dsUnitList", unitList);
        result.addVariable("responseInt", intVariable);
        result.addVariable("responseString", stringVariable);
        return result;
    }

    /**
     * Random pick from {@code /static/images/{example_7k.jpg, nexacro_5k.png}}.
     * Returns an empty array if either image is missing or unreadable —
     * blob round-trip then echoes empty bytes (still type-correct).
     */
    private static final String[] IMAGE_NAMES = { "example_7k.jpg", "nexacro_5k.png" };
    private static final String IMAGE_PATH = "/static/images/";
    private final Random random = new Random();

    public byte[] getImageBytes() {
        String name = IMAGE_NAMES[random.nextInt(IMAGE_NAMES.length)];
        try (InputStream in = getClass().getResourceAsStream(IMAGE_PATH + name)) {
            if (in == null) {
                log.warn("classpath image missing: {}{}", IMAGE_PATH, name);
                return new byte[0];
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error reading image {}{}", IMAGE_PATH, name, e);
            return new byte[0];
        }
    }
}
