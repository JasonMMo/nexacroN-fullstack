package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.Dept;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MyBatis mapper for the DEPT table.
 * Statements are defined in {@code resources/mybatis/mappers/dept-mapper.xml}.
 */
@Mapper
public interface DeptMapper {

    /** All enabled departments, flat, ordered by LEVEL_NO, SORT_ORDER. */
    List<Dept> selectList();

    /** All enabled departments via recursive CTE, ordered by PATH. */
    List<Dept> selectTree();
}
