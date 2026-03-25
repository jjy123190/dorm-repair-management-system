# ABCD协作历史记录

这份文档专门记录 `A / B / C / D` 四块协作进度。

## 使用规则

- 每次开始改之前，先读这份文档
- 每次改完后，必须补一条日志
- 这轮任务如果已经做完，写完日志后必须继续 `push`，不要把已完成改动长时间只留在本地
- 一条日志只写本次真实改动，不写空话
- 如果这次改动影响到别的块，要在日志里点名提醒
- 如果修改了共享文件，日志里必须把文件名写明

## 建议格式

```md
### 2026-03-25 16:20 | A
- 改动文件：
  - `src/main/resources/styles/app.css`
  - `src/main/java/com/scau/dormrepair/ui/LoginView.java`
- 完成内容：
  - 登录页入口卡片重做
  - 修掉标题出框
- 影响提醒：
  - 动了 `app.css`，B / C 如果也改样式，先拉最新再改
- Push 结果：
  - 已 push / 未 push
  - 提交号：
- 下一步：
  - 继续收首页卡片间距
```

## 当前基线

### 2026-03-25 16:24 | D
- 改动文件：
  - `项目协作根基.md`
  - `AGENTS.md`
  - `ABCD协作历史记录.md`
- 完成内容：
  - 纠正认领关系，明确写成“你本人负责 A”
- 影响提醒：
  - 队友默认不要直接改 A 区域文件，尤其是 `AppShell.java`、`LoginView.java`、`app.css`
- Push 结果：
  - 已 push / 未 push
  - 提交号：
- 下一步：
  - B / C / D 认领后，各自在这里继续补自己的第一条日志

### 2026-03-25 16:20 | D
- 改动文件：
  - `项目协作根基.md`
  - `AGENTS.md`
  - `README.md`
  - `ABCD协作历史记录.md`
- 完成内容：
  - 把协作前提写死：开始前先 `git pull origin main`
  - 把项目拆成 `A / B / C / D` 四块
  - 明确所有改动都必须同步记日志
- 影响提醒：
  - 从现在开始，任何人开工前都先看 `项目协作根基.md` 和这份历史记录
- Push 结果：
  - 已 push / 未 push
  - 提交号：
- 下一步：
  - 各自认领模块后，第一条真实开发日志就接着往下写



### 2026-03-25 16:46 | D
- 改动文件：
  - `项目协作根基.md`
  - `AGENTS.md`
  - `ABCD协作历史记录.md`
- 完成内容：
  - 新增“每次完成任务后必须尽快 commit + push”的协作硬规则
  - 把日志模板补成要记录 push 结果和提交号
- 影响提醒：
  - 从现在开始，谁本地做完却不 push，就算流程没走完
  - 队友和各自的 Codex 都要遵守“先 pull，做完就 push”
- Push 结果：
  - 本次文档更新将随当前这轮提交一起推送
  - 提交号：以本次 push 后的最新提交为准
- 下一步：
  - A / B / C / D 后续每条日志都按新模板补 push 结果
### 2026-03-25 16:41 | A
- 改动文件：
  - `src/main/java/com/scau/dormrepair/DormRepairApplication.java`
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/support/UiAlerts.java`
  - `src/main/resources/styles/app.css`
- 完成内容：
  - 重做工作台顶部壳层，把旧的大标题头部改成更紧凑的品牌区 + 当前模块摘要栏
  - 启动失败不再回退系统默认 Alert，统一走项目自己的提示弹窗
  - 同步补了 header 这层的新样式类，收紧退出按钮和角色/模块信息展示
- 影响提醒：
  - 这次动了 `AppShell.java` 和 `app.css`，B / C 如果本地还保留旧壳层缓存，先 pull 最新再接业务页
  - 当前 `mvn -DskipTests compile` 仍然会带机器环境尾噪，但编译结果实际是 BUILD SUCCESS
- Push 结果：
  - 本次 A 改动将随当前这轮提交一起推送
  - 提交号：以本次 push 后的最新提交为准
- 下一步：
  - 继续收登录页和工作台首页的高级感，尤其是顶层窗口感和信息密度

### 2026-03-25 16:55 | A
- 改动文件：
  - `src/main/resources/styles/app.css`
- 完成内容：
  - 统一重写全局滚轴样式，把默认白色粗滚轴改成细拇指 + 透明轨道
  - 隐藏上下箭头按钮，让表格、文本域和滚动容器都走同一套低存在感滚轴
  - 保留 hover 时更清晰的 thumb 反馈，避免完全透明后找不到拖拽位置
- 影响提醒：
  - 这次动的是全局 `.scroll-bar`，B / C 新增可滚动控件时会自动继承这套样式
  - 当前桌面端已重新启动验证，滚轴样式需要在新实例里看，不看旧窗口
- Push 结果：
  - 本次 A 改动将随当前这轮提交一起推送
  - 提交号：以本次 push 后的最新提交为准
- 下一步：
  - 继续收首页和业务页里仍然偏丑的默认控件，优先处理表格和输入框的廉价感

### 2026-03-25 17:15 | A
- 改动文件：
  - `src/main/java/com/scau/dormrepair/ui/support/UiMotion.java`
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AdminDispatchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/WorkerProcessingModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容：
  - 全局排查了 UI 卡顿和组件乱动的根因，并直接收掉最明显的 4 处
  - 下拉框不再给弹层做位移和缩放动画，避免“自己变大、自己移动”
  - 页面切换去掉淡入，改成直接静态替换
  - 管理员页和维修页的左右区块从弹性 `HBox` 改成固定比例 `GridPane`
  - 全局卡片、对话框、登录页和按钮的重阴影全部减掉，降低重绘压力
- 影响提醒：
  - 这次同时动了 `UiMotion.java` 和 `app.css`，B / C 如果本地还带旧下拉动画，先 pull 最新再看交互
  - 目前次一级风险点还剩 `TableView.CONSTRAINED_RESIZE_POLICY`，后面如果表格宽度继续抖，再继续改列宽策略
- Push 结果：
  - 本次 A 改动将随当前这轮提交一起推送
  - 提交号：以本次 push 后的最新提交为准
- 下一步：
  - 继续处理表格列宽重算和个别文本溢出点，把剩余的“抖”和“卡”清到最低
