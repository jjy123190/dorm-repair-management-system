package com.scau.dormrepair.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;

class DemoAccountDirectoryTest {

    @Test
    void shouldExposeStableAccountsPerRole() {
        List<DemoAccount> students = DemoAccountDirectory.accountOptions(UserRole.STUDENT);

        assertEquals(3, students.size());
        assertEquals(1001L, students.get(0).id());
        assertEquals("张三", students.get(0).displayName());
        assertSame(students.get(0), DemoAccountDirectory.defaultAccount(UserRole.STUDENT));
    }

    @Test
    void shouldResolveCurrentAccountByStableAccountIdFirst() {
        AppSession appSession = new AppSession();
        appSession.login("张三", UserRole.STUDENT);
        appSession.currentAccountIdProperty().set(1002L);

        DemoAccount current = DemoAccountDirectory.resolveCurrent(appSession);

        assertEquals(1002L, current.id());
        assertEquals("李晓雨", current.displayName());
    }

    @Test
    void shouldFallbackToDefaultAccountWhenDisplayNameDoesNotMatch() {
        AppSession appSession = new AppSession();
        appSession.login("陌生同学", UserRole.STUDENT);

        DemoAccount current = DemoAccountDirectory.resolveCurrent(appSession);

        assertEquals(1001L, current.id());
        assertEquals("张三", current.displayName());
    }

    @Test
    void shouldReturnWorkerDisplayNameOrReadableFallback() {
        assertEquals("王师傅", DemoAccountDirectory.workerName(3001L));
        assertEquals("维修员#3999", DemoAccountDirectory.workerName(3999L));
        assertEquals("", DemoAccountDirectory.workerName(null));
    }

    @Test
    void shouldReturnNullWhenSessionHasNoRole() {
        assertNull(DemoAccountDirectory.resolveCurrent(new AppSession()));
        assertNull(DemoAccountDirectory.resolveCurrent(null));
    }
}
