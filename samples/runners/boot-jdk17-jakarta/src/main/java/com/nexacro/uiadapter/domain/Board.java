package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Board POJO mirroring the SAMPLE_BOARD table.
 *
 * <p>Field names use camelCase to match MyBatis
 * {@code mapUnderscoreToCamelCase=true}. Database columns map as:
 * BOARD_ID→boardId, TITLE→title, CONTENT→content, AUTHOR_ID→authorId,
 * VIEW_COUNT→viewCount, CREATED_AT→createdAt, UPDATED_AT→updatedAt.
 *
 * <p>Used both as MyBatis result type and as the element type for
 * {@code @ParamDataSet List<Board>} controller arguments — the uiadapter
 * argument resolver maps Nexacro DataSet columns onto these properties.
 */
@Getter
@Setter
public class Board {
    private Integer boardId;
    private String  title;
    private String  content;
    private String  authorId;
    private Integer viewCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
