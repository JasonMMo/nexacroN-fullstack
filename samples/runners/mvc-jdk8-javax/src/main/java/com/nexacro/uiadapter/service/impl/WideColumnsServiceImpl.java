package com.nexacro.uiadapter.service.impl;

import com.nexacro.uiadapter.domain.WideColumns;
import com.nexacro.uiadapter.mapper.WideColumnsMapper;
import com.nexacro.uiadapter.service.WideColumnsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WideColumnsServiceImpl implements WideColumnsService {

    private final WideColumnsMapper wideColumnsMapper;

    @Override
    public List<WideColumns> selectList(String keyId) {
        return wideColumnsMapper.selectList(keyId);
    }
}
