package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * 学生报修记录独立页。
 * 这里专门用来看记录，避免和提交表单挤在一屏导致信息难看清。
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
        return "\u62a5\u4fee\u8bb0\u5f55";
    }

    @Override
    public String moduleDescription() {
        return "\u5355\u72ec\u67e5\u770b\u81ea\u5df1\u7684\u6700\u8fd1\u62a5\u4fee\u8bb0\u5f55\uff0c\u4e0d\u518d\u548c\u63d0\u4ea4\u8868\u5355\u6324\u5728\u4e00\u5c4f\u3002";
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
            throw new IllegalStateException("\u5b66\u751f\u8eab\u4efd\u672a\u521d\u59cb\u5316\uff0c\u65e0\u6cd5\u8fdb\u5165\u62a5\u4fee\u8bb0\u5f55\u6a21\u5757\u3002");
        }

        TableView<RecentRepairRequestView> historyTable = buildHistoryTable();
        refreshStudentHistory(historyTable, currentStudent.id());

        VBox content = new VBox(18, wrapPanel("\u6700\u8fd1\u62a5\u4fee\u8bb0\u5f55", historyTable));
        content.setFillWidth(true);

        return createPage(
                "\u5b66\u751f\u62a5\u4fee\u8bb0\u5f55",
                "\u8fd9\u4e2a\u9875\u9762\u53ea\u8d1f\u8d23\u770b\u8bb0\u5f55\uff0c\u4f60\u53ef\u4ee5\u76f4\u63a5\u6838\u5bf9\u5355\u53f7\u3001\u5bbf\u820d\u3001\u6545\u969c\u7c7b\u578b\u548c\u5f53\u524d\u72b6\u6001\u3002",
                content
        );
    }

    private TableView<RecentRepairRequestView> buildHistoryTable() {
        TableView<RecentRepairRequestView> historyTable = new TableView<>();
        historyTable.setPlaceholder(new Label("\u6682\u65e0\u62a5\u4fee\u8bb0\u5f55"));
        historyTable.getColumns().addAll(
                createTextColumn("\u62a5\u4fee\u5355\u53f7", "requestNo", 120),
                createTextColumn("\u5bbf\u820d", "locationText", 120),
                createFaultColumn(),
                createStatusColumn(),
                createDateTimeColumn("\u63d0\u4ea4\u65f6\u95f4")
        );
        configureFixedTable(historyTable, 540, 1.618, 1.382, 1.236, 0.764, 1.382);
        return historyTable;
    }

    private void refreshStudentHistory(TableView<RecentRepairRequestView> historyTable, Long studentId) {
        historyTable.setItems(FXCollections.observableArrayList(
                appContext.repairRequestService().listStudentSubmittedRequests(studentId, 20)
        ));
        fitTableHeightToRows(historyTable, historyTable.getItems().size(), 1, 8);
    }

    private TableColumn<RecentRepairRequestView, String> createTextColumn(String title, String property, double minWidth) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(minWidth);
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createFaultColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("\u6545\u969c\u7c7b\u578b");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.faultCategory(cell.getValue().getFaultCategory()))
        );
        column.setMinWidth(128);
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createStatusColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("\u72b6\u6001");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.repairRequestStatus(cell.getValue().getStatus()))
        );
        column.setMinWidth(96);
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createDateTimeColumn(String title) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> {
                    if (cell.getValue().getSubmittedAt() == null) {
                        return "";
                    }
                    return TIME_FORMATTER.format(cell.getValue().getSubmittedAt());
                })
        );
        column.setMinWidth(168);
        return column;
    }
}
