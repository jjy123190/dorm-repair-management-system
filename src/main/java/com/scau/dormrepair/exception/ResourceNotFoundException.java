package com.scau.dormrepair.exception;

/**
 * 查询结果缺失时统一抛这个异常，方便后续界面层做提示。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
