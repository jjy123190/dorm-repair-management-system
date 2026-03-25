package com.scau.dormrepair.ui.support;

import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import java.sql.SQLException;

/**
 * 把底层异常翻译成界面层可直接展示的短句。
 * 这样弹窗只给用户能处理的信息，不把 MyBatis/SQL 栈细节整段甩出来。
 */
public final class UiErrorMessages {

    private UiErrorMessages() {
    }

    public static String resolve(Throwable throwable) {
        Throwable rootCause = rootCause(throwable);
        if (rootCause instanceof BusinessException
                || rootCause instanceof ResourceNotFoundException
                || rootCause instanceof IllegalArgumentException) {
            return safeMessage(rootCause.getMessage(), "操作失败，请检查输入后再试。");
        }
        if (rootCause instanceof SQLException sqlException) {
            return resolveSqlMessage(sqlException);
        }

        String rootMessage = firstMeaningfulLine(rootCause.getMessage());
        if (!rootMessage.isBlank()) {
            return resolve(rootMessage);
        }

        String topMessage = firstMeaningfulLine(throwable.getMessage());
        if (!topMessage.isBlank()) {
            return resolve(topMessage);
        }
        return "操作失败，请稍后重试。";
    }

    public static String resolve(String rawMessage) {
        String message = firstMeaningfulLine(rawMessage);
        if (message.contains("doesn't have a default value")) {
            return "数据库表结构还是旧版本，应用已自动兼容。请关闭当前窗口后重试一次。";
        }
        if (message.contains("Data too long")) {
            return "输入内容过长，请缩短后再提交。";
        }
        if (message.contains("Cannot add or update a child row")) {
            return "关联宿舍数据无效，请检查楼栋和房间信息。";
        }
        if (message.contains("Duplicate entry")) {
            return "生成的业务编号发生重复，请重新提交一次。";
        }
        return safeMessage(message, "操作失败，请稍后重试。");
    }

    private static String resolveSqlMessage(SQLException exception) {
        return resolve(exception.getMessage());
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private static String firstMeaningfulLine(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        for (String line : message.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("###")
                    || trimmed.startsWith("The error may involve")
                    || trimmed.startsWith("SQL:")
                    || trimmed.startsWith("Cause:")) {
                continue;
            }
            return trimmed;
        }
        return "";
    }

    private static String safeMessage(String message, String fallback) {
        String normalized = firstMeaningfulLine(message);
        return normalized.isBlank() ? fallback : normalized;
    }
}
