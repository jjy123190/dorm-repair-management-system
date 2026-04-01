package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserAccountMapper {

    UserAccount selectById(@Param("id") Long id);

    UserAccount selectByUsername(@Param("username") String username);

    boolean existsByUsername(@Param("username") String username);

    List<UserAccount> selectEnabledByRole(@Param("roleCode") UserRole roleCode);

    List<UserAccount> selectAccounts(
            @Param("roleCode") UserRole roleCode,
            @Param("enabled") Boolean enabled
    );

    void insertStudentAccount(UserAccount userAccount);

    void insertAccount(UserAccount userAccount);

    int updateOwnProfileById(
            @Param("id") Long id,
            @Param("displayName") String displayName,
            @Param("phone") String phone
    );

    int updateInternalProfileById(
            @Param("id") Long id,
            @Param("displayName") String displayName,
            @Param("phone") String phone,
            @Param("roleCode") UserRole roleCode
    );

    int updatePasswordHashById(
            @Param("id") Long id,
            @Param("passwordHash") String passwordHash
    );

    int updateEnabledById(
            @Param("id") Long id,
            @Param("enabled") boolean enabled
    );

    int updatePasswordHashByUsernameAndPhone(
            @Param("username") String username,
            @Param("phone") String phone,
            @Param("passwordHash") String passwordHash
    );
}