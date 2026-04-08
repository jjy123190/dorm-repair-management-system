package com.scau.dormrepair.ui.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.WorkOrderRecordView;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import javafx.scene.Parent;
import org.junit.jupiter.api.Test;

class UiModuleCopySmokeTest {

    @Test
    void shouldExposeStableChineseModuleNames() {
        assertEquals("\u7ba1\u7406\u5458\u6d3e\u5355", new AdminDispatchModule(null).moduleName());
        assertEquals("\u7ef4\u4fee\u5904\u7406", new WorkerProcessingModule(null).moduleName());
        assertEquals("\u5bbf\u820d\u76ee\u5f55", new DormCatalogManagementModule(null).moduleName());
    }

    @Test
    void shouldKeepKeyPromptsReadableInSourceFiles() throws IOException {
        String adminSource = readSource("src/main/java/com/scau/dormrepair/ui/module/AdminDispatchModule.java");
        String abstractSource = readSource("src/main/java/com/scau/dormrepair/ui/module/AbstractWorkbenchModule.java");
        String workerSource = readSource("src/main/java/com/scau/dormrepair/ui/module/WorkerProcessingModule.java");
        String dormSource = readSource("src/main/java/com/scau/dormrepair/ui/module/DormCatalogManagementModule.java");

        assertTrue(adminSource.contains("\u641c\u7d22\u62a5\u4fee\u5355\u53f7\u3001\u5b66\u751f\u6216\u5bbf\u820d\u4f4d\u7f6e"));
        assertTrue(abstractSource.contains("resolveCompactWorkspaceBreakpoint"));
        assertTrue(adminSource.contains("\u521b\u5efa\u5de5\u5355"));
        assertTrue(workerSource.contains("\u641c\u7d22\u5de5\u5355\u53f7\u3001\u62a5\u4fee\u5355\u53f7\u3001\u62a5\u4fee\u4eba\u6216\u5bbf\u820d\u4f4d\u7f6e"));
        assertTrue(workerSource.contains("\u66f4\u65b0\u5904\u7406\u72b6\u6001"));
        assertTrue(dormSource.contains("\u641c\u7d22\u5bbf\u820d\u533a\u3001\u697c\u680b\u7f16\u53f7\u6216\u697c\u680b\u540d\u79f0"));
        assertTrue(dormSource.contains("\u4fdd\u5b58\u697c\u680b"));

        assertFalse(abstractSource.contains("COMPACT_WORKSPACE_BREAKPOINT = 1120"));
        assertFalse(adminSource.contains("\u7ee0\uff3c\u60a7\u935b\u6dc3\u9357"));
        assertFalse(workerSource.contains("\u7f01\u4da8\u6170\u6fa7\u52d5\u501e"));
        assertFalse(dormSource.contains("\u7009\u80c4\u5797\u942e\u989c\u79ff"));
    }

    @Test
    void shouldRenderSharedTimelineCopyInChinese() throws Exception {
        TestModule module = new TestModule();
        WorkOrderRecordView record = new WorkOrderRecordView();
        record.setOperatorName("\u5f20\u4e09");
        record.setOperatorRole(UserRole.WORKER);
        record.setRecordNote("\u5df2\u4e0a\u95e8\u68c0\u6d4b");

        Method method = AbstractWorkbenchModule.class.getDeclaredMethod("composeTimelineCopy", WorkOrderRecordView.class);
        method.setAccessible(true);
        String text = (String) method.invoke(module, record);

        assertEquals("\u64cd\u4f5c\u4eba\uff1a\u5f20\u4e09 / \u7ef4\u4fee\u5458" + System.lineSeparator() + "\u5907\u6ce8\uff1a\u5df2\u4e0a\u95e8\u68c0\u6d4b", text);
    }

    private String readSource(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath), StandardCharsets.UTF_8);
    }

    private static final class TestModule extends AbstractWorkbenchModule {

        private TestModule() {
            super(null);
        }

        @Override
        public String moduleCode() {
            return "test";
        }

        @Override
        public String moduleName() {
            return "test";
        }

        @Override
        public String moduleDescription() {
            return "";
        }

        @Override
        public Set<UserRole> supportedRoles() {
            return EnumSet.noneOf(UserRole.class);
        }

        @Override
        public Parent createView() {
            return null;
        }
    }
}
