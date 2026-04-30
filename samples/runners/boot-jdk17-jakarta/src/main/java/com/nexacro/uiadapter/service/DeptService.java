package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.Dept;

import java.util.List;

/**
 * Dept service contract — supplies flat list and recursive tree.
 */
public interface DeptService {

    /** Flat list of all enabled departments. */
    List<Dept> selectList();

    /** Tree (recursive CTE) of all enabled departments, ordered by PATH. */
    List<Dept> selectTree();
}
