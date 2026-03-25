package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.DormBuilding;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 宿舍区与楼栋基础资料 Mapper。
 */
public interface DormBuildingMapper {

    List<String> selectDistinctAreas();

    List<DormBuilding> selectByArea(@Param("campusName") String campusName);
}
