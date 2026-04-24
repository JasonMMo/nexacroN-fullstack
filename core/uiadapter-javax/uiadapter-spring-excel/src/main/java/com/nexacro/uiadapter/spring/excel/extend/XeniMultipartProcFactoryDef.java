package com.nexacro.uiadapter.spring.excel.extend;

import com.nexacro.java.xeni.extend.XeniMultipartProcBase;
import com.nexacro.java.xeni.extend.XeniMultipartProcFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class XeniMultipartProcFactoryDef implements XeniMultipartProcFactory{

    private final Logger logger = LoggerFactory.getLogger(XeniMultipartProcFactoryDef.class);

    private static final String DEFAULT_MULTIPART_PROC = "com.nexacro.uiadapter.spring.excel.servlet.XeniMultipartHandler";
    private static XeniMultipartProcBase multipartProc ;

    static {
        try {
            multipartProc = new com.nexacro.uiadapter.spring.excel.servlet.XeniMultipartHandler();
        } catch (Throwable var3) {
            System.out.println(var3.getMessage());
        }
    }

    public XeniMultipartProcBase getMultipartProc(String var1) {
        if(logger.isDebugEnabled()) {
            logger.debug("xeni :: getMultipartProc returned multipartHandler : {} ", var1);
        }
        return multipartProc;
    }
}
