# ABCD协作历史记录

这份文档专门记录 `A / B / C / D` 四块协作的真实进度，给你、小组成员和各自的 Codex 看。

## 使用规则

- 每次开始改代码前，先 `git pull origin main`
- `pull` 完后先读：
  - `项目协作根基.md`
  - `ABCD协作历史记录.md`
- 凡是改动，结束前必须补一条日志
- 这轮任务确认完成后，必须立刻 `commit + push`
- 日志必须写清：
  - 时间
  - 模块归属（A / B / C / D）
  - 改动文件
  - 完成内容
  - 影响提醒 / 下一步
  - Push 结果和提交号

## 日志模板

```md
### 2026-03-25 20:30 | A
- 改动文件
  - `src/main/resources/styles/app.css`
  - `src/main/java/com/scau/dormrepair/ui/LoginView.java`
- 完成内容
  - 登录页角色卡重新排版，修掉文本出框
  - 统一登录页按钮和提示区的主题色
- 影响提醒 / 下一步
  - `app.css` 属于共享文件，B / C / D pull 后再继续改
  - 下一步继续收首页和弹窗细节
- Push 结果
  - 已 push
  - 提交号：`abcdef1`
```

## 当前已知认领

- `A`：你本人负责
- `B / C / D`：由队友继续认领
- 队友默认不要直接改 `A` 区域文件，除非先沟通

## 协作记录

### 2026-03-25 16:20 | D
- 改动文件
  - `项目协作根基.md`
  - `AGENTS.md`
  - `README.md`
  - `ABCD协作历史记录.md`
- 完成内容
  - 建立 `A / B / C / D` 协作入口
  - 明确开工前先 `git pull origin main`
  - 约定协作历史必须单独记录
- 影响提醒 / 下一步
  - 所有成员开工前先读协作文档，不要直接闷头改
  - 历史记录文档后续必须持续维护
- Push 结果
  - 已 push
  - 提交号：`bd3ee2d`

### 2026-03-25 16:24 | D
- 改动文件
  - `项目协作根基.md`
- 完成内容
  - 明确当前已知认领：你本人负责 `A`
  - 同步补充了 `A` 区域的责任边界
- 影响提醒 / 下一步
  - 队友默认不要反向改 `A` 的壳层、登录页、主题和弹窗
- Push 结果
  - 已 push
  - 提交号：`f2b8b14`

### 2026-03-25 16:41 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/DormRepairApplication.java`
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/support/UiAlerts.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 收紧工作台头部结构，不再保留大块空白头图
  - 统一启动失败提示和错误弹窗入口，避免回退默认 `Alert`
  - 壳层头部开始朝“身份 + 模块 + 动作”的工作台方向收口
- 影响提醒 / 下一步
  - `AppShell.java` 和 `app.css` 是共享高风险文件，改之前先 pull
  - 下一步继续处理滚轴、卡片和切页观感
- Push 结果
  - 已 push
  - 提交号：`169a2e0`

### 2026-03-25 16:46 | D
- 改动文件
  - `项目协作根基.md`
  - `AGENTS.md`
  - `ABCD协作历史记录.md`
- 完成内容
  - 把“任务完成后必须立刻 `commit + push`”写成硬规则
  - 日志要求补充 `Push 结果` 和 `提交号`
- 影响提醒 / 下一步
  - 不允许本地攒很多天不推，避免后期冲突放大
- Push 结果
  - 已 push
  - 提交号：`d70ab42`

### 2026-03-25 16:55 | A
- 改动文件
  - `src/main/resources/styles/app.css`
- 完成内容
  - 重做滚轴样式，改成更细、更轻、更贴合当前配色的桌面风格
  - 隐藏上下箭头按钮，降低滚轴存在感
- 影响提醒 / 下一步
  - 如果后面还要改滚轴，只在 `app.css` 里继续收，不要新起一套组件
- Push 结果
  - 已 push
  - 提交号：`d999d65`

### 2026-03-25 17:15 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/support/UiMotion.java`
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AdminDispatchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/WorkerProcessingModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 清掉容易造成错觉的下拉弹层和平移动效
  - 把管理员页、维修页的左右布局从弹性 `HBox` 改成固定比例 `GridPane`
  - 降低重阴影，减少组件乱动和点击发黏
- 影响提醒 / 下一步
  - 如果表格仍然抖动，下一步要查 `TableView.CONSTRAINED_RESIZE_POLICY`
- Push 结果
  - 已 push
  - 提交号：`a450bd0`

### 2026-03-25 17:28 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/LoginView.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 修掉登录页角色卡文字出框问题
  - 固定角色卡的安全内边距、文本位置和卡片高度
- 影响提醒 / 下一步
  - 后续继续收登录页整体气质，不要再回退成初代表单拼接页
- Push 结果
  - 已 push
  - 提交号：`38a553b`

### 2026-03-25 19:18 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AbstractWorkbenchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AdminDispatchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/DashboardModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/WorkerProcessingModule.java`
  - `src/main/resources/styles/app.css`
  - `项目协作根基.md`
- 完成内容
  - 把工作台公共构件上收进 `AbstractWorkbenchModule`
  - 学生、管理员、维修页面开始复用共享布局工具，不再各自复制一套
  - 壳层头部改成只保留身份、模块和动作提示
- 影响提醒 / 下一步
  - 后面新增业务页时，优先复用共享构件，不要再新造一套比例布局和摘要卡
- Push 结果
  - 已 push
  - 提交号：`ca49596`

### 2026-03-25 19:25 | A
- 改动文件
  - `ABCD协作历史记录.md`
- 完成内容
  - 回填上一轮 A 模块化提交的真实 push 记录
- 影响提醒 / 下一步
  - 协作日志必须和真实提交号对齐，不能只写“已完成”
- Push 结果
  - 已 push
  - 提交号：`cf95bbb`

### 2026-03-25 20:05 | D
- 改动文件
  - `ABCD协作历史记录.md`
- 完成内容
  - 修复协作文档编码损坏问题
  - 整份文档按正常 UTF-8 重写，恢复可读中文内容
- 影响提醒 / 下一步
  - 后续写这份文档时统一保持 UTF-8，不要再用错误编码覆盖
  - 如果以后出现同类现象，优先检查写入方式和终端编码，不要继续在坏文件上追加
- Push 结果
  - 已 push
  - 提交号：`5edd465`

### 2026-03-25 19:58 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/support/UiAlerts.java`
  - `src/main/java/com/scau/dormrepair/ui/support/UiMotion.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 头部右侧信息区改成居中摘要卡，退出按钮不再孤零零偏在一边
  - 弹窗去掉外层壳和描边，只保留一层圆角内容卡片，确认按钮改为居中
  - 下拉框公共入口彻底改成空动画实现，避免组件再因为弹层逻辑出现乱动错觉
  - 同步补齐头部摘要卡、弹窗和确认按钮的主题样式
- 影响提醒 / 下一步
  - `AppShell.java`、`UiAlerts.java`、`UiMotion.java`、`app.css` 都属于 A 共享文件，队友改前先 pull
  - 如果后面还出现组件“自己变大”或“位置乱飘”，优先先查共享样式和公共 support，不要先怀疑业务逻辑
- Push 结果
  - 已 push
  - 提交号：`2add28f`

### 2026-03-25 20:36 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/java/com/scau/dormrepair/domain/command/CreateRepairRequestCommand.java`
  - `src/main/java/com/scau/dormrepair/domain/entity/RepairRequest.java`
  - `src/main/java/com/scau/dormrepair/domain/entity/DormBuilding.java`
  - `src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java`
  - `src/main/java/com/scau/dormrepair/common/AppContext.java`
  - `src/main/java/com/scau/dormrepair/config/DatabaseConfig.java`
  - `src/main/java/com/scau/dormrepair/config/SchemaCompatibilitySupport.java`
  - `src/main/java/com/scau/dormrepair/mapper/DormBuildingMapper.java`
  - `src/main/java/com/scau/dormrepair/service/DormCatalogService.java`
  - `src/main/java/com/scau/dormrepair/service/impl/DormCatalogServiceImpl.java`
  - `src/main/resources/mapper/DormBuildingMapper.xml`
  - `src/main/resources/mapper/RepairRequestMapper.xml`
  - `sql/mysql/01_init_schema_v1.sql`
  - `项目协作根基.md`
- 完成内容
  - 学生报修页改成 `宿舍区 -> 宿舍楼 -> 房间号` 的固定链路，不再手填楼栋文本
  - 新增宿舍资料查询 service / mapper，宿舍区和楼栋下拉直接从数据库读取
  - `repair_requests` 增加 `dorm_area_snapshot`，列表位置展示同步改成 `宿舍区 + 楼栋 + 房间`
  - `SchemaCompatibilitySupport` 启动时会自动补 5 个宿舍区、每区 15 栋楼的种子数据
  - 本机 MySQL 已验证：`repair_requests.dorm_area_snapshot` 已补上，`dorm_buildings` 当前共 `75` 条
- 影响提醒 / 下一步
  - 学生端现在依赖 `dorm_buildings` 基础资料，后面如果要做房间资料维护，优先在这套资料层上继续扩
  - 当前运行中的旧 JavaFX 窗口已经全部关掉，并重新拉起了最新实例，避免看到上一次残留界面
- Push 结果
  - 已 push
  - 功能提交号：`a2c1994`
  - 日志回填提交号：`9c887b4`

### 2026-03-25 20:50 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/resources/mapper/DormBuildingMapper.xml`
- 完成内容
  - 修复“点击提交报修后头部切到新模块、但主体还是上一页”的真实根因
  - 根因不是按钮事件失效，而是学生报修页加载时触发 `DormBuildingMapper.selectDistinctAreas` SQL 报错，导致主体视图创建失败
  - `AppShell` 现在即使点击当前已激活模块，也会强制重载主体页面；如果模块加载失败，会回退并弹窗提示，不再只切头部状态
  - `DormBuildingMapper.xml` 去掉了会在 MySQL 8 下报错的 `DISTINCT + ORDER BY id` 写法，宿舍区列表改为按 `MIN(id)` 稳定排序
- 影响提醒 / 下一步
  - 如果后面再出现“头部模块变了，但正文没变”，先查模块创建链路是否抛异常，不要只盯导航按钮
  - `AppShell.java` 和 `DormBuildingMapper.xml` 都是共享高风险文件，队友改前先 pull
- Push 结果
  - 已 push
  - 提交号：`010fcbf`

### 2026-03-25 21:10 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/config/SchemaCompatibilitySupport.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AbstractWorkbenchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/DashboardModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AdminDispatchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/WorkerProcessingModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StatisticsModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 清掉宿舍区基础资料中的脏值 `???`，并把宿舍区种子逻辑改成稳定常量，避免下拉再次混出第六项脏数据
  - 下拉列表弹层改回更像正常列表的紧凑样式，不再铺成一大块透明面板
  - 学生报修页和学生报修记录页删掉了重复的身份摘要条，避免和头部摘要重复
  - 把首页、学生记录、管理员派单、维修处理、月度统计这些表格统一成固定列比
  - 所有表格统一加边界线，并禁止用户拖拽改列宽、改顺序或点击表头乱排序
- 影响提醒 / 下一步
  - `AbstractWorkbenchModule.java` 和 `app.css` 现在是表格共享底座，后续新表格优先复用这一套
  - `SchemaCompatibilitySupport.java` 负责启动期修复宿舍区基础数据，D 区如果再动 DDL，记得同步这层兼容逻辑
- Push 结果
  - 已 push
  - 提交号：`010fcbf`

### 2026-03-25 21:14 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `项目协作根基.md`
- 完成内容
  - 把头部右上 summary 卡改成固定高度的静态结构，只保留角色、演示身份、当前模块和退出按钮
  - 删除 summary 卡里那行会随模块变化的说明文案，避免点击三个工作模块时把上半部分整体顶高或压低
  - 头部容器本身也锁了固定高度，壳层不再跟着模块切换上下跳
- 影响提醒 / 下一步
  - 后续如果再改 `AppShell.java`，不要往右上 summary 区继续塞多行描述，模块说明回到页面主体里
  - 这条规则属于 A 区固定约束，队友改壳层前先 pull
- Push 结果
  - 已 push
  - 提交号：`010fcbf`

### 2026-03-25 21:20 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AbstractWorkbenchModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 删掉头部左侧 `DESKTOP WORKBENCH` 和壳层演示口吻，只保留正式系统标题
  - 表格共享列宽算法改成按整表宽度直接分配，不再额外减掉一段补偿宽度
  - 所有共享表格列内容统一改成居中显示，列边界和表头对齐更清晰
- 影响提醒 / 下一步
  - 后续新表格继续走 `configureFixedTable(...)`，不要再单独手写列宽逻辑
  - 如果某张表必须左对齐，先单独写列级样式，不要把全局共享规则再改散
- Push 结果
  - 已 push
  - 提交号：`010fcbf`

### 2026-03-25 21:22 | A
- 改动文件
  - `ABCD协作历史记录.md`
- 完成内容
  - 回填 20:50、21:10、21:14、21:20 这四轮 A 区改动的真实提交号
  - 统一把本轮长期挂起的 `待 push` 状态收口
- 影响提醒 / 下一步
  - 后续继续遵守“完成即 push”，不要再让协作日志里连续堆积多条 `待 push`
- Push 结果
  - 已 push
  - 提交号：`025bd08`

### 2026-03-25 21:30 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/module/AbstractWorkbenchModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 共享表格从 `UNCONSTRAINED_RESIZE_POLICY` 改回受控缩放，列宽按固定比例收进可视区域
  - 去掉会把表格撑出横向滚动的宽度绑定，改成固定比例首选宽度作为统一基线
  - 表头标题、表体单元格和表格横向滚动条一起收口，避免表头表体继续显得歪
- 影响提醒 / 下一步
  - 后续不要再把共享表格切回 `UNCONSTRAINED_RESIZE_POLICY`
  - 如果某张表仍然特殊错位，要查该页单独的列定义，不要先怀疑共享 CSS
- Push 结果
  - 已 push
  - 提交号：`53775b1`

### 2026-03-25 21:34 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/module/AbstractWorkbenchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/DashboardModule.java`
- 完成内容
  - 给共享表格底座补了“按实际行数收表高”的能力，避免记录条数很少时还撑出一大块空表
  - 学生报修记录页和首页最近报修表改成按真实数据条数收高度，去掉半截行和多余滚动条带来的“表格歪了”观感
- 影响提醒 / 下一步
  - 小数据量表格优先走 `fitTableHeightToRows(...)`，不要再硬塞一个很大的固定高度
  - 如果某张大表仍然需要滚动，再单独给那张表保留固定高度
- Push 结果
  - 已 push
  - 提交号：`53775b1`

### 2026-03-25 21:36 | A
- 改动文件
  - `src/main/resources/styles/app.css`
- 完成内容
  - 补上表头标题居中样式
  - 补上表格横向滚动条隐藏样式，和前面的受控列宽一起完成整套表格收口
- 影响提醒 / 下一步
  - 表格这类共享样式仍然集中在 `app.css`，后续别在页面里零散写内联样式
- Push 结果
  - 已 push
  - 提交号：`9a2bfac`

### 2026-03-25 21:52 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AbstractWorkbenchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/DashboardModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/java/com/scau/dormrepair/service/impl/DormCatalogServiceImpl.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 把壳层改成真正静态结构：顶部只保留标题、身份 chip、当前模块 chip 和退出按钮，左侧只保留模块入口
  - 删除顶部和侧栏重复解释文案，模块页默认不再强制渲染副说明，避免切换模块时上半区高度抖动
  - 首页去掉右侧重复角色说明列，只保留主视觉、指标卡和最近报修表
  - 宿舍区列表加稳定常量兜底，只允许 `泰山区 / 华山区 / 启林区 / 黑山区 / 燕山区` 出现在下拉里，屏蔽数据库脏值 `???`
  - 下拉弹层高度统一锁定，避免再次弹成整块大浮层
- 影响提醒 / 下一步
  - 顶部身份区以后不要再塞模块说明句子，保持固定高度
  - 宿舍区如果后面再扩展，先同步 `DormCatalogServiceImpl` 的稳定常量，不要只改数据库
- Push 结果
  - 已 push
  - 提交号：`bab640b`

### 2026-03-26 10:18 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/module/AbstractWorkbenchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/DashboardModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StatisticsModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 把首页最近报修、学生报修记录、月度统计这三类只读表格从 `TableView` 改成共享静态数据网格
  - 静态表格统一锁定列宽比例、边界线、行高和空白行，不再允许表头表体错位，也不再依赖滚动条和列拖拽
  - `AbstractWorkbenchModule` 现在同时维护两套底座：只读表用静态网格，管理员/维修员这类需要选择态的表仍保留受控 `TableView`
  - 用 `clean compile` 重新编译确认新类文件已经落到 `target/classes`
- 影响提醒 / 下一步
  - 后续新增只读展示表格优先走 `createStaticDataTable(...)`，不要再默认上 `TableView`
  - 如果某张表仍然要支持行选择，再单独保留 `TableView`，不要把只读表也带回去
- Push 结果
  - 已 push
  - 提交号：`c82e8ab`

### 2026-03-26 11:12 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 顶部右上 summary 区改成单一身份标签，只显示 `角色•姓名`，不再拆成“演示身份”“当前模块”“首页概览”这类多 chip
  - 顶部壳层只保留正式系统标题和退出按钮，summary 卡高度保持固定，不再因为模块切换产生额外文本或布局抖动
  - 重新用 `clean compile` 验证，确认新的壳层类文件已经落到 `target/classes`
- 影响提醒 / 下一步
  - 后续不要再把模块名塞回右上 summary 区，当前模块应由左侧导航和页面标题表达
  - 如果用户还要继续收壳层观感，优先改 `AppShell.java` 和 `app.css`，不要去业务页重复拼 header 信息
- Push 结果
  - 已 push
  - 提交号：`452ec44`

### 2026-03-26 12:52 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/support/UiMotion.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 把下拉控件的滚轮处理从空实现改成弹层内部的平滑滚动逻辑，只作用于 popup 里的 `ListView` 和竖向滚动条
  - 通过短时 `Timeline` 缓动滚动条值，让楼栋和宿舍区下拉在滚轮滚动时不再一格一格硬跳
  - 同时去掉下拉弹层的固定 cell 高度，改成更自然的行内边距，减少机械感
  - 用 `clean compile` 重新编译确认新的交互类文件已经落到 `target/classes`
- 影响提醒 / 下一步
  - 后续如果再调下拉手感，优先改 `UiMotion.java`，不要再往组件本体上加位移或缩放动画
  - 如果某个下拉仍旧发卡，先看是不是业务页单独覆盖了控件或弹层样式
- Push 结果
  - 已 push
  - 提交号：`74fd079`

### 2026-03-26 14:20 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/LoginView.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/java/com/scau/dormrepair/ui/support/ProjectImageStore.java`
  - `src/main/java/com/scau/dormrepair/ui/support/UiMotion.java`
  - `src/main/resources/styles/app.css`
  - `pics/.gitkeep`
  - `AGENTS.md`
  - `项目协作根基.md`
- 完成内容
  - 登录页去掉“演示入口”口吻，统一成正式应用的身份登录文案
  - 学生报修页把“图片地址”占位改成真实图片选择：点击按钮打开本地文件选择器，页面内先预览文件名，提交时自动复制到项目 `pics/` 目录并以相对路径入库
  - 新增 `ProjectImageStore` 统一处理项目内图片存储，后续本地演示上传都可以继续复用
  - 下拉弹层滚轮继续收口成更连续的缓动，不再一格一格硬跳
  - 协作文档同步改口径：本地图片上传已落地，后续剩下真实云存储接入
- 影响提醒 / 下一步
  - 当前图片上传是“本地选择 -> 复制到仓库 pics/ -> 相对路径入库”的演示方案，后面如果接对象存储，优先替换 `ProjectImageStore`
  - `UiMotion.java` 和 `app.css` 仍然是下拉手感的共享入口，后续调滚轮不要再直接给组件本体加动画
- Push 结果
  - 已 push
  - 提交号：`0b6a8c8`

### 2026-03-27 12:03 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/component/AppDropdown.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/AdminDispatchModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/WorkerProcessingModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 把学生、管理员、维修员和学生评价里几个关键下拉统一切到共享 `AppDropdown`，不再依赖默认 `ComboBox`
  - 新下拉现在是固定本体、轻弹层、按实际选项数量收高度，打开时会自动把当前选中项滚到可视区
  - 弹层滚轮改成像素级缓动滚动，减少“一格一格跳”的机械感，同时保留组件本体完全静止
  - 亮色业务页里的次级操作按钮改成 `surface-button`，不再复用侧栏的 `nav-button`，图片上传和刷新按钮的文字对比度恢复正常
  - 已执行 `mvn -DskipTests clean compile`，结果为 `BUILD SUCCESS`；并用 `mvn javafx:run` 拉起本地窗口确认运行链路可用
- 影响提醒 / 下一步
  - `AppDropdown.java` 和 `app.css` 现在是所有关键下拉的共享入口，后续要调手感优先改这两处，不要再各个业务页单独覆写
  - `UiMotion.java` 还在仓库里，但主链业务页已经优先走 `AppDropdown`
  - 亮色表单页的次级按钮以后统一走 `surface-button`，不要再拿侧栏按钮样式直接复用
- Push 结果
  - 已 push
  - 提交号：`49b113c`
### 2026-03-26 13:04 | B
- 改动文件
  - `src/main/java/com/scau/dormrepair/service/RepairRequestService.java`
  - `src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java`
  - `src/main/java/com/scau/dormrepair/mapper/RepairRequestMapper.java`
  - `src/main/java/com/scau/dormrepair/mapper/RepairRequestImageMapper.java`
  - `src/main/java/com/scau/dormrepair/domain/view/StudentRepairDetailView.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/resources/mapper/RepairRequestMapper.xml`
  - `src/main/resources/mapper/RepairRequestImageMapper.xml`
- 完成内容
  - 补齐学生侧“只看本人单条报修详情”的 service / mapper / XML 链路
  - 学生报修记录页改成“左侧历史列表 + 右侧详情面板”，可直接查看宿舍位置、联系电话、故障描述和图片地址
  - 详情查询严格按 `student_id + request_id` 过滤，避免学生看到别人的报修记录
- 影响提醒 / 下一步
  - 当前图片仍然是“图片地址列表”方案，正式文件上传还没有落地，但已经和 `repair_request_images` 表结构对接上
  - 本机缺少 `mvn` 命令，暂时没法在当前环境执行 `mvn compile`，后续需要在装好 Maven 的机器上再补一次编译验证
- Push 结果
  - 已 push 到 `origin/codex/b-student-mainchain`
  - 代码提交号：`20b7339`
  - 日志提交号：`a154b08`

### 2026-03-26 16:11 | B
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java`
  - `src/main/java/com/scau/dormrepair/ui/support/ProjectImageStore.java`
- 完成内容
  - 学生报修页升级成“左侧提交表单 + 右侧填写概况与最近提交记录”的工作区，不再只是单列表单
  - 图片上传补齐数量、格式、大小、去重校验，并继续沿用项目 `pics/` 目录落地方案
  - service 层加强了联系电话、房间号、描述长度和图片数量校验，避免脏数据直接进入学生报修主链
  - 学生报修记录页补了手动刷新入口，详情展示继续限定为“当前学生只看自己的记录”
- 影响提醒 / 下一步
  - 我这边仍然没有看到你提到的第二张图片，所以这轮是按仓库文档、现有结构和已落地代码继续高级化补强
  - 当前环境没有 `mvn` 和 `java` 命令，没法本机完成编译验证；建议你在装好 JDK/Maven 的机器上再跑一次 `mvn compile`
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`19936f9`

### 2026-03-26 16:32 | B
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java`
  - `src/main/java/com/scau/dormrepair/domain/view/StudentRepairDetailView.java`
- 完成内容
  - 学生报修记录页补了图片缩略图和大图预览，不再只显示图片地址文本
  - 已完成报修现在可以直接在学生详情页提交评分和评价，评价后会回显到当前记录详情
  - 学生详情查询在原有基础上补齐了评价信息读取，学生主链从“提交 -> 查看 -> 评价”形成闭环
- 影响提醒 / 下一步
  - 图片预览仍然依赖项目 `pics/` 目录中的本地文件，后续如果改成真实上传，需要同步替换这层路径解析
  - 当前环境仍然没有 `mvn` / `java`，无法本机完成编译验证
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`77a8c2a`

### 2026-03-26 17:18 | B
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 学生详情页新增正式的状态进度展示，按 `已提交 -> 已派单 -> 处理中 -> 已完成` 四段显示当前所处阶段
  - 根据状态给进度卡加了进行中、已完成和已关闭的视觉区分，让学生一眼能看懂当前处理位置
- 影响提醒 / 下一步
  - 这轮为了做进度视图，顺手同步改了共享样式文件 `app.css`
  - 已尝试执行 `mvn compile`，当前阻塞不是 B 代码，而是仓库缺少 `lib/vfx/vfx-1.3.3.jar`、`lib/vfx/commons-1.1.1.jar`、`lib/vfx/jnativehook-2.2.2.jar` 这 3 个本地依赖文件
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`becb797`

### 2026-03-26 17:58 | B
- 改动文件
  - `pom.xml`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/resources/styles/app.css`
  - `ABCD协作历史记录.md`
- 完成内容
  - 把原来依赖本地 `lib/vfx` 目录的 3 个 `systemPath` 依赖改成 Maven 中央仓库可解析依赖，准备把项目编译链路真正跑通
  - 学生报修记录页重构为更正式的详情工作台，补强了状态摘要、评价状态反馈、缩略图选中态和大图预览切换
  - 清理了协作日志中残留的冲突标记，恢复文档可继续追加维护
- 影响提醒 / 下一步
  - `app.css` 仍然是共享样式文件，A / C / D 后续改动前先 pull
  - 已在本地补齐 Maven 依赖后执行 `mvn -Dmaven.repo.local=.m2\repository compile`，当前项目已经编译通过
- Push 结果
  - 已 push 到 `origin/main`
  - 代码提交号：`492e03a`
  - 日志提交号：`78ccdee`

### 2026-03-26 20:05 | B
- 改动文件
  - `src/main/java/com/scau/dormrepair/service/RepairRequestService.java`
  - `src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java`
  - `src/main/java/com/scau/dormrepair/mapper/RepairRequestMapper.java`
  - `src/main/resources/mapper/RepairRequestMapper.xml`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 为学生报修主链补上“催办一次 / 取消报修”两类学生侧操作，并沿用现有 `repair_requests.urge_count` 与 `CANCELLED` 状态，不新增额外表结构
  - service 与 mapper 层新增学生本人催办、取消的状态校验，限制为“只能操作自己的工单”，并按 `SUBMITTED / ASSIGNED / IN_PROGRESS` 等真实状态开放入口
  - 学生报修详情页新增催办次数展示、操作状态横幅和按钮联动，评价提交后也会自动刷新当前详情与列表状态，整体闭环升级为“提交 -> 查询 -> 催办/取消 -> 完成后评价”
  - 共享样式 `app.css` 补充学生侧操作横幅的空闲、可操作、关闭三种视觉状态
- 影响提醒 / 下一步
  - 这轮继续改动了共享文件 `app.css`，A / C / D 在接着开发前需要先 pull 最新
  - 已在本地执行 `mvn -Dmaven.repo.local=.m2\repository compile`，当前项目编译通过
- Push 结果
  - 本轮待 commit + push

### 2026-03-26 20:15 | B
- 改动文件
  - `src/main/java/com/scau/dormrepair/domain/view/StudentRepairDetailView.java`
  - `src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java`
  - `src/main/resources/mapper/RepairRequestMapper.xml`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/java/com/scau/dormrepair/ui/support/UiAlerts.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 学生报修详情页继续补强为“正式追踪视角”，现在会回显工单编号、维修人员、派单备注、派单时间、接单时间，并用处理时间线展示关键节点
  - 学生详情查询不再只看 `repair_requests`，而是联动现有 `work_orders` 真实字段，把派单与接单过程接回 B 主链
  - 取消报修前新增二次确认弹窗，避免误触后直接关单；弹窗仍沿用项目现有自定义对话框风格，没有退回默认 `Alert`
  - 共享样式 `app.css` 补充了时间线卡片样式，学生详情页的产品感和演示观感进一步提升
- 影响提醒 / 下一步
  - 这轮继续改动了共享文件 `app.css` 和 `UiAlerts.java`，A / C / D 开发前需要先 pull 最新
  - 已在本地执行 `mvn -Dmaven.repo.local=.m2\repository compile`，当前项目编译通过
- Push 结果
  - 已 push
  - 提交号：`e206555`

### 2026-03-27 12:20 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/LoginView.java`
- 完成内容
  - 清掉登录页所有用户可见的技术栈和原型阶段文案，不再出现 `MyBatis`、`本地登录占位`、`进入工作台` 这类样板口径
  - 登录页品牌条、入口标题、说明文字和主按钮统一改成正式宿舍维修系统场景，保持业务语言一致
- 影响提醒 / 下一步
  - A 区域后续继续改登录页和首页时，禁止把技术栈词、原型提示或“桌面工作台”一类样板文案重新带回 UI
  - 已在本地执行 `mvn -DskipTests clean compile`，结果为 `BUILD SUCCESS`
- Push 结果
  - 功能提交：`8638216`

### 2026-03-27 12:59 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/component/AppDropdown.java`
- 完成内容
  - 修正自定义下拉的滚轮缓动起点，连续滚动时不再拿“上一次目标值”做动画起点，而是改为读取当前真实滚动位置
  - 下拉滚动步长改成更平滑的像素区间，避免滚轮一格一格地发黏、停顿
- 影响提醒 / 下一步
  - 这轮改的是共享下拉公共件，学生报修、管理员派单、维修员处理、评分选择都会一起受影响
  - 已在本地执行 `mvn -DskipTests clean compile`，结果为 `BUILD SUCCESS`
- Push 结果
  - 功能提交：`4ad67ec`

### 2026-03-27 13:18 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/common/AppSession.java`
  - `src/main/java/com/scau/dormrepair/common/DemoAccountDirectory.java`
  - `src/main/java/com/scau/dormrepair/ui/AppShell.java`
  - `src/main/java/com/scau/dormrepair/ui/LoginView.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 把登录入口从“角色 + 手填姓名”改成了“角色 + 稳定账号选择”，登录页开始按真实系统身份入口组织，不再保留样板式姓名输入框
  - `AppSession` 新增当前账号 ID，`DemoAccountDirectory` 改成按稳定账号列表解析当前身份，避免继续靠自由文本匹配用户
  - 学生提交报修时，提交人姓名直接取当前学生账号，和壳层当前身份保持一致
  - 登录页新增账号卡片样式，角色切换后会自动切到该角色的默认账号
- 影响提醒 / 下一步
  - 这轮同时改了 `AppShell.java`、`LoginView.java`、`app.css` 和 `DemoAccountDirectory.java`，都属于共享文件，B / C / D 继续开发前先 pull
  - 已在本地执行 `mvn -DskipTests clean compile`，日志显示 `BUILD SUCCESS`；命令退出码仍会被这台机器的环境尾噪带偏，不是项目编译失败
- Push 结果
  - 已 push
  - 功能提交：`a824d65`

### 2026-03-27 14:37 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/service/DashboardService.java`
  - `src/main/java/com/scau/dormrepair/service/impl/DashboardServiceImpl.java`
  - `src/main/java/com/scau/dormrepair/mapper/DashboardMapper.java`
  - `src/main/resources/mapper/DashboardMapper.xml`
  - `src/main/java/com/scau/dormrepair/service/RepairRequestService.java`
  - `src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java`
  - `src/main/java/com/scau/dormrepair/mapper/RepairRequestMapper.java`
  - `src/main/resources/mapper/RepairRequestMapper.xml`
  - `src/main/java/com/scau/dormrepair/ui/module/DashboardModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
- 完成内容
  - 修复学生首页看板误读全局数据的权限漏洞，首页统计和最近报修现在按“当前账号 ID + 当前姓名”双重过滤
  - 学生报修记录页、详情页、催办、取消也统一收严到同一套过滤口径，避免旧脏数据从其他页面漏出来
  - 已用真实 MySQL 数据验证：`1003/相逢的` 查到 `0` 条，`1001/张三` 只看 `张三` 自己的 3 条，旧的 `czb` 记录被隔离
- 影响提醒 / 下一步
  - 当前数据库里仍有早期自由文本阶段留下的错绑记录，后续如果要彻底清理，需要单独做 demo 数据整治；但 UI 可见层已经被双重过滤挡住
  - 这轮改动同时触达 dashboard、repair request 和两个学生模块，队友继续改学生链前先 pull
  - 已在本地执行 `mvn -DskipTests clean compile`，日志显示 `BUILD SUCCESS`
- Push 结果
  - 已 push
  - 提交号：`e703d02`

### 2026-03-27 15:04 | A
- 改动文件
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
  - `src/main/java/com/scau/dormrepair/ui/support/UiAlerts.java`
  - `src/main/resources/styles/app.css`
- 完成内容
  - 学生报修记录页左侧列表改成“只保留选中记录所需字段”的窄表结构，去掉重复的故障类别列，并把左右布局比例调成 `52 / 48`，避免左侧记录号和位置被挤没
  - 统一提示弹窗补了正式描边，不再和页面浅色底板糊成一片
  - 统一提示弹窗补上键盘确认：`Enter` 直接确认，`Esc` 直接关闭，避免每次都必须鼠标点按钮
  - 学生详情页状态条、操作条、评价条的边框也同步加深，右侧信息块层次更清楚
- 影响提醒 / 下一步
  - 这轮继续改了共享样式文件 `app.css` 和公共提示层 `UiAlerts.java`，B / C / D 开发前需要先 pull
  - 如果学生记录页后面还嫌左侧拥挤，下一步优先继续精简左侧列表字段，不要再把完整详情塞回左栏
  - 已在本地执行 `mvn -DskipTests clean compile`，日志显示 `BUILD SUCCESS`；命令退出码仍会被这台机器的环境尾噪影响
- Push 结果
  - 本轮待 commit + push

### 2026-03-27 20:30 | B
- 改动文件
  - `src/main/java/com/scau/dormrepair/domain/command/SubmitRepairFeedbackCommand.java`
  - `src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java`
  - `src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java`
- 完成内容
  - 学生侧提交评价现在会把当前学生 `id + name` 一起带入 command，补齐“只能评价自己的已完成报修”这层 service 校验
  - `submitFeedback(...)` 不再直接按 `requestId` 放行，而是复用现有“学生本人持有工单”校验链路，避免越权评价别人的工单
  - 评价内容增加去空白与最大长度限制，空白评价会按 `null` 入库，超长内容会直接拦截
- 影响提醒 / 下一步
  - 这轮只收口 B 主链内部的评价权限和数据校验，没有改 A / C / D 的对外接口
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`；尝试 `clean compile` 时因为当前沙箱网络无法下载 `maven-clean-plugin` 失败，并在 `.m2/repository/org/apache/maven/plugins/maven-clean-plugin/` 留下未跟踪缓存目录
- Push 结果
  - 本轮待 commit + push
### 2026-03-31 21:26 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/config/DatabaseConfig.java
  - src/main/resources/mapper/WorkOrderCompletionImageMapper.xml
  - src/test/java/com/scau/dormrepair/service/UserAccountIntegrationSupport.java
  - src/test/java/com/scau/dormrepair/service/WorkOrderServiceImplTest.java
  - src/test/java/com/scau/dormrepair/service/RepairRequestServiceImplTest.java
  - src/main/java/com/scau/dormrepair/ui/module/AuditLogModule.java
  - src/main/java/com/scau/dormrepair/ui/AppShell.java
  - src/main/java/com/scau/dormrepair/ui/module/StatisticsModule.java
  - src/main/java/com/scau/dormrepair/ui/module/WorkerProcessingModule.java
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - src/main/java/com/scau/dormrepair/ui/module/AdminDispatchModule.java
- 完成内容
  - 注册新的审计日志与完工凭证 mapper，修正完工凭证 SQL，并把集成测试清理逻辑补到 udit_logs 和 work_order_completion_images。
  - 闭环状态语义收口为“维修员完工先进入待学生确认；学生确认后同单完结；学生申请返修则回到原链继续流转”。
  - 管理端补上审计模块，统计页补上楼栋 drill-down、类别 Top 和超时阶段分析，学生/管理员/维修员详情页都能回看完工说明与完工凭证。
  - 维修员处理页支持上传最多 3 张完工凭证图片。
- 影响提醒 / 下一步
  - 这轮同时改动了 AppShell.java、统计页、学生历史、管理员派单、维修员处理和测试基线，队友继续接手前先 pull。
  - 后续应继续优先收“可演示、可解释、可闭环”的 UI 与状态一致性，不再扩消息中心或导出链。
- Push 结果
  - 已 push
  - 提交号：2c38c16

### 2026-04-01 16:02 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/domain/view/DashboardOverview.java
  - src/main/java/com/scau/dormrepair/service/AuditLogService.java
  - src/main/java/com/scau/dormrepair/service/StatisticsService.java
  - src/main/java/com/scau/dormrepair/service/impl/DashboardServiceImpl.java
  - src/main/java/com/scau/dormrepair/service/impl/AuditLogServiceImpl.java
  - src/main/java/com/scau/dormrepair/service/impl/StatisticsServiceImpl.java
  - src/main/java/com/scau/dormrepair/ui/component/StatusChip.java
  - src/main/java/com/scau/dormrepair/ui/component/TimeoutChip.java
  - src/main/java/com/scau/dormrepair/ui/component/EvidenceGallery.java
  - src/main/java/com/scau/dormrepair/ui/module/DashboardModule.java
  - src/main/java/com/scau/dormrepair/ui/module/AuditLogModule.java
  - src/main/java/com/scau/dormrepair/ui/module/AdminDispatchModule.java
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - src/main/java/com/scau/dormrepair/ui/module/WorkerProcessingModule.java
  - src/main/java/com/scau/dormrepair/ui/module/StatisticsModule.java
  - src/main/resources/mapper/AuditLogMapper.xml
  - src/main/resources/mapper/DashboardMapper.xml
  - src/main/resources/mapper/StatisticsMapper.xml
  - src/main/resources/styles/app.css
- 完成内容
  - 首页明确展示超时预警数、超时数、待学生确认数和返修中数量，不再只停留在后端统计层。
  - 抽出 StatusChip、TimeoutChip、EvidenceGallery 三个复用组件，并统一到首页、管理员派单、维修员处理和学生历史详情里。
  - 审计页补上按操作类型、操作人、时间范围筛选；统计页新增 drill-down 对应明细；学生和管理员详情页都能直接回看完工图片与说明。
  - 修复 WorkerProcessingModule.java 关键中文文案，保证 UI copy smoke test 继续覆盖高可见文案。
- 影响提醒 / 下一步
  - 这轮继续修改共享样式与多个高可见页面，后续若还要调整演示观感，优先沿用这套组件，不要再分散写一套状态或凭证展示。
  - 已执行 mvn test，38 个测试全部通过；mvn javafx:run 也成功拉起 JavaFX 程序，末尾 Windows tail noise 与 SLF4J NOP 仍属环境噪声。
- Push 结果
  - 已 push
  - 提交号：2c38c16

### 2026-04-01 16:06 | A
- 改动文件
  - ABCD协作历史记录.md
- 完成内容
  - 修复协作历史文档在远端显示为乱码的问题，恢复到最近一版正常 UTF-8 内容，并补回最近关键任务记录。
  - 明确当前协作文档以 UTF-8 正常中文为准，后续不要再把这份文档以错误编码覆盖提交。
- 影响提醒 / 下一步
  - 这份文档属于共享协作入口，后续任何人改动前都应先确认编辑器编码为 UTF-8。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`fb66317`

### 2026-04-03 14:12 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/DormRepairApplication.java
  - src/main/java/com/scau/dormrepair/ui/support/WindowResizeSupport.java
  - ABCD协作历史记录.md
- 完成内容
  - 按用户要求撤销了主窗口八方向缩放改动，恢复为项目此前的窗口行为，不再额外挂载四边和四角的自定义缩放支持。
  - 同步删除本轮新增的 `WindowResizeSupport` 支持类，避免后续继续被主舞台引用。
- 影响提醒 / 下一步
  - 当前主窗口已回到撤销前状态，如果后续还要继续优化窗口交互，建议先单独确认是要保留 vfx 原生行为，还是完全接管窗口边框交互。
- Push 结果
  - 本轮待 commit + push

### 2026-04-03 14:38 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/DormRepairApplication.java
  - src/main/java/com/scau/dormrepair/ui/support/WindowResizeSupport.java
  - ABCD协作历史记录.md
- 完成内容
  - 为主窗口补齐四条边和四个角的拖拽缩放命中区域，不再只能从右下角调整窗口大小。
  - 新增 `WindowResizeSupport` 统一处理鼠标命中、方向光标和最小宽高约束，并在主窗口启动时挂载到 `Stage`。
- 影响提醒 / 下一步
  - 这次只调整桌面端窗口交互层，不涉及业务模块、数据库和样式资源；后续若继续改 `DormRepairApplication.java`，请先 pull 再合并。
- Push 结果
  - 本轮待 commit + push

### 2026-04-03 15:05 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/DormRepairApplication.java
  - ABCD协作历史记录.md
- 完成内容
  - 去掉主窗口沿用 `application.yml` 中 `1280 x 820` 的最小尺寸限制，改为接近技术下限的最小宽高设置。
  - 让四边和四角缩放时不再过早被大窗口下限拦住，窗口可以继续明显缩小。
- 影响提醒 / 下一步
  - 这次只放开窗口最小尺寸，不改业务页面布局；极小尺寸下界面会继续按比例压缩，但可操作性会自然下降，这是预期表现。
- Push 结果
  - 本轮待 commit + push

### 2026-04-03 15:18 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/support/ProportionalViewport.java
  - ABCD协作历史记录.md
- 完成内容
  - 把主窗口视口从“等比缩放居中”调整为“按宽高分别贴满视口”，解决缩放后上下或左右出现大块留白的问题。
  - 让登录页和工作台在窗口尺寸变化时始终紧贴外层边框，不再悬空显示在中间区域。
- 影响提醒 / 下一步
  - 这次改动会让界面在非设计比例下出现一定程度的横向或纵向拉伸，但能换来窗口填满效果，符合当前交互目标。
- Push 结果
  - 本轮待 commit + push

### 2026-04-03 15:28 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/component/PasswordInputControl.java
  - ABCD协作历史记录.md
- 完成内容
  - 调整密码可视化切换行为，点击眼睛图标后不再自动全选整段密码。
  - 切换显示状态后把输入焦点保留在密码框，并把光标定位到密码末尾，方便继续编辑。
- 影响提醒 / 下一步
  - 这次只影响登录页和改密弹窗复用的密码输入控件，不改认证逻辑和样式资源。
- Push 结果
  - 本轮待 commit + push

### 2026-04-03 15:40 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/DormRepairApplication.java
  - ABCD协作历史记录.md
- 完成内容
  - 调整登录页首次启动窗口尺寸计算，改为严格按设计稿宽高比等比求解，不再分别截取宽度和高度后直接组合。
  - 让程序初次打开时保持更自然的纵横比例，避免首页一开始看起来偏扁。
- 影响提醒 / 下一步
  - 这次只调整初始窗口比例，不影响后续拖拽缩放能力；如果继续手动把窗口拉成超宽比例，界面仍会按当前贴满策略拉伸。
- Push 结果
  - 本轮待 commit + push

### 2026-04-03 20:36 | B
- 改动文件
  - src/main/java/com/scau/dormrepair/service/RepairRequestService.java
  - src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java
  - src/main/java/com/scau/dormrepair/mapper/RepairRequestImageMapper.java
  - src/main/resources/mapper/RepairRequestImageMapper.xml
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - src/test/java/com/scau/dormrepair/service/RepairRequestServiceImplTest.java
- 完成内容
  - 给 B 主链补上“学生提交后继续补充现场图片”的正式入口，沿用现有 `repair_request_images` 表和 `pics/` 本地图片存储，不额外新建表结构。
  - service 层新增“只能补自己未结束报修、总图数最多 5 张、重复图片不重复入库”的校验，并把图片排序接到现有明细查询里。
  - 学生报修记录详情页新增补图工作区，学生可以在历史详情里选择图片、清空待传、提交补图，提交后会自动刷新右侧详情。
  - 补了对应集成测试，覆盖“本人可补图”和“超出 5 张上限会被拦截”两条关键路径。
- 影响提醒 / 下一步
  - 这轮改动只触达 B 允许范围内的 service、mapper、学生记录页和测试，没有动 A 区壳层和 C 区工单主链。
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
  - 已尝试执行 `mvn -Dmaven.repo.local=.m2\repository test`，当前被 `org.junit.platform:junit-platform-commons:1.10.2` 依赖下载握手中断阻塞，测试未完整跑完。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`848b358`

### 2026-04-03 21:14 | B
- 改动文件
  - src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - src/test/java/com/scau/dormrepair/service/RepairRequestServiceImplTest.java
- 完成内容
  - 学生报修记录页补齐派单时间、接单时间、派单备注展示，学生侧不再只能靠时间线和一句状态提示推测管理员处理过程。
  - 学生历史记录查询上限从默认 20 条放宽到最多 100 条，避免记录稍多时旧报修直接从工作台里消失。
  - 新增测试覆盖“同一学生请求 100 条上限时仍能拿到 25 条历史记录”的场景，补上这次产品级收口的验证。
- 影响提醒 / 下一步
  - 这轮仍然只动了 B 范围内的 service、学生记录页和测试，没有触碰 A 壳层和 C 工单处理逻辑。
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`3438abe`

### 2026-04-03 21:17 | B
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - src/test/java/com/scau/dormrepair/service/RepairRequestServiceImplTest.java
- 完成内容
  - 学生侧“确认完成”和“申请返修”补上二次确认，避免误点后直接把报修推进到不可逆状态。
  - 学生历史补图在复制到 `pics/` 之前，先按“当前已有图片数 + 待补图片数”做前置拦截，避免超上限时先落一批无效图片文件。
  - 新增“已完成报修不能继续补图”的回归测试，继续收严 B 主链边界。
- 影响提醒 / 下一步
  - 这轮只动了学生记录页和 B 的测试基线，没有改 service 接口、数据库结构或 A/C 区共享逻辑。
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`0805b6e`

### 2026-04-03 21:55 | B
- 改动文件
  - src/main/java/com/scau/dormrepair/service/RepairRequestService.java
  - src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java
  - src/main/java/com/scau/dormrepair/mapper/RepairRequestImageMapper.java
  - src/main/resources/mapper/RepairRequestImageMapper.xml
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - src/test/java/com/scau/dormrepair/service/RepairRequestServiceImplTest.java
- 完成内容
  - 学生提交页补了即时产品反馈：联系电话格式提示、故障描述字数和剩余字数提示，填写阶段就能知道哪里还不稳。
  - 学生报修记录页新增状态筛选，支持按“全部 / 处理中 / 待确认 / 已完成 / 已关闭”快速看自己的历史记录。
  - 学生报修图片支持删除单张已上传图片，老师如果追问“传错图怎么办”，现在可以在学生详情页直接演示删除。
  - 补图流程在复制文件到 `pics/` 前先做剩余数量拦截，避免超上限时先落无效图片文件。
  - 新增测试覆盖“学生可删自己的图片”和“已完成报修不能删图”两条边界。
- 影响提醒 / 下一步
  - 这轮仍然只触达 B 范围内的 service、mapper、学生提交页、学生记录页和测试，没有改 A 壳层与 C 工单主链。
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`fb66317`

### 2026-04-03 22:08 | B
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - src/main/java/com/scau/dormrepair/ui/support/ProjectImageStore.java
  - ABCD协作历史记录.md
- 完成内容
  - 学生提交页新增“提交条件”即时反馈，未选宿舍、故障类型、联系电话异常、描述过短或超长时会直接提示，并同步禁用提交按钮，避免把明显不完整的报修单送进主链。
  - 学生历史记录页在状态筛选之外再补了关键字检索，支持按报修单号、位置、故障类型、状态快速定位记录，并实时显示当前筛选结果数量。
  - 学生补图流程改成先记录复制到 `pics/` 的路径，再在 service 成功后保留；如果补图失败会自动回收刚复制的本地图片，删除单张旧图时也会同步清理 `pics/`，减少本地脏文件堆积。
- 影响提醒 / 下一步
  - 这轮只继续收紧 B 的学生提交页、学生记录页和本地图片存储工具，没有改 A 壳层、C 工单主链和数据库结构。
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`9c1bba5`

### 2026-04-04 14:43 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/support/ProportionalViewport.java
  - src/test/java/com/scau/dormrepair/ui/support/ProportionalViewportTest.java
  - ABCD协作历史记录.md
- 完成内容
  - 把主窗口视口从“宽高分别拉伸”改回“单一缩放因子 + 居中”，窗口缩放时不再把登录页和工作台内容横向或纵向压扁。
  - 抽出视口布局计算并补了回归测试，明确验证“窄窗口时保持等比缩放”和“横向有余量时居中显示”两条关键行为。
  - 已执行 `git pull origin main`，并执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- 影响提醒 / 下一步
  - 这轮只收 A 壳层的缩放适配，没有改业务模块、数据库结构和共享 service / mapper 主链。
  - 当窗口长宽比和设计稿不一致时，界面会保留留白而不是继续拉伸变形；如果后面还想“铺满且不变形”，应改成窗口本身锁定比例，而不是再把 UI 横纵分别拉伸。
  - 尝试执行 `mvn -Dmaven.repo.local=.m2\repository -Dtest=ProportionalViewportTest test` 时，当前环境缺少 `maven-surefire-plugin` 本地缓存且网络下载被拦截，测试未能在本机跑完。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`771de36`

### 2026-04-04 15:38 | A
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/support/ProportionalViewport.java
  - src/test/java/com/scau/dormrepair/ui/support/ProportionalViewportTest.java
  - ABCD协作历史记录.md
- 完成内容
  - 把视口缩放从“等比完整显示”切到“等比 cover 铺满”，缩放时继续保持比例，但会优先贴满整个窗口，不再在上下或左右留下灰白空边。
  - 给视口容器补了 clip 裁切边界，放大后超出视口的部分会被裁掉，不会把溢出的内容露到窗口外层。
  - 同步更新回归测试，明确验证窄窗口和宽窗口下都会铺满视口且保持比例。
  - 已执行 `git pull origin main`，并执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- 影响提醒 / 下一步
  - 这轮仍然只改 A 壳层的视口缩放适配，没有改业务模块和数据库结构。
  - 现在当窗口比例和设计稿不一致时，界面会裁掉一部分边缘内容来换取“无留白铺满”；这是有意的 cover 行为，不再是之前的变形拉伸。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`d367047`

### 2026-04-04 20:48 | B
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairModule.java
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - ABCD协作历史记录.md
- 完成内容
  - 学生提交页的待上传图片列表补了“单张移除”操作，不再只能整批清空；学生如果选错一张图，可以直接在预览区删掉那一张再继续提交。
  - 学生历史详情里的补图队列也补了“单张移除”操作，补图前可以逐张整理待传图片，不用每次点错都整批清空重选。
  - 补图计数提示改成同时显示“已上传多少张、待补多少张、还可补多少张”，学生在历史详情里更容易判断剩余额度。
- 影响提醒 / 下一步
  - 这轮只继续优化 B 的图片交互细节，没有改 service 接口、数据库结构和 A/C 模块。
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`b727c58`

### 2026-04-04 21:07 | B
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - src/main/java/com/scau/dormrepair/service/impl/RepairRequestServiceImpl.java
  - src/test/java/com/scau/dormrepair/service/RepairRequestServiceImplTest.java
  - ABCD协作历史记录.md
- 完成内容
  - 学生历史详情里的“申请返修”和“提交评价”补了即时字数反馈，返修说明会显示已写/剩余字数，评价内容会显示已写/剩余字数并联动评分选择。
  - 前端按钮现在会跟随内容状态自动禁用：未选评分不能提交评价，返修说明为空或超长时不能直接发起返修，减少最后一步才报错的体验断点。
  - service 层新增评价内容长度兜底校验，统一限制在 1000 字以内，并补了一条超长评价的集成测试，保证前后端边界一致。
- 影响提醒 / 下一步
  - 这轮只继续优化 B 的学生历史详情和评价/返修校验，没有改数据库结构和 A/C 模块。
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`a929b86`

### 2026-04-04 21:19 | B
- 改动文件
  - src/main/java/com/scau/dormrepair/ui/module/StudentRepairHistoryModule.java
  - ABCD协作历史记录.md
- 完成内容
  - 学生历史记录工具栏新增“清空筛选”，切过状态筛选和关键字检索后可以一键回到默认列表，不用手动逐项还原。
  - 学生历史卡片补充了故障类型这一行，列表页扫读时能更快区分同一宿舍位置下的不同报修内容，查单更直观。
- 影响提醒 / 下一步
  - 这轮只优化 B 的学生历史列表展示和筛选操作，没有改 service、数据库结构和其他模块。
  - 已执行 `mvn -Dmaven.repo.local=.m2\repository compile`，结果为 `BUILD SUCCESS`。
- Push 结果
  - 已 push 到 `origin/main`
  - 提交号：`8affe03`
