package com.commul.ailcode.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(com.commul.ailcode.exception.ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(com.commul.ailcode.exception.ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
