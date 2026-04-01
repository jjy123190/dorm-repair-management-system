package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.WorkOrderCompletionImage;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface WorkOrderCompletionImageMapper {

    int batchInsert(
            @Param("workOrderId") Long workOrderId,
            @Param("imageUrls") List<String> imageUrls
    );

    int deleteByWorkOrderId(@Param("workOrderId") Long workOrderId);

    List<WorkOrderCompletionImage> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);
}