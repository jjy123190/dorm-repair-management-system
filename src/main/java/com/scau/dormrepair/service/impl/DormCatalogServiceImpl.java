package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.mapper.DormBuildingMapper;
import com.scau.dormrepair.service.DormCatalogService;
import java.util.List;

/**
 * 宿舍区和楼栋基础资料实现。
 */
public class DormCatalogServiceImpl implements DormCatalogService {

    private final MyBatisExecutor myBatisExecutor;

    public DormCatalogServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public List<String> listDormAreas() {
        return myBatisExecutor.executeRead(
                session -> session.getMapper(DormBuildingMapper.class).selectDistinctAreas()
        );
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
