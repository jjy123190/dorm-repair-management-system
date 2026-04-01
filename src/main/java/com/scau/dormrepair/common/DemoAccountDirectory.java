package com.scau.dormrepair.common;

import com.scau.dormrepair.domain.enums.UserRole;
import java.util.List;

/**
 * Legacy local reference accounts only.
 * Do not use this class as a live authentication source.
 */
@Deprecated
public final class DemoAccountDirectory {

    private static final List<ReferenceAccount> STUDENTS = List.of(
            new ReferenceAccount("student01", "\u5f20\u4e09", UserRole.STUDENT),
            new ReferenceAccount("student02", "\u674e\u6653\u96e8", UserRole.STUDENT),
            new ReferenceAccount("student03", "\u76f8\u9022\u7684", UserRole.STUDENT)
    );

    private static final List<ReferenceAccount> ADMINS = List.of(
            new ReferenceAccount("admin01", "\u674e\u8001\u5e08", UserRole.ADMIN),
            new ReferenceAccount("admin02", "\u9648\u8001\u5e08", UserRole.ADMIN)
    );

    private static final List<ReferenceAccount> WORKERS = List.of(
            new ReferenceAccount("worker01", "\u738b\u5e08\u5085", UserRole.WORKER),
            new ReferenceAccount("worker02", "\u5468\u5e08\u5085", UserRole.WORKER),
            new ReferenceAccount("worker03", "\u9648\u5e08\u5085", UserRole.WORKER)
    );

    private DemoAccountDirectory() {
    }

    public static List<ReferenceAccount> referenceAccounts() {
        return List.of(
                STUDENTS.get(0), STUDENTS.get(1), STUDENTS.get(2),
                ADMINS.get(0), ADMINS.get(1),
                WORKERS.get(0), WORKERS.get(1), WORKERS.get(2)
        );
    }

    public static List<ReferenceAccount> referenceAccounts(UserRole role) {
        if (role == null) {
            return List.of();
        }
        return switch (role) {
            case STUDENT -> STUDENTS;
            case ADMIN -> ADMINS;
            case WORKER -> WORKERS;
        };
    }

    public record ReferenceAccount(String username, String displayName, UserRole role) {
    }
}