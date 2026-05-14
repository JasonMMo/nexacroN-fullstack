package com.nexacro.uiadapter.service.impl;

import com.nexacro.uiadapter.service.VideoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class VideoServiceImpl implements VideoService {

    @Value("${media.storage.base-path:./videos}")
    private String videoDir;

    @Override
    public File resolve(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        try {
            File base = new File(videoDir).getCanonicalFile();
            File target = new File(base, fileName).getCanonicalFile();
            if (!target.getPath().startsWith(base.getPath() + File.separator)
                    && !target.getPath().equals(base.getPath())) {
                return null;
            }
            return target;
        } catch (IOException e) {
            return null;
        }
    }
}
