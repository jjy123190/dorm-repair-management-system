package com.scau.dormrepair.domain.command;

/**
 * 学生提交评价时的命令对象。
 */
public record SubmitRepairFeedbackCommand(
        Long studentId,
        String studentName,
        Long repairRequestId,
        Integer rating,
        String feedbackComment,
        boolean anonymousFlag
) {
}
