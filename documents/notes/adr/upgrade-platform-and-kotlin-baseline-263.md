# 升级平台基线至 IntelliJ IDEA 2026.3 与 Kotlin 2.4

> - 模块：构建 > 平台与语言基线
> - 版本：v3.1.0
> - 日期：2026-11-26
> - 状态：已实施
> - 标签：`IntelliJ 2026.3` / `Kotlin 2.4` / `JDK 25` / `Coroutines` / 兼容性清理 / API 治理

## 背景与战略意图

作为 `v3.1.0`“清场与换代”的关键环节，插件需要摆脱历史技术债务，并将开发与运行基线直接升级至最新的现代技术栈：
1. **跳过过渡版本直达现代基线**：跨越 IntelliJ 2026.2 直接对齐已正式发布的 IntelliJ Platform 2026.3 (`263`)，为后续利用现代 IDE 异步化基础设施（如异步挂起工具栏、基于协程的监听器生命周期）打通通道；
2. **语言与协程工具链演进**：将 Kotlin 编译器升级至 `2.4.21-RC`，协程库升级至 `1.11.0-intellij-1`，同步升级 `kotlinx-serialization`，享受编译器性能提升、现代代码生成优化与协程调试支持；
3. **彻底清理历史兼容包袱与过渡分支**：全面移除针对 252/253/262 等旧版平台的补丁逻辑与绕行代码（如自制 `PathEnvironmentVariableUtil`、旧版外部链接打开分支等），恢复对平台原生标准 API 的直接调用；
4. **治理平台弃用警告与修复潜在破坏性变更**：全面排查编译期与运行期的潜在隐患（包括平台私有字段重命名导致的反射破坏、API 契约收紧等），确保全量 3490+ 测试用例绿灯通过，保障生产环境稳定。

## 决策与权衡

### 1. 平台版本与编译目标升级（JDK 25）

- **最低支持平台基线**：`gradle.properties` 与 `plugin.xml` 声明升级为 `pluginSinceBuild = 263`（`since-build="263"`），不再支持 262 及更早版本；
- **JDK 工具链与字节码目标提升至 JDK 25**：
  - IntelliJ 2026.3 平台核心库本身使用 JDK 25 编译，其公开的 Kotlin 内联函数产物字节码 target 为 25；
  - 若项目继续维持 `jdkVersion=21`，Kotlin 编译器会触发 `INLINE_FROM_HIGHER_PLATFORM` 错误（无法内联来自更高平台版本的内联函数）；
  - 决策将 `gradle.properties` 中的 `jdkVersion` 明确提升至 `25`，同步更新 CI 工作流与 IDE 配置（`.idea/kotlinc.xml` 对齐 Kotlin 2.4），将全局编译目标与运行时环境完整锚定至 JDK 25。

### 2. JNA 模块插件化分发适配

在 2026.3 平台中，JetBrains 将 JNA（Java Native Access）及其平台实现从核心根类路径移出，重构成独立的内置插件 `com.intellij.modules.jna`：
- **编译期引入**：在 `build.gradle.kts` 的 IntelliJ Platform 配置中显式声明 `bundledPlugins("com.intellij.modules.jna")`；
- **运行期契约**：在 `plugin.xml` 中以可选依赖形式补充 `<depends optional="true" config-file="pls-jna.xml">com.intellij.modules.jna</depends>`，确保在仅分发核心环境的特殊 IDE 变体中也能优雅降级。

### 3. 清理历史兼容分支与过时绕行逻辑

彻底清理此前为了跨版本兼容而引入的“自制兼容层”，收敛代码复杂度：
- **`PathEnvironmentVariableUtil`**：删除 `core/execution/configurations/PathEnvironmentVariableUtil.kt`（此前为 252 版本缺失 `isOnPath` 编写的包装补丁），全仓直连官方 `com.intellij.execution.configurations.PathEnvironmentVariableUtil.isOnPath`；
- **`SpecialUrlServiceImpl`**：清理 252 遗留的 `hasXdgOpen` 绕行逻辑与条件分支，统一依托平台的 `BrowserUtil.browse` 与系统默认通道；
- **`isDevVersion` 判断**：改用官方推荐的 `PluginDetailsService` 规范化接口判定开发版本。

### 4. 修复运行时破坏与反射契约变更

在 2026.3 中，部分基础组件的内部实现发生结构性重构，进行了针对性修复：
- **`HierarchyBrowserBaseEx` 反射字段破坏**：
  - 263 平台中，`HierarchyBrowserBaseEx#hierarchyBase` 私有字段已被重构为 `mySmartPsiElementPointer`，继续反射该字段会导致类调用层次分析在运行时抛出 `NoSuchFieldError`；
  - 决策：改为反射调用其保留的 `protected getHierarchyBase()` 方法，既规避了底层字段名的硬编码依赖，又保证了层次视图导航的稳定。
- **语法高亮器参数可空性收紧**：
  - `SyntaxHighlighter#getTokenHighlights` 的参数签名契约在 263 中严格标注为 `@NotNull IElementType`；
  - 决策：同步修正 `ParadoxScriptSyntaxHighlighter`、`ParadoxLocalisationSyntaxHighlighter`、`ParadoxCsvSyntaxHighlighter` 与 `CwtSyntaxHighlighter` 的重写方法签名，消除参数类型不匹配隐患。
- **折叠选区行为与测试光标调整**：
  - 263 的 `CodeFoldingManager` 在执行 `shouldExpandNewRegion(EXCEPT_CARET_REGION)` 时，会强制展开光标所处的折叠区域；
  - 导致原先光标默认停留在 offset 0（落在首行注释折叠区内）的 3 个 `_expand_false` 语法折叠测试用例失败；
  - 决策：在测试数据末尾显式添加 `<caret>` 将光标置于折叠区外，保证无交互状态下的纯折叠状态断言准确。
- **`Issue385Test` 虚拟文件信息映射**：
  - 263 中 `copyFileToProject` 对二进制文件不会在复制阶段触发 `getFileInfo`，导致测试中复制的 `.dds` 文件未能及时挂载 `fileInfo`；
  - 决策：在复制完成后显式对返回的 `VirtualFile` 调用 `injectFileInfo`，确保测试契约严谨。

### 5. 迁移现代 IntelliJ 263 API 与弃用治理

系统性治理 40 余处弃用警告与老旧 API：
- **读锁阻塞式执行**：全仓 `runReadAction` 迁移为现代无死锁风险的 `runReadActionBlocking`；
- **Daemon 重启**：`DaemonCodeAnalyzer.getInstance(project).restart()` 迁移为带有重分析原因追踪的 `restart("...")` 重载；
- **浮动工具栏提供者**：`FloatingToolbarProvider` 弃用同步阻塞式判定，迁移为基于协程的异步挂起函数 `isApplicableAsync`；
- **UTF-8 BOM 配置提供者**：弃用已废弃的旧重载，改写为返回 `ThreeState` 的现代 `getBOMDecisionForNewUtf8File` 规范接口；
- **扩展点监听器生命周期**：`addExtensionPointListener` 补充带 `CoroutineScope` 的重载，绑定现代组件生命周期；
- **缓存弱引用键映射**：使用 Caffeine 原生 `weakKeys()` 替换老旧平台缓存容器，防范内存泄露；
- **Kotlin 2.4 编译器清理**：清理全仓 12 处已废弃的 `@BuilderInference` 注解。

### 6. 核心内部 API 保留与 Verifier 治理策略

- **保留 `icu.windea.pls.inject` 底层注入**：
  - 插件用于动态拦截与补丁扩展的代码注入框架直接依赖平台内部类，目前 JetBrains 官方尚未开放等效扩展点；
  - 决策保留该子系统，统一使用 `@Suppress("JetBrainsInternalApiUsage")` 收敛警告，并在 Plugin Verifier 验证策略中对 `INTERNAL_API_USAGES` 维持明确豁免；
- **Plugin Verifier 本地与 CI 分工**：
  - 确认本地执行 `./gradlew runPluginVerifier` 时，易因直连 JetBrains API 清单服务（AWS `3.169.121.43:443`）发生网络超时与 Socket 挂起；
  - 在全量单元测试与打包检验全部绿灯的前提下，遵照项目既定实践，将 Plugin Verifier 的全面兼容性扫描交由 GitHub Actions CI 承接。

备注：
- 必须改为使用 `@Suppress("JetBrainsInternalApiUsage")` 抑制，以往的 `@Suppress("UnstableApiUsage")` 不再有效。
- 文档注释中对内部 API 的引用也会被包含在内。

## 实施与验证结果

1. **编译构建**：`./gradlew compileKotlin compileTestKotlin` 零报错、零未受控警告，成功完成针对 JDK 25 字节码的编译；
2. **全量单元测试**：`./gradlew test` 全部 380 个测试套件、3490 个测试用例 100% 通过（`0 failures, 0 errors, 69 skipped`）；
3. **插件分发包打包**：`./gradlew buildPlugin` 成功产出 `Paradox-Chronicle-3.1.0-dev.jar` (14.56 MB) 与 `Paradox-Chronicle-3.1.0-dev.zip` (37.16 MB)，解包校验验证 `plugin.xml` 声明的 `since-build="263"`、8327 个 entry、1269 个 CWT 规则文件与相关 JNA 插件依赖均完整合规。
