package com.scau.dormrepair.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 报修图片 Mapper。
 */
public interface RepairRequestImageMapper {

    int batchInsert(
            @Param("repairRequestId") Long repairRequestId,
            @Param("imageUrls") List<String> imageUrls
    );
}
