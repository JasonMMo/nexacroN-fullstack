package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.FileMeta;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** MyBatis mapper for FILE_META. */
@Mapper
public interface FileMapper {

    int insert(FileMeta meta);

    FileMeta selectById(@Param("fileId") String fileId);

    List<FileMeta> selectList();

    /** Bulk lookup for the multi-download (zip) endpoint. */
    List<FileMeta> selectByIds(@Param("ids") List<String> ids);
}
