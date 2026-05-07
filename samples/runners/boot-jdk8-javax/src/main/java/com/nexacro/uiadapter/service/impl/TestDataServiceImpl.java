package com.nexacro.uiadapter.service.impl;

import com.nexacro.uiadapter.domain.TestDataType;
import com.nexacro.uiadapter.mapper.TestDataMapper;
import com.nexacro.uiadapter.service.TestDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestDataServiceImpl implements TestDataService {

    private final TestDataMapper testDataMapper;

    @Override
    public List<TestDataType> selectAll() {
        return testDataMapper.selectAll();
    }
}
