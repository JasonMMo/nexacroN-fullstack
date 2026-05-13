package com.nexacro.uiadapter.controller;

import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.uiadapter.service.LargeDataService;
import com.nexacro.uiadapter.spring.core.annotation.ParamVariable;
import com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler;
import com.nexacro.uiadapter.spring.core.data.NexacroResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Large-data streaming endpoint (javax lane).
 *
 * <p>Mirrors the canonical Nexacro N reference at
 * {@code https://gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta/-/blob/master/src/main/java/example/nexacro/uiadapter/web/LargeDataController.java}
 * (canonical sample is jakarta — javax lane uses {@code com.nexacro.uiadapter.spring.core.*}).
 *
 * <p>Client xfdl ({@code pattern04-largeData.xfdl}) declares
 * {@code outData="dsList=ds_largeData"}, so the response dataset is named
 * {@code ds_largeData}. The service streams chunks via SSV ContentType.
 *
 * <p>Inbound variables (both optional):
 * <ul>
 *   <li>{@code firstRowCount} — rows in the first chunk (default 1000)</li>
 *   <li>{@code chunkSize}     — rows in each subsequent chunk (default 1000)</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
public class LargeController {

    private static final Logger log = LoggerFactory.getLogger(LargeController.class);

    private final LargeDataService largeDataService;

    @RequestMapping("/sampleLargeData.do")
    public NexacroResult mybatisLargeData(
            NexacroFirstRowHandler firstRowHandler,
            @ParamVariable(name = "firstRowCount", required = false) Integer firstRowCount,
            @ParamVariable(name = "chunkSize",     required = false) Integer chunkSize) {

        // SSV 포맷으로 설정
        firstRowHandler.setContentType(PlatformType.CONTENT_TYPE_SSV);

        String sendDataSetName     = "ds_largeData";
        int    defaultFirstRowCount = (firstRowCount != null && firstRowCount > 0) ? firstRowCount : 1000;
        int    defaultChunkSize     = (chunkSize     != null && chunkSize     > 0) ? chunkSize     : 1000;

        log.info("========================================");
        log.info("대량 데이터 조회 시작");
        log.info("데이터셋   : {}", sendDataSetName);
        log.info("초기 Row 수: {}", defaultFirstRowCount);
        log.info("청크 크기  : {}", defaultChunkSize);
        log.info("========================================");

        long startTime = System.currentTimeMillis();
        try {
            largeDataService.selectLargeData(firstRowHandler, sendDataSetName, defaultFirstRowCount, defaultChunkSize);
            log.info("대량 데이터 조회 완료 - 소요시간: {}ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("대량 데이터 조회 중 오류 발생", e);
            throw e;
        }

        // canonical MVC 패턴 — NexacroResult 리턴으로 NexacroHandlerMethodReturnValueHandler
        // 가 chunked 응답을 완료(0\r\n\r\n terminator) 시키도록 함. void 리턴 시 Spring MVC
        // 기본 void 핸들러가 우선되어 Content-Type=text/plain 오버라이드 + terminator 누락 발생.
        return new NexacroResult();
    }
}
