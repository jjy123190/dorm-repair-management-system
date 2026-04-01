package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.AuditLogView;
import com.scau.dormrepair.ui.component.AppDropdown;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AuditLogModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AuditLogModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "audit-log";
    }

    @Override
    public String moduleName() {
        return "操作审计";
    }

    @Override
    public String moduleDescription() {
        return "";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.ADMIN);
    }

    @Override
    public Parent createView() {
        AppDropdown<ActionFilterOption> actionBox = new AppDropdown<>();
        actionBox.setItems(List.of(ActionFilterOption.values()));
        actionBox.setTextMapper(ActionFilterOption::label);
        actionBox.setValue(ActionFilterOption.ALL);
        actionBox.setVisibleRowCount(10);

        AppDropdown<TimeRangeOption> timeBox = new AppDropdown<>();
        timeBox.setItems(List.of(TimeRangeOption.values()));
        timeBox.setTextMapper(TimeRangeOption::label);
        timeBox.setValue(TimeRangeOption.LAST_7_DAYS);
        timeBox.setVisibleRowCount(6);

        AppDropdown<OperatorFilterOption> operatorBox = new AppDropdown<>();
        operatorBox.setTextMapper(OperatorFilterOption::label);
        operatorBox.setVisibleRowCount(10);

        VBox tableContainer = new VBox();
        tableContainer.setFillWidth(true);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        Runnable refresh = () -> renderTable(tableContainer, actionBox.getValue(), operatorBox.getValue(), timeBox.getValue());

        List<OperatorFilterOption> operators = loadOperatorOptions();
        operatorBox.setItems(operators);
        operatorBox.setValue(operators.get(0));
        actionBox.valueProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        operatorBox.valueProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        timeBox.valueProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        refresh.run();

        GridPane filterGrid = createFilterGrid(28, 28, 28, 16);
        var refreshButton = createFilterActionButton("刷新列表", refresh);
        filterGrid.add(createFieldLabel("动作类型"), 0, 0);
        filterGrid.add(createFieldLabel("操作人"), 1, 0);
        filterGrid.add(createFieldLabel("时间范围"), 2, 0);
        filterGrid.add(actionBox, 0, 1);
        filterGrid.add(operatorBox, 1, 1);
        filterGrid.add(timeBox, 2, 1);
        filterGrid.add(refreshButton, 3, 1);
        GridPane.setValignment(refreshButton, VPos.TOP);
        GridPane.setHgrow(actionBox, Priority.ALWAYS);
        GridPane.setHgrow(operatorBox, Priority.ALWAYS);
        GridPane.setHgrow(timeBox, Priority.ALWAYS);

        VBox content = new VBox(16, filterGrid, tableContainer);
        content.setFillWidth(true);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        return createPage(
                "关键操作审计",
                "集中回看派单、状态变更、账号维护和宿舍目录维护的关键留痕，并支持最小可用筛选。",
                wrapPanel("审计记录", content)
        );
    }

    private List<OperatorFilterOption> loadOperatorOptions() {
        Map<Long, OperatorFilterOption> options = new LinkedHashMap<>();
        options.put(null, new OperatorFilterOption(null, "全部操作人"));
        appContext.auditLogService().listLatest(200).stream()
                .filter(row -> row.getOperatorId() != null)
                .sorted(Comparator.comparing(this::operatorText))
                .forEach(row -> options.putIfAbsent(row.getOperatorId(), new OperatorFilterOption(row.getOperatorId(), operatorText(row))));
        return List.copyOf(options.values());
    }

    private void renderTable(
            VBox tableContainer,
            ActionFilterOption actionFilter,
            OperatorFilterOption operatorFilter,
            TimeRangeOption timeRange
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdFrom = timeRange == null ? null : timeRange.resolveFrom(now);
        List<AuditLogView> rows = appContext.auditLogService().listFiltered(
                actionFilter == null ? null : actionFilter.actionType(),
                operatorFilter == null ? null : operatorFilter.operatorId(),
                createdFrom,
                now,
                60
        );
        tableContainer.getChildren().setAll(buildTable(rows));
    }

    private Parent buildTable(List<AuditLogView> rows) {
        return (Parent) createStaticDataTableOrEmpty(
                List.of(
                        staticColumn("时间", 0.95, item -> formatTime(item.getCreatedAt())),
                        staticColumn("操作人", 1.1, this::operatorText),
                        staticColumn("对象 + 动作", 1.55, this::targetAndActionText),
                        staticColumn("前后值", 2.2, this::changeText)
                ),
                rows,
                10,
                "暂无审计数据",
                "关键操作产生后，符合筛选条件的记录会显示在这里。"
        );
    }

    private String operatorText(AuditLogView row) {
        String name = blankToDash(row.getOperatorName());
        String role = blankToDash(row.getOperatorRole());
        return name + " / " + role;
    }

    private String targetAndActionText(AuditLogView row) {
        return actionText(row.getActionType())
                + " / "
                + targetTypeText(row.getTargetType())
                + " / "
                + blankToDash(row.getTargetLabel());
    }

    private String changeText(AuditLogView row) {
        String oldValue = trimToNull(row.getOldValue());
        String newValue = trimToNull(row.getNewValue());
        if (oldValue == null && newValue == null) {
            return "--";
        }
        if (oldValue == null) {
            return "新值：" + prettyValue(newValue);
        }
        if (newValue == null) {
            return "原值：" + prettyValue(oldValue);
        }
        return "原值：" + prettyValue(oldValue) + " -> 新值：" + prettyValue(newValue);
    }

    private String prettyValue(String raw) {
        if (raw == null) {
            return "--";
        }
        return raw.replace(',', '，').replace('=', '：');
    }

    private String actionText(String actionType) {
        if (actionType == null || actionType.isBlank()) {
            return "--";
        }
        return switch (actionType) {
            case "WORK_ORDER_ASSIGN" -> "派单";
            case "WORK_ORDER_STATUS_UPDATE" -> "工单状态变更";
            case "USER_ACCOUNT_ENABLE" -> "启用账号";
            case "USER_ACCOUNT_DISABLE" -> "停用账号";
            case "USER_ACCOUNT_RESET_PASSWORD" -> "重置密码";
            case "USER_ACCOUNT_ROLE_CHANGE" -> "角色变更";
            case "DORM_BUILDING_CREATE" -> "新增楼栋";
            case "DORM_BUILDING_UPDATE" -> "编辑楼栋";
            case "DORM_BUILDING_DELETE" -> "删除楼栋";
            case "DORM_ROOM_CREATE" -> "新增房间";
            case "DORM_ROOM_UPDATE" -> "编辑房间";
            case "DORM_ROOM_DELETE" -> "删除房间";
            default -> actionType;
        };
    }

    private String targetTypeText(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            return "--";
        }
        return switch (targetType) {
            case "WORK_ORDER" -> "工单";
            case "USER_ACCOUNT" -> "账号";
            case "DORM_BUILDING" -> "楼栋";
            case "DORM_ROOM" -> "房间";
            default -> targetType;
        };
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "--" : TIME_FORMATTER.format(value);
    }

    private String blankToDash(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "--" : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private enum ActionFilterOption {
        ALL("全部动作", null),
        WORK_ORDER_ASSIGN("派单", "WORK_ORDER_ASSIGN"),
        WORK_ORDER_STATUS_UPDATE("工单状态变更", "WORK_ORDER_STATUS_UPDATE"),
        USER_ACCOUNT_DISABLE("账号启停用", "USER_ACCOUNT_DISABLE"),
        USER_ACCOUNT_ENABLE("账号启停用", "USER_ACCOUNT_ENABLE"),
        USER_ACCOUNT_RESET_PASSWORD("重置密码", "USER_ACCOUNT_RESET_PASSWORD"),
        USER_ACCOUNT_ROLE_CHANGE("角色变更", "USER_ACCOUNT_ROLE_CHANGE"),
        DORM_BUILDING_UPDATE("楼栋维护", "DORM_BUILDING_UPDATE"),
        DORM_ROOM_UPDATE("房间维护", "DORM_ROOM_UPDATE");

        private final String label;
        private final String actionType;

        ActionFilterOption(String label, String actionType) {
            this.label = label;
            this.actionType = actionType;
        }

        public String label() {
            return label;
        }

        public String actionType() {
            return actionType;
        }
    }

    private record OperatorFilterOption(Long operatorId, String label) {
    }

    private enum TimeRangeOption {
        TODAY("今天", 0),
        LAST_3_DAYS("近 3 天", 3),
        LAST_7_DAYS("近 7 天", 7),
        LAST_30_DAYS("近 30 天", 30),
        ALL("不限", -1);

        private final String label;
        private final int days;

        TimeRangeOption(String label, int days) {
            this.label = label;
            this.days = days;
        }

        public String label() {
            return label;
        }

        public LocalDateTime resolveFrom(LocalDateTime now) {
            if (days < 0) {
                return null;
            }
            if (days == 0) {
                return now.toLocalDate().atStartOfDay();
            }
            return now.minusDays(days);
        }
    }
}