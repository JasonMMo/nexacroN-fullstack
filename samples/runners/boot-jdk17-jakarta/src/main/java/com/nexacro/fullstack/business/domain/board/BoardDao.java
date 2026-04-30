package com.nexacro.fullstack.business.domain.board;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface BoardDao {
    List<Map<String, Object>> selectAll();
    Map<String, Object> selectById(Integer boardId);
    int insert(Map<String, Object> row);
    int update(Map<String, Object> row);
    int softDelete(Integer boardId);
}
