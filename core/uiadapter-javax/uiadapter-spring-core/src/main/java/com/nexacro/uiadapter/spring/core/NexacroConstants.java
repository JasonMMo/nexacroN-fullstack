package com.nexacro.uiadapter.spring.core;

/**
 * <p>Constants used in the Nexacro UI Adapter for Spring Framework.
 * <p>This class provides constant values used throughout the uiadapter-spring modules.
 * 
 * @author Park SeongMin
 * @since 08.05.2015
 * @version 1.0
 * @see com.nexacro.uiadapter.spring.core.NexacroException
 */
public final class NexacroConstants {

    // attribute data
    /**
     * Constants for attribute names used in the Nexacro UI Adapter.
     * These attributes are used to store and retrieve data in various contexts.
     */
    public static final class ATTRIBUTE {
        /* Attribute name for storing Nexacro request object */
        public static final String NEXACRO_REQUEST       = "NexacroRequest";
        /* Attribute name for storing cached Nexacro data */
        public static final String NEXACRO_CACHE_DATA    = "NexacroCachedData";
        /* Attribute name for storing Nexacro platform data */
        public static final String NEXACRO_PLATFORM_DATA = "NexacroPlatformData";
        /* Attribute name for storing Nexacro file data */
        public static final String NEXACRO_FILE_DATA     = "NexacroFileData";
    }

    // error
    /**
     * Constants related to error handling in Nexacro UI Adapter.
     * These constants define error codes and messages used in communication with Nexacro platform.
     */
    public static final class ERROR {

        /* Default error code (0 indicates success) */
        public final static int DEFAULT_ERROR_CODE = 0; 
        /* Property name for error code in result data (values >= 0 indicate success) */
        public final static String ERROR_CODE = "ErrorCode";
        /* Property name for error message in result data */
        public final static String ERROR_MSG  = "ErrorMsg";
    }

    // first row
    /**
     * Constants for error information in the first row of dataset.
     * These constants are used when returning error information in the first row of a dataset.
     */
    public final class ERROR_FIRST_ROW {
        /* Dataset name for first row status information */
        public final static String ERROR_DATASET = "FirstRowStatus";
        /* Property name for error code in first row */
        public final static String ERROR_CODE    = ERROR.ERROR_CODE;
        /* Property name for error message in first row */
        public final static String ERROR_MSG     = ERROR.ERROR_MSG;
    }

    /* Logger name for performance monitoring */
    public static final String PERFORMANCE_LOGGER = "com.nexacro.performance";
}
