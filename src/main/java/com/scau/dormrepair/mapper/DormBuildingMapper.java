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

    List<DormBuilding> selectAll();

    DormBuilding selectById(@Param("id") Long id);

    DormBuilding selectByCampusAndBuildingNo(@Param("campusName") String campusName, @Param("buildingNo") String buildingNo);

    int insert(DormBuilding dormBuilding);

    int update(DormBuilding dormBuilding);

    int countRooms(@Param("buildingId") Long buildingId);

    int deleteById(@Param("id") Long id);
}