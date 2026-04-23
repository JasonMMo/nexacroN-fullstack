package com.nexacro.fullstack.business.domain.file;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FileDao {
    int insert(Map<String, Object> meta);
    Map<String, Object> findById(@Param("fileId") String fileId);
    List<Map<String, Object>> listAll();
}
