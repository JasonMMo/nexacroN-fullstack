package com.nexacro.uiadapter.domain;

import com.nexacro.uiadapter.spring.core.data.DataSetRowTypeAccessor;

/**
 * Local base for every domain POJO exchanged with the nexacro client
 * (javax lane).
 *
 * <p>Mirrors the canonical pattern from
 * {@code example.nexacro.uiadapter.pojo.NexacroBase} — implements
 * {@link DataSetRowTypeAccessor} so the uiadapter argument-resolver
 * can preserve {@code _RowType_} when binding a DataSet row into a
 * POJO. Without this, POJO-mode CRUD loses the per-row I/U/D/N/O state
 * that drives dispatch.
 *
 * <p>javax lane uses {@code com.nexacro.uiadapter.spring.core.data}
 * (canonical {@code uiadapter-spring-core}); the jakarta lane uses
 * {@code com.nexacro.uiadapter.jakarta.core.data}.
 */
public class NexacroBase implements DataSetRowTypeAccessor {

    /** Per-row {@code _RowType_} carried over from the inbound DataSet. */
    private int rowType;

    @Override
    public int getRowType() {
        return this.rowType;
    }

    @Override
    public void setRowType(int rowType) {
        this.rowType = rowType;
    }
}
