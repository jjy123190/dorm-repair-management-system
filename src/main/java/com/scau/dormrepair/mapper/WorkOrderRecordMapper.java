package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.WorkOrderRecord;
import com.scau.dormrepair.domain.view.WorkOrderRecordView;
import java.util.List;

/**
 * Work-order timeline mapper.
 */
public interface WorkOrderRecordMapper {

    int insert(WorkOrderRecord workOrderRecord);

    List<WorkOrderRecordView> selectTimelineByWorkOrderId(Long workOrderId);
}
