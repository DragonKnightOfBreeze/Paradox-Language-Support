# 测试指南

> 本文档拆自 [开发规范](../guidelines.md)，作为平台测试、集成测试与单元测试的现行活规范；微基准测试规范请见 [benchmarking.md](benchmarking.md)。

## 概述

### 原则

- **单元测试**：适用于组件、工具类、扩展方法等纯逻辑测试。通常不依赖 IntelliJ Platform API 或其他外部集成。
- **集成测试**：适用于 PSI、索引、查询器、语义匹配、语义解析、外部集成等的测试。通常依赖 IntelliJ Platform API 或外部环境。
- 专门针对 IntelliJ Platform API 的集成测试，也被称为**平台测试**。
- 如无特殊要求，统一使用 Kotlin 编写单元测试和集成测试（技术栈：JUnit4 + IntelliJ Platform Test Framework）。

### 约定

- 若测试类存在明确的目标类或目标方法，应在测试类的 KDoc 中以 `@see TestTarget` 标出。
- 用例较多或可按模块切分时，使用 `// region ... // endregion` 注释分组。
- 在必要时补充简短的行内注释，说明测试边界、细节或预期结果。
- 预期通过的测试优先使用默认/警告级别日志（`--warn`）运行，仅在失败排查时切换更详细的日志级别。

### 相关命令

- 全量测试：`./gradlew test --no-daemon --console=plain`（耗时数十分钟，不推荐日常迭代使用）
- 针对特定测试类：`./gradlew test --tests "fully.qualified.TestClass" --no-daemon --console=plain`
- 针对特定关键词：`./gradlew test --tests "*SomeKeyword*" --no-daemon --console=plain`

> 在 Agent Shell 中执行 Gradle 任务时务必附加 `--no-daemon --console=plain` 并设置合理超时，避免长驻 daemon 继承标准输出流导致假挂起。

---

## 平台测试

平台测试用于验证语法、语义、语言构造、语言功能等依赖 IntelliJ Platform API 的各方面行为。

- 测试类通常直接继承自 `BasePlatformTestCase`，并混入 `ChronicleTestScope` 接口（`class XxxTest : BasePlatformTestCase(), ChronicleTestScope`）。仅少数特定测试族使用更窄的抽象基类（如 `ParadoxComplexExpressionTest`）或继承自 `UsefulTestCase` 的其他子类。
- 简单场景优先使用 `myFixture.configureByText(fileName, text)`，更为清晰直观。
- 复杂场景或多文件场景使用 `myFixture.configureByFile(filePath)` 或 `myFixture.copyFileToProject(...)`。

---

## 测试数据约定

- 集成测试 / 平台测试的测试数据统一位于 `src/test/testData/` 目录下。
- 根据期望的文件类型，测试数据文件分别使用 `.cwt`、`.txt`、`.yml`、`.csv` 扩展名（如 `test_entries.txt`），命名推荐 `snake_case`。
- 为明确标识测试数据文件，可在文件名中包含 `.test`（如 `example.test.txt`）。
- 为显式指定游戏类型，可在文件名中包含 `.{gameTypeId}`（如 `example.stellaris.txt`）。
- 对于脚本文件、本地化文件和 CSV 文件，需在 `doSetUp()` 中调用 `markIntegrationTest()`，以允许从文件名推断文件类型和游戏类型。
- 部分功能测试会在测试数据目录下提供测试专用的 `.config/` 目录（如 `features/index/.config`、`features/inspections/.config`）。**规则文件必须放在 `.config/` 的特定子目录中**（如 `core/`、`stellaris/`），不得直接放在 `.config/` 根目录下。

---

## 测试作用域（`ChronicleTestScope`）

插件是规则驱动的，许多语义功能依赖于 CWT 规则分组与模拟的“游戏/模组上下文”。`icu.windea.pls.test.ChronicleTestScope` 提供了一组声明式作用域方法，使平台测试具备确定性：

- `findElementAtCaret()` / `findReferenceAtCaret()`：`CodeInsightTestFixture` 扩展方法，用于基于光标位置的元素与引用查询。
- `addAdditionalAllowedRoots(...)`：将额外的文件系统根目录加入 VFS 访问白名单（例如 `ParadoxModImporterTest` 访问本地游戏目录）。
- `markIntegrationTest()` / `clearIntegrationTest()`：启用或关闭仅用于集成测试的推断行为并清空注入状态。
- `markRootDirectory(relPath)`：注入游戏或模组的根目录（路径相对于 `src/test/testData`）。实际用于测试的文件不必真正物理位于该目录下。
- `markConfigDirectory(relPath)`：注入规则目录（路径相对于 `src/test/testData`）。规则文件应置于其中的 `core/` 或 `{gameTypeId}/` 子目录中，参见 `ParadoxGameType`。
- `createRootInfo(gameType, gameVersion = null)`：构建注入的根信息，可指定游戏版本以测试版本门控逻辑。
- `markFileInfo(gameType or rootInfo, path, entry = "", group = null)`：为**后续**配置（如 `myFixture.configureByFile`）的测试数据文件注入文件元信息。传入路径相对于入口目录（而非 `game/` 前缀，参见 `ParadoxGameTypeMetadata`）。
- `VirtualFile.injectFileInfo(gameType or rootInfo, path, entry = "", group = null)`：直接为**已存在**的 `VirtualFile` 注入文件元信息。
- `initConfigGroups(project, ...gameTypes)`：为指定的一组游戏类型初始化规则分组（内置 `core` 分组总是会被初始化）。

### 实践要点与排错检查

- 需要在 `doSetUp()` 中按需调用 `markRootDirectory()`、`markConfigDirectory()` 和 `initConfigGroups()`，并在测试方法中按需注入文件信息。
- **二进制夹具注入规范（IDEA 263+）**：从 `263` 基线起，通过 `copyFileToProject(...)` 复制的二进制测试夹具（如 `.dds` 图标文件）在复制过程中不再受待决的 `markFileInfo` 生效；必须改为在返回的 `VirtualFile` 上**直接链式调用 `.injectFileInfo(...)`**（参见 `Issue385Test`）。
- 注入的文件路径与实际测试文件路径无需对齐，也可直接使用 `myFixture.configureByText(fileName, text)`。
- 若规则驱动测试意外失败，优先排查是否漏掉了 `markRootDirectory(...)`（部分缓存以根目录为键）、规则文件未放入子目录，或对应的规则分组未初始化。

示例参考：`icu.windea.pls.test.chronicle.ChronicleHelloWorldTest`。

---

## 测试 DSL（`icu.windea.pls.test.dsl`）

- **`ExpectDsl.kt`**：在 `expectScope { ... }` 中提供扩展函数风格的链式断言，直接返回接收者并在需要时应用 Kotlin Contract 与类型收窄；断言消息 `message` 通常作为最后一个可选参数。
  - 示例：`expectScope { result.expectNotNull().someMethodForNonNullType() }`、`expectScope { result.expectIs<SomeType>().someMethodForThisType() }`
- **`HighlightingDsl.kt`**：在 `highlightingScope { ... }` 及 `myFixture.configureByText(fileName) { ... }`、`myFixture.createFile(fileName) { ... }` 中，通过字符串插值构建代码高亮检查文本，比直接维护原始 XML 标记更具可读性与可维护性。
  - 示例：`myFixture.configureByText("test.txt") { "${error(message)}key${errorEnd} = value" }`

示例参考：`icu.windea.pls.test.chronicle.ChronicleHelloWorldDslTest`。

---

## 专项测试族

### 解析测试（`ParsingTestCase`）

用于验证各自定义语言的词法分析与语法分析是否正确。

- 测试数据输入文件为 `<caseName>.test.<ext>`，期望的 PSI 树快照文件为 `<caseName>.<ext>`（无 `.test.` 段）。
- 示例：`icu.windea.pls.script.ParadoxScriptParsingTest`。

### 高亮测试

用于验证注解器（Annotators）与代码检查（Inspections）的结果高亮。

- 简单场景优先使用 `myFixture.configureByText(fileName) { ... }` 配合 `HighlightingDsl` 与 `myFixture.checkHighlighting(...)`。
- 复杂场景可在测试数据文件中显式标记高亮预期（如 `<warning descr="...">target</warning>`）。
- 示例：`icu.windea.pls.script.annotator.ParadoxScriptSyntaxAnnotatorTest`、`icu.windea.pls.lang.inspections.script.common.IncorrectSyntaxInspectionTest`。

### 回归测试

专门针对具体缺陷、边缘行为或 GitHub Issue 编写，以防止未来回归。

- 测试类位于 `icu.windea.pls.test.issues` 包下（如 `Issue430Test`、`Issue451Test`）。
- 测试数据位于 `src/test/testData/issues/<number>/`（并非强制一一对应，视用例形态而定）。
- 原则上，带有 `regression` 等回归性质标记的 GitHub Issue 均应补充回归测试并沉淀对应案卷。
- **回归测试类的 KDoc 规范**（使用英文编写，参考 `Issue430Test`）：
  - 指向问题的链接文本；
  - 问题概述；
  - 原因分析（段落或列表）；
  - 关键代码位置的 `fullQualifiedName` 引用。
- **测试数据目录 `README.md` 规范**（参考 `src/test/testData/issues/430/README.md`）：
  - 仅包含指向问题的链接文本与对应回归测试类的 `fullQualifiedName` 行内代码引用。

### 快照测试

`icu.windea.pls.test.chronicle` 包下托管了一组基于自包含展示树 `src/test/testData/chronicle/`（不依赖真实本地游戏目录）的快照测试：

- `ChronicleSnapshotTest`：抽象基类，提供基于 `chronicle/` 的路径计算。
- `ChronicleInspectionBasedSnapshotTest`：在 `chronicle/` 全部文件上运行插件注册的所有 `LocalInspectionTool`，断言无非预期警告或错误。
- `ChronicleAnnotatedSnapshotTest`：使用 `ParadoxScriptTextAnnotatedRenderer` / `ParadoxCsvTextAnnotatedRenderer` 渲染文件，并与 `chronicle/.annotated/**/*.annotated.*` 对拍。

### 受限测试（`ChronicleAssume`）

部分依赖 AI、网络、本地游戏环境或外部工具的测试默认处于跳过状态，通过 `icu.windea.pls.test.ChronicleAssume`（内部调用 `org.junit.Assume.assumeTrue(...)`，未启用时报告为 *skipped* 而非 *failed*）与 `icu.windea.pls.ChronicleCapabilities.Test` 读取的系统属性控制：

| `ChronicleAssume` 方法 | 系统属性 | 类别 |
| :--- | :--- | :--- |
| `includeAi()` | `chronicle.test.include.ai` | 依赖 AI 服务的测试 |
| `includeRemote()` | `chronicle.test.include.remote` | 需要访问远程网络的测试（如 `SpecialUrlServiceTest`） |
| `includeLocalEnv()` | `chronicle.test.include.local.env` | 依赖真实本地游戏/模组目录的测试（如 `ParadoxModImporterTest`） |
| `includeConfigGenerator()` | `chronicle.test.include.config.generator` | 规则生成器测试（依赖本地游戏目录） |
| `includeExternalTools()` | `chronicle.test.include.external.tools` | 外部工具测试（需要下载可执行文件） |

传入 `-Dchronicle.test.include.all=true` 可无条件启用以上全部受限测试。例如：

```bash
./gradlew test -Dchronicle.test.include.local.env=true --no-daemon --console=plain
```

---

## 基准测试

基于 JMH 的微基准测试约定、运行方式及其与 Profiling 的定位边界，参见 [benchmarking.md](benchmarking.md)。
