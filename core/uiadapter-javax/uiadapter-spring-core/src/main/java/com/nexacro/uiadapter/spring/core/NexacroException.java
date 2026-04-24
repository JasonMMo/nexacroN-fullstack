package com.nexacro.uiadapter.spring.core;

import com.nexacro.uiadapter.spring.core.resolve.NexacroMappingExceptionResolver;

/**
 * An <code>Exception</code> that represents a Nexacro-specific exception.
 * 
 * <p>This exception stores error codes and error messages that can be transmitted to the Nexacro platform.
 * 
 * <p>The error message information transmitted varies according to the <code>NexacroMappingExceptionResolver</code> configuration.
 *
 * @author Park SeongMin
 * @since 2015. 7. 30.
 * @version 1.0
 * @see NexacroMappingExceptionResolver
 */
public class NexacroException extends Exception {

    /* serialVersionUID */
    private static final long serialVersionUID = 4095922735986385233L;

    public static final int DEFAULT_ERROR_CODE = -1;
    public static final String DEFAULT_MESSAGE = "An Error Occured. check the ErrorCode for detail of error infomation.";

    private int errorCode = DEFAULT_ERROR_CODE;
    private String errorMsg;

    /**
     * Default constructor.
     */
    public NexacroException() {
        super();
    }

    /**
     * Constructor with a message.
     * 
     * @param message the detail message
     */
    public NexacroException(final String message) {
        this(message, null);
    }

    /**
     * Constructor with a message and a cause.
     * 
     * @param message the detail message
     * @param cause the cause exception
     */
    public NexacroException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with a message, error code, and error message.
     * 
     * @param message the detail message
     * @param errorCode the error code to be transmitted to Nexacro platform
     * @param errorMsg the error message to be transmitted to Nexacro platform
     */
    public NexacroException(final String message, final int errorCode, final String errorMsg) {
    	this(message, null, errorCode, errorMsg);
    }

    /**
     * Constructor with a message, cause, error code, and error message.
     * 
     * @param message the detail message
     * @param cause the cause exception
     * @param errorCode the error code to be transmitted to Nexacro platform
     * @param errorMsg the error message to be transmitted to Nexacro platform
     */
    public NexacroException(final String message, final Throwable cause, final int errorCode, final String errorMsg) {
        super(message, cause);

        this.errorCode = errorCode;
        this.errorMsg  = errorMsg;
    }

    /**
     * Returns the error code.
     * @return the error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code.
     * 
     * @param errorCode the error code to be transmitted to Nexacro platform
     */
    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Returns the error message.
     * @return the error message
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Sets the error message.
     * @param errorMsg the error message to be transmitted to Nexacro platform
     */
    public void setErrorMsg(final String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
