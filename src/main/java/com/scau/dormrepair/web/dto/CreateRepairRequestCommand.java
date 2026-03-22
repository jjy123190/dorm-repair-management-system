package com.scau.dormrepair.web.dto;

import com.scau.dormrepair.domain.enums.FaultCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 学生提交报修的请求体。
 * @param studentName 学生姓名
 * @param buildingNo 宿舍楼栋号
 * @param roomNo 宿舍房间号
 * @param faultCategory 故障类型
 * @param description 故障描述
 * @param contactPhone 联系电话
 * @param imageUrls 图片地址列表
 */
public record CreateRepairRequestCommand(
        @NotBlank(message = "学生姓名不能为空")
        @Size(max = 64, message = "学生姓名长度不能超过64")
        String studentName,
        @NotBlank(message = "宿舍楼栋不能为空")
        @Size(max = 32, message = "宿舍楼栋长度不能超过32")
        String buildingNo,
        @NotBlank(message = "房间号不能为空")
        @Size(max = 32, message = "房间号长度不能超过32")
        String roomNo,
        @NotNull(message = "故障类型不能为空")
        FaultCategory faultCategory,
        @NotBlank(message = "故障描述不能为空")
        @Size(max = 2000, message = "故障描述长度不能超过2000")
        String description,
        @NotBlank(message = "联系电话不能为空")
        @Size(max = 32, message = "联系电话长度不能超过32")
        String contactPhone,
        List<String> imageUrls
) {
}
