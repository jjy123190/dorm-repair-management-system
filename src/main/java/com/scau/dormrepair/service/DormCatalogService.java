package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.entity.DormBuilding;
import java.util.List;

/**
 * 宿舍资料服务。
 */
public interface DormCatalogService {

    List<String> listDormAreas();

    List<DormBuilding> listBuildingsByArea(String dormArea);
}
