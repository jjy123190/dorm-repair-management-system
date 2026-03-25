package com.scau.dormrepair.common;

import com.scau.dormrepair.domain.enums.UserRole;
import java.util.List;

/**
 * 现在项目还没有正式登录体系。
 * 这里先把几组稳定的演示账号收口，保证学生报修、管理员派单、维修员处理三条链路能贯通。
 */
public final class DemoAccountDirectory {

    private static final DemoAccount STUDENT = new DemoAccount(1001L, "张三", UserRole.STUDENT);
    private static final DemoAccount ADMIN = new DemoAccount(2001L, "李老师", UserRole.ADMIN);
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

        return switch (appSession.getCurrentRole()) {
            case STUDENT -> STUDENT;
            case ADMIN -> ADMIN;
            case WORKER -> resolveWorkerByName(appSession.getDisplayName());
        };
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

    private static DemoAccount resolveWorkerByName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return WORKERS.get(0);
        }
        return WORKERS.stream()
                .filter(worker -> worker.displayName().equals(displayName.trim()))
                .findFirst()
                .orElse(WORKERS.get(0));
    }

    public record DemoAccount(Long id, String displayName, UserRole role) {

        @Override
        public String toString() {
            return displayName;
        }
    }
}
