package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

/**
 * 学生报修记录单独成页，只负责查看记录。
 * 这样提交表单和历史记录不会再挤在同一屏里。
 */
public class StudentRepairHistoryModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StudentRepairHistoryModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "student-history";
    }

    @Override
    public String moduleName() {
        return "报修记录";
    }

    @Override
    public String moduleDescription() {
        return "";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.STUDENT);
    }

    @Override
    public boolean cacheViewOnSwitch() {
        return true;
    }

    @Override
    public Parent createView() {
        DemoAccount currentStudent = DemoAccountDirectory.resolveCurrent(appContext.appSession());
        if (currentStudent == null) {
            throw new IllegalStateException("学生身份未初始化，无法进入报修记录模块。");
        }

        List<RecentRepairRequestView> historyRows =
                appContext.repairRequestService().listStudentSubmittedRequests(currentStudent.id(), 20);

        VBox content = new VBox(
                18,
                wrapPanel("最近报修记录", buildHistoryTable(historyRows))
        );
        content.setFillWidth(true);

        return createPage("学生报修记录", "", content);
    }

    private Parent buildHistoryTable(List<RecentRepairRequestView> historyRows) {
        return (Parent) createStaticDataTable(
                List.of(
                        staticColumn("报修单号", 1.618, RecentRepairRequestView::getRequestNo),
                        staticColumn("宿舍", 1.382, RecentRepairRequestView::getLocationText),
                        staticColumn("故障类型", 1.236, item -> UiDisplayText.faultCategory(item.getFaultCategory())),
                        staticColumn("状态", 0.764, item -> UiDisplayText.repairRequestStatus(item.getStatus())),
                        staticColumn("提交时间", 1.382, item ->
                                item.getSubmittedAt() == null ? "" : TIME_FORMATTER.format(item.getSubmittedAt()))
                ),
                historyRows,
                4
        );
    }
}
