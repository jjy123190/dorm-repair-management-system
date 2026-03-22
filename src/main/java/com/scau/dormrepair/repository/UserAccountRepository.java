package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户账号数据访问接口。
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
}
