package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.LargeData;
import com.nexacro.uiadapter.jakarta.core.data.NexacroFirstRowHandler;

import java.util.List;

/** LargeData paging + chunked-streaming service. */
public interface LargeDataService {

    /** Total row count for the optional category filter. */
    int count(String category);

    /**
     * One 1-indexed page of LARGE_DATA rows.
     *
     * @param page     1-based page number
     * @param pageSize rows per page
     * @param category optional category filter (null = all)
     */
    List<LargeData> page(int page, int pageSize, String category);

    /**
     * Stream all TB_LARGE rows back to the client in chunks via
     * {@link NexacroFirstRowHandler}. Mirrors the canonical Nexacro N reference
     * (gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta) {@code
     * LargeDataService#selectLargeData}.
     *
     * <p>Caller (controller) is expected to have already configured
     * {@code firstRowHandler.setContentType(PlatformType.CONTENT_TYPE_SSV)}.
     *
     * <p>Internal {@code PlatformException}s are wrapped as
     * {@link RuntimeException} so the canonical controller signature
     * (no checked-exception throws) compiles.
     *
     * @param handler        first-row handler bound to the response
     * @param dataSetName    response dataset name (canonical: {@code ds_largeData})
     * @param firstRowCount  rows in the first chunk (initial flush size)
     * @param chunkSize      rows in each subsequent chunk
     */
    void selectLargeData(NexacroFirstRowHandler handler,
                         String dataSetName,
                         int firstRowCount,
                         int chunkSize);
}
