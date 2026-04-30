package com.nexacro.fullstack.business.domain.testdata;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * MyBatis DAO for TB_TEST_DATA_TYPE.
 *
 * Ported from <boot-jdk17-jakarta-legacy>/<TestDataDao.java>@e49a17791d on 2026-04-24.
 * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
 */
@Mapper
public interface TestDataDao {

    /** Return all rows from TB_TEST_DATA_TYPE as plain maps. */
    List<Map<String, Object>> selectAll();
}
