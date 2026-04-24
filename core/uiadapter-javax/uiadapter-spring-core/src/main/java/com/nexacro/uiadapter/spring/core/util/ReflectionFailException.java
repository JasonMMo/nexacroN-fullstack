package com.nexacro.uiadapter.spring.core.util;

public class ReflectionFailException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3594758528789596899L;

	public ReflectionFailException(String message) {
        super(message);
    }

    public ReflectionFailException(String message, Throwable cause) {
        super(message, cause);
    }
}