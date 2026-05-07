package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.Board;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MyBatis mapper for SAMPLE_BOARD.
 *
 * <p>Statements are defined in
 * {@code resources/mybatis/mappers/board-mapper.xml}; the
 * {@code mapUnderscoreToCamelCase} setting in
 * {@code mybatis/sql-mapper-config.xml} bridges DB columns to {@link Board}
 * fields.
 */
@Mapper
public interface BoardMapper {

    /** All non-deleted boards, ordered newest first. */
    List<Board> selectList(Board search);

    /** One board by primary key, or {@code null} if missing/deleted. */
    Board selectById(Integer boardId);

    /** Insert a new row; returns affected row count. */
    int insert(Board board);

    /** Update an existing row by primary key; returns affected row count. */
    int update(Board board);

    /** Soft-delete by primary key (sets DELETED=TRUE); returns affected row count. */
    int softDelete(Integer boardId);
}
