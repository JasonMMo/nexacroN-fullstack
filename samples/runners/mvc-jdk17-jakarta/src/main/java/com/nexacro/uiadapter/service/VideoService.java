package com.nexacro.uiadapter.service;

import java.io.File;

/** Video service contract — file resolution under the configured media dir. */
public interface VideoService {

    /**
     * Resolve {@code fileName} against the configured media base path,
     * with a path-traversal guard. Returns {@code null} if the resolved
     * file would escape the base directory.
     */
    File resolve(String fileName);
}
