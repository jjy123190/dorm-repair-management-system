package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.RepairRequestImage;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 报修图片 Mapper。
 */
public interface RepairRequestImageMapper {

    int batchInsert(
            @Param("repairRequestId") Long repairRequestId,
            @Param("startSortNo") int startSortNo,
            @Param("imageUrls") List<String> imageUrls
    );

    List<RepairRequestImage> selectByRepairRequestId(@Param("repairRequestId") Long repairRequestId);
}
