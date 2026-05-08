package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.Board;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper for TB_BOARD (canonical schema).
 *
 * <p>Statements are defined in
 * {@code resources/mybatis/mappers/board-mapper.xml}; the
 * {@code mapUnderscoreToCamelCase} setting in
 * {@code mybatis/sql-mapper-config.xml} bridges DB columns to {@link Board}
 * fields.
 */
@Mapper
public interface BoardMapper {

    /** All boards matching optional filters, ordered newest first. */
    List<Board> selectList(Board search);

    /** One board by primary key, or {@code null} if missing. */
    Board selectById(Integer postId);

    /** Canonical single-row Map-mode response. */
    Map<String, Object> selectDataSingle(Map<String, String> param);

    /** Map-mode list response aligned with Nexacro canonical endpoint. */
    List<Map<String, Object>> selectDatalistMap(Map<String, String> param);

    /** Insert a new row using POJO payload. */
    int insertBoard(Board board);

    /** Update an existing row using POJO payload. */
    int updateBoard(Board board);

    /** Delete by primary key. */
    int deleteBoard(Integer postId);

    /** Insert via Map-mode payload. */
    int insertBoardMap(Map<String, Object> row);

    /** Update via Map-mode payload. */
    int updateBoardMap(Map<String, Object> row);

    /** Delete via Map-mode payload. */
    int deleteBoardMap(Map<String, Object> row);
}
