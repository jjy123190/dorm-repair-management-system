package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.DormRoom;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 宿舍房间数据访问接口。
 */
public interface DormRoomRepository extends JpaRepository<DormRoom, Long> {
}
