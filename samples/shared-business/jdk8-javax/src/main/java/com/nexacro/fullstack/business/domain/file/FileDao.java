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

    /**
     * Ported from <boot-jdk17-jakarta-legacy>/<FileDao.java>@e49a17791d on 2026-04-24.
     * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
     */
    List<Map<String, Object>> findByIds(List<String> fileIds);
}
