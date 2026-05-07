package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.LargeData;

import java.util.List;

/** LargeData paging service. */
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
}
