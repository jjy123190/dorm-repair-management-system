package com.scau.dormrepair.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

class AppSessionTest {

    @Test
    void shouldTrimDisplayNameForManualLogin() {
        AppSession appSession = new AppSession();

        appSession.login("  张三  ", UserRole.STUDENT);

        assertTrue(appSession.isAuthenticated());
        assertEquals("张三", appSession.getDisplayName());
        assertEquals(UserRole.STUDENT, appSession.getCurrentRole());
        assertNull(appSession.getCurrentAccountId());
    }

    @Test
    void shouldFallbackToUnnamedUserWhenManualNameIsBlank() {
        AppSession appSession = new AppSession();

        appSession.login("   ", UserRole.ADMIN);

        assertEquals("未命名用户", appSession.getDisplayName());
        assertEquals(UserRole.ADMIN, appSession.getCurrentRole());
    }

    @Test
    void shouldStoreStableAccountIdentity() {
        AppSession appSession = new AppSession();
        DemoAccountDirectory.DemoAccount account = DemoAccountDirectory.defaultAccount(UserRole.WORKER);

        appSession.login(account);

        assertTrue(appSession.isAuthenticated());
        assertEquals(account.displayName(), appSession.getDisplayName());
        assertEquals(account.role(), appSession.getCurrentRole());
        assertEquals(account.id(), appSession.getCurrentAccountId());
    }

    @Test
    void shouldRejectNullStableAccountLogin() {
        AppSession appSession = new AppSession();

        assertThrows(IllegalArgumentException.class, () -> appSession.login((DemoAccountDirectory.DemoAccount) null));
    }

    @Test
    void shouldResetSessionOnLogout() {
        AppSession appSession = new AppSession();
        appSession.login(DemoAccountDirectory.defaultAccount(UserRole.STUDENT));

        appSession.logout();

        assertFalse(appSession.isAuthenticated());
        assertEquals("未登录", appSession.getDisplayName());
        assertNull(appSession.getCurrentRole());
        assertNull(appSession.getCurrentAccountId());
    }
}
