package com.scau.dormrepair.common;

import com.scau.dormrepair.domain.enums.UserRole;
import java.util.List;

/**
 * 正式登录体系还没接数据库前，先把本地可用账号收口到这里。
 * 页面统一从稳定账号列表里选身份，不再允许手填名字伪装成不同用户。
 */
public final class DemoAccountDirectory {

    private static final List<DemoAccount> STUDENTS = List.of(
            new DemoAccount(1001L, "张三", UserRole.STUDENT),
            new DemoAccount(1002L, "李晓雨", UserRole.STUDENT),
            new DemoAccount(1003L, "相逢的", UserRole.STUDENT)
    );

    private static final List<DemoAccount> ADMINS = List.of(
            new DemoAccount(2001L, "李老师", UserRole.ADMIN),
            new DemoAccount(2002L, "陈老师", UserRole.ADMIN)
    );

    private static final List<DemoAccount> WORKERS = List.of(
            new DemoAccount(3001L, "王师傅", UserRole.WORKER),
            new DemoAccount(3002L, "周师傅", UserRole.WORKER),
            new DemoAccount(3003L, "陈师傅", UserRole.WORKER)
    );

    private DemoAccountDirectory() {
    }

    public static DemoAccount resolveCurrent(AppSession appSession) {
        if (appSession == null || appSession.getCurrentRole() == null) {
            return null;
        }

        Long accountId = appSession.getCurrentAccountId();
        if (accountId != null) {
            return accountOptions(appSession.getCurrentRole()).stream()
                    .filter(account -> account.id().equals(accountId))
                    .findFirst()
                    .orElse(defaultAccount(appSession.getCurrentRole()));
        }

        String displayName = appSession.getDisplayName();
        return accountOptions(appSession.getCurrentRole()).stream()
                .filter(account -> account.displayName().equals(displayName))
                .findFirst()
                .orElse(defaultAccount(appSession.getCurrentRole()));
    }

    public static List<DemoAccount> accountOptions(UserRole role) {
        return switch (role) {
            case STUDENT -> STUDENTS;
            case ADMIN -> ADMINS;
            case WORKER -> WORKERS;
        };
    }

    public static DemoAccount defaultAccount(UserRole role) {
        List<DemoAccount> options = accountOptions(role);
        if (options.isEmpty()) {
            throw new IllegalStateException("当前角色没有可用账号: " + role);
        }
        return options.get(0);
    }

    public static List<DemoAccount> workerOptions() {
        return WORKERS;
    }

    public static String workerName(Long workerId) {
        if (workerId == null) {
            return "";
        }
        return WORKERS.stream()
                .filter(worker -> worker.id().equals(workerId))
                .map(DemoAccount::displayName)
                .findFirst()
                .orElse("维修员#" + workerId);
    }

    public record DemoAccount(Long id, String displayName, UserRole role) {

        @Override
        public String toString() {
            return displayName;
        }
    }
}
