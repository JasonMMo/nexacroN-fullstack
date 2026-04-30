package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.TestDataType;

import java.util.List;

/** TestData service contract. */
public interface TestDataService {

    /** All test-data-type rows for the all-types demo. */
    List<TestDataType> selectAll();
}
