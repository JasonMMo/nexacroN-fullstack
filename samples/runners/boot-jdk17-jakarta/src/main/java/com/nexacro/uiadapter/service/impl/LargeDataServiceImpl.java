package com.nexacro.uiadapter.service.impl;

import com.nexacro.uiadapter.domain.LargeData;
import com.nexacro.uiadapter.mapper.LargeDataMapper;
import com.nexacro.uiadapter.service.LargeDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LargeDataServiceImpl implements LargeDataService {

    private final LargeDataMapper largeDataMapper;

    @Override
    public int count(String category) {
        return largeDataMapper.count(category);
    }

    @Override
    public List<LargeData> page(int page, int pageSize, String category) {
        int safePage = page < 1 ? 1 : page;
        int safeSize = pageSize < 1 ? 50 : pageSize;
        int offset = (safePage - 1) * safeSize;
        return largeDataMapper.page(offset, safeSize, category);
    }
}
