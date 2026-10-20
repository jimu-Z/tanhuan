# 学校谈话管理系统

### 通知正文阅读权限与标签菜单边界 — 2026-10-20

- 普通教师顶部通知列表可见但正文误走管理权限接口；独立认证发布公告读取仅返回正常状态，保留管理权限。详情和标记成功后更新已读，并防重复并发减数。271项后端、6项组件回归通过，独立审查通过；用户明确确认必要QA重启后Java10028加载，四角色正文/首次与重复已读/全部已读刷新持久化真实复测通过；测试公告13/14关闭保留。
- 标签下拉边界误用上次操作标签导致关闭右侧错误禁用，改为当前标签，右键显式目标。6项组件回归、教师真实正反边界/关闭与全屏恢复通过，其他角色公共控件仍待。证据与阶段报告qa-runtime/ui-full-role-20261012/notice-tags-review-and-verification-20261020.md；59/69页通过，整体验证未完成，不构建、不改远程、不关机。


### 校管理员新菜单管理范围 — 2026-10-14

- 校管理员新建未分配菜单保存成功后列表不可见；管理列表按目标userId数据库启用school_admin精确角色取得全范围，导航selectMenuTreeByUserId仍按原分配。6项回归红1失败，完整Maven217项零失败错误，校管理员真实查询/打开修改/取消/删除新隐藏停用无权限QA目录复测并清理。证据school-menu-new-unassigned-*-before/after，临时2105与role_menu关联0。最新后端70993/backend-menu-management-scope.log，70265已正常停止；frontend90691/mysql5100沿用。全角色测试/总报告仍未完成，未构建部署产物、不改远程数据库、不退出程序或关机。


### 教师班级学院边界与共享中文下拉 — 2026-10-14

- admin原可保存外院班级；前端限定目标教师学院树、防加载失败/迟到响应误覆盖，后端先验证全部班级合法及所属学院再保存。13项红10失败，完整Maven211项零失败错误；admin/school本院班级保存重开与清空恢复已实际复测，外院搜索无匹配。其他角色及缓存班级迁移真实UI负向仍待，不迁移历史关联。后端句柄70265，日志backend-teacher-class-boundary.log，前端90691/MySQL5100不变。
- 共享Treeselect中文清空/无选项/无匹配/加载/重试等默认，5处引用保持原事件/render/数据值；继承检查通过，教师实际清空全部/暂无匹配结果中文，其他页面尚待。布局大小中文四标签/切换提示及admin公共搜索/全屏已实测。证据与备份qa-runtime/ui-full-role-20261012，整体全角色测试未完成，不构建部署产物、不改远程DB、不关机。


### 操作日志业务失败状态 — 2026-10-14

- 原AjaxResult.error/warn业务拒绝无抛异常时被记为成功；LogAspect补业务结果判定及中文原因，保留成功/异常优先/长度限制。198项完整Maven测试通过；校管理员浏览器角色重名拒绝的新日志显示失败与code500一致。只修改新日志生成，历史不改。隔离后端句柄4306，保留备份/浏览器证据，不生成部署产物；全角色按钮测试仍未完成。

### 超级管理员角色按钮保护 — 2026-10-13

- 系统角色列表的内置roleId=1原勾选框/状态开关仍能操作，后端已禁止修改。前端补禁用、选择过滤及状态事件守卫；数字/字符串ID回归红/绿及模板编译通过，admin和school_admin浏览器实际禁用复测。未改内置角色或现有用户权限。QA无权限测试角色/菜单已清理，证据ui-full-role-20261012；全角色测试仍未完成，不构建部署产物。

### 用户最新要求与弹窗修复 — 2026-10-13

- 用户已取消关机安排：不自动退出程序、网络代理或关闭电脑，全角色测试和报告继续。自动跟进及测试计划已同步，不修改其用户设定的50分钟周期。
- 非全屏弹窗增加max-width适应窗口，公告原780px在窄474px完整宽442px，桌面1280px仍780px，实际浏览器验证并保留截图。无部署构建/远程修改，整体测试未完成。


### 全角色按钮测试修复 — 2026-10-13（富文本中文提示）

- 公告编辑器实际链接查看提示Visit URL/Edit/Remove以及视频Embed URL残留英文，补查看/编辑/移除CSS中文、按编辑器实例设置链接和视频输入提示，保留原保存和格式行为。实际链接编辑保存移除及视频中文空保存已复测；Quill2.0.2本地实现与Context7官方文档核对，无依赖升级。图片错误类型拒绝、合法QA PNG上传、公告保存后图片回显通过；真实视频未测。备份/证据qa-runtime/ui-full-role-20261012，完整全角色测试仍未完成，不生成部署产物。


### 全角色按钮测试修复 — 2026-10-13（导出错误与内置管理员入口）

- 统一查询登录过期认证JSON被下载成ZIP，新增DOCX/ZIP头校验后拒绝错误内容并走中文失败提示；红/绿测试及正常浏览器导出通过，修复后过期负向UI仍待测。
- 用户管理内置admin可勾选启用工具栏/切状态，后端已有禁止保护。前端内置ID1数字/字符串均不可选、switch禁用、选择与状态事件守卫；完整reload后校管理员实际checkbox/switch禁用，普通QA用户修改备注与原角色状态保留通过。备份与证据在qa-runtime/ui-full-role-20261012；仍未全角色全页完成，不构建部署产物，不改远程数据库。


### 全角色按钮测试修复 — 2026-10-13（查询行发起联动）

- 未谈学生行的发起谈话链接带studentId却没有选中学生，浏览器复现。发起页增加一次性待选ID、数据加载后的选择及路由query监听；保留正在保存/附件重试的参与学生，消费后不干扰手动选择。不在可选权限列表内显示中文错误。新增脚本先红后绿，附件重试脚本仍通过；老师实际首次及缓存页第二学生带入均通过。其余角色仍待测，证据在qa-runtime/ui-full-role-20261012，不构建部署产物或改远程DB。


### 用户确认 — 2026-10-13（默认谈话表）

- 默认谈话表不自动追加已保存的学生反馈、跟进计划、状态，保留原有版式与数据保存功能。已撤回自动追加代码，测试 defaultTemplateDoesNotAppendFeedbackPlanOrStatus 通过，全套192项零失败错误；保留统一查询按选中记录导出的范围/格式修复。不构建部署产物，不修改远程数据库。完整全角色UI覆盖尚未完成。


### 全角色按钮测试修复 — 2026-10-13（多附件上传重试与下载）

- 后续完整reload仍复现部分失败时上一步可用，新增backToStudents守卫与显式禁用分支/key。红导航断言失败、绿通过；老师本人新会话部分附件失败时实际返回按钮与详情均disabled，teacher-upload-failed-back-locked.txt。老师换合法附件重试完成；院管理员两学生集体会话分别反馈/计划及两附件保存回显通过，college-group-two-feedback-persisted.txt。早期下述待复核说明以本条后续实测为准，整体全角色覆盖仍未完成。

- 实际个别谈话选择两附件后上传被重复提交拦截：通用拦截器将FormData序列化为{}，不同文件被误判同一数据。附件API设置repeatSubmit=false；前端保存已创建sessionId、顺序上传并记录成功文件uid，失败重试复用同一谈话且仅上传剩余文件，成功后清理状态。失败后锁定已保存的详情、显示中文重试提示，附件可移除/换文件。原流程两个失败点击已创建920592/920593，后续实际批量删除清理，保留备份及证据。
- check-initiate-attachment-retry.cjs先红后绿，覆盖multipart配置、首文件成功/次文件失败、重试不重复创建/上传、完成清理及模板编译。真实院管理员920594文档成功后，故意错误格式的图片被后端拒绝；移除并换正确PNG再点击重试，同一920594完成两份附件，SQL与管理列表一致，没有新增第二条重试会话。错误文件是截图JPEG字节却使用png后缀的测试资产，不能将内容校验拒绝算系统bug。
- 附件下载直接锚点/profile路径没有走后端下载，实际按钮没有获得文件。谈话管理及保留的旧会话页面改用现有$download.resource，带认证及API前缀。check-attachment-download.cjs红/绿通过；实际院管理员图片及文档下载成功，SHA256与原文件一致。仅实际可见管理页完成浏览器复测，旧页面脚本覆盖不冒充UI覆盖。
- 证据/原文件备份与red/green日志均在../qa-runtime/ui-full-role-20261012/。失败状态上一步按钮实际显示可用，与新增disabled表达式存在差异，需全量reload后复核；其他角色上传/下载、跨院下载拒绝、刷新时部分上传恢复、多人集体等尚待测。没有构建部署产物或修改远程数据库，不宣称全部测试完成。

### 全角色按钮测试修复 — 2026-10-13（老师会话按钮归属）

- 老师可读本学院全部谈话，但页面原仅校验通用权限，对他人会话仍显示修改/删除及明细新增/修改/删除。按现有后端requireOwnedWrite规则，前端仅全局/院管理员或talk_teacher本人createBy匹配时开放写按钮；工具栏选到任一只读会话时禁用修改删除，批量导出保留。后端校验不变。
- 真实老师浏览器：本人行仍可修改、工具栏修改删除启用并正常打开表单；院管理员新建会话仅导出/汇总，明细无新增修改删除，选中该行后修改删除禁用但导出启用。证据teacher-talk-ownership-before/after.txt/png、teacher-talk-own-edit-after.txt；其他管理员浏览器复测进行中。新增记录姓名/学号选择器在老师本人会话搜索命中本学院学生。
- 已有记录后端禁止更换学生，编辑选择器相应禁用，保持姓名学号回显。check-talk-owner-actions.cjs红失败、绿通过，覆盖四角色/停用旧角色规则、选中写限制与导出保留。备份talk-management.before-owner-actions.vue。不能以这些局部检查声明全角色全功能完成。

### 全角色按钮测试修复 — 2026-10-13（会话全部页签及学生选择）

- 院管理员实际保存默认集体谈话后修改地点，全部页签显示空列表；网络证据为talkType=0。全部页签name为空被组件回退为索引，改为显式all并映射服务端null。真实浏览器重新加载、个别/集体/全部切换、再次修改及刷新：2个个别+1个集体均保留，更新地点正常回显。check-talk-type-tabs.cjs红失败、绿通过；备份talk-management.before-tabs.vue及前后DOM/截图在../qa-runtime/ui-full-role-20261012/。
- 新增/修改谈话记录原要求手填内部学生ID，替换为按姓名或完整学号远程搜索选择，选项显示姓名与学号；复用原权限过滤列表/详情，不扩大范围。姓名与学号查询结果去重，保留已选标签，序列号阻止旧响应覆盖新搜索。脚本验证查询、去重、初始结果、标签保留及并发响应。院管理员实际按完整学号新增记录成功，姓名搜索与修改回显正常、空提交中文提示通过；其他角色仍待浏览器复测。
- 集体谈话保存后反馈/计划/标签在管理明细回显；浏览器导出汇总DOCX、会话ZIP与按地点筛选Excel成功。Excel仅1条数据，汇总正文含测试学生、地点与计划；文档视觉布局与其他导出分支仍待检验。当前不宣称全角色按钮测试完成，无部署构建或远程数据修改。

### 全角色按钮测试修复 — 2026-10-12（发起谈话学号搜索）

- 发起谈话学生选择将搜索词转小写，却对原学号直接includes，含大写字母学号无法匹配；学号同样转小写后比较。浏览器修复前UI20261012NEWCLASS无匹配，当前源码reload后准确命中测试学生。
- check-initiate-search.cjs修复前大小写学号断言失败，修复后大写/小写/混合、中文姓名、数字学号及空搜索通过；前后DOM、red/green日志和原文件备份保存在../qa-runtime/ui-full-role-20261012/。发起谈话后续选择、提交及其他角色仍待测。

### 全角色按钮测试修复 — 2026-10-12（系统模板可用不可维护）

- 实测院管理员系统模板行只有修改/删除，缺使用/预览；权限条件错误挂在前两个按钮。将canManageDefinition条件移至修改/删除，使用和预览对授权可读模板开放；后端维护权限不变。
- 编译真实操作列的四角色×两类型按钮渲染回归：红college_admin/system为修改删除，绿全部通过。院管理员实际预览显示正文，系统行仅使用/预览；使用操作复制内容到剪贴板，103字符与预期文本匹配。没有导航到发起谈话，使用语义为复制，不虚报预填。
- 修复前后及预览/复制证据在../qa-runtime/ui-full-role-20261012/college-admin-template-*.txt/png、template-buttons-red/green.log，备份templates-index.before-buttons.vue。模板新增、学院编辑/删除及其他角色浏览器复测仍待完成。

### 全角色按钮测试修复 — 2026-10-12（有学生的班级不可删除）

- 部门删除只查sys_user，学生新增不创建账号，因此有学生资料的班级仍可删除。权限范围校验后通过ITalkStudentService查询实际学生数量（含下级）；存在学生返回中文警告并停止删除，空部门仍可删除。
- 红用例模拟有学生无账号的班级，原返回200并执行删除；绿全套183项零失败/错误/跳过。院管理员当前源码浏览器确认删除新班级920005，收到“部门存在学生，不允许删除”，原行仍保留，证据college-admin-occupied-class-delete-after.txt/png。测试学生与班级均未删除。
- 院管理员组织类型学校/学院选项禁用，点击学院后类型仍年级；合法年级修改负责人成功。后端已重启应用此前目标类型限制，完整全角色按钮矩阵仍未完成。

### 全角色按钮测试修复 — 2026-10-12（院管理员目标组织类型）

- 组织类型选择补齐后审计发现，部门修改只保护已有学院节点，却未限制院管理员提交的新类型；新增已有grade/class限制。修改补同样限制，仅grade/class可提交，未提交类型的部分修改兼容保留旧值，原学院节点保护保持。
- DepartmentTypePermissionTest红7项4失败0错误，绿色全套181项0失败/错误/跳过。新增ruoyi-admin测试依赖只在test范围；没有构建部署产物。实际浏览器院管理员验证待完成，不能以测试方法代替。
- 校管理员浏览器完成新建年级→班级→新增学生闭环：UI20261012NEWCLASS成功保存并回显UI组织类型测试班级1012，证据school-admin-new-class-student.txt；隔离QA测试数据保留待其他角色及删除验证。

### 全角色按钮测试修复 — 2026-10-12（组织类型表单缺项）

- 校管理员从班级设置新增年级，原表单不提供deptType，数据库默认为dept，无法作为真实年级/班级识别。新增与修改表单补组织类型必填选择，列表中文显示学校/学院/年级/班级/其他部门；历史dept在编辑时兼容保留，未知值显示未分类。院管理员学校/学院选项禁用，实际后端限制仍须继续审计与浏览器验证。
- check-department-type.cjs修复前必填断言失败，修复后四类新增/修改提交字段、中文映射及模板编译通过。校管理员浏览器将测试节点920004保存grade，在其下新增920005保存class，列表及本地数据库证据一致；未选择类型时中文必填通过。学生选择联动和其他角色复测尚未完成，不宣称全功能完成。
- 备份dept-index.before-type.vue与dept-type-red/green.log、dept-type-required-after.txt、dept-type-saved-after.txt/png保存在../qa-runtime/ui-full-role-20261012/。无部署构建、无远程数据库变更。

### 全角色按钮测试修复 — 2026-10-12（校管理员组织范围）

- 校管理员实测新增教师时学院树显示No options available，框架DataScope按未填充的role.permissions将查询限定为dept_id=0。按当前领域约束，仅启用school_admin与内置管理员不加组织范围过滤；停用角色、无匹配权限角色及学院角色仍沿用原限制，清除外部传入dataScope的步骤保留。
- 新增6项权限回归：修复前1失败/0错误，修复后全部后端174项零失败/错误/跳过。页面复测可展开学校、看到多个学院并选择QA学院920001，部门管理也正常显示全校树。证据在../qa-runtime/ui-full-role-20261012/school-admin-college-selector-before/after.txt/png、school-admin-dept-after.txt及school-scope-red/green.log。
- 当前仅局部权限修复通过，全角色逐按钮测试继续；未构建部署产物、未修改远程数据库。

### 全角色按钮测试修复 — 2026-10-12（工具栏修改与取消语义）

- 选中学生后点击工具栏修改，原代码将MouseEvent视为学生行对象，请求studentId=undefined。改为先检查行对象实际studentId，再使用选中ID；工具栏、无事件调用、行内修改三类回归通过，并从浏览器验证正确学生与班级回显。
- 标签、学生、教师删除以及谈话管理的删除/Word导出，将确认框取消/关闭错误显示为操作失败。仅忽略cancel/close，真实请求失败仍保留提示；9个方法×3种结果的前端回归27项通过。老师标签页取消/关闭浏览器复测均保留原数据且无错误提示，其他角色及页面仍需逐按钮实测。
- 证据在 `../qa-runtime/ui-full-role-20261012/`：student-toolbar-red/green.log、student-toolbar-fixed-after.txt、confirmation-red/green.json、talk_teacher-tag-cancel-after.txt/png与talk_teacher-tag-confirm-close-after.txt。原确认处理源码保存在confirmation-before目录。

### 全角色按钮测试修复 — 2026-10-12（学生必须绑定班级）

- 老师在新增表单选择学院节点后可成功保存，违反学校→学院→年级→班级的学生归属规则。新增及变更学生部门时，后端校验所选节点实际为class，拒绝学院、年级及不存在节点；学院权限校验保持有效。
- 表单班级选择仅选择叶节点，树映射不为班级添加空children，消除可展开空子菜单；查询条件仍保留学院/年级层级筛选。
- 回归红日志 `../qa-runtime/ui-full-role-20261012/student-class-red.log`（16项、12失败、0错误），绿色全套168项零失败/错误/跳过；补充四角色16项接口验证通过，记录于student-class-http.json。浏览器当前源码班级流程复测继续进行；接口结果不替代UI按钮覆盖。

### 全角色按钮测试修复 — 2026-10-12（导入预览与模板说明）

- 老师实测选择覆盖已有数据时，预览仍称将跳过，而执行结果为成功1条/已更新。仅对已存在学号的预览提示按实际 `importMode=update` 显示覆盖；Excel内重复等其他提示保持其真实处理语义。
- 下载的当前模板第1行为说明、第2行为标题、第3行填数据，更新导入窗口说明与实际模板一致。
- 修复前 `../qa-runtime/ui-full-role-20261012/student-import-overwrite-before.txt`，执行证据 `student-import-overwrite-execute.txt`，复测 `student-import-overwrite-after.txt/png`；切回跳过模式仍显示将跳过，预览返回保留文件。原页面备份 `student-index-before-import-fix.vue`。中文状态21项检查通过。

### 全角色按钮测试修复 — 2026-10-12（学生详情资料缺项）

- 老师实测新增学生后打开详情，原详情仅展示部分资料，未包含班级、身份证号、父母姓名电话、班长、舍长、宿舍楼号、备注。按既有学生实体字段补齐这11项，并增加返回列表按钮。
- 修复前 `../qa-runtime/ui-full-role-20261012/student-detail-before.txt`，原文件 `student-detail.before.vue`；当前源码浏览器复测 `student-detail-values-after.txt/png` 验证实际保存的班级、父母姓名、宿舍、备注及中文学籍/心理状态；返回按钮实际导航至 `/talk/student`。
- 不修改资料权限：四角色学院边界继续由后端校验；老师仅查看授权学院学生。全角色其他按钮及管理角色对新增详情字段的验证尚在进行。

### 全角色按钮测试修复 — 2026-10-12（学生班级必填提示）

- 浏览器实测学生新增表单，空提交的班级错误提示为技术字段“部门ID(班级)不能为空”；改为“请选择班级”，校验触发改为选择变更，以便选班后及时清除提示。
- 修复前与修复后浏览器证据保存于 `../qa-runtime/ui-full-role-20261012/student-required-before.txt` 和 `student-required-after.txt`，修复后截图 `student-required-after.png`。原页面备份为 `student-index.before.vue`。
- 全角色按钮测试仍在进行，不将此前接口测试或此局部复测宣称为全部功能通过。未构建部署产物，未修改远程数据库。

## v10 当前基线（2026-09-09）

本节覆盖下文保留的历史轮次说明；实现与后续开发以 `CONTEXT.md`、`docs/adr/0003-four-role-college-scope.md` 和 `sql/talk_role_permission_v9.sql` 为准。

- 当前权限角色仅为内置 `admin`、`school_admin`、`college_admin`、`talk_teacher`；岗位仅用于教师资料展示，不参与授权。
- `admin` 与校管理员全校管理；院管理员管理本学院；普通教师读取本学院全部业务数据，但学生不可删除、谈话只能修改或删除本人创建的数据。
- 统一查询和统计分析上线；学生端暂停上线，预约、预警、大屏、回访、学生反馈、我的记录仅保留代码和数据，菜单隐藏。
- 系统标签与系统模板全校可用，只允许 `admin`/校管理员维护；学院标签与学院模板由本学院教师共同维护。
- 首次部署 v9 代码前必须执行 `sql/talk_role_permission_v9.sql`。

基于若依（RuoYi-Vue）框架构建，面向高校辅导员、学院书记/副书记的日常谈话管理场景。

## 项目信息

| 属性 | 值 |
|------|-----|
| 框架 | RuoYi-Vue (Spring Boot + Vue2 + Element UI) |
| 后端路径 | `ruoyi-admin/` + `ruoyi-system/src/main/java/com/ruoyi/talk/` |
| 前端路径 | `ruoyi-ui/src/views/talk/` + `ruoyi-ui/src/api/talk/` |
| 学生端 | `Student-UI/` (独立 Vue2 项目) |
| SQL脚本 | `sql/` 目录（12个文件） |
| 文档 | `CONTEXT.md` (领域术语) + 本文件 |

## Agent skills

### Issue tracker

使用 GitHub Issues，仓库 `jimu-Z/tanhuan`。详见 `docs/agents/issue-tracker.md`。

### Triage labels

使用标准 5 级标签体系：`needs-triage` / `needs-info` / `ready-for-agent` / `ready-for-human` / `wontfix`。详见 `docs/agents/triage-labels.md`。

### Domain docs

单上下文布局，`CONTEXT.md` + `docs/adr/` 位于项目根目录。详见 `docs/agents/domain.md`。

## Agent 开发规范

### 数据权限铁律（第五轮修复后强制）

**任何新增或修改的查询接口必须通过以下检查清单：**

```
[ ] 该接口是否涉及学生/谈话记录数据？
[ ] 如果是，是否调用了 applyCounselorFilter() 或 applySecretaryFilter()？
[ ] 统计类接口是否有角色维度过滤（书记限学院、辅导员隐藏）？
[ ] Mapper XML 是否使用了参数化查询（#{} 而非 ${}）？
[ ] 新增接口是否在 TalkStatisticsController 中添加了防御性过滤？
```

### 角色数据范围速查表

| 角色 | 学生列表 | 谈话记录 | 统计分析 | 预警 | 教师管理 | 预约 |
|------|:--------:|:--------:|:-------:|:----:|:--------:|:----:|
| admin | 全校 | 全校 | 全校排名 | 全部 | CRUD | 查看 |
| secretary | 本学院 | 本学院 | **本学院排名** | **本学院** | 本学院CRUD | - |
| counselor | **本班** | **自己创建** | **隐藏** | **本班** | - | - |
| student | 仅自己 | 仅自己(脱敏) | - | - | - | 自己创建 |

### 代码修改必读

1. **Service层**：所有列表查询方法必须调用 `applyCounselorFilter(params)` 或 `applySecretaryFilter(params)`
2. **Controller层**：统计/聚合接口必须在 Controller 内做角色判断，不能依赖 Service 层
3. **Mapper XML**：禁止 `${}` 直接拼接用户输入，统一使用 `#{}` 参数化
4. **前端页面**：API 路径必须与 Controller 的 `@RequestMapping` 匹配
5. **SQL脚本**：全部使用 `INSERT IGNORE` 保证幂等

## BUG 修复历史

### 第二十轮修复 — 2026-10-12（额度重置后追加优化）

| 优化轮次 | 问题与修复 | 验证 |
|---|---|---|
| 6 | 删除有谈话记录的学生时保留明确中文业务原因 | 后端 124 项，四角色两次 220/220 项全通过 |
| 7 | 登录验证码及账号错误专项中文提示，未知异常继续隐藏内部细节 | 后端 124 项，四角色两次 224/224 项全通过 |
| 8 | 教师新增与导入必填字段去空白校验，导入更新状态校验及停用状态保留 | 后端 124 项，四角色两次 224/224 项全通过 |
| 9 | 标签批量删除提前授权、去重并使用事务，拒绝后保留全部标签 | 后端 124 项，四角色两次 226/226 项全通过 |
| 10 | 模板标签使用JSON序列化，特殊字符正确转义并拒绝无效数组 | 后端 128 项，四角色两次 230/230 项全通过 |
| 11 | Word导出附件复用上传目录边界，跳过目录外和缺失文件并保留合法附件 | 后端 130 项，四角色两次 230/230 项全通过 |
| 12 | Word归档安全唯一命名、保留重名附件及标签，集体回退ZIP与批量扩展名一致 | 后端 152 项，四角色两次 234/234 项全通过 |
| 13 | 谈话创建组织上溯限制环路和深度，异常或无权组织节点不阻断正常部门树 | 后端 138 项，四角色两次 238/238 项全通过 |
| 14 | 跟进状态限制新输入为四种上线编码，历史读取与未提交状态字段保持兼容 | 后端 147 项，四角色两次 246/246 项全通过 |
| 15 | 教师快捷升降保护内置与缺失账号、先验证启用角色，复用角色分配并撤销旧会话 | 后端 152 项，四角色两次 250/250 项全通过 |

### 第十九轮修复 — 2026-10-12（持续优化：教师账号与记录关联）

追加优化：第 6 轮保留学生受记录保护删除提示；第 7 轮针对 UserException 返回本地化登录错误，未知异常仍隐藏内部细节。验证证据按优化轮次保存。

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | HIGH | 新增教师选择停用仍被强制启用，院管理员停用自身或最后启用管理员可绕过角色安全规则 | 新增状态保留并同步账号，校验合法状态，停用复用管理员保留规则 | TalkTeacherServiceImpl.java, TeacherImportRoleTest.java |
| 2 | HIGH | 教师删除缺少内置管理员保护，批量操作先删前项再验证后项 | 全批次提前验证教师和实际关联账号，保护内置管理员与校管理员 | TalkTeacherServiceImpl.java, TeacherImportRoleTest.java |
| 3 | HIGH | 已有谈话记录修改允许重绑会话或学生；新增记录未校验学生与会话学院一致 | 固定历史关系，新增校验学生存在及学院一致，组织上溯环路保护 | TalkStudentRecordServiceImpl.java, TalkRecordIntegrityTest.java |
| 4 | MEDIUM | 谈话创建接受非法类型和日期、重复参与学生、不属于参与学生的记录数据 | 写入前验证类型、严格日期、唯一有效参与人、个别谈话单人及反馈关系 | TalkSessionServiceImpl.java, TalkSessionInputTest.java |
| 5 | HIGH | 附件路径拼接允许越过上传目录；批量删除后项权限拒绝前已经删除前项文件 | 统一规范化及实际目录边界检查，批量提前验证所有项目权限 | AttachmentPathUtils.java, TalkAttachmentServiceImpl.java, TalkSessionServiceImpl.java, TalkAttachmentDeletionTest.java |
| 6 | MEDIUM | 标签批量读取重复查询会话，忽略非法编号，缺失会话仍读取孤立标签 | 使用现有批量 Mapper 一次检查会话，编号去重及严格校验，先校验存在性与学院权限再取标签 | TalkSessionServiceImpl.java, TalkTagBatchTest.java |

优化过程使用独立本地数据库，不更新服务器数据或构建部署产物；逐轮验证证据见工作区 qa-runtime/optimization-20261012-135435。

### 第十八轮修复 — 2026-10-12（业务编码中文显示扫描）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | MEDIUM | 学生列表/详情直接显示政治面貌、学籍、心理健康及贫困等级编码，列表与详情字典名称不一致 | 集中业务中文映射，优先读取系统字典，兼容中文值及旧数字值；教师学生弹窗也使用同一映射与字典 | talkLabels.js, main.js, talkStudent/index.vue, talkStudent/detail.vue, teacher/index.vue |
| 2 | LOW | 未知跟进类型回显英文，查询和首页将未知谈话类型误判；部分自定义标签回显标识 | 统一类型及跟进中文兜底，补齐模板 personal 类型；标签使用数据库名称并兼容 JSON 数组/逗号字符串；保留学生端同类页面也同步中文显示 | talk 相关视图, Student-UI 相关视图 |
| 3 | MEDIUM | Word 导出缺少健康/预警状态及政治面貌映射，程序回退导出直接输出标签 JSON 编码 | 补齐常量，导出学生字段优先系统字典；标签解析数组后翻译数据库名称；新增模板占位符及真实 DOCX 回退输出回归测试 | TalkConstants.java, TalkDocxService.java, TalkDocxServiceTest.java |
| 4 | MEDIUM | Excel 导出直接输出学生状态、政治面貌、贫困等级、教师角色和跟进状态编码 | 使用仅导出转换的字段处理器，导入保持原值；优先系统字典，支持中文值及旧数字值；通过真实 Excel 导出读取验证 | TalkExcelLabelHandler.java, TalkStudent.java, TalkTeacher.java, TalkStudentRecord.java, TalkExcelLabelsTest.java |
| 5 | LOW | 缓存监控备份状态显示 ok/err，哨兵运行模式误显示集群 | 补齐备份结果及 Redis 三种运行模式中文映射 | talkLabels.js, monitor/cache/index.vue |

按用户要求本轮不生成部署产物，仅修改源码和执行测试。

### 第十七轮修复 — 2026-10-12（教师学生弹窗状态中文显示）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | LOW | 教师学生列表弹窗只映射数字状态，实际 healthy/active 等编码直接显示英文 | 补齐当前及旧版心理健康/学籍状态中文映射，保留数字编码兼容；空值显示未设置，未知编码显示未知状态；通过实际 Vue 单元格渲染验证，不构建部署产物 | teacher/index.vue, qa-runtime/check-teacher-student-status.cjs |

### 第十六轮修复 — 2026-10-12（教师新增及编辑表单）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | MEDIUM | 教师新增/编辑缺少角色与备注，编辑接口不保存角色调整 | 共享表单加入三角色必选下拉、备注文本框，新增默认老师；编辑根据已有账号验证并同步业务角色，变化时撤销会话；院管理员不可分配校管理员，保护内置账号及自己/最后一名院管理员；新增三个服务回归测试及表单提交检查 | teacher/index.vue, TalkTeacherServiceImpl.java, TeacherImportRoleTest.java |

### 第十五轮更新 — 2026-10-12（黑白导入模板及角色列）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | MEDIUM | 教师导入只能默认老师，无法按模板指定角色；已有账号角色刷新缺少显式选择和逐行原子提交 | 增加“角色”列及三角色下拉；服务端校验角色、学院及管理员保护规则；更新时仅替换业务角色；每行事务回滚失败记录 | TalkTeacher.java, TalkTeacherServiceImpl.java, TeacherImportRoleTest.java |
| 2 | LOW | 模板字段顺序、分割及配色不符合实际填写要求 | 学生学号/姓名置前、移除序号；两模板单页无冻结、黑白边框、必填浅灰表头；说明改为表头单元格批注 | teacher_import_template.xlsx, student_import_template.xlsx, ImportTemplatesTest.java |
| 3 | HIGH | 角色降级后 Redis 登录缓存可能保留全校权限；单次删除会话存在并发旧缓存写回窗口 | 角色变化撤销目标账号全部会话；每个已登录请求重读数据库中的账号状态、角色、学院和权限；停用角色不授予权限 | JwtAuthenticationTokenFilter.java, TalkPermissionUtils.java, JwtRoleRefreshTest.java |
| 4 | MEDIUM | 校管理员不在教师角色查询投影中，列表误显示老师并显示无效院管理员切换按钮 | 补齐三角色查询优先级及正确显示，隐藏校管理员的院管理员升降按钮 | TalkTeacherMapper.xml, teacher/index.vue |

### 第十四轮修复 — 2026-10-12（导入模板兼容性）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | MEDIUM | 学生模板缺少性别且三组电话重名，编号格式及状态编码缺少明确说明 | 补齐性别和清晰电话表头，保持导入识别顺序；两份模板使用文本列、必填标识、下拉选项、独立说明及示例页；不生成空白数据行；用真实导入器验证映射和前导零 | teacher_import_template.xlsx, student_import_template.xlsx, ImportTemplatesTest.java |

### 第十三轮修复 — 2026-10-12（GIF 附件文件头校验）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | MEDIUM | GIF 校验连续读取两次文件头，误拒绝合法 GIF89a，也可能接受第七字节才出现 GIF89a 的伪装文件 | 一次读取首六字节并校验 GIF87a/GIF89a；新增两种合法图片、偏移伪装头和截断头回归测试 | AttachmentFileValidator.java, AttachmentFileValidatorTest.java |

### 第十二轮修复 — 2026-09-11（浏览器 QA 控制台与无障碍回归）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | MEDIUM | 学生导入弹窗的 `ElUpload` 未提供必填 `action`，每次打开均产生 Vue 控制台错误 | 明确传入空 `action`；实际文件预览仍走既有手动处理流程，不触发自动上传 | talkStudent/index.vue |
| 2 | MEDIUM | 教师班级管理以班级名称作为树节点值，同名班级导致 vue-treeselect 重复 ID 警告 | 树节点统一使用部门 ID；打开时将已存班名映射为 ID，保存时再去重还原为班名 | teacher/index.vue |
| 3 | LOW | 列表右侧图标按钮缺少可访问名称 | 为搜索显示、刷新、显隐列按钮补充 `aria-label` | RightToolbar/index.vue |

### 第十一轮修复 — 2026-09-10（本地 QA 预约、导出与异常回归）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | HIGH | 确认预约创建谈话时遗漏 `college_dept_id`，MySQL 拒绝插入，并把 SQL 与服务器路径回显给调用方 | 预约确认前校验学生/教师及学院一致性；写入会话学院快照并以当前确认人作为创建者 | TalkAppointmentServiceImpl.java |
| 2 | MEDIUM | 未处理运行时/系统异常将数据库驱动、SQL 和文件路径直接回显 | 服务端记录完整异常，客户端仅返回固定的内部错误提示；业务异常仍保留明确提示 | GlobalExceptionHandler.java |
| 3 | MEDIUM | 批量导出忽略不存在、越权或生成失败的会话，导致 HTTP 200 空 ZIP；单个不存在会话也可能生成错误响应 | 先完成会话存在性与权限校验，再输出 ZIP；不存在的单项 DOCX/详情/参与人请求返回明确错误 | TalkSessionController.java |
| 4 | MEDIUM | 相同请求可并发重复创建会话与学生记录 | 为创建会话接口启用基于 Redis 的重复提交拦截 | TalkSessionController.java |

### 第十轮修复 — 2026-09-10（本地 QA 安全与附件回归）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | HIGH | 学生导入错误内容被拼接进 `dangerouslyUseHTMLString` 弹窗，可导致 DOM XSS | 使用 Vue VNode 和文本子节点渲染错误数组，移除危险 HTML 选项 | talkStudent/index.vue |
| 2 | HIGH | 附件上传返回 `/profile//...`，按原返回地址无法下载 | 规范化 profile 与上传目录路径，空相对目录不再额外拼接 `/`；新增路径回归测试 | FileUploadUtils.java, FileUploadUtilsTest.java |
| 3 | MEDIUM | 附件接口仅检查声明 MIME 和扩展名，HTML 等内容可伪装成 XLSX 上传 | 校验文件签名及 OOXML ZIP 必要目录；新增真实结构与伪装文本回归测试 | TalkAttachmentController.java, AttachmentFileValidator.java, AttachmentFileValidatorTest.java |
| 4 | MEDIUM | CORS 对任意 Origin 回显允许来源 | 改为显式前端白名单，默认仅本机 6080；禁止跨域凭据，部署时可用环境变量覆盖 | ResourcesConfig.java, application.yml |

### 第九轮修复 — 2026-09-09（统一查询、统计分析与门户首页）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | HIGH | v9 迁移脚本隐藏统一查询和统计分析，且新角色缺少菜单 | 两个菜单恢复显示，并授权院管理员和老师 | talk_role_permission_v9.sql, localhost_v10_query_statistics.sql |
| 2 | HIGH | 统一查询依赖前端拉取 9999 条会话并逐学生查询 | 改为后端参数化分页组合查询，标签批量加载 | TalkStudentRecordMapper.xml, unifiedQuery/index.vue |
| 3 | MEDIUM | 批量导出请求体格式与 Controller 不一致 | 统一使用 `{ sessionIds: [...] }` API | unifiedQuery/index.vue |
| 4 | MEDIUM | 覆盖率按记录数计算会重复统计学生 | 改为按 `distinct student_id` 计算 | TalkStatisticsController.java, TalkStudentRecordMapper.xml |
| 5 | MEDIUM | 月度趋势取得最早 12 个月 | 改为最近 12 个月后按月份正序展示 | TalkSessionMapper.xml |
| 6 | MEDIUM | 学院学生数仅统计学院直属学生 | 统计学院及其下级年级、班级全部学生 | TalkStudentMapper.xml |
| 7 | LOW | 首页快捷路径与动态菜单路径不一致、视觉层级弱 | 重构为数据化网站门户首页并修正入口 | index.vue |
| 8 | MEDIUM | 管理端开发代理固定指向生产服务器，无法联调本机后端 | 默认改为 `127.0.0.1:6060`，支持环境变量覆盖 | vue.config.js |

### 第八轮修复 — 2026-06-16（生产部署+导入BUG+权限+仪表盘重构）

> 触发条件：部署到 211.64.39.248 后，教师/学生导入失败、系统管理员无权限、仪表盘排名竞争性

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | CRITICAL | `SysDeptMapper.xml` insertDept 缺少 `useGeneratedKeys`，导致新建部门后 deptId 返回 null | 添加 `useGeneratedKeys="true" keyProperty="deptId"` | SysDeptMapper.xml |
| 2 | CRITICAL | 教师导入"所属学院不能为空"：getOrCreateCollegeDept 用 like 模糊匹配 | 改用精确匹配(deptName+parentId=100) + 二次确认 | TalkTeacherServiceImpl.java |
| 3 | CRITICAL | 学生导入"dept_id doesn't have default value"：findOrCreateDept 返回 null | 增加主键回填安全检查 + 参数校验 | TalkStudentServiceImpl.java |
| 4 | CRITICAL | 学生导入创建 parent_id=0 的脏部门数据 | findOrCreateDept 增加 null 结果跳过 | TalkStudentServiceImpl.java |
| 5 | HIGH | 系统管理员角色看不到教师管理数据：自定义 applyDataScopeFilter 只判 isAdmin | 删除自定义过滤，改用 @DataScope(deptAlias="d") 注解 | TalkTeacherServiceImpl.java, TalkTeacherMapper.xml |
| 6 | HIGH | 统一查询页面 403：talk:record:list 权限不存在 | 新增菜单2026并分配给角色3/4/5 | 数据库SQL |
| 7 | MEDIUM | 仪表盘"各学院谈话排名"具有竞争性 | 替换为：工作提醒+预警概览+最近谈话动态 | TalkStatisticsController, dashboardV2/index.vue |
| 8 | MEDIUM | 前端标签显示 ["mental_health"] 而非中文 | getTagLabel 增加 JSON 数组字符串解析兼容 | talkManagement, talkSession, unifiedQuery |
| 9 | MEDIUM | 我的谈话记录：跟进状态+教师已读列对学生无意义 | 删除两列，谈话内容列拉长 | myRecords/index.vue |
| 10 | MEDIUM | 统一查询：跟进状态列和搜索条件多余 | 删除跟进状态列和搜索，添加谈话人列 | unifiedQuery/index.vue |
| 11 | LOW | 标签管理标签标识列显示英文key | 改为显示 tagName（中文） | talkTag/index.vue |
| 12 | LOW | Student-UI 端口非60系 + 代理指向localhost | 端口改6090，代理改211.64.39.248:6060 | Student-UI/vue.config.js, .env.production |

### 第七轮修复 — 2026-06-15（书记/辅导员页面403权限错误）

> 触发条件：书记/辅导员登录后，多个页面弹"当前操作没有权限"

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | CRITICAL | 前端调用 `/system/dept/list` 需 `system:dept:list` 权限，书记/辅导员无此权限 | 前端改用 talk 模块自己的 `getDeptTree` API | talkStudent/index.vue, teacher/index.vue |
| 2 | CRITICAL | `/talkrecord/list` 需 `talk:record:list` 权限，书记/辅导员只有 `talk:record:query` | 给书记和辅导员补上菜单2020权限 | talk_menu_fix.sql |
| 3 | CRITICAL | `/talktag/*` 需 `talk:tag:list` 权限，书记/辅导员无此权限 | 给书记和辅导员补上菜单2080权限 | talk_menu_fix.sql |
| 4 | CRITICAL | `/talk/alert/list` 需 `talk:alert:list` 权限，预警菜单(2055)缺少子菜单 | 新建预警子菜单(2056-2059)并分配给书记/辅导员 | talk_menu_fix.sql |
| 5 | MEDIUM | 后端 `deptTree` 只查 `deptType=class` 导致学院节点缺失 | 移除 `dept.setDeptType("class")` 过滤 | TalkStudentController.java |

### 第六轮修复 — 2026-06-15（编译错误 + 安全漏洞 + 跟进菜单隐藏）

> 触发条件：全量扫描后发现编译错误、安全漏洞和跟进菜单需隐藏

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | CRITICAL | TalkStatisticsController.dashboard() 重复声明 scopeParams 变量 | 删除第144行重复声明，复用第134行变量 | TalkStatisticsController.java L144 |
| 2 | CRITICAL | TalkAlertController /debug 接口 @Anonymous 绕过认证 | 删除整个 debug() 方法及相关 import/字段 | TalkAlertController.java |
| 3 | HIGH | 谈话跟进菜单(2075)需隐藏但功能保留 | sys_menu visible='1' + UPDATE 语句保证幂等 | talk_menu_fix.sql L61-62, L153-154 |

### 第五轮修复 — 2026-06-15（数据权限泄漏 + 菜单缺失 + 安全加固）

> 触发条件：以书记/辅导员账号登录系统，查看菜单发现报错和数据泄漏

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | CRITICAL | dashboard collegeRanking 泄漏全校学院数据 | 添加 createQueryParams() 角色过滤 | TalkStatisticsController.java L141-160 |
| 2 | CRITICAL | alerts deptCoverage 泄漏全校部门数据 | 添加 ancestors 判定+角色过滤 | TalkStatisticsController.java L201-219 |
| 3 | CRITICAL | selectUntalkedStudentsInPeriod 无权限控制 | 新增 params 参数传递过滤器 | TalkStudentServiceImpl + Mapper 接口 + XML |
| 4 | HIGH | talk_menu_fix.sql 缺V3菜单INSERT(2090/2100) | 补全8个菜单+2个角色授权块 | talk_menu_fix.sql L119-136 |
| 5 | HIGH | 预约页教师用纯文本输入 | 改为el-select下拉选择器 | appointment/index.vue |
| 6 | MEDIUM | console.error调试残留 | 替换为$modal.msgError() | teacher/index.vue L520 |
| 7 | MEDIUM | Token弱密钥abcdefghijklmnopqrstuvwxyz | 替换为强随机密钥 | application.yml L100 |

### 历史轮次摘要

| 轮次 | 日期 | 核心内容 | 修复数 |
|------|------|---------|:------:|
| 第一轮 | — | SQL注入、级联删除、通知机制、空catch、页面整合 | ~20 |
| 第二轮 | — | DateUtils导入、teacher_notified逻辑、标签DB驱动、批量API、集体谈话汇总导出 | ~15 |
| 第三轮 | — | 全量扫描：软删除失效、SQL注入参数化、NPE、Student-UI表格Bug、字段误用 | ~20 |
| 第四轮 | 2026-06-02 | 深度扫描84项：文件上传校验、DROP TABLE风险、主键冲突、索引建议 | ~55累计 |
| **第五轮** | **2026-06-15** | **数据权限泄漏×3、菜单缺失×2、安全加固×2** | **10** |
| **第六轮** | **2026-06-15** | **编译错误×1、安全漏洞×1、跟进菜单隐藏×1** | **3** |
| **第七轮** | **2026-06-15** | **跨模块权限依赖×2、部门树过滤×1** | **3** |

## 关键架构决策记录 (ADR)

| ADR编号 | 决策 | 状态 |
|---------|------|:----:|
| ADR-0001 | 页面整合：会话管理+记录管理→谈话管理(4→2) | 已实施 |
| ADR-0002 | Student-UI独立部署，不混入ruoyi-ui | 已实施 |
| ADR-0003 | 标签从硬编码常量改为数据库驱动 | 已实施 |
| ADR-0004 | 数据权限双层过滤：业务自定义 + DataScope AOP | 已实施 |
| ADR-0005 | V3重构：talk_teacher表 + talk_alert表 + talk_appointment表 | 实施中 |
| ADR-0006 | 生产部署：211.64.39.248，60系端口(6060/6080/6090)，Nginx反代 | 已实施 |
| ADR-0007 | 仪表盘去竞争化：删除学院排名，改为工作提醒+预警概览+最近动态 | 已实施 |
| ADR-0008 | 教师数据权限：从自定义applyDataScopeFilter改为@DataScope注解 | 已实施 |


## BUG 修复历史补充（2026-10-16）

| 问题 | 根因与修复 | 验证 |
| --- | --- | --- |
| 学生按中文性别、贫困等级筛选无结果 | 查询表单自由文本直接提交中文，Mapper精确匹配存储编码。查询改用现有性别/贫困字典下拉，中文展示、编码传参，支持清除与更改后回第一页。未改接口、数据权限或数据库。 | QA_SCOPE_C1016实际浏览器修复前两项无结果；编译查询表单行为测试先3失败，修复后和浏览器复测结果见 qa-runtime/ui-full-role-20261012/evidence-scope-audit-20261016。 |


### 2026-10-16 学生导入文件选择状态修复

| 问题 | 修复 | 验证 |
| --- | --- | --- |
| 移除已选文件后仍可预览旧文件；非Excel只报系统错误 | Element Upload移除不触发on-change。补on-remove同步清空原文件与列表；选择时校验.xls/.xlsx并给中文提示，手动预览入口不再保留移除文件。 | 实际浏览器复现与check-student-import-selection.cjs修复前2失败/1通过，修复后与浏览器证据见qa-runtime/ui-full-role-20261012/evidence-scope-audit-20261016。 |

### 2026-10-16 学生导入预览与覆盖一致性修复

| 问题 | 根因与修复 | 验证 |
| --- | --- | --- |
| 学生姓名为空却在预览显示正确 | 预览必填分支遗漏姓名，执行阶段才跳过。预览补姓名为空错误，不改变权限及数据库。 | 真实四行Excel预览复现，TalkStudentImportValidationTest该断言修复前失败；学生相关23项回归通过。浏览器复测证据见qa-runtime/ui-full-role-20261012/evidence-scope-audit-20261016。 |
| 覆盖模式把Excel内后续重复学号再覆盖第一行 | 执行未记录本批次已处理学号，与预览“重复将跳过”不一致。执行增加本次请求集合，重复行计跳过，保留首条。 | 真实UI原成功3/跳过1、B姓名变QA重复1016；行为回归原失败，修复后通过。仅本轮虚构学生参与。 |

- 最终已有学号预览明确两种模式；TalkStudentImportValidationTest 3项及学生相关共24项通过，真实预览/执行/首条保留复测通过，证据已保存。

### 2026-10-16 学生期间未谈筛选修复

- 日期控件原直接传数组，列表未绑定有效期间且导出忽略日期。列表与导出共用buildStudentQuery转换params.beginTime/endTime，两条原SQL复用参数化NOT EXISTS日期条件，保留所有学号、性别及学院过滤，结束日包含全天；界面明确标注未谈日期范围。前端3项原失败/修复后通过，MyBatis真实动态SQL原1失败/修复后2通过，学生相关共29项后端通过，旧前端6项及工具栏通过。浏览器QA910101本月/14日/12日有谈话正确排除，2020无谈话期间返回1；新QA按学号日期实际导出仅1条，证据evidence-scope-audit-20261016。


### 2026-10-16 中文学生模板导入编码修复

- 真实中文模板原把女/群众/休学/轻度干预/C一般困难原文入库，列表看似中文正常但编码筛选失效。导入预览及执行均独立规范化五类枚举，性别按0/1/2，其余按当前系统字典标签转值，已有编码保留。两项行为回归原2失败，修复后与31项学生后端回归全部通过。浏览器只重导本轮920300/920301成功2跳过2，SQL只读确认1/people/suspended/mild_warning/severe，女+贫困+日期组合1、男组合0；证据evidence-scope-audit-20261016。


### 2026-10-16 学生级联班级查询修复

- 查询级联器原返回路径数组而后端只接收Long，选择不同班级仍出现目标学生。列表导出共用查询助手取末级ID，清空路径移除过滤；原列表/导出SQL保留学院隔离并允许选择学院或年级时包含下属班级。前端新增2项原失败、修复后5项通过；动态SQL父级分支原失败，学生相关32项回归通过。真实浏览器复测证据及剩余项记录于qa-runtime/ui-full-role-20261012/progress.md。

### 2026-10-16 历史中文枚举筛选兼容

- 收尾只读发现旧QA920292仍存男/在读/健康，不能仅修新导入后宣称完整通过。查询及导出按当前字典同时匹配编码和历史中文原值，别名仅服务端生成并清除客户端输入，学院隔离保留，不批改旧记录。服务参数及真实SQL回归先失败，学生相关35项通过；实际浏览器前后证据见evidence-scope-audit-20261016。


### 2026-10-16 学生Excel性别与班级可读性修复

- 真实导出旧QA中文男字段变空，班级只显示内部部门ID。性别改用兼容0/1/2及中文的导出处理器，班级改导出已有deptName，不改变数据库ID及原值。新增真实工作簿两项回归先失败，修复后与学生相关42项后端回归全部通过；实际下载前后工作簿保留，见evidence-scope-audit-20261016。


### 2026-10-17 集体谈话汇总Word类型中文映射修复

- 浏览器实际导出汇总Word时，谈话类型仍显示 `group`；根因为汇总生成器直接写入存储编码，其余个人表和模板导出已使用中文。
- 汇总类型显示改为与现有个人表一致的中文映射，保留原存储编码、反馈和跟进内容。
- 新增实际DOCX回归测试，覆盖 group/individual 中文显示且原编码不变；修复前两例断言失败，修复后定向回归与隔离QA浏览器导出复测证据保存在 qa-runtime/ui-full-role-20261012/evidence-scope-audit-20261016。


### 2026-10-17 失效认证响应伪装为谈话文件修复

- 隔离QA后端重启后，旧认证下汇总导出返回JSON 401；页面原来仍保存为 `.docx` 并提示导出成功。真实下载不是ZIP文件，已保留失败证据，未计为通过。
- 六种谈话Word/ZIP导出API在下载前统一拒绝JSON错误Blob，沿用页面的中文失败提示；正常二进制响应原样返回。
- API调用行为回归覆盖失效认证和真实二进制：修复前6项错误响应未拒绝，修复后12项通过；另需浏览器过期认证分支复测。


### 2026-10-17 全局管理员学院模板归属表单修复

- 校管理员实际选择学院模板保存后变成系统预置；原因是界面没有所属学院字段，而后端按collegeDeptId是否存在确定范围。
- 新增学院模板时补齐必选所属学院，复用谈话模块部门树，仅列学院节点；切回系统预置清除学院ID。已有模板类型与学院只读，与后端更新时保留范围一致；老师和院管理员仍自动归属本学院。
- 编译真实表单回归覆盖admin/school_admin必选学院、切换清除归属和已有类型只读，以及两种学院角色自动归属；修复前6失败2通过。真实浏览器及修复后回归结果见qa-runtime/ui-full-role-20261012/evidence-scope-audit-20261016。

- 本修复验证补录：修复后8项编译真实表单回归全通过，四角色原按钮可见性回归通过；qa_school实际浏览器缺学院阻止提交、personal/910001保存及修改保留、切系统后system/NULL保存均通过SQL核对。三个临时模板已备份软删除，原九条保留。其他角色本次表单实际UI回归仍待，不宣称全角色已复测。


### 2026-10-17 院管理员学院节点写控件错误展示修复

- 院管理员组织页原显示学院修改/删除和可编辑排序，打开学院修改还出现100 (unknown)上级；后端现有约束已禁止维护学院，页面却诱导无效操作。
- 根据现有角色规则：仅当院管理员且没有全局角色时，学院/历史非年级班级节点隐藏修改删除并禁用排序；新增子节点保留。全局管理员、校管理员和年级班级控件不变，后端保护不改。
- 编译真实表格插槽回归，修复前8通过2失败，修复后10项全过；院管理员真实UI确认学院仅有新增、排序禁用，本院年级班级仍可维护。其余全局角色当前源实际浏览器回归和本页其他分支继续补测，详见QA进度。

- 院管理员学院节点控件修复补录：本院年级班级真实新增/编辑/排序/保护及清理均验证；admin/school_admin已使用当前源实际登录确认学院编辑及排序保留。后端不变、原组织保留。


### 2026-10-18 统计标签图例过滤后百分比错位修复

- 实际浏览器点击标签图例后，扇区已按可见数据变化，标签/提示百分比仍取旧位置。根因是固定依赖ECharts5.4的PieSeries在getInitialData缓存seats，legend过滤后getDataParams仍按过滤索引读取旧数组；已保存稳定错误截图。
- 仅统计页标签与提示格式化按当前legend.selected和原始可见值求和计算百分比；富文本提示避免把标签名当HTML。未升级依赖、未改统计接口/数据库。
- 使用安装中的真实ECharts SVG渲染回归：隐藏一项50/25/25、隐藏两项50/50、恢复原比例，原代码2失败1通过；修复后再覆盖提示同分母与全隐藏恢复100%，5项全通过。管理员实际浏览器已复测关闭学风后50/25/25并保存前后截图；其他角色共享修复浏览器回归仍待，不能当全部完成。

- 统计图例百分比修复补录：school_admin、college_admin、talk_teacher已分别登录当前源码真实验证图例过滤/恢复、富文本提示与本角色数据范围，连同admin四角色回归完整。截图及统计实际按钮证据见20261018审计记录。


### 2026-10-18 手机号规则错误接受竖线修复

- 老师个人中心实际把1|000000000保存为合法手机号，根因为字符组[3|4|5|6|7|8|9]包含了竖线，并非数字选择。部门和用户管理也有相同规则。
- 三个前端表单改为^1[3-9]\d{9}$，保留13至19号段与11位长度要求，无后端/数据库结构修改。备份已保留。
- 读取真实组件data规则的回归：原3失败3通过，现6项全通过；老师当前源码真实浏览器再次输入含竖线被中文校验阻止。原QA老师资料已按备份清理恢复，凭据/角色/部门不变。其余角色与部门/用户管理当前规则实际回归待测，不能以6项规则测试替代。


### 2026-10-18 首页数据范围说明修正

- 全局角色学生卡片原写本学院，院管理员班级卡片原称维护学院，和实际权限范围不符。按现有isGlobalAdmin分支显示全校/本学院学生教师、院管理员只维护本院年级班级；不改变导航/权限/接口。已保留备份与校管理员前后实际页面证据。

- 手机号规则修复补录：四角色个人中心、三管理角色部门新增、admin/school_admin用户新增已分别在当前源码真实验证非法号码错误与合法数字恢复；所有草稿取消，未创建新账号部门。

### 2026-10-18 系统导出沿用旧日期修复

- 参数设置实际复现：重置后选择10月18日，不点搜索而直接导出，错误包含全部8条。根因是addDateRange仅在列表查询时把日期写入queryParams.params，导出没有重新读取当前dateRange；正常先搜索再导出并无此问题。
- 用户/角色/参数/操作日志/登录日志五入口导出均在点击时重新合并当前日期，清除日期同时清除旧条件，不改数据库和权限。五组件真实方法与共用日期函数回归原10失败、修复10通过。
- admin五入口实际选择空日期直接下载Excel均0数据行；参数清除日期实际导出8行。热更新旧实例一份仍8的文件保留，完整重新载入后实际正确。school_admin五入口回归待补，不虚报全角色完成。

- 日期导出修复补录：school_admin五入口已分别真实选择11月18–19日后直接导出，五份Excel均仅表头0行；admin与校管理员10个实际入口回归完成，非API替代。

### 2026-10-18 锁屏刷新安全提示被遮挡修复
- lock.vue固定层级9999高于Element安全提示2001，刷新锁屏时焦点被提示占用而按钮被锁屏盖住；真实DOM命中和截图缓冲证明根因。锁屏改为2000，保持盖住普通布局，提示可点击。校管理员实际提示取消、旧密码解锁/错误密码拒绝/退出登录验证；其余角色回归继续。备份qa-runtime/ui-full-role-20261012/lock-dialog-backup-20261018-2310。

- 锁屏层级修复四角色实际回归补齐：均完成空/错密码仍锁定、原密码解锁、刷新保持锁屏及退出重新登录；school/college/teacher刷新安全提示2001高于锁屏2000，按钮实际命中为true且可取消。admin无初始密码提醒，未虚报其提示测试。

### 2026-10-18 字典类型日期导出补修
- 全部可见带日期的导出源码补查发现字典类型同样仅克隆旧queryParams。真实未来空日期未搜索导出24条，修复导出时重新合并当前dateRange，扩展六页面回归原2失败/10通过→12通过；管理员新实例实测空日期0、清除日期24，校管理员回归继续。备份dict-date-export-backup-20261018-2330，未构建部署。

### 2026-10-19 字典类型批量删除失败后部分删除修复

- 实际UI只用新QA类型32(空)、31(含数据)，顺序32,31批删返回保护错误却已删32。根因是deleteDictTypeByIds逐个检查并立即删除，缺少事务。
- 改为先校验整批再删除，增加事务回滚；DictTypeBatchDeletionTest原2失败1通过→3通过。备份dict-batch-delete-backup-20261018-2350，未做部署打包。
- admin/school_admin分别真实顺序空33→保护31失败后两条保留，两个角色分别正常空批删成功；全部测试类型与两数据经UI清理，SQL零残留、原24类型保留。QA后端加载修复的新PID28872，数据库/前端不重建。数据库故障注入未执行，不承诺所有异常均已穷尽。
- 字典日期导出补录：校管理员未来日期直接导出0、实际清除后26（当时含两QA类型），与管理员0/24一起完成两角色回归，原前五页加字典共六页12项方法回归通过。

### 2026-10-19 角色树独立模式全选漏选修复
- 关闭父子联动后部门树全选只选根1/37；菜单同根因。原setCheckedNodes仅传顶层，独立模式不传播子节点。递归全部id并setCheckedKeys，清空仍传空数组；没有改后端、数据库或授权规则。
- 使用实际Element UI2.15.14组件，菜单/部门、独立/联动、全选/清空8用例红2失败6通过→绿8通过，内置角色保护及SFC编译回归通过。版本官方文档：https://github.com/ElemeFE/element/blob/v2.15.14/examples/docs/zh-CN/tree.md
- admin/school_admin实际UI两模式菜单全选38/部门37/清空0，全部树操作只草稿取消。首次热更新仍旧菜单5根逻辑，完整刷新后通过；部分DOM缓冲重赋值后丢失，未伪造快照，工具实际计数保存JSON，独立模式8份完整DOM已重跑补采。
- 备份role-tree-all-backup-20261019、红绿日志及真实UI证据在../qa-runtime/ui-full-role-20261012。QA12仍停用scope5/0菜单0用户，13/14已清理。完整全角色测试/报告未完成；不构建、不改远程、不关机。

### 2026-10-19 角色分配绕过内置账号保护修复

- 校管理员可从角色“分配用户”给内置user1追加角色，服务insertAuthUsers/deleteAuthUser/deleteAuthUsers缺少统一保护。仅用停用0菜单QA12真实复现1/12，立即清理，原1/1保留；没有增加有效敏感访问。
- 服务三个入口统一在写入前验证角色不可为内置role1、角色数据范围、全部目标用户不可为内置user1；插入添加事务。两个UI子页禁选内置用户、行点击/表头全选排除、取消授权按钮隐藏及事件保护。没有扩大权限或修改远程DB。
- RoleAuthorizationProtectionTest9项先红9失败0错误→绿9通过；check-role-auth-builtin.cjs前端6项红4失败2通过→绿6通过（首次导入占位测试夹具错误另存，不算红用例）；选中ruoyi-admin及依赖完整Maven248项0失败错误。只重启验证过的QA Java28872→29296加载编译类，连接原本地63306，Node9448/MySQL30408保留，未构建部署包。
- school_admin和admin实际浏览器均内置用户勾选禁用/行与全选仍0/提交中文拒绝，普通QA老师单条添加撤销及QA院管+老师两条添加批撤正常；全部测试关联已清理。后端三条实际拒绝接口仅辅助，不冒充按钮。QA角色12–16和关联全清理，原8角色及QA用户原1/7/8恢复。完整全角色测试及最终报告仍未完成。
- 备份role-auth-builtin-backup-20261019，红绿/完整回归日志、真实DOM/SQL/辅助接口JSON位于../qa-runtime/ui-full-role-20261012及其evidence-scope-audit-20261016。
