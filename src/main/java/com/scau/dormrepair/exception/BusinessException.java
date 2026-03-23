package com.scau.dormrepair.exception;

/**
 * 业务规则不满足时抛出的异常。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
