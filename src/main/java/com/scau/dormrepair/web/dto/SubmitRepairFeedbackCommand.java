package com.scau.dormrepair.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 提交评价的请求体。
 * @param repairRequestId 报修单 ID
 * @param rating 评分
 * @param feedbackComment 评价内容
 * @param anonymous 是否匿名
 */
public record SubmitRepairFeedbackCommand(
        @NotNull(message = "报修单ID不能为空")
        Long repairRequestId,
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最小为1")
        @Max(value = 5, message = "评分最大为5")
        Integer rating,
        @Size(max = 1000, message = "评价内容长度不能超过1000")
        String feedbackComment,
        Boolean anonymous
) {
}
