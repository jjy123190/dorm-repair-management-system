package com.scau.dormrepair.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 统一生成业务编号，避免各个 service 自己拼单号。
 */
public final class BusinessNumberGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private BusinessNumberGenerator() {
    }

    public static String nextRepairRequestNo() {
        return "RR" + FORMATTER.format(LocalDateTime.now()) + randomSuffix();
    }

    public static String nextWorkOrderNo() {
        return "WO" + FORMATTER.format(LocalDateTime.now()) + randomSuffix();
    }

    private static String randomSuffix() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
    }
}
