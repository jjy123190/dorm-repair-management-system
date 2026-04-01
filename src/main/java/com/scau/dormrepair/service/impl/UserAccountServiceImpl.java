package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.AppSession;
import com.scau.dormrepair.common.AuditLogSupport;
import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.common.PasswordHasher;
import com.scau.dormrepair.domain.command.AccountFilter;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.mapper.AuditLogMapper;
import com.scau.dormrepair.mapper.UserAccountMapper;
import com.scau.dormrepair.service.UserAccountService;
import java.util.List;
import java.util.regex.Pattern;

public class UserAccountServiceImpl implements UserAccountService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{4,32}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{6,32}$");
    private static final int MAX_DISPLAY_NAME_LENGTH = 32;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 32;

    private final MyBatisExecutor myBatisExecutor;
    private final AppSession appSession;

    public UserAccountServiceImpl(MyBatisExecutor myBatisExecutor) {
        this(myBatisExecutor, null);
    }

    public UserAccountServiceImpl(MyBatisExecutor myBatisExecutor, AppSession appSession) {
        this.myBatisExecutor = myBatisExecutor;
        this.appSession = appSession;
    }

    @Override
    public UserAccount login(String username, String password) {
        String normalizedUsername = normalizeUsername(username, false);
        validatePasswordInput(password, "\u5bc6\u7801");
        return myBatisExecutor.executeRead(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount account = mapper.selectByUsername(normalizedUsername);
            if (account == null) {
                throw new BusinessException("\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u8bf7\u68c0\u67e5\u540e\u91cd\u65b0\u8f93\u5165\u3002");
            }
            ensureEnabled(account, "\u8d26\u53f7\u5df2\u505c\u7528\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u5904\u7406\u3002");
            if (account.getPasswordHash() == null || !PasswordHasher.matches(password, account.getPasswordHash())) {
                throw new BusinessException("\u5bc6\u7801\u9519\u8bef\uff0c\u8bf7\u91cd\u65b0\u8f93\u5165\u3002");
            }
            return account;
        });
    }

    @Override
    public Long registerStudent(String username, String password, String confirmPassword, String displayName, String phone) {
        String normalizedUsername = normalizeUsername(username, true);
        String normalizedDisplayName = normalizeDisplayName(displayName);
        String normalizedPhone = normalizePhone(phone);
        validatePasswordPair(password, confirmPassword);
        return myBatisExecutor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            ensureUsernameAvailable(mapper, normalizedUsername);
            UserAccount account = buildAccount(
                    normalizedUsername,
                    password,
                    normalizedDisplayName,
                    normalizedPhone,
                    UserRole.STUDENT
            );
            mapper.insertStudentAccount(account);
            return account.getId();
        });
    }

    @Override
    public void resetPasswordByPhone(String username, String phone, String newPassword, String confirmPassword) {
        String normalizedUsername = normalizeUsername(username, false);
        String normalizedPhone = normalizePhone(phone);
        validatePasswordPair(newPassword, confirmPassword);
        String passwordHash = PasswordHasher.sha256Hex(newPassword);
        myBatisExecutor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount account = mapper.selectByUsername(normalizedUsername);
            if (account == null) {
                throw new BusinessException("\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u8bf7\u68c0\u67e5\u540e\u91cd\u65b0\u8f93\u5165\u3002");
            }
            ensureEnabled(account, "\u8d26\u53f7\u5df2\u505c\u7528\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u5904\u7406\u3002");
            int affectedRows = mapper.updatePasswordHashByUsernameAndPhone(normalizedUsername, normalizedPhone, passwordHash);
            if (affectedRows == 0) {
                throw new BusinessException("\u8d26\u53f7\u4e0e\u624b\u673a\u53f7\u4e0d\u5339\u914d\uff0c\u65e0\u6cd5\u91cd\u7f6e\u5bc6\u7801\u3002");
            }
            return null;
        });
    }

    @Override
    public void changePassword(Long accountId, String oldPassword, String newPassword, String confirmPassword) {
        if (accountId == null) {
            throw new BusinessException("\u5f53\u524d\u8d26\u53f7\u672a\u767b\u5f55\uff0c\u65e0\u6cd5\u4fee\u6539\u5bc6\u7801\u3002");
        }
        validatePasswordInput(oldPassword, "\u65e7\u5bc6\u7801");
        validatePasswordPair(newPassword, confirmPassword);
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException("\u65b0\u5bc6\u7801\u4e0d\u80fd\u4e0e\u65e7\u5bc6\u7801\u76f8\u540c\u3002");
        }
        myBatisExecutor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount account = requireAccount(mapper, accountId, "\u5f53\u524d\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u4fee\u6539\u5bc6\u7801\u3002");
            ensureEnabled(account, "\u8d26\u53f7\u5df2\u505c\u7528\uff0c\u65e0\u6cd5\u4fee\u6539\u5bc6\u7801\u3002");
            if (account.getPasswordHash() == null || !PasswordHasher.matches(oldPassword, account.getPasswordHash())) {
                throw new BusinessException("\u65e7\u5bc6\u7801\u9519\u8bef\uff0c\u8bf7\u91cd\u65b0\u8f93\u5165\u3002");
            }
            mapper.updatePasswordHashById(accountId, PasswordHasher.sha256Hex(newPassword));
            return null;
        });
    }

    @Override
    public Long createInternalAccount(String username, String password, String confirmPassword, String displayName, String phone, UserRole role) {
        UserRole normalizedRole = normalizeInternalRole(role);
        String normalizedUsername = normalizeUsername(username, true);
        String normalizedDisplayName = normalizeDisplayName(displayName);
        String normalizedPhone = normalizePhone(phone);
        validatePasswordPair(password, confirmPassword);
        return myBatisExecutor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            ensureUsernameAvailable(mapper, normalizedUsername);
            UserAccount account = buildAccount(
                    normalizedUsername,
                    password,
                    normalizedDisplayName,
                    normalizedPhone,
                    normalizedRole
            );
            mapper.insertAccount(account);
            return account.getId();
        });
    }

    @Override
    public UserAccount updateInternalAccountProfile(Long accountId, String displayName, String phone, UserRole role) {
        if (accountId == null) {
            throw new BusinessException("\u8d26\u53f7\u4e0d\u5b58\u5728\u3002");
        }
        UserRole normalizedRole = normalizeInternalRole(role);
        String normalizedDisplayName = normalizeDisplayName(displayName);
        String normalizedPhone = normalizePhone(phone);
        return myBatisExecutor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount account = requireAccount(mapper, accountId, "\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u66f4\u65b0\u8d44\u6599\u3002");
            ensureEnabled(account, "\u8d26\u53f7\u5df2\u505c\u7528\uff0c\u65e0\u6cd5\u66f4\u65b0\u8d44\u6599\u3002");
            ensureInternalAccount(account, "\u5b66\u751f\u8d26\u53f7\u4e0d\u5728\u5185\u90e8\u8d26\u53f7\u7ba1\u7406\u8303\u56f4\u5185\u3002");
            mapper.updateInternalProfileById(accountId, normalizedDisplayName, normalizedPhone, normalizedRole);
            return requireAccount(mapper, accountId, "\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u8bfb\u53d6\u6700\u65b0\u8d44\u6599\u3002");
        });
    }

    @Override
    public UserAccount updateOwnProfile(Long accountId, String displayName, String phone) {
        if (accountId == null) {
            throw new BusinessException("\u8d26\u53f7\u4e0d\u5b58\u5728\u3002");
        }
        String normalizedDisplayName = normalizeDisplayName(displayName);
        String normalizedPhone = normalizePhone(phone);
        return myBatisExecutor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount account = requireAccount(mapper, accountId, "\u5f53\u524d\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u4fdd\u5b58\u8d44\u6599\u3002");
            ensureEnabled(account, "\u5f53\u524d\u8d26\u53f7\u5df2\u505c\u7528\uff0c\u65e0\u6cd5\u4fdd\u5b58\u8d44\u6599\u3002");
            mapper.updateOwnProfileById(accountId, normalizedDisplayName, normalizedPhone);
            return requireAccount(mapper, accountId, "\u5f53\u524d\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u8bfb\u53d6\u6700\u65b0\u8d44\u6599\u3002");
        });
    }

    @Override
    public void setAccountEnabled(Long accountId, boolean enabled) {
        if (accountId == null) {
            throw new BusinessException("\u8d26\u53f7\u4e0d\u5b58\u5728\u3002");
        }
        myBatisExecutor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount account = requireAccount(mapper, accountId, "\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u66f4\u65b0\u72b6\u6001\u3002");
            ensureInternalAccount(account, "\u5b66\u751f\u8d26\u53f7\u4e0d\u5728\u5185\u90e8\u8d26\u53f7\u7ba1\u7406\u8303\u56f4\u5185\u3002");
            mapper.updateEnabledById(accountId, enabled);
            return null;
        });
    }

    @Override
    public void adminResetPassword(Long accountId, String newPassword, String confirmPassword) {
        if (accountId == null) {
            throw new BusinessException("\u8d26\u53f7\u4e0d\u5b58\u5728\u3002");
        }
        validatePasswordPair(newPassword, confirmPassword);
        myBatisExecutor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount account = requireAccount(mapper, accountId, "\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u91cd\u7f6e\u5bc6\u7801\u3002");
            ensureInternalAccount(account, "\u5b66\u751f\u8d26\u53f7\u4e0d\u5728\u5185\u90e8\u8d26\u53f7\u7ba1\u7406\u8303\u56f4\u5185\u3002");
            mapper.updatePasswordHashById(accountId, PasswordHasher.sha256Hex(newPassword));
            return null;
        });
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        String normalizedUsername = normalizeUsername(username, true);
        return myBatisExecutor.executeRead(session -> !session.getMapper(UserAccountMapper.class).existsByUsername(normalizedUsername));
    }

    @Override
    public List<UserAccount> listAccounts(AccountFilter filter) {
        AccountFilter effectiveFilter = filter == null ? AccountFilter.all() : filter;
        return myBatisExecutor.executeRead(session -> session.getMapper(UserAccountMapper.class)
                .selectAccounts(effectiveFilter.roleCode(), effectiveFilter.enabled()));
    }

    @Override
    public UserAccount getById(Long accountId) {
        if (accountId == null) {
            return null;
        }
        return myBatisExecutor.executeRead(session -> session.getMapper(UserAccountMapper.class).selectById(accountId));
    }

    @Override
    public UserAccount requireCurrentAccount(AppSession appSession, UserRole expectedRole) {
        if (appSession == null || appSession.getCurrentAccountId() == null) {
            throw new IllegalStateException("\u5f53\u524d\u672a\u627e\u5230\u767b\u5f55\u4f1a\u8bdd\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55\u3002");
        }
        UserAccount account = getById(appSession.getCurrentAccountId());
        if (account == null) {
            throw new IllegalStateException("\u5f53\u524d\u767b\u5f55\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55\u3002");
        }
        if (expectedRole != null && account.getRoleCode() != expectedRole) {
            throw new IllegalStateException("\u5f53\u524d\u767b\u5f55\u8eab\u4efd\u4e0e\u9875\u9762\u6743\u9650\u4e0d\u5339\u914d\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55\u3002");
        }
        ensureEnabled(account, "\u5f53\u524d\u8d26\u53f7\u5df2\u505c\u7528\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u5904\u7406\u3002");
        return account;
    }

    @Override
    public List<UserAccount> listEnabledAccountsByRole(UserRole role) {
        if (role == null) {
            throw new BusinessException("\u89d2\u8272\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        return myBatisExecutor.executeRead(session -> session.getMapper(UserAccountMapper.class).selectEnabledByRole(role));
    }

    private static UserAccount buildAccount(
            String username,
            String password,
            String displayName,
            String phone,
            UserRole role
    ) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(PasswordHasher.sha256Hex(password));
        account.setDisplayName(displayName);
        account.setPhone(phone);
        account.setRoleCode(role);
        account.setEnabled(Boolean.TRUE);
        return account;
    }

    private static void ensureUsernameAvailable(UserAccountMapper mapper, String username) {
        if (mapper.existsByUsername(username)) {
            throw new BusinessException("\u8be5\u8d26\u53f7\u5df2\u5b58\u5728\uff0c\u8bf7\u66f4\u6362\u540e\u518d\u8bd5\u3002");
        }
    }

    private static UserAccount requireAccount(UserAccountMapper mapper, Long accountId, String notFoundMessage) {
        UserAccount account = mapper.selectById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
        return account;
    }

    private static void ensureEnabled(UserAccount account, String disabledMessage) {
        if (Boolean.FALSE.equals(account.getEnabled())) {
            throw new BusinessException(disabledMessage);
        }
    }

    private static void ensureInternalAccount(UserAccount account, String message) {
        if (account.getRoleCode() == UserRole.STUDENT) {
            throw new BusinessException(message);
        }
    }

    private static UserRole normalizeInternalRole(UserRole role) {
        if (role == null) {
            throw new BusinessException("\u8bf7\u9009\u62e9\u5185\u90e8\u8d26\u53f7\u89d2\u8272\u3002");
        }
        if (role == UserRole.STUDENT) {
            throw new BusinessException("\u5b66\u751f\u8d26\u53f7\u53ea\u80fd\u901a\u8fc7\u81ea\u52a9\u6ce8\u518c\u521b\u5efa\u3002");
        }
        return role;
    }

    private static String normalizeUsername(String username, boolean strictPattern) {
        if (username == null) {
            throw new BusinessException("\u8d26\u53f7\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        String normalized = username.trim();
        if (normalized.isBlank()) {
            throw new BusinessException("\u8d26\u53f7\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        if (strictPattern && !USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException("\u8d26\u53f7\u9700\u4e3a 4-32 \u4f4d\u5b57\u6bcd\u3001\u6570\u5b57\u6216\u4e0b\u5212\u7ebf\u3002");
        }
        return normalized;
    }

    private static String normalizeDisplayName(String displayName) {
        if (displayName == null) {
            throw new BusinessException("\u59d3\u540d\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        String normalized = displayName.trim();
        if (normalized.isBlank()) {
            throw new BusinessException("\u59d3\u540d\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        if (normalized.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new BusinessException("\u59d3\u540d\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc7 " + MAX_DISPLAY_NAME_LENGTH + " \u4e2a\u5b57\u7b26\u3002");
        }
        return normalized;
    }

    private static String normalizePhone(String phone) {
        if (phone == null) {
            throw new BusinessException("\u624b\u673a\u53f7\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        String normalized = phone.trim();
        if (normalized.isBlank()) {
            throw new BusinessException("\u624b\u673a\u53f7\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException("\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u5408\u6cd5\u3002");
        }
        return normalized;
    }

    private static void validatePasswordPair(String password, String confirmPassword) {
        validatePasswordInput(password, "\u5bc6\u7801");
        validatePasswordInput(confirmPassword, "\u786e\u8ba4\u5bc6\u7801");
        if (!password.equals(confirmPassword)) {
            throw new BusinessException("\u4e24\u6b21\u8f93\u5165\u7684\u5bc6\u7801\u4e0d\u4e00\u81f4\u3002");
        }
    }

    private Long currentOperatorId() {
        return appSession == null ? null : appSession.getCurrentAccountId();
    }

    private static String summarizeAccount(UserAccount account) {
        if (account == null) {
            return null;
        }
        return "username=" + account.getUsername() + ", displayName=" + account.getDisplayName() + ", phone=" + account.getPhone() + ", role=" + account.getRoleCode() + ", enabled=" + account.getEnabled();
    }

    private static void validatePasswordInput(String password, String fieldName) {
        if (password == null || password.isBlank()) {
            throw new BusinessException(fieldName + "\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new BusinessException(fieldName + "\u957f\u5ea6\u9700\u4e3a " + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH + " \u4f4d\u3002");
        }
    }
}