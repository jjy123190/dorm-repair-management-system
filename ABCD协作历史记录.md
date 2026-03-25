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
  - 待 push

