package com.nexacro.fullstack.business.domain.file;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface FileDao {

    int insert(Map<String, Object> row);

    Map<String, Object> findById(String fileId);

    List<Map<String, Object>> listAll();
}
