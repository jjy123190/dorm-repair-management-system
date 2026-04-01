package com.scau.dormrepair.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.common.PasswordHasher;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.service.UserAccountIntegrationSupport;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserAccountMapperTest extends UserAccountIntegrationSupport {

    @Test
    void shouldInsertQueryAndUpdateInternalAccount() {
        String username = uniqueUsername("mapper");

        executor.executeWrite(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount account = new UserAccount();
            account.setUsername(username);
            account.setPasswordHash(PasswordHasher.sha256Hex("mapper123"));
            account.setDisplayName("Mapper用户");
            account.setPhone("13855550009");
            account.setRoleCode(UserRole.WORKER);
            account.setEnabled(Boolean.TRUE);
            mapper.insertAccount(account);
            mapper.updateEnabledById(account.getId(), false);
            mapper.updatePasswordHashById(account.getId(), PasswordHasher.sha256Hex("mapper456"));
            return null;
        });

        executor.executeRead(session -> {
            UserAccountMapper mapper = session.getMapper(UserAccountMapper.class);
            UserAccount byUsername = mapper.selectByUsername(username);
            assertNotNull(byUsername);
            assertEquals(UserRole.WORKER, byUsername.getRoleCode());
            assertEquals(Boolean.FALSE, byUsername.getEnabled());
            assertTrue(PasswordHasher.matches("mapper456", byUsername.getPasswordHash()));

            List<UserAccount> disabledWorkers = mapper.selectAccounts(UserRole.WORKER, Boolean.FALSE);
            assertTrue(disabledWorkers.stream().anyMatch(account -> username.equals(account.getUsername())));
            return null;
        });
    }
}