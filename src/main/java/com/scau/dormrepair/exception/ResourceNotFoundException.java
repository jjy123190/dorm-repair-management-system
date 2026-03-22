package com.scau.dormrepair.exception;

/**
 * 资源不存在异常。
 * 用在“按 ID 查询但数据库里没有这条记录”的场景。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
