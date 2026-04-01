package com.scau.dormrepair.service;

import com.scau.dormrepair.common.AppSession;
import com.scau.dormrepair.domain.command.AccountFilter;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import java.util.List;

public interface UserAccountService {

    UserAccount login(String username, String password);

    Long registerStudent(String username, String password, String confirmPassword, String displayName, String phone);

    void resetPasswordByPhone(String username, String phone, String newPassword, String confirmPassword);

    void changePassword(Long accountId, String oldPassword, String newPassword, String confirmPassword);

    Long createInternalAccount(String username, String password, String confirmPassword, String displayName, String phone, UserRole role);

    UserAccount updateInternalAccountProfile(Long accountId, String displayName, String phone, UserRole role);

    UserAccount updateOwnProfile(Long accountId, String displayName, String phone);

    void setAccountEnabled(Long accountId, boolean enabled);

    void adminResetPassword(Long accountId, String newPassword, String confirmPassword);

    boolean isUsernameAvailable(String username);

    List<UserAccount> listAccounts(AccountFilter filter);

    UserAccount getById(Long accountId);

    UserAccount requireCurrentAccount(AppSession appSession, UserRole expectedRole);

    List<UserAccount> listEnabledAccountsByRole(UserRole role);
}