package com.scau.dormrepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.common.PasswordHasher;
import com.scau.dormrepair.domain.command.AccountFilter;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserAccountServiceImplTest extends UserAccountIntegrationSupport {

    @Test
    void shouldRegisterStudentAndLoginWithHash() {
        String username = uniqueUsername("student");
        Long accountId = userAccountService.registerStudent(
                username,
                "student123",
                "student123",
                "\u6d4b\u8bd5\u5b66\u751f",
                "13855550001"
        );

        UserAccount account = userAccountService.getById(accountId);
        assertNotNull(account);
        assertEquals(UserRole.STUDENT, account.getRoleCode());
        assertTrue(PasswordHasher.matches("student123", account.getPasswordHash()));

        UserAccount loginAccount = userAccountService.login(username, "student123");
        assertEquals(accountId, loginAccount.getId());
    }

    @Test
    void shouldRejectDuplicateStudentUsername() {
        String username = uniqueUsername("dupstudent");
        userAccountService.registerStudent(username, "student123", "student123", "\u6d4b\u8bd5\u5b66\u751f", "13855550002");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userAccountService.registerStudent(username, "student123", "student123", "\u91cd\u590d\u5b66\u751f", "13855550003")
        );
        assertEquals("\u8be5\u8d26\u53f7\u5df2\u5b58\u5728\uff0c\u8bf7\u66f4\u6362\u540e\u518d\u8bd5\u3002", exception.getMessage());
    }

    @Test
    void shouldCreateInternalAccountDisableAndBlockLogin() {
        String username = uniqueUsername("worker");
        Long accountId = userAccountService.createInternalAccount(
                username,
                "worker123",
                "worker123",
                "\u6d4b\u8bd5\u5e08\u5085",
                "13855550004",
                UserRole.WORKER
        );

        userAccountService.setAccountEnabled(accountId, false);
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userAccountService.login(username, "worker123")
        );
        assertEquals("\u8d26\u53f7\u5df2\u505c\u7528\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u5904\u7406\u3002", exception.getMessage());
    }

    @Test
    void shouldResetPasswordByPhoneAndInvalidateOldPassword() {
        String username = uniqueUsername("studentreset");
        userAccountService.registerStudent(username, "student123", "student123", "\u6d4b\u8bd5\u5b66\u751f", "13855550005");

        userAccountService.resetPasswordByPhone(username, "13855550005", "student456", "student456");

        BusinessException oldPasswordError = assertThrows(
                BusinessException.class,
                () -> userAccountService.login(username, "student123")
        );
        assertEquals("\u5bc6\u7801\u9519\u8bef\uff0c\u8bf7\u91cd\u65b0\u8f93\u5165\u3002", oldPasswordError.getMessage());

        UserAccount loginAccount = userAccountService.login(username, "student456");
        assertEquals(username, loginAccount.getUsername());
    }

    @Test
    void shouldChangePasswordAndRejectSameAsOld() {
        String username = uniqueUsername("adminchange");
        Long accountId = userAccountService.createInternalAccount(
                username,
                "admin123",
                "admin123",
                "\u6d4b\u8bd5\u7ba1\u7406\u5458",
                "13855550006",
                UserRole.ADMIN
        );

        BusinessException samePasswordError = assertThrows(
                BusinessException.class,
                () -> userAccountService.changePassword(accountId, "admin123", "admin123", "admin123")
        );
        assertEquals("\u65b0\u5bc6\u7801\u4e0d\u80fd\u4e0e\u65e7\u5bc6\u7801\u76f8\u540c\u3002", samePasswordError.getMessage());

        userAccountService.changePassword(accountId, "admin123", "admin456", "admin456");
        UserAccount loginAccount = userAccountService.login(username, "admin456");
        assertEquals(accountId, loginAccount.getId());
    }

    @Test
    void shouldResetInternalPasswordAndFilterAccounts() {
        String adminUsername = uniqueUsername("adminlist");
        String workerUsername = uniqueUsername("workerlist");
        Long adminId = userAccountService.createInternalAccount(
                adminUsername,
                "admin123",
                "admin123",
                "\u7ba1\u7406\u5458\u7532",
                "13855550007",
                UserRole.ADMIN
        );
        Long workerId = userAccountService.createInternalAccount(
                workerUsername,
                "worker123",
                "worker123",
                "\u7ef4\u4fee\u5458\u7532",
                "13855550008",
                UserRole.WORKER
        );
        userAccountService.setAccountEnabled(workerId, false);
        userAccountService.adminResetPassword(adminId, "admin789", "admin789");

        UserAccount resetLogin = userAccountService.login(adminUsername, "admin789");
        assertEquals(adminId, resetLogin.getId());

        List<UserAccount> adminAccounts = userAccountService.listAccounts(new AccountFilter(UserRole.ADMIN, Boolean.TRUE));
        assertTrue(adminAccounts.stream().anyMatch(account -> adminUsername.equals(account.getUsername())));

        List<UserAccount> disabledWorkers = userAccountService.listAccounts(new AccountFilter(UserRole.WORKER, Boolean.FALSE));
        assertTrue(disabledWorkers.stream().anyMatch(account -> workerUsername.equals(account.getUsername())));
    }

    @Test
    void shouldUpdateInternalAccountProfile() {
        String username = uniqueUsername("workeredit");
        Long workerId = userAccountService.createInternalAccount(
                username,
                "worker123",
                "worker123",
                "\u65e7\u7ef4\u4fee\u5458",
                "13855550009",
                UserRole.WORKER
        );

        UserAccount updated = userAccountService.updateInternalAccountProfile(
                workerId,
                "\u65b0\u7ef4\u4fee\u5458",
                "13955550009",
                UserRole.ADMIN
        );

        assertEquals("\u65b0\u7ef4\u4fee\u5458", updated.getDisplayName());
        assertEquals("13955550009", updated.getPhone());
        assertEquals(UserRole.ADMIN, updated.getRoleCode());
    }

    @Test
    void shouldRejectInternalProfileUpdateForStudentAccount() {
        String username = uniqueUsername("studentedit");
        Long studentId = userAccountService.registerStudent(
                username,
                "student123",
                "student123",
                "\u5b66\u751f\u8d26\u53f7",
                "13855550010"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userAccountService.updateInternalAccountProfile(
                        studentId,
                        "\u4e0d\u5e94\u88ab\u4fee\u6539",
                        "13955550010",
                        UserRole.ADMIN
                )
        );
        assertEquals("\u5b66\u751f\u8d26\u53f7\u4e0d\u5728\u5185\u90e8\u8d26\u53f7\u7ba1\u7406\u8303\u56f4\u5185\u3002", exception.getMessage());
    }

    @Test
    void shouldUpdateOwnProfile() {
        String username = uniqueUsername("selfedit");
        Long studentId = userAccountService.registerStudent(
                username,
                "student123",
                "student123",
                "\u539f\u59cb\u59d3\u540d",
                "13855550011"
        );

        UserAccount updated = userAccountService.updateOwnProfile(studentId, "\u65b0\u59d3\u540d", "13955550011");

        assertEquals("\u65b0\u59d3\u540d", updated.getDisplayName());
        assertEquals("13955550011", updated.getPhone());
        assertEquals(UserRole.STUDENT, updated.getRoleCode());
    }
}