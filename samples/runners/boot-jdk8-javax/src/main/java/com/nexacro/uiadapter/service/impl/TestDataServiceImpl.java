package com.nexacro.uiadapter.service.impl;

import com.nexacro.uiadapter.domain.TestDataType;
import com.nexacro.uiadapter.mapper.TestDataMapper;
import com.nexacro.uiadapter.service.TestDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Default {@link TestDataService}.
 *
 * <p>Upsert semantics for {@code update_datalist*}: row with {@code id == null}
 * is treated as INSERT, otherwise UPDATE. This mirrors how the canonical sample
 * persists a {@code List&lt;ExampleDataType&gt;} (POJO binding loses the
 * {@code DataSet.rowType} 1=ins/2=upd/4=del flag, so 4=delete is out of scope
 * for the list-bound endpoint).
 */
@Service
@RequiredArgsConstructor
public class TestDataServiceImpl implements TestDataService {

    private final TestDataMapper testDataMapper;

    @Override
    public List<TestDataType> selectAll() {
        return testDataMapper.selectAll();
    }

    @Override
    public List<TestDataType> select_datalist(Map<String, String> searchMap) {
        return testDataMapper.select_datalist(searchMap);
    }

    @Override
    public List<Map<String, Object>> select_datalist_map(Map<String, String> searchMap) {
        return testDataMapper.select_datalist_map(searchMap);
    }

    @Override
    @Transactional
    public void update_datalist(List<TestDataType> dataList) {
        if (dataList == null) return;
        for (TestDataType row : dataList) {
            if (row.getId() == null) {
                testDataMapper.insert_data(row);
            } else {
                testDataMapper.update_data(row);
            }
        }
    }

    @Override
    @Transactional
    public void update_datalist_map(List<Map<String, Object>> dataList) {
        if (dataList == null) return;
        for (Map<String, Object> row : dataList) {
            Object id = row.get("id");
            if (id == null) {
                testDataMapper.insert_data_map(row);
            } else {
                testDataMapper.update_data_map(row);
            }
        }
    }
}
