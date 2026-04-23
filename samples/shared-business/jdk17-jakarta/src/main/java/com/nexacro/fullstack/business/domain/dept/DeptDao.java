package com.nexacro.fullstack.business.domain.dept;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeptDao {

    List<Map<String, Object>> listAll();

    List<Map<String, Object>> tree();
}
