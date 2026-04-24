package com.nexacro.fullstack.business.domain.file;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface FileDao {

    int insert(Map<String, Object> row);

    Map<String, Object> findById(String fileId);

    List<Map<String, Object>> listAll();

    /**
     * Find multiple file metadata rows by FILE_IDs.
     *
     * @param fileIds list of FILE_ID values to look up
     * @return matching rows (never null — empty list if no matches)
     */
    List<Map<String, Object>> findByIds(List<String> fileIds);
}
