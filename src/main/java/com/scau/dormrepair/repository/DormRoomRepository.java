package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.DormRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DormRoomRepository extends JpaRepository<DormRoom, Long> {
}
