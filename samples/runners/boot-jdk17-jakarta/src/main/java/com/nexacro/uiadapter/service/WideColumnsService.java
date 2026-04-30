package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.WideColumns;

import java.util.List;

/** WideColumns service contract — 50-column demo. */
public interface WideColumnsService {

    /** All wide-column rows, optionally filtered by KEY_ID. */
    List<WideColumns> selectList(String keyId);
}
