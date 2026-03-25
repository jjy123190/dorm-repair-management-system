package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.mapper.DormBuildingMapper;
import com.scau.dormrepair.service.DormCatalogService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 宿舍区和楼栋基础资料实现。
 */
public class DormCatalogServiceImpl implements DormCatalogService {

    private static final List<String> STABLE_DORM_AREAS = List.of(
            "泰山区",
            "华山区",
            "启林区",
            "黑山区",
            "燕山区"
    );

    private final MyBatisExecutor myBatisExecutor;

    public DormCatalogServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public List<String> listDormAreas() {
        List<String> areas = myBatisExecutor.executeRead(
                session -> session.getMapper(DormBuildingMapper.class).selectDistinctAreas()
        );
        Set<String> normalizedAreas = new LinkedHashSet<>(STABLE_DORM_AREAS);
        for (String area : areas) {
            if (STABLE_DORM_AREAS.contains(area)) {
                normalizedAreas.add(area);
            }
        }
        return normalizedAreas.stream().toList();
    }

    @Override
    public List<DormBuilding> listBuildingsByArea(String dormArea) {
        if (dormArea == null || dormArea.isBlank()) {
            return List.of();
        }
        return myBatisExecutor.executeRead(
                session -> session.getMapper(DormBuildingMapper.class).selectByArea(dormArea.trim())
        );
    }
}
