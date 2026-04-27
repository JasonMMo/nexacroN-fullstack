package com.nexacro.fullstack.business.domain.wide;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * MyBatis DAO for WIDE_COLUMNS (50+ column table).
 *
 * Ported from <mvc-jdk8-javax-legacy>/<UiadapterManyColumnMapper.java>@69cdaf1a9d on 2026-04-24.
 * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
 * Table/column names align with fullstack seed-data (WIDE_COLUMNS / COL_01...COL_50)
 * rather than legacy tbl_many_columns / column0...column70; uses @Param for optional KEY_ID filter.
 */
@Mapper
public interface WideColumnsDao {

    /**
     * Select rows from WIDE_COLUMNS, optionally filtered by KEY_ID.
     *
     * @param keyId optional exact-match filter; null returns all rows
     * @return list of wide-column rows
     */
    List<Map<String, Object>> search(@Param("keyId") String keyId);
}
