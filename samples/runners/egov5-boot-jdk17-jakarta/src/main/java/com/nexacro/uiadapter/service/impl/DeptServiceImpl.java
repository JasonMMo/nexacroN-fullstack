package com.nexacro.uiadapter.service.impl;

import com.nexacro.uiadapter.domain.Dept;
import com.nexacro.uiadapter.mapper.DeptMapper;
import com.nexacro.uiadapter.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Default {@link DeptService} backed by {@link DeptMapper}. */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public List<Dept> selectList() {
        return deptMapper.selectList();
    }

    @Override
    public List<Dept> selectTree() {
        return deptMapper.selectTree();
    }
}
