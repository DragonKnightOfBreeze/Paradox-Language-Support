# 测试覆盖度报告：`icu.windea.pls.core`

> - 版本：v3.0.1
> - 日期：2026-07-27
> - 模型：DeepSeek V4 Pro

---

## 一、报告概述

### 1.1 报告目的

本报告对 `icu.windea.pls.core` 包下的所有生产代码（共 105 个 Kotlin 源文件，分布在 20 个子包中）进行逐文件、逐子包的测试覆盖度分析。报告旨在：

- 评估当前测试的完整性，识别已覆盖、部分覆盖和完全缺失的区域。
- 评估各模块的“可测试性”，区分可简单通过单元测试覆盖的代码与需要 IntelliJ 平台集成测试（或根本不适合测试）的代码。
- 为后续测试补充工作提供优先级参考和行动指引。

### 1.2 总体统计

| 指标 | 数值 |
|---|---|
| 生产代码文件总数 | 105 |
| 已有测试文件总数 | 42 |
| 测试文件覆盖的生产文件比例 | ~40%（42/105） |
| 根级扩展文件有专用测试 | 6/14 |
| 子包有测试 | 10/20 |
| 完全无测试的子包 | 10/20 |

### 1.3 测试类型分布

| 测试类型 | 文件数 | 说明 |
|---|---|---|
| 纯单元测试（JUnit4，无平台依赖） | 38 | 大多数已有测试属于此类 |
| 平台测试 / 集成测试 | 4 | 如 `PsiServiceTest`、`CommandLineExecutorTest` |
| 基准测试（受 `ChronicleAssume` 控制） | 3 | `MatchersBenchmarkTest` 及两个 Similarity 基准测试 |

---

## 二、逐子包覆盖度分析

### 2.1 根级扩展文件（14 个生产文件，6 个测试文件）

#### 覆盖良好的文件

| 生产文件 | 对应测试 | 测试数 | 说明 |
|---|---|---|---|
| `ComparisonExtensions.kt` | `ComparisonExtensionsTest.kt` | 3 | 所有 3 个函数均被覆盖，含 null 处理和排序语义 |
| `StdlibFastExtensions.kt` | `StdlibFastExtensionsTest.kt` | 3 | `trimFast`、`splitFast` 均全面覆盖，含 ignoreCase 和 limit 参数 |

#### 部分覆盖的文件

| 生产文件 | 测试文件 | 测试数 | 已覆盖 | 总 API 数 | 主要缺口 |
|---|---|---|---|---|---|
| `StdlibExtensions.kt` | `StdlibExtensionsTest.kt` | 28 | ~22 | ~80+ | `letIf`/`letUnless`、`surroundsWith`/`removeSurrounding` 系列（12 个函数）、`escapeXml`、`Boolean.toByte/Byte.toBoolean`、`cast/castOrNull`、所有 URL/Path 转换函数 |
| `PlatformExtensions.kt` | `PlatformExtensionsTest.kt` | 4 | 4 | ~50+ | RWA（`runSmartReadAction` 8 个重载）、PSI 操作（~30+个函数）、VFS 转换、AST 遍历、代码洞察、命令执行 |
| `ReflectionExtensions.kt` | `ReflectionExtensionsTest.kt` | 3 | 3 | 6 | `isClassPresent`、`toClass`、`toKClass` 未测试 |
| `RecursionExtensions.kt` | `RecursionExtensionsTest.kt` | 1 | 1 | 4 | `runWithRecursionGuard`、`withContextRecursionGuard`、`runWithContextRecursionGuard` 未测试；防重入机制未经测试 |
| `MatchExtensions.kt` | 无专用测试 | 0 | 2（碎片化） | 5 | 仅在 `StdlibExtensionsTest` 中碎片化覆盖了 `matchesAntPattern` 和 `matchesRegex`，其余函数及 ignoreCase/trimSeparator/delimiter 等参数均未测试 |

#### 完全缺失测试的文件

| 生产文件 | 行数 | API 数 | 可测试性 | 说明                                                                                                                                                                |
|---|---|---|---|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AccessorExtensions.kt` | 71 | 12 | **可单元测试** | 底层 `AccessorBuilder` 已有测试，但此处的 12 个扩展包装器均未直接测试                                                                                               |
| `AddonExtensions.kt` | 34 | 5 | **可单元测试** | `runOnce`、`withErrorRef` 从不被测试；`loadText`/`withState` 仅在其他测试中作为装置使用                                                                             |
| `ConcurrentExtensions.kt` | 50 | 2 | **可单元测试** | `withDoubleLock`（同步版和协程版）完全未测试                                                                                                                        |
| `ExecutionExtensions.kt` | 32 | 2 | **需集成测试** | 底层 `CommandLineExecutor` 已有测试，但此处的扩展包装器未测试                                                                                                       |
| `IndexExtensions.kt` | 108 | 11 | **可单元测试** | `readOrReadFrom`/`writeOrWriteFrom` 增量序列化逻辑、`writeIndexedStringList`/`readIndexedStringList` 压缩序列化逻辑完全未测试                                     |
| `OptimizerExtensions.kt` | 51 | 10 | **可单元测试** | 底层 `Optimizer` 已有充分测试，但此处的 10 个扩展函数均未直接测试                                                                                                   |
| `UiExtensions.kt` | 185 | ~20 | **部分可测试** | UI 组件扩展（`resize`、`toIcon`、`registerSingleClickListener`）需 UI 上下文，但纯逻辑函数（`Color.component*`、`toAtomicProperty`、`toMutableProperty`）可单元测试 |

---

### 2.2 `accessor/`（11 个生产文件，2 个测试文件）

**覆盖度：PARTIAL (~30%)**

| 已测试 | 未测试                                                                                            |
|---|---------------------------------------------------------------------------------------------------|
| `AccessorBuilder.property()` 读 | 所有写操作（`set()`）                                                                             |
| `AccessorBuilder.memberProperty()` 读 | `staticProperty(String, String)` 重载                                                             |
| `AccessorBuilder.staticProperty()` 读 | Kotlin 属性代理（`getValue/setValue`）                                                            |
| `AccessorBuilder.function()` 调用 | `AccessorProviderImpl` 直接测试                                                                   |
| `AccessorBuilder.memberFunction()` 调用 | 查找写访问器和调用访问器路径                                                                      |
| `AccessorBuilder.staticFunction()` 调用 | Java 反射回退路径                                                                                 |
| | 错误路径（`ClassCastException`、`UnsupportedAccessorException`、`ProcessCanceledException` 传播） |
| | `AccessorContext` 异常包装逻辑                                                                                |
| | `AccessorDelegateBuilder` 工厂方法                                                                |

**测试类型：** 纯单元测试，无 IntelliJ 平台依赖。

**可测试性评估：** 大部分未覆盖的功能均可作为纯单元测试实现。建议至少补测写操作和错误路径。

---

### 2.3 `cache/`（7 个生产文件，0 个测试文件）

**覆盖度：MISSING (0%)**

| 生产文件 | 可测试性 | 说明 |
|---|---|---|
| `Caches.kt` | 单元测试 | `CacheBuilder` 扩展函数，可用 mock Caffeine 缓存测试 |
| `CancelableCache.kt` | 单元测试 | `get/getIfPresent` 的 `ProcessCanceledException` 处理可用 mock 缓存测试 |
| `CancelableLoadingCache.kt` | 单元测试 | 同上 + `get(key)`/`getAll(keys)` |
| `NestedCache.kt` | 单元测试 | 基于 `ConcurrentHashMap` 的二层缓存，纯逻辑，可单元测试 |
| `NestedLoadingCache.kt` | 单元测试 | 同上 + `LoadingCache` |
| `TrackingCache.kt` | 需平台测试 | 依赖 `com.intellij.openapi.util.ModificationTracker` |
| `TrackingLoadingCache.kt` | 需平台测试 | 同上 |

**说明：** `CancelableCache`、`NestedCache`、`NestedLoadingCache` 为薄包装器，非常适合作为高优先级单元测试目标。`TrackingCache`/`TrackingLoadingCache` 需要 mock `ModificationTracker` 或使用平台集成测试。

---

### 2.4 `codeInsight/`（8 个生产文件，0 个测试文件）

**覆盖度：MISSING (0%)**

| 生产文件 | 可测试性 | 说明 |
|---|---|---|
| `TemplateEditingFinishedListener.kt` | 不可测试 | fun interface，纯平台适配 |
| `LimitedCompletionProcessor.kt` | **可单元测试** | 纯逻辑类，限制处理数量不超过 IDE 设置的 completion limit |
| `NavigationGutterIconBuilderFacade.kt` | 不可测试 | 薄平台 API 包装 |
| `InlayPresentationExtensions.kt` | **可单元测试** | `mergePresentations()` 为纯工具方法 |
| `ContainerBasedMover.kt` | 需平台集成测试 | 202 行复杂抽象类，耦合 IntelliJ Editor/PSI/LineMover |
| `CompletionContext.kt` | 不可测试 | 接口，数据载体 |
| `GlobalCompletionContext.kt` | 不可测试 | 数据类，数据载体 |
| `GlobalBasedCompletionContext.kt` | 不可测试 | 抽象类，数据载体 |

**说明：** 仅 `LimitedCompletionProcessor` 和 `InlayPresentationExtensions.mergePresentations()` 有实际的单元测试价值。其余为胶水代码，测试 ROI 极低。

---

### 2.5 `collections/`（7 个生产文件，5 个测试文件）

**覆盖度：PARTIAL (~60%)**

| 已充分测试 | 部分测试 | 完全未测试 |
|---|---|---|
| `ArrayExtensions.kt`（`orNull`、`mapToArray`） | `CollectionExtensions.kt`（缺 `associateByNotNull`、`findLastIsInstance`、`processValue`、`getOne`/`getAll`） | `CollectionFastExtensions.kt`（全部 10 个函数，0 测试） |
| `SequenceExtensions.kt` | `CollectionBuilders.kt`（缺 `ImmutableList`/`ImmutableSet`、`MutableStringSet`/`MutableStringKeyMap`、`CaseInsensitiveStringSet`/`CaseInsensitiveStringKeyMap`、`CaseInsensitiveStringHashingStrategy`） | `WalkingSequence.kt`（整个文件，含 `WalkingSequence`、`WalkingContext`、`transform`、`context`、`forward`，0 测试） |
| | `SequenceBuilders.kt`（缺 `generateSequenceFromSeeds`） | |

**测试类型：** 大部分为纯单元测试。`SequenceBuildersTest` 依赖 `com.intellij.util.containers.TreeTraversal`（IntelliJ 平台类）。

**可测试性评估：** 所有缺失测试均可作为纯单元测试补充。`CollectionFastExtensions.kt` 和 `WalkingSequence.kt` 为 **最高优先级**，两者在运行时被广泛依赖但没有任何测试保护。

---

### 2.6 `coroutines/`（1 个生产文件，1 个测试文件）

**覆盖度：GOOD (~85%)**

| 已测试 | 未测试（边缘用例） |
|---|---|
| `Flow.chunked()` 基本分块 + 尾部不足 | `chunkSize=1`、`chunkSize` 恰好等于总数、空上游流 |
| `Flow.chunked()` 非法 `chunkSize=0` 抛出异常 | |
| `Flow.toLineFlow()` 多块 + 嵌入换行符 | 空输入、仅换行符输入 |
| `Flow.toLineFlow()` 尾部无换行符 | |

**测试类型：** 纯单元测试（JUnit4 + `runBlocking`）。

**说明：** 两条函数的核心路径和一条错误路径已测试。缺失的仅为边缘情况，优先级低。

---

### 2.7 `data/`（3 个生产文件，2 个测试文件）

**覆盖度：LOW (~25%)**

| 生产文件 | 测试状态 | 缺口 |
|---|---|---|
| `MarkdownService.kt` | 部分（1 个 smoke 测试） | 仅用一段 Markdown 测试 `toHtml()`。无空字符串、特殊字符、畸形 Markdown 测试 |
| `JsonService.kt` | 部分（1 个 smoke 测试） | 仅 `jsonMapper` 测试了简单 POJO 往返。**`json5Mapper` 完全未测试**（无 JSON5 注释、无引号字段名、尾部逗号等特性） |
| `JsonModuleWithType.kt` | **完全未测试** | 抽象类，无任何测试引用 |

**测试类型：** 纯单元测试，无 IntelliJ 平台依赖。

**可测试性评估：** 三个文件的测试价值都很高，尤其是 `json5Mapper`（JSON5 是 CWT 配置格式的基础）和 `JsonModuleWithType`。

---

### 2.8 `editor/`（1 个生产文件，0 个测试文件）

**覆盖度：NONE (0%)**

`EditorService.kt` — `selectElement` 和 `selectElements` 方法，依赖 `Editor`、`PsiElement`、`CaretModel`。需要 IntelliJ 平台集成测试（含 editor fixture + PSI 文件 + 光标操作）。

**可测试性：** 需要平台集成测试，ROI 中等。胶水代码主导，逻辑简单。

---

### 2.9 `execution/`（5 个生产文件，1 个测试文件）

**覆盖度：VERY LOW (~10%)**

| 生产文件 | 测试状态 |
|---|---|
| `CommandLineExecutor.kt` | 部分（2 个测试，仅 Windows + CMD/PowerShell + UTF-8 输出） |
| `CommandLineService.kt` | 仅间接使用，无直接测试 |
| `CommandType.kt` | 枚举，间接使用 |
| `CommandLineExecutionException.kt` | 完全未测试 |
| `PathEnvironmentVariableService.kt` | 完全未测试 |

**测试类型：** 现有测试为集成测试（需真实 OS 进程）。`PathEnvironmentVariableUtilC` 需 IntelliJ Platform（`PathEnvironmentVariableUtil`）。

**可测试性：** 未测试的方法包括 `execute(commands: List<String>)` 变体、`AUTO`/`SHELL`/`NONE` 类型、非零退出码处理、超时行为、`environment`/`workDirectory` 属性、`getOutputCharset` 等。这些需要集成测试框架或系统级 mock。

---

### 2.10 `inspections/`（1 个生产文件，0 个测试文件）

**覆盖度：NONE (0%)**

`InspectionService.kt` — 所有方法（`getToolState`、`getTool`、`isEnabled`、`getEnabledTool`、`getWeakerHighlightType`）均依赖 `Project`、`InspectionProfileManager`、`LocalInspectionTool` 等 IntelliJ 平台 API。

**可测试性：** 需要 IntelliJ 平台集成测试（含工程 + inspection profile 设置）。薄平台包装器，测试 ROI 较低。

---

### 2.11 `io/`（1 个生产文件，1 个测试文件）

**覆盖度：HIGH (~90%)**

| 方法 | 已测试场景 | 未测试 |
|---|---|---|
| `ensureDirectoryCreated` | 不存在时创建、已作为文件存在时抛异常 | **已作为目录存在时的短路返回路径**（幂等情况） |
| `ensureFileCreatedFromClasspath` | 不存在时创建、已作为目录存在时抛异常、空文件覆盖、非空文件保留 | 所有代码路径均已覆盖 |

**测试类型：** 纯单元测试（使用真实文件系统 I/O，`@Before`/`@After` 清理）。

---

### 2.12 `match/`（14 个生产文件，12 个测试文件）

**覆盖度：HIGH (~80%)**

#### 覆盖良好的文件

| 生产文件 | 说明 |
|---|---|
| `TextMatcher.kt` | 24 个测试用例，所有 5 个公开方法均覆盖。仅 `matchesIntPercentageField` 缺少独立测试 |
| `PathMatcher.kt` | 6 个测试，所有 3 个重载均覆盖良好 |
| `GlobMatcher.kt` | 30+ 断言，覆盖所有通配符组合 |
| `AntMatcher.kt` | 50+ 断言，覆盖 `**`、`*`、`?`、`trimSeparator`、`ignoreCase` 及边缘情况 |
| `SimilarityMatchService.kt` | 11 个功能测试 + 额外场景测试 + 基准测试，全面覆盖 |
| `SimilarityMatchOptions.kt` 及 `SimilarityMatchResult.kt` | 通过服务测试间接充分覆盖 |
| `SimilarityMatchers.kt`（3 个实现） | 13 个测试，全面覆盖 `Prefix`、`Snippet`、`Typo` |

#### 覆盖不足的文件

| 生产文件 | 缺失 |
|---|---|
| `RegexMatcher.kt` | 仅 2 个基本断言，无 `ignoreCase`、无边缘情况 |
| `MatchService.kt` | `matchesPatterns`/`matchesAntPatterns` 已测试，但 `matchesPattern`、`matchesAntPattern`、`matchesRegex` 无直接测试（虽委托给已测试的匹配器） |
| `KeywordMatcher.kt` | `matches(Array)` 重载和 null 输入情况未测试 |
| `AntFromRegexMatcher.kt` | 仅基准测试（已标记为 deprecated） |

**测试类型：** 所有测试均为纯单元测试（无 IntelliJ 平台依赖）。基准测试受 `ChronicleAssume.includeBenchmark()` 控制。

---

### 2.13 `math/`（9 个生产文件，4 个测试文件）

**覆盖度：MEDIUM-HIGH (~65%)**

| 生产文件 | 测试状态 | 缺口 |
|---|---|---|
| `MathExtensions.kt` | **已完善** | 36 个断言覆盖 `precision=0`、`>0`、`<0` 两种浮点模式 |
| `TextMathExpressionScanner.kt` | **较完善** | 8 个测试覆盖有效扫描和错误情况。缺 `precision`/`isFloatingPoint` 传播、`\|`（绝对值）切换行为 |
| `TokenBasedMathExpressionEvaluator.kt` | 部分 | `evaluate()` 已测试。`evaluateOrNull()` **未测试**；`precision`/`isFloatingPoint` 构造参数未测试；5 条错误抛出路径大多未测试 |
| `TextBasedMathExpressionEvaluator.kt` | 部分 | `evaluate()` 已测试。`evaluateOrNull()` **未测试**；`precision`/`isFloatingPoint` 未测试；空字符串边缘情况未测试 |
| `MathResult.kt` | 部分 | 工厂方法通过 scanner/evaluator 测试间接覆盖。但 `isFloatingPointValue()`、`normalized()`、`formatted()` 方法本身 **未直接测试** |
| 接口和密封类（`MathExpression`、`MathToken`、`MathOperator`、`MathExpressionEvaluator`） | 间接 | 通过 evaluator 使用间接测试 |

**测试类型：** 纯单元测试。`TokenBasedMathExpressionEvaluator` 引用了 `ProgressManager.checkCanceled()`（IntelliJ API），但测试中未触发取消路径。

---

### 2.14 `navigation/`（1 个生产文件，0 个测试文件）

**覆盖度：NONE (0%)**

`NavigationElement.kt` — 继承 `FakePsiElement`，将 `name`/`presentableText`/`locationString`/`icon` 委托给父级 `NavigatablePsiElement`。需要 `PsiManager`、`Presentation` 和 `javax.swing.Icon`。

**可测试性：** 需要 IntelliJ 平台集成测试。纯转发胶水代码，测试 ROI 极低。

---

### 2.15 `optimizer/`（4 个生产文件，1 个测试文件）

**覆盖度：HIGH (~90%)**

| 已测试 | 未测试 |
|---|---|
| `StringOptimizer`（interning 等价性、幂等性、空字符串短路） | `deoptimize()` 默认抛出 `UnsupportedOperationException`（价值极低） |
| `ListOptimizer`（4 个场景） | `getTyped` 的 reified cast 行为 |
| `SetOptimizer`（3 个场景） | |
| `MapOptimizer`（3 个场景） | |
| 小字符串列表/集合的 interning（size≤8） | |
| `ReadWriteAccessOptimizer`（往返值 + 未知字节回退） | |

**测试类型：** 纯单元测试，无 IntelliJ 平台依赖。

---

### 2.16 `options/`（1 个生产文件，0 个测试文件）

**覆盖度：NONE (0%)**

`OptionsService.kt` — 包装 `ShowSettingsUtil` 和 `Settings` API，共 7 个公开方法。

**可测试性：** **不适合测试。** 纯 IDE 平台胶水代码，所有方法依赖 `ShowSettingsUtil.getInstance()`、`DataManager.getInstance()`、`Settings.KEY.getData()`，以及通过反射调用内部 API `PluginManagerConfigurable.openMarketplaceTab`。无实际业务逻辑。测试需启动完整 IDE 实例，ROI 极低。

---

### 2.17 `psi/`（7 个生产文件，1 个测试文件）

**覆盖度：LOW (~10-25%)**

#### `PsiService.kt` — 12 个方法，约 7 个已测试

| 已测试方法（29 个测试） | 未测试方法（5 个） |
|---|---|
| `getAttachedComments`（6 个用例） | `toPresentableString` |
| `getAttachingElement`（5 个用例） | `collectBetween` |
| `findSiblingComments`（8 个用例） | `collectBetweenBounds` |
| `findAllSiblingCommentsIn`（5 个用例） | `isBeforeLeftBound`/`isBeforeLeftBoundEnd` |
| `getOwnedComments`（5 个用例） | `containsLineBreak`/`containsBlankLine` |
| `getLineCommentText`（4 个用例） | `findAcceptableElementInStructureView` |
| `getDocCommentText`（4 个用例） | `findTextStartOffsetInView`/`findTextEndOffsetInView` |
| | `getReferenceElement` |
| | `getSpaceExtendedTextRange` |

#### 其他 6 个生产文件

| 文件 | 测试状态 | 可测试性 |
|---|---|---|
| `PsiFileService.kt` | 完全未测试 | **可平台测试**（需 `PsiFile` fixtures） |
| `FilteredRequestResultProcessor.kt` | 完全未测试 | **可平台测试**（需引用搜索基础设施） |
| `LightElementBase.kt` | 完全未测试 | 抽象胶水类，ROI 极低 |
| `PsiBoundElement.kt` | N/A | 纯接口，无逻辑可测 |
| `PsiCompositeReference.kt` | N/A | 纯接口，无逻辑可测 |
| `PsiReadWriteAccessAwareElement.kt` | N/A | 纯接口，无逻辑可测 |

**测试类型：** 需 IntelliJ 平台集成测试（`BasePlatformTestCase` + CWT PSI + `ChronicleTestScope` + 标记集成测试）。

**可测试性评估：** `PsiService` 和 `PsiFileService` 的大部分未测试方法均可通过平台测试覆盖，建议优先补充。`FilteredRequestResultProcessor` 的测试难度较高（需引用搜索基础设施）。

---

### 2.18 `text/`（4 个生产文件，0 个测试文件）

**覆盖度：NONE (0%) — 最大遗漏点**

| 生产文件 | 可测试性 | 说明 |
|---|---|---|
| `TextPatterns.kt` | **高优先级可测试** | 非平凡解析/构建逻辑。`TextPattern` 密封接口 + 4 个子类型、`TextPatternResult` + 3 个子类型、`TextPatternBasedProvider`、`TextPatternBasedBuilder`。纯字符串操作，零平台依赖。 |
| `HtmlBuilder.kt` | **可测试** | 纯字符串构建器，`append`/`link`/`image` 产出确定性 HTML。零平台依赖。 |
| `DocumentationBuilder.kt` | **可测试** | 类似 HtmlBuilder，使用 `DocumentationMarkup` 常量生成确定性字符串。轻度依赖 `toFileUrl()` 但可互换。 |
| `EscapeType.kt` | 低价值 | 纯枚举，3 个值。不建议单独测试。 |

**可测试性评估：** 整个 `text/` 子包是 core 包中**最大的可测试漏洞**。所有 4 个文件均为纯逻辑、零 IntelliJ 平台依赖，且 `TextPatterns.kt` 包含非平凡的匹配/构建算法。强烈建议优先为 `TextPatterns.kt` 编写单元测试。

---

### 2.19 `ui/`（4 个生产文件，0 个测试文件）

**覆盖度：NONE (0%)**

| 生产文件 | 可测试性 | 说明 |
|---|---|---|
| `UiService.kt` | **不可测试** | 一行代码返回静态 AWT `Cursor`，纯胶水 |
| `threeStateCheckBox.kt` | **不适合测试** | 深度耦合 IntelliJ UI DSL（`Cell<JBCheckBox>`、`ThreeStateCheckBox.State`），需运行 UI 面板 |
| `textFieldWithHistoryWithBrowseButton.kt` | **不适合测试** | 依赖 `Row`、`Cell`、`TextFieldWithHistoryWithBrowseButton`、`DialogValidation`、`FileChooserDescriptor`、project，需完整 IDE/Swing 上下文 |
| `EntryListTableModel.kt` | **不适合测试** | 继承 `ListTableModel`，创建 `TableView`、`ToolbarDecorator`、`TableViewSpeedSearch`，需 UI 上下文 |

**可测试性评估：** 所有文件均为 UI 胶水代码，深度耦合 IntelliJ 平台。不适合单元测试。可能通过 UI 级的集成测试间接覆盖（但对 IntelliJ 插件而言很少实用）。

---

### 2.20 `util/` 及其子包（18 个生产文件，6 个测试文件）

**覆盖度：~40-45%**

#### 已有测试的文件（6 个）

| 生产文件 | 测试文件 | 测试数 | 覆盖评估 |
|---|---|---|---|
| `Keys.kt` | `KeysTest.kt` | ~190 行 | **良好** — `createKey`、`Key.clear`/`Key.copy`、`KeyRegistry`、`registerKey` 系列、`provideDelegate`、缓存/幂等性 |
| `KeyAccessors.kt` | `KeyAccessorsTest.kt` | ~347 行 | **良好** — 所有 `UserDataHolder.getOrPutUserData` 重载（含 `EMPTY_OBJECT` 缓存）、属性代理读写、`ProcessingContext.getOrPut` 全部重载 |
| `OptionProvider.kt` | `OptionProviderTest.kt` | ~80 行 | **良好** — 值/默认值/环境变量/默认环境变量优先级链、`fromEnv()` 重载 |
| `RangeInfos.kt` | `RangeInfoTest.kt` | ~260 行 | **良好** — `IntRangeInfo.from`（8 种解析用例）、`IntRangeInfo.contains`（8 种边界）、`FloatRangeInfo`（4 种解析 + 5 种边界） |
| `ObservableProperty.kt` / `ObservableMutableProperty.kt` / `ObservableExtensions.kt`（部分） | `ObservableTest.kt` | ~34 行 | **部分** — `ObservableProperty` 属性代理、`fromDelimitedString` 往返。但 `observeMutable` 及 nullable source 的 `fromDelimitedString`/`toDelimitedString` 未全面覆盖 |

#### 完全未测试的文件（10 个）— 全部可单元测试

| 生产文件 | 说明 | 优先级 |
|---|---|---|
| `Processors.kt` | `FindProcessor`、`DuplicateProcessor`、`CollectProcessor` 纯 Kotlin 类，实现 `Processor<T>`。**逻辑非平凡，强烈推荐。** | **高** |
| `Markers.kt` | `ToggleMarker`、`OnceMarker` 带 `@Volatile` 字段的纯状态机。 | **高** |
| `RecursionGuard.kt` | `ArrayDeque` 递归检测 + `StackOverflowPreventedException` 抛出。 | **高** |
| `RecursionService.kt` | 委托 `RecursionGuardContext`。`withContextRecursionGuard` 变体需 `UserDataHolder`。 | **高** |
| `RecursionGuardContext.kt` | `ThreadLocal` 缓存管理。上下文变体依赖 `UserDataHolder`，但 thread-local 路径可独立测试。 | **高** |
| `CallbackLock.kt` | `MutableSet<String>` 去重，2 个方法。简单但被多处依赖。 | **中** |
| `LazyValue.kt` | DCL 初始化 + `check()` + `clear()`。线程安全测试需要并发框架。 | **中** |
| `ReversibleValue.kt` | 带 `from()` 解析、`reversed()`、`withOperator()` 的数据类。 | **中** |
| `SoftValue.kt` | `SoftReference` + `dereference()` + 工厂方法。GC 相关路径不可靠但逻辑路径可测。 | **低** |
| `FallbackValue.kt` | inline value class + 字符串回退扩展。逻辑过于简单，ROI 低。 | **低** |
| `ConvertibleValue.kt` | inline value class + 集合转换扩展。逻辑过于简单，ROI 低。 | **低** |
| `ModificationTrackers.kt` | `PatternsBasedModificationTracker`、`MergedModificationTracker`、`ComputedModificationTracker`。需 `SimpleModificationTracker`（IntelliJ 平台类）。 | **中** |
| `Tuples.kt` | typealias + `Tuple4` data class + `tupleOf()` 工厂。低风险，低价值。 | **低** |
| `Entry.kt` | data class + `Map`/`List` 平凡扩展。低风险，低价值。 | **低** |

**测试类型：** 大部分为纯单元测试。`KeysTest`、`KeyAccessorsTest`、`ObservableTest` 依赖 `UserDataHolderBase`、`Key`、`ProcessingContext`（轻量级 IntelliJ 数据结构，不属于平台测试），但无 `Project` 或 IDE 生命周期依赖。

---

### 2.21 `vfs/`（2 个生产文件，0 个测试文件）

**覆盖度：NONE (0%)**

| 生产文件 | 可测试性 | 说明 |
|---|---|---|
| `VirtualFileService.kt` | **不适合测试** | 所有方法依赖 `VirtualFile` 子类型（`LightVirtualFileBase`、`VirtualFileWindow`、`StubVirtualFile`）、`AnActionEvent`、`LangDataKeys`、`VfsUtil`、文件系统。需完整 IDE + VFS 上下文。 |
| `VirtualFileBomService.kt` | **不适合测试** | 依赖 `VirtualFile.bom` 属性、`WriteAction`、`file.setBinaryContent()`。需真实 VFS 文件 + 字节级操作。 |

**可测试性评估：** 薄平台包装器。可考虑将 BOM 操作逻辑（UTF-8 BOM 的纯字节数组操作）重构提取出纯函数进行单元测试。`VirtualFileService` 不适合测试。

---

### 2.22 `annotations/`（4 个生产文件，0 个测试文件）

**覆盖度：N/A — 无需测试**

| 文件 | 注解 | Retention |
|---|---|---|
| `Optimized.kt` | `@Optimized` | SOURCE |
| `Inferred.kt` | `@Inferred` | SOURCE |
| `Fast.kt` | `@Fast` | SOURCE |
| `CaseInsensitive.kt` | `@CaseInsensitive` | SOURCE |

**说明：** 纯标记注解，`RetentionPolicy.SOURCE` 级别。无行为、无方法、无逻辑。仅用于文档/自描述目的。测试注解类无意义。

---

## 三、综合评估与建议

### 3.1 总体覆盖度评估

| 覆盖等级 | 子包数量 | 子包 |
|---|---|---|
| **高（>80%）** | 4 | `coroutines/`、`io/`、`match/`、`optimizer/` |
| **中（40-80%）** | 4 | `collections/`、`math/`、`accessor/`、`psi/`（仅评论方法） |
| **低（1-39%）** | 3 | `data/`、`execution/`、根级扩展文件（平均） |
| **零（0%）** | 6 | `cache/`、`codeInsight/`、`editor/`、`inspections/`、`text/`、`ui/`、`vfs/`、`options/`、`navigation/` |
| **不适用** | 1 | `annotations/`（无需测试） |

### 3.2 按可测试性分类

| 分类 | 生产文件数 | 已测试 | 建议 |
|---|---|---|---|
| **可单元测试但无任何测试** | ~35 | 0 | **最高优先级**。尤以 `text/`（4 文件）、`util/`（10 文件）、`cache/`（5 文件）最急切 |
| **可单元测试，部分覆盖** | ~40 | ~42 测试文件 | 中优先级。补充边缘情况和遗漏的函数/重载 |
| **需平台集成测试** | ~15 | 2 测试文件 | 中低优先级。`PsiService`、`PsiFileService` 值得补充 |
| **不可测试 / ROI 极低** | ~15 | 0 | 不建议投入。包括 UI 胶水、纯平台包装器、标记注解 |

### 3.3 推荐优先行动清单

#### 第一优先级：零覆盖但高度可测试的模块

1. **`text/`** — 全部 4 个文件均可纯单元测试，`TextPatterns.kt` 含非平凡逻辑。**立即行动。**
2. **`util/`** — 10 个未测试文件均纯单元测试。按逻辑复杂度排序：`Processors.kt` → `RecursionGuard.kt` → `RecursionService.kt` → `Markers.kt` → `CallbackLock.kt` → `LazyValue.kt`。
3. **`cache/`** — `CancelableCache`、`NestedCache`、`NestedLoadingCache` 为薄包装器，测试成本低、价值高。

#### 第二优先级：部分覆盖需补充的模块

4. **根级扩展文件** — `StdlibExtensions.kt`（量最大，约 58+ 个未测试函数）、`PlatformExtensions.kt`（约 46+ 个未测试函数）。
5. **`accessor/`** — 补测写操作、错误路径、`AccessorProviderImpl`。
6. **`collections/`** — `CollectionFastExtensions.kt`（10 个函数完全无测试）、`WalkingSequence.kt`。
7. **`math/`** — 补测 `evaluateOrNull()` 变体、`MathResult` 直接方法、错误路径。
8. **`data/`** — 补测 `json5Mapper`、`JsonModuleWithType`。

#### 第三优先级：需平台集成测试的模块

9. **`psi/`** — 补测 `PsiService` 的 5 个未测试方法和 `PsiFileService` 的 3 个方法。
10. **`execution/`** — 扩展 `CommandLineExecutor` 测试覆盖更多命令类型和非 Windows 路径。

#### 不建议投入

- `ui/` — UI 胶水代码，不适合单元测试。
- `options/`、`vfs/`、`navigation/`、`editor/`、`inspections/` — 纯平台包装器，无独立业务逻辑。
- `annotations/` — 标记注解，无行为可测。

### 3.4 可测试性改进建议

部分生产代码的设计使其天然不易测试。以下为可考虑的重构方向（非必需，按需评估）：

1. **`VirtualFileBomService`** — 将 BOM 字节数组操作（`addBom`/`removeBom`）提取为纯函数（接收/返回 `ByteArray`），与 `VirtualFile` 写动作分离。这样纯逻辑部分即可被单元测试覆盖。
2. **`ExecutionExtensions`** — `executeCommandLine` 扩展函数将参数委托给 `CommandLineExecutor`，可考虑将命令构建逻辑（`CommandLineService.getCommands`）与执行逻辑解耦，使前者可单元测试。
3. **`ContainerBasedMover`** — 作为 202 行的复杂抽象类，部分纯逻辑（容器-成员语义判断）可提取为单独的纯函数进行单元测试，减少对完整 Editor/PSI 环境的依赖。

---

## 四、报告数据来源与方法说明

- **生产代码清单**：基于 `src/main/kotlin/icu/windea/pls/core/` 的完整递归遍历（含 20 个子包，共 105 个 `.kt` 文件）。
- **测试代码清单**：基于 `src/test/kotlin/icu/windea/pls/core/` 的完整递归遍历（共 42 个测试文件），以及 `src/test/unused/` 和外部包中引用 `icu.windea.pls.core` 的 58 个测试文件。
- **覆盖度判定**：通过对比每个生产文件的公开/内部 API 与对应测试文件中的实际断言进行人工判定。
- **可测试性判定**：基于对 IntelliJ Platform API 依赖程度、逻辑复杂度、以及纯逻辑与胶水代码的比例进行主观评估。
