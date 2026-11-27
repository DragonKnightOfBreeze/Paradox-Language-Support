# 脚本片段注入机制调研与既有设计文档现行性核对（3.1.0 快照）

> - 版本：`v3.1.0`
> - 日期：`2026-11-27`
> - 状态：`调研快照`
> - 代码基线：`17be200da3`（`[doc] Update changelog and roadmap for v3.1.0 Wrap-up 2` 之后的只读工作树）
> - 调研范围：`icu.windea.pls.lang.injection`、`icu.windea.pls.extensions.markdown`、`icu.windea.pls.model.injection`、`icu.windea.pls.ep.resolve.config`、`icu.windea.pls.ep.resolve.parameter`、`icu.windea.pls.lang.resolve` 与 `icu.windea.pls.inject` 中的注入相关链路，以及 `icon-design.md`、`color-scheme.md`、`spellchecking.md`、`tool-integration-tiger.md`、`mod-import-export.md` 五份既有设计文档对应的当前源码。
> - 约束：本次为只读调研，未修改代码、`documents/design/` 下文件或 `documents/notes/reports.md`，未执行 `git add` 或 `git commit`。

## 一、脚本片段注入全链路

### 1.1 结论摘要

- 当前注入子系统的核心实现已经收敛为 `ParadoxLanguageInjector`、`ParadoxLanguageInjectionService`、`ParadoxLanguageInjectionManager` 三个类；任务描述中的 `ParadoxScriptInjector` / `ParadoxScriptLanguageInjector` 等旧类名已不再存在，其中 `ParadoxScriptLanguageInjector` 与 `ParadoxScriptInjectionManager` 已在 `5bb5865012` 中合并为当前三个类。
- 当前物理语言注入只发生在两类脚本宿主上：`ParadoxScriptString` 与 `ParadoxScriptNormalParameterArgument`；前者承载参数传入值与本地化富文本，后者承载参数默认值 `$PARAM|DEFAULT$` 中的默认值部分，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjector.kt:33`。
- 规则上下文并不是通过第二层物理注入传递的，而是通过 `CwtConfigContextProvider` 扩展点在读取注入文件时按优先级选择一个上下文提供者，再由 `CwtConfigContext` 生成上下文规则与匹配规则；这层语义注入与 IntelliJ 的语言注入是两套不同机制，参见 `src/main/kotlin/icu/windea/pls/ep/resolve/config/CwtConfigContextProvider.kt:33` 与 `src/main/kotlin/icu/windea/pls/lang/resolve/CwtConfigContext.kt:37`。
- Markdown 内联代码不是语言注入，而是 `ImplicitReferenceProvider` 驱动的符号引用；Markdown 代码围栏的语言注入由 Markdown 插件按围栏语言完成，本插件只在注入完成后通过 `InjectedFileProcessor` 给注入文件附加 `ParadoxFileInfo`，参见 `src/main/kotlin/icu/windea/pls/extensions/markdown/psi/MarkdownInlineCodeReferenceProvider.kt:14` 与 `src/main/kotlin/icu/windea/pls/extensions/markdown/injection/MarkdownCodeFenceInjectedFileProcessor.kt:16`。
- 在 `263` 基线上，平台仍不接受嵌套语言注入，因此当前实现采用“单层物理注入 + 顶层宿主回溯 + 规则上下文推断”的绕过策略，而不是在已注入的文件里再请求第二层注入；这一策略对普通脚本文件有效，但在 Markdown 代码围栏内会丢失部分参数值与内联脚本专用上下文，详见 1.5。
- 在 `v3.0.5..HEAD` 范围内，`lang/injection`、`model/injection`、`CwtConfigContextProviders.kt`、`ep/resolve/parameter`、`ParadoxInlineScriptService.kt` 均没有提交记录；注入逻辑最近一次实质性变更仍是 `5bb5865012`（2026-09-18，属于 v3.0.3/v3.0.4 周期）。本周期内的实质变动集中在 `extensions.markdown` 的 263 兼容与测试补充、Tiger 的命令行与请求重构、以及模组导入导出的 UTF-8 BOM 兼容，详见第二部分。

### 1.2 注入场景与宿主

#### 1.2.1 参数传入值注入

- 触发位置是脚本属性值中的字符串，也就是 `ParadoxScriptString`；典型形式是定义调用或内联脚本调用中的 `ARG = "some_script_fragment"`。
- 触发开关是 `ChronicleSettings.state.inference.injectionForParameterValue`，默认开启，参见 `src/main/kotlin/icu/windea/pls/base/settings/ChronicleSettings.kt:326`。
- 注册入口是 `src/main/resources/META-INF/chronicle-lang.xml:458` 中的 `<multiHostInjector implementation="icu.windea.pls.lang.injection.ParadoxLanguageInjector"/>`；旧的 `ParadoxScriptLanguageInjector` 注册已随 `5bb5865012` 迁移为当前类名。
- 注入前先通过 `ParadoxParameterManager.getContextReferenceInfo(host, InContextReference)` 向上找到当前脚本片段所属的定义调用或内联脚本调用，再收集该调用中落在宿主文本范围内的传入参数，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjectionService.kt:53`。
- 参数名必须通过 `ParadoxNameValidators.checkParameterName`，参数值必须非空且不是 `ParadoxSeparatorType` 中的操作符样式，才接受注入；这是一条启发式过滤，不要求参数值一定带引号，参见同文件 `:116`。
- 注入范围会先按参数值的 `TextRange` 截取，再对带引号的值调用 `unquote` 去掉两侧引号；`parameterValueQuoted` 记录参数值是否两侧都带引号，后续补全用它判断当前位置是否可以视作属性键或属性值，参见同文件 `:68` 与 `src/main/kotlin/icu/windea/pls/lang/codeInsight/completion/script/ParadoxScriptExpressionCompletionProvider.kt:45`。
- 注入信息通过 `ParadoxLanguageInjectionKeys.parameterValueInjectionInfos` 缓存到宿主 PSI 的 user data 上，同时 `ParadoxLanguageInjectionManager.getParameterValueInjectionInfoFromInjectedFile` 在读取注入文件时按 `DocumentWindow` 的 shred 范围匹配回对应的注入信息，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjectionKeys.kt:9` 与 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjectionManager.kt:68`。

#### 1.2.2 参数默认值注入

- 触发位置是 `$PARAM|DEFAULT$` 中的 `DEFAULT`，对应 PSI 宿主 `ParadoxScriptNormalParameterArgument`；宿主父节点必须是 `ParadoxScriptParameter`，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjectionService.kt:96`。
- 参数名必须合法、默认值必须非空且不是操作符样式，注入范围是 `0..defaultValue.length`，`parameterValueQuoted` 固定为 `false`。
- 参数元素通过 `ParadoxParameterService.resolveParameter(parameter)` 解析，用于后续 `CwtParameterValueConfigContextProvider` 推断上下文；这条路径与 1.2.1 的传入值路径共享同一个 `ParadoxParameterValueInjectionInfo` 模型。
- 需要注意，当前 `ParadoxScriptParameter` 本身不再是语言注入宿主，只有其 `normal_parameter_argument` 子节点实现 `ParadoxLanguageInjectionHost`；这与旧实现直接以 `ParadoxScriptParameter` 为宿主不同，参见 `src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf:309` 与 `src/main/gen/icu/windea/pls/script/psi/ParadoxScriptNormalParameterArgument.java:12`。

#### 1.2.3 本地化富文本注入

- 触发位置同样是 `ParadoxScriptString`，但只有在前一步参数值注入没有产生任何注入信息时才会尝试，`ParadoxLanguageInjector.applyInjection` 的顺序决定了参数值注入优先于本地化文本注入，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjector.kt:46`。
- 触发开关是 `ChronicleSettings.state.inference.injectionForLocalisationText`，默认开启；文本必须两侧带引号、通过 `ParadoxLocalisationManager.isRichLocalisationText` 判断包含富文本标记，并且匹配的 CWT 规则表达式类型必须是 `Literal`、`Scalar` 或 `LocalisationAware` 之一，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjectionService.kt:135`。
- 注入语言是 `ParadoxLocalisationLanguage`，注入时使用 `"${localisationName}: \""` 作为前缀、`"\""` 作为后缀，默认 `localisationName` 为 `INJECTED`，从而让注入文本按本地化属性的值来解析，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjector.kt:63` 与 `src/main/kotlin/icu/windea/pls/model/injection/ParadoxLocalisationTextInjectionInfo.kt:11`。
- 本地化文本注入信息没有像参数值注入那样写入宿主 user data，而是每次注入时重新解析；这是当前实现的一个不对称点，升级设计文档时应明确参数值与本地化文本的缓存策略差异。

#### 1.2.4 Markdown 代码围栏注入

- 语言注入本身由 Markdown 插件根据围栏语言 ID 完成，本插件没有注册面向 Markdown 的 `MultiHostInjector`；插件的入口是 `InjectedFileProcessor` 扩展点，在 `InjectionRegistrarImpl.cacheEverything` 之后处理已创建的注入文件，参见 `src/main/kotlin/icu/windea/pls/inject/injectors/InjectionRegistrarImplCodeInjector.kt:25`。
- 相关注册为 `src/main/resources/META-INF/chronicle-extension-markdown.xml:20` 的 `<injectedFileProcessor implementation="icu.windea.pls.extensions.markdown.injection.MarkdownCodeFenceInjectedFileProcessor"/>`，以及 `src/main/resources/META-INF/chronicle-inject.xml:29` 的 `<codeInjector implementation="icu.windea.pls.inject.injectors.InjectionRegistrarImplCodeInjector"/>`。
- `MarkdownCodeFenceInjectedFileProcessor.process` 先检查 `injectCodeBlocks` 设置、跳过 stub 文件、从注入文件反查 `MarkdownCodeFence` 宿主，再解析围栏语言 ID 后面的 `path={gameType}:{path}` 元数据，最后把 `ParadoxFileInfo` 注入到 `VirtualFileWindow` 上，参见 `src/main/kotlin/icu/windea/pls/extensions/markdown/injection/MarkdownCodeFenceInjectedFileProcessor.kt:16`。
- `path` 元数据解析位于 `MarkdownExtensionManager.getPathInjectionInfo`：要求围栏语言是 `ParadoxLanguage`、`path` 以 `gameType:path` 形式出现、扩展名属于脚本/本地化/CSV 之一；解析结果按代码围栏 PSI 缓存，参见 `src/main/kotlin/icu/windea/pls/extensions/markdown/MarkdownExtensionManager.kt:123`。
- 如果 Markdown 文件位于游戏或模组根目录内，`getInjectedFileInfoFromInjectedFile` 会沿用其 `rootInfo`；否则回退到 `ParadoxRootInfo.Injected(gameType = pathInjectionInfo.gameType)`，因此 Markdown 文件可以位于游戏/模组目录之外而仍获得上下文，参见同文件 `:163`。
- 注入文件信息最终由 `CwtBaseConfigContextProvider`、`CwtDefinitionConfigContextProvider` 等按普通脚本文件路径消费；`ParadoxFileTypeOverrider` 也会对 `VirtualFileWindow` 使用注入文件信息重载文件类型，参见 `src/main/kotlin/icu/windea/pls/lang/ParadoxFileTypeOverrider.kt:20`。

#### 1.2.5 Markdown 内联代码（非语言注入）

- 内联代码由 `MarkdownInlineCodeReferenceProvider` 实现 `ImplicitReferenceProvider`，从 `MarkdownCodeSpan` 中提取目标名并创建 `MarkdownInlineCodeSymbolReference`；它不创建注入文件，也不经过 `ParadoxLanguageInjector`，参见 `src/main/kotlin/icu/windea/pls/extensions/markdown/psi/MarkdownInlineCodeReferenceProvider.kt:14`。
- 目标名前缀 `@` 表示封装变量，空前缀表示定义或本地化；解析顺序是先定义、后本地化，参见 `src/main/kotlin/icu/windea/pls/extensions/markdown/MarkdownExtensionManager.kt:88`。
- `MarkdownInlineCodeSymbol` 同时实现 `DocumentationSymbol`、`NavigatableSymbol`、`SearchTarget` 与 `RenameTarget`，因此内联代码可以参与快速文档、导航、查找用法与重命名；`MarkdownInlineCodeReferencesSearcher` 让目标声明在 `ReferencesSearch` 中能找到 Markdown 内联代码用法，`MarkdownInlineCodeRenameUsageSearcher` 再把目标声明与所有引用一起交给重命名流程，参见 `src/main/kotlin/icu/windea/pls/extensions/markdown/psi/MarkdownInlineCodeSymbol.kt:34` 与 `src/main/kotlin/icu/windea/pls/extensions/markdown/refactoring/rename/MarkdownInlineCodeRenameUsageSearcher.kt:35`。
- 从 263 基线开始，`MarkdownLinkDestination` 的引用不再通过 `PathReferenceManager` 构建，因此插件新增 `MarkdownLinkReferenceContributor` 显式接入 `ParadoxPathReferenceProvider`；同时 `MarkdownInlineCodeManager.getIdentifierText` 接受任意叶子词元（例如 GFM `AT_PATH_TOKEN`），不再只接受纯文本词元，参见 `src/main/kotlin/icu/windea/pls/extensions/markdown/references/MarkdownLinkReferenceContributor.kt:16` 与 `src/main/kotlin/icu/windea/pls/extensions/markdown/MarkdownInlineCodeManager.kt:19`。
- 用户参考 `docs/zh/extensions.md:110-174` 已经描述了内联代码的 `@name` / `name` 格式与代码围栏的 `path=` 格式；但用户参考尚未提及 3.1.0 的内联代码重命名、`resolveInlineCodes` / `injectCodeBlocks` 开关，以及 263 下 GFM `@` 词元识别修复。

#### 1.2.6 手动语言注入

- 当前实现对可注入宿主显式调用 `InjectionUtils.enableInjectLanguageAction(host, false)`，并让 `ParadoxLanguageInjectionHost` 实现 `InjectionBackgroundSuppressor` 以关闭注入背景高亮，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjector.kt:40` 与 `src/main/kotlin/icu/windea/pls/lang/psi/ParadoxLanguageInjectionHost.kt:9`。
- 另有 `InjectLanguageActionCodeInjector` 对 `InjectLanguageAction.isAvailable` 做前置修正，避免在未启用注入的宿主上误开放动作，参见 `src/main/kotlin/icu/windea/pls/inject/injectors/fix/InjectLanguageActionCodeInjector.kt:15`。
- 因此，`documents/design/script-injection.md` 中“手动语言注入”作为注入场景的表述已经不符合现状；现行设计应改为“自动注入（参数值、默认值、本地化富文本、Markdown 代码围栏）+ Markdown 内联代码符号引用”，手动注入不应再作为受支持入口。

### 1.3 规则上下文注入与透传

#### 1.3.1 上下文提供者与优先级

- `CwtConfigContextProvider` 扩展点按 `chronicle-ep.xml` 的注册顺序与 `order` 属性排序，首个 `supports(gameType)` 为真且返回非空上下文的提供者生效；`CwtConfigContextProvider.getAll()` 通过 `LazyValue` 缓存扩展列表，并在扩展点变化时重新初始化，参见 `src/main/kotlin/icu/windea/pls/ep/resolve/config/CwtConfigContextProvider.kt:54`。
- 当前实际优先级从高到低为：`CwtDefineVariableConfigContextProvider`、`CwtParameterValueConfigContextProvider`、`CwtInlineScriptUsageConfigContextProvider`、`CwtInlineScriptFileConfigContextProvider`、`CwtDefinitionInjectionConfigContextProvider`、`CwtDefinitionConfigContextProvider`、`CwtBaseConfigContextProvider`，注册表见 `src/main/resources/META-INF/chronicle-ep.xml:291-297`。
- 各提供者的职责与根上下文规则来源如下表所示。

| 提供者 | 触发条件 | 根上下文规则来源 | 关键说明 |
|---|---|---|---|
| `CwtDefineVariableConfigContextProvider` | 定值变量文件、成员路径深度大于 1、能解析到定值变量声明 | `defineVariableInfo.config.rootConfig` | 用于 define 变量声明内部的规则上下文，参见 `CwtConfigContextProviders.kt:132-178`。 |
| `CwtParameterValueConfigContextProvider` | 当前 PSI 文件是脚本参数值注入文件，且能解析到参数元素 | `ParadoxParameterManager.getInferredContextConfigs(parameterElement)` | 参数值注入的上下文入口；不把参数值内容内联到调用处再做检查，参见 `CwtConfigContextProviders.kt:189-238`。 |
| `CwtInlineScriptUsageConfigContextProvider` | 相对文件的成员路径中包含 `inline_script` 子路径，且游戏类型支持内联脚本 | `macroModel.forInlineScripts` 中的宏规则经 `CwtConfigInlineService.inlineMacro` 内联后的规则 | 用于内联脚本调用处的上下文；根级 `inline_script` 跳过 unresolved 检查，参见 `CwtConfigContextProviders.kt:243-286`。 |
| `CwtInlineScriptFileConfigContextProvider` | 非注入的脚本文件，且文件路径位于 `common/inline_scripts` 并解析出内联脚本表达式 | `ParadoxInlineScriptManager.getInferredContextConfigs(expression, element, context, options)` | 会将内联脚本内容内联到每个调用处并合并上下文；显式跳过注入文件，参见 `CwtConfigContextProviders.kt:294-347`。 |
| `CwtDefinitionInjectionConfigContextProvider` | VIC3/EU5 定义注入声明内部 | `definitionInjectionInfo.declaration` | 用于定义注入声明的规则上下文；当前不先内联目标定义内容，参见 `CwtConfigContextProviders.kt:356-415`。 |
| `CwtDefinitionConfigContextProvider` | 当前成员位于某个定义声明内部 | 定义声明的 declaration 规则或按成员路径展开的规则 | 定义声明的规则上下文；根级属性跳过 unresolved 检查，参见 `CwtConfigContextProviders.kt:77-127`。 |
| `CwtBaseConfigContextProvider` | 任意带文件信息的脚本文件 | 空列表 | 最后的兜底提供者，只负责创建基础上下文；unresolved/missing/too-many 三类检查全部跳过，参见 `CwtConfigContextProviders.kt:49-70`。 |

#### 1.3.2 参数值注入的上下文恢复

- `CwtParameterValueConfigContextProvider.getContext` 通过 `ParadoxLanguageInjectionManager.getParameterValueInjectionInfoFromInjectedFile(file)` 拿到注入信息，再取其中懒加载的 `parameterElement`；如果参数元素解析失败，该提供者返回 `null`，继续回退到后续提供者，参见 `CwtConfigContextProviders.kt:193`。
- `parameterElement` 对传入值路径来自 `resolveParameterForArgumentValue(referenceInfo)`：它要求参数名元素的范围和引用范围一致，再通过引用解析到 `ParadoxParameterLightElement`；对默认值路径来自 `ParadoxParameterService.resolveParameter(parameter)`，参见 `src/main/kotlin/icu/windea/pls/lang/injection/ParadoxLanguageInjectionService.kt:80`。
- `ParadoxParameterContextReferenceInfo.From` 区分 `Argument`、`ContextReference`、`InContextReference` 三种查找模式：参数值注入使用 `InContextReference`，补全/快速文档通常使用 `Argument` 或 `ContextReference`；三种模式分别决定额外参数是 CWT 规则、上下文规则还是仅按当前偏移向上查找，参见 `src/main/kotlin/icu/windea/pls/model/ParadoxParameterContextReferenceInfo.kt:35`。
- 定义注入场景下，参数声明解析必须使用 `definitionInfo.name` 而不是上下文元素的 `name`，否则 `REPLACE_OR_CREATE:x = {...}` 会把注入键 `x` 误当成定义名；Issue #429 与 `Issue429Test` 覆盖了这一边界，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/util/ParadoxParameterSupportFactory.kt:71` 与 `src/test/kotlin/icu/windea/pls/test/issues/Issue429Test.kt:26`。
- `ParadoxParameterManager.getInferredContextConfigs` 按设置决定先看扩展参数规则还是先看使用处：`configContextForParametersFromConfig` 为真时优先使用 `parameters/{name}` 扩展规则中的 `context_key`、`context_configs_type`、`inherit`；否则或结果为空时，再通过 `configContextForParametersFromUsages` 遍历参数使用处并合并上下文，参见 `src/main/kotlin/icu/windea/pls/lang/util/ParadoxParameterManager.kt:302`。
- `## inherit` 为真时，`ParadoxExtendedConfigService.getContextConfigs` 从参数的使用处上下文继承规则，并调用 `ParadoxThreadContext.resolvingConfigContext?.markDynamic()` 把当前上下文标记为动态，避免把依赖使用处的规则误缓存到规则分组级别，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxExtendedConfigService.kt:31`。
- 参数值注入的上下文不会把参数值内容内联到调用处再做 `MissingExpressionInspection`、`TooManyExpressionInspection` 或语法合法性检查；根级只跳过 missing/too-many 检查，unresolved 检查仍然可能触发，这是设计文档升级时必须明确的一条边界。

#### 1.3.3 内联脚本上下文

- 内联脚本调用处的上下文来自宏规则本身：`CwtInlineScriptUsageConfigContextProvider` 用 `configGroup.macroModel.forInlineScripts` 取出所有内联脚本宏规则，经 `CwtConfigInlineService.inlineMacro` 内联后作为根上下文规则，参见 `CwtConfigContextProviders.kt:274`。
- 内联脚本文件本身的上下文来自 `ParadoxInlineScriptManager.getInferredContextConfigs`：先看 `inline_scripts/{name}` 扩展规则，若没有或为空再通过 `ParadoxInlineScriptService.getInferredContextConfigsFromUsages` 搜索所有调用处并合并调用处上下文，参见 `src/main/kotlin/icu/windea/pls/lang/util/ParadoxInlineScriptManager.kt:170`。
- `ParadoxInlineScriptService.getInlineScriptArgumentMapFromUsageElement` 从调用块的属性中提取参数键值映射，并通过 `ParadoxParameterContextManager.getArguments(block, "script")` 排除 `script` 键；当前代码中没有该方法的直接调用点，实际参数替换由 `ParadoxParameterContextManager.replaceTextWithArgs` 在 `ParadoxPsiService` 等展示/内联路径中使用，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxInlineScriptService.kt:110` 与 `src/main/kotlin/icu/windea/pls/lang/psi/ParadoxPsiService.kt:129`。
- 使用处推断会设置 `inlineScriptHasConflict` 与 `inlineScriptHasRecursion` 标记：`mergeConfigs` 失败时视为冲突并清空结果，`withRecursionCheck(expression)` 阻止同一表达式递归展开，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxInlineScriptService.kt:127`。
- Issue #244 的递归内联脚本复现数据已加入 `src/test/testData/issues/244`，当前通过 `withRecursionCheck(expression)` 阻断同一表达式的递归展开；这使嵌套内联脚本的“语义递归”与“物理嵌套注入”解耦。
- `fast` 模式下，如果已经得到唯一且足够具体的规则，就提前结束推断；`Any` 与 `Literal` 回退形式被视为不够具体，继续展开，参见同文件 `:162`。
- 内联脚本文件中的定义声明目前仍不受支持，`CwtInlineScriptFileConfigContextProvider` 保留 `TODO 1.1.0+` 注释；用户参考 `docs/zh/README.md:59` 也明确列出了这一限制。

#### 1.3.4 定义注入、定值变量与直接规则上下文

- `CwtDefinitionInjectionConfigContextProvider` 服务于 VIC3/EU5 的 `inject:xxx` / `REPLACE_OR_CREATE` 等定义注入声明，根规则来自 `definitionInjectionInfo.declaration`，不会先把目标定义内容内联到注入声明中，参见 `CwtConfigContextProviders.kt:392`。
- `CwtDefineVariableConfigContextProvider` 只对定值变量文件生效，要求成员路径深度大于 1 且能解析到定值变量声明；根规则来自 `defineVariableInfo.config.rootConfig`，参见 `CwtConfigContextProviders.kt:165`。
- `CwtDefinitionConfigContextProvider` 从 `definitionInfo.memberPath` 相对化当前成员路径，再从定义声明的 declaration 规则展开；根级属性跳过 unresolved 检查，因为根级属性本身就是定义名，参见 `CwtConfigContextProviders.kt:114`。
- `CwtBaseConfigContextProvider` 没有上下文规则，只创建带 `element`、`rootFile`、`memberPathFromFile` 的基础上下文；它是所有规则的兜底，并跳过 unresolved/missing/too-many 三类检查，参见 `CwtConfigContextProviders.kt:49`。

#### 1.3.5 缓存、动态上下文与失效

- `ParadoxConfigManager.getConfigContext` 在 PSI 成员上缓存 `CwtConfigContext`，缓存依赖 `element` 与 `ChronicleModificationTrackers.ConfigResolution`；`getConfigs` 还会在 PSI 成员上缓存一个以 options 哈希为键的软引用并发 Map，参见 `src/main/kotlin/icu/windea/pls/lang/util/ParadoxConfigManager.kt:66` 与 `:101`。
- `ParadoxConfigService.getConfigsForConfigContext` 以 `rootFile` 和 `provider.getCacheKey(context, options)` 为键写入规则分组级缓存；`getOrPut` 外有 `withRecursionGuard` 与 `withRecursionCheck(cacheKey)` 双重保护，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxConfigService.kt:172`。
- 动态上下文使用上下文对象自己的 `configsDynamicCache` 而不是规则分组缓存；如果结果被标记为动态，`getConfigsForConfigContext` 会在 `finally` 中使规则分组缓存中的对应键失效，参见同文件 `:176` 与 `:206`。
- `CwtConfigContext` 的 `declaration`、`dynamic` 是上下文基类的实际字段，`parameterValueQuoted`、`inlineScriptExpression`、`definitionInjectionInfo` 等扩展字段则以 `KeyRegistry` 形式挂在上下文对象上，而不是直接放在 PSI 上；这使同一 PSI 成员在不同语义角色下可以拥有不同上下文，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/CwtConfigContext.kt:37` 与 `src/main/kotlin/icu/windea/pls/lang/resolve/_accessors.kt:38`。

### 1.4 Markdown 内联代码、围栏与参数值注入的协作

- Markdown 内联代码与参数值注入没有直接协作关系：前者是符号引用，后者是语言注入；内联代码解析目标时使用 `ParadoxDefinitionSearch`、`ParadoxLocalisationSearch`、`ParadoxScriptedVariableSearch` 的 `contextSensitive()` 选择器，不依赖注入上下文，参见 `src/main/kotlin/icu/windea/pls/extensions/markdown/MarkdownExtensionManager.kt:88`。
- 代码围栏与参数值注入的交汇点是围栏内的脚本内容：围栏本身由 Markdown 插件注入为脚本语言，`CwtBaseConfigContextProvider` 等按 `path=` 文件信息提供上下文；但围栏内的 `ParadoxScriptString` 不会再次触发 `ParadoxLanguageInjector`，因为平台不支持嵌套注入，且插件的 `MultiHostInjector` 入口只面向物理脚本宿主。
- 具体后果是：围栏内的参数传入值、参数默认值不会获得 `CwtParameterValueConfigContextProvider` 的注入上下文；围栏内如果声明的是内联脚本文件，`CwtInlineScriptFileConfigContextProvider` 也会因为 `VirtualFileService.isInjectedFile(vFile)` 为真而直接返回 `null`，只保留基于 `path=` 的 `CwtBaseConfigContextProvider` / `CwtDefinitionConfigContextProvider` 上下文。
- `MarkdownCodeFenceInjectedFileProcessorTest` 只验证了“注入文件信息 + 基础配置上下文”，没有覆盖围栏内参数值或内联脚本专用上下文；如果后续要在设计文档中承诺这些场景，需要先补充测试，参见 `src/test/kotlin/icu/windea/pls/extensions/markdown/injection/MarkdownCodeFenceInjectedFileProcessorTest.kt:23`。
- 263 兼容修复还包含 `MarkdownInlineCodeManager` 接受 GFM `AT_PATH_TOKEN`、`MarkdownLinkReferenceContributor` 显式接入路径引用、`MarkdownInlineCodeReferenceProvider` 与 `MarkdownInlineCodeSymbol` 的快速文档/导航/重命名链路，这些属于 3.1.0 Wrap-up 1 的实质变化，参见 `documents/notes/roadmap/v3.1.0.md:58-62` 与提交 `da83b8858f`、`aeb3e8450d`、`50fc37b519`、`d16b3d8773`。
- Wrap-up 1 抽取 `MarkdownInlineCodeManager` 之后，`MarkdownExtensionManager` 仍保留 `getIdentifierFromInlineCode`、`resolveInfoFromInlineCode`、`resolveTargetFromInlineCode` 等相同职责的入口，而 `MarkdownInlineCodeInfo.from` 仍调用旧入口；当前是两套 API 并存，设计文档升级时应指定唯一权威入口，避免后续维护分叉。

### 1.5 263 基线下的嵌套注入限制与绕过策略

- 平台限制：当前设计仍以“IDEA 不支持嵌套语言注入”为前提，仓库历史笔记 `documents/notes/_archived/TODO.md` 也明确记录了这一点；263 基线没有改变这一前提，注入子系统在 `v3.0.5..HEAD` 也没有新增嵌套注入支持。
- 单层物理注入：`ParadoxLanguageInjector` 只对物理脚本宿主注册 place，且一个宿主上参数值注入优先于本地化文本注入；没有“在已注入文件里再注入”的路径，参见 `ParadoxLanguageInjector.kt:46`。
- 顶层宿主回溯：`ParadoxLanguageInjectionManager.findTopHostElementOrThis` 与 `findTopHostFileOrThis` 通过 `InjectedLanguageManager.getInjectionHost` 和 `VirtualFileWindow.delegate` 逐层向上回溯；这使得注入文件中的参数声明仍然可以回到最外层脚本文件寻找定义或内联脚本上下文，参见 `ParadoxLanguageInjectionManager.kt:26` 与 `:38`。
- 参数上下文绕过：`ParadoxDefinitionParameterSupport.findContext` 与 `ParadoxInlineScriptParameterSupport.findContext` 都先调用 `findTopHostElementOrThis`，再分别查找父定义或外层内联脚本文件；这是“参数在注入片段中仍能解析”的关键，参见 `src/main/kotlin/icu/windea/pls/ep/resolve/parameter/ParadoxParameterSupports.kt:44` 与 `:113`。
- Issue #153 的验收标准是任意深度嵌套内联脚本中的局部变量可识别；当前通过顶层宿主回溯与使用处上下文推断支撑，但实际能力仍受单层物理注入与 Markdown 围栏边界限制。
- 内联脚本上下文绕过：`ParadoxInlineScriptService.getInferredContextConfigsFromUsages` 通过搜索所有调用处并合并调用处上下文来模拟嵌套内联脚本语义，而不是通过嵌套注入；`withRecursionCheck(expression)` 保证递归内联脚本不会无限展开，参见 `ParadoxInlineScriptService.kt:127`。
- Markdown 围栏边界：`CwtInlineScriptFileConfigContextProvider` 对注入文件显式返回 `null`，`ParadoxParameterSupport` 的顶层回溯最终会停到 `MarkdownCodeFence`（它不是 `ParadoxLanguageInjectionHost`），因此围栏内参数声明与内联脚本文件专用上下文的恢复能力有限；当前测试未覆盖这两个场景，属于设计文档升级时应标注的已知限制而不是既成承诺。
- 平台已知缺陷绕过：`IdentifierHighlightingComputerCodeInjector` 针对 `IdentifierHighlightingComputer.computeRanges` 返回多个目标且一个目标包含另一个目标的情况，移除第一个目标；这是 p20 中“注入参数值用法高亮可能错误高亮之前参数值”的代码注入层缓解，参见 `src/main/kotlin/icu/windea/pls/inject/injectors/fix/IdentifierHighlightingComputerCodeInjector.kt:17`。p20 笔记当前状态仍是“待验证”，因此该绕过不应被描述为彻底修复。
- 转义引号限制：`ParadoxLanguageInjectionManager.getParameterValueInjectionInfoFromInjectedFile` 对 `ParadoxScriptString` 只取 `shreds?.singleOrNull()` 并用 `it.rangeInsideHost in rangeInsideHost` 匹配；当参数值中包含转义双引号时，注入信息范围与 shred 范围可能不完全一致，这与 Issue #60 记录的已知问题一致，参见 `ParadoxLanguageInjectionManager.kt:74-80`。
- 反射与内部 API 风险：`ParadoxLanguageInjectionManager.getShreds` 通过 `DocumentWindow` 的扩展函数间接访问 `DocumentWindowImpl.getShreds`，该实现位于 `src/main/kotlin/icu/windea/pls/lang/_reflectionAccessors.kt:24`；263 平台内部实现变化时，这条路径需要优先回归。

### 1.6 性能、防抖与测试坑

#### 1.6.1 缓存与递归保护

- 注入信息缓存：参数值注入信息写入宿主 PSI 的 user data；Markdown 路径注入信息按 `MarkdownCodeFence` 使用 `CachedValue` 缓存；规则上下文缓存位于 `ParadoxConfigManager` 与 `ParadoxConfigService` 两层。
- 规则上下文缓存：`CwtConfigGroup.configsCache` 以 `rootFile` 和 provider cache key 为键；`CwtConfigContext.configsDynamicCache` 用于动态上下文；`ParadoxConfigManager.getConfigs` 用软引用并发 Map 按 options 哈希缓存匹配规则。
- 递归保护：内联脚本使用表达式级 `withRecursionCheck`；参数使用 `ParadoxParameterService` 与 `ParadoxExtendedConfigService` 的递归保护；配置上下文使用 `withRecursionGuard` 与 `withRecursionCheck(cacheKey)`。
- 取消检查：`ParadoxLanguageInjectionService`、`ParadoxConfigService`、`ParadoxExpressionMatchService` 等关键路径均调用 `ProgressManager.checkCanceled()`，避免在大目录或大规则集下无响应。

#### 1.6.2 “防抖”现状

- 代码库中没有 `debounce` / `防抖` 关键字；当前并不存在显式的 UI 防抖或延迟合并机制。
- 等效机制是：设置页面通过 `CallbackLock` 保证同一个 apply 回调只执行一次；`ChronicleAnalysisManager.refreshFiles` 异步重启 daemon；`refreshForInlineScriptInference` 只重解析打开的内联脚本文件；`refreshFiles` 和 `refreshInlayHints` 分别异步刷新高亮与内嵌提示，参见 `src/main/kotlin/icu/windea/pls/base/settings/ChronicleSettingsConfigurable.kt:823-847` 与 `src/main/kotlin/icu/windea/pls/base/analysis/ChronicleAnalysisManager.kt:126`。
- 因此，如果设计文档要描述“防抖”，建议改写为“基于 PSI 缓存、修改追踪器、CallbackLock 与取消检查的重复计算抑制”，不要写成存在显式 debounce 定时器。

#### 1.6.3 关联问题与性能风险

- p03（内联脚本注入卡死）：历史堆栈显示 `CwtConfigContext.getConfigs` 经 `ParadoxDynamicValueIndexSupport`、`ParadoxParameterIndexSupport` 等索引路径等待；结论是 `LoadingCache.get` 或 `ConcurrentMap.computeIfAbsent` 的锁行为导致冻结，当前 `ParadoxConfigService.getConfigsForConfigContext` 明确使用 `cache.asMap().getOrPut` 并保留“or will cause deadlock”的警告注释，参见 `ParadoxConfigService.kt:188-189` 与 `documents/notes/issues/p03-inline-script-injection-freeze.md:17`。
- p20（注入参数值用法高亮错误）：平台 `IdentifierHighlightingManagerImpl.getMarkupData` 可能在不同注入文件之间复用错误缓存；当前有 `IdentifierHighlightingComputerCodeInjector` 的局部绕过，但 p20 状态仍为“待验证”，参见 `documents/notes/issues/p20-injected-parameter-usage-highlighting.md:32`。
- p21（内联脚本目录全局检查 OOM）：OOM 主要来自别名规则展开导致 `ParadoxMatchCandidate` 数量爆炸；当前 `ParadoxConfigExpansionService` 使用 `aliasModel.expandedForConst` / `expandedForNonConst` 预分组和常量短路来降低规模，参见 `src/main/kotlin/icu/windea/pls/lang/manipulation/ParadoxConfigExpansionService.kt:124` 与 `src/main/kotlin/icu/windea/pls/config/model/CwtAliasModel.kt:16`。p21 笔记中的 `expandAndMatchAliasKeys` / `expandMatchedAliasKeys` 方法名已经不对应当前代码，应由设计文档改为描述“模型预分组 + 常量快速返回 + 参数化正则过滤”的现行为。
- p22（Markdown 内联代码查找用法意外指向）：根因是 `CwtConfigSymbolChooseByNameContributor.processElementsWithName` 未按参数 `name` 过滤查询结果；当前实现先通过 `ChronicleChooseByNameUtil.getNameToSearch` 调整名字，再以该名字发起 `CwtConfigSymbolSearch`，参见 `src/main/kotlin/icu/windea/pls/lang/navigation/CwtConfigSymbolChooseByNameContributor.kt:46` 与 `documents/notes/issues/p22-markdown-find-usages-unexpected-behavior.md:48`。
- 反射风险：`DocumentWindowImpl.getShreds`、`InjectLanguageAction.findInjectionHost`、`IdentifierHighlightingComputer` 等平台内部实现都有代码注入或反射访问；263 升级后这些点属于高优先级回归面。

#### 1.6.4 测试坑

- 测试上下文要先用 `markIntegrationTest()`、`markRootDirectory`、`markConfigDirectory`、`initConfigGroups` 建立注入根与规则分组，再在测试方法内按需调用 `markFileInfo` 或 `VirtualFile.injectFileInfo`，参见 `documents/notes/guidelines/testing.md:58-70`。
- `markFileInfo` 必须发生在后续 `configureByFile` / `configureByText` 之前；对已经存在的 `VirtualFile` 应直接调用 `injectFileInfo`。从 263 基线开始，复制二进制夹具不再受待决 `markFileInfo` 生效，必须在返回的 `VirtualFile` 上直接链式调用 `.injectFileInfo(...)`，参见同文件 `:69`。
- 注入片段的 highlighting 测试使用 `inject()` / `injectEnd()` 标记；`Issue412Test` 是参数值注入、内联脚本参数和定义声明上下文的代表性用例，参见 `src/test/kotlin/icu/windea/pls/test/issues/Issue412Test.kt:26` 与 `src/test/kotlin/icu/windea/pls/test/dsl/HighlightingDsl.kt:62`。
- Markdown 测试需要 `MarkdownTestBase` 提供固定测试根、恢复 `resolveLinks` / `resolveInlineCodes` / `injectCodeBlocks` 设置，并在结束前提交文档和保存文档；`findInjectedFile` 通过 `InjectedLanguageManager.getInjectedPsiFiles` 获取注入文件，参见 `src/test/kotlin/icu/windea/pls/extensions/markdown/MarkdownTestBase.kt:28`。
- `MarkdownCodeFenceInjectedFileProcessorTest` 在测试中手动调用 `MarkdownCodeFenceInjectedFileProcessor().process(injectedFile)`，而不是依赖真实 IDE 的 code injector 生命周期；这是理解该测试时的关键点，也说明该处理器需要可独立测试。
- `Issue429Test` 覆盖了 `REPLACE_OR_CREATE` 参数化定义的声明、注入、嵌套与注入嵌套四种组合，是参数上下文与定义注入交叉回归的主要测试，参见 `src/test/kotlin/icu/windea/pls/test/issues/Issue429Test.kt:26`。
- 测试数据中的 `.config/` 必须放在 `core/` 或具体 `{gameTypeId}/` 子目录下；测试类通常使用 `@TestDataPath("$CONTENT_ROOT/testData")` 并在 `doSetUp` 中初始化规则分组。

### 1.7 对 `script-injection.md` 的逐条现行性判断

现有 `documents/design/script-injection.md` 仅 36 行，且开头仍标注“记录历史性的设计与实现思路”；下表按现行源码逐条核对，可作为将其升级为现行设计文档的事实依据。

| 现有表述 | 现行事实 | 判断 |
|---|---|---|
| 注入场景包括手动语言注入 | 对可注入宿主禁用了 `InjectLanguageAction` 与注入背景，手动注入不再是受支持入口 | 需要删除或改写。 |
| Markdown 代码围栏语言注入 | Markdown 插件完成语言注入，本插件通过 `InjectedFileProcessor` 附加 `path=` 文件信息 | 保留，但需要补充实现分工。 |
| 对引号括起的参数值强制注入 | 当前覆盖参数传入值与 `$PARAM\|DEFAULT$` 默认值，且不要求带引号；只过滤空值和操作符样式 | 需要扩展并修正“必须带引号”的暗示。 |
| 基于 CWT 规则提供高亮、引用、补全 | 实际通过 `CwtConfigContext`、`ParadoxScriptExpressionSupport` 与匹配服务实现，注入只负责生成语言与文件信息 | 方向正确，但需要补充上下文提供者与匹配链路。 |
| 规则上下文分为直接、内联调用、内联文件、参数值、手动注入五类 | 前四类分别对应 `CwtBase`/`CwtDefinition`/`CwtDefineVariable`、`CwtInlineScriptUsage`、`CwtInlineScriptFile`、`CwtParameterValue`；手动注入不再成立；另有 `CwtDefinitionInjection` | 需要重排并补定义注入。 |
| 简单脚本片段手动实现，复杂片段通过语言注入 | 当前统一由 `ParadoxLanguageInjector` 的 `MultiHostInjector` 实现，没有独立“简单片段”分支 | 需要删除旧实现分类。 |
| IDEA 不支持嵌套语言注入 | 仍然成立，且 263 基线未改变；当前通过顶层宿主回溯与上下文推断绕过 | 保留，但需要补绕过策略与 Markdown 围栏边界。 |

## 二、既有设计文档与当前源码核对

### 2.0 `v3.0.5..v3.1.0` 期间代码变动概览

| 领域 | 期间是否有实质代码变动 | 关键提交或事实 |
|---|---|---|
| 脚本注入子系统 | 否 | `v3.0.5..HEAD` 对 `lang/injection`、`model/injection`、`CwtConfigContextProviders.kt`、`ep/resolve/parameter`、`ParadoxInlineScriptService.kt` 无提交；最近实质变更为 `5bb5865012`。 |
| Markdown 扩展 | 是 | 263 兼容、内联代码符号与重命名、链接引用贡献者、33 个单元测试；提交 `da83b8858f`、`aeb3e8450d`、`50fc37b519`、`d16b3d8773`。 |
| 图标 | 小范围 | `e7937bf841` 新增 gutter 章节并修正若干图标，同时删除 `icons/expui/gutter/targets*.svg`。 |
| 配色方案 | 否 | `v3.0.5..HEAD` 对 `*HighlighterColors.kt` 无提交；文档中的包路径与部分 key 仍是旧信息。 |
| 拼写检查 | 否 | `v3.0.5..HEAD` 对 `*SpellcheckerStrategy.kt` 无提交；文档仍停留在概括性原则。 |
| Tiger | 是 | 命令行执行与请求重构、`84e9b6447a` 传入模组描述符路径、`CommandBased*` 重命名为 `CommandLineBased*`、`TigerConfFileListener` 等。 |
| 模组导入导出 | 是 | `34e578eef9` 支持 UTF-8 BOM 并统一 `JsonService.read/write`，涉及 `ParadoxJsonBasedModImporter/Exporter`。 |
| 设计文档整理 | 是 | `79ca0ecc1f` 统一调整设计文档的标题与索引链接，未实质更新技术内容。 |

### 2.1 `icon-design.md`

**依然准确。**

- 文件类型、节点、规则、装订线四类图标的总体设计语言（颜色、形状、字母组合）与 `ChronicleIcons.kt` 基本一致；`nodes`、`configs` 两个章节列出的绝大多数图标名称都能在 `src/main/resources/icons` 与 `icons/expui` 中找到对应文件。
- 装订线章节是 2026-11-25 新增的，`scriptedVariable`、`definition`、`localisation`、`related*`、`inlineScripts`、`definitionInjections`、`definitionInjectionTargets` 的描述与当前资源一致。
- `configs` 章节对扩展规则统一使用灰白色 `#9AA7B0` 的描述与源码注释一致；`ChronicleIcons.Configs.Extended*` 的路径命名也大多对应。

**需要修正或增补。**

- 文件类型章节把本地化文件图标写成了第二个 `paradoxScript.svg`，实际文件是 `paradoxLocalisation.svg`；同时文档说“4 种自定义 DSL 的文件类型图标”，但 CSV 使用的是平台 `AllIcons.FileTypes.Csv`，应补充说明 CSV 不单独提供自定义图标，参见 `src/main/kotlin/icu/windea/pls/ChronicleIcons.kt:32` 与 `icon-design.md:26-31`。
- 装订线章节缺少 `defineVariable.svg`；当前 `ChronicleIcons.Gutter.DefineVariable` 已存在并使用，参见 `ChronicleIcons.kt:169` 与 `icons/gutter/defineVariable.svg`。
- 文档没有覆盖 `ChronicleIcons.Actions`、`ChronicleIcons.EditorActions` 与 `General.Steam` 三组图标；这些图标在 v3.0.4 之前就已存在，不是本周期新增，但文档概述声称相关目录包含全部图标，建议增加“动作/编辑器动作/通用图标”三个小节或明确声明本文只覆盖四类图标。
- 源码存在一个明确的图标路径缺陷：`ChronicleIcons.Configs.ExtendedComplexEnumValue` 指向 `/icons/configs/extended/extendedEnumValue.svg`，但资源目录只有 `extendedComplexEnumValue.svg`；`icon-design.md:147` 列出的文件名反而是正确的，参见 `ChronicleIcons.kt:155`。该缺陷在 `ParadoxExtendedCompletionManager.kt:110` 与 `CwtConfigTypes.kt:509` 会被访问，存在运行时图标加载失败风险。
- `src/main/resources/ChronicleIconMappings.json` 不是当前 `ChronicleIcons` 的实际数据源，且包含 11 条指向不存在资源的映射，例如 `nodes/localisationCommandScope.svg`、`nodes/localisationConcept.svg`、`configs/extended/extendedScriptedVariable.svg` 等；如果该文件仍被视为设计资产，需要单独清理，否则应在文档中注明它已不是权威来源。

**结论。**

- 文档整体仍然可用，但需要修正 1 处文件名、补 1 个装订线图标、补 3 组图标章节，并修复 `ExtendedComplexEnumValue` 的源码路径缺陷；属于小幅修正加少量增补。
- 文档元数据仍写 `v3.0.4` / `2026-10-05`，但装订线章节实际在 2026-11-25 加入；建议同步更新版本与日期。

### 2.2 `color-scheme.md`

**依然准确。**

- 四门语言的词法层 key、继承关系与主要颜色值基本与当前 `*HighlighterColors.kt` 和 `src/main/resources/colorSchemes/*.xml` 一致；`CWT` 章节的 13 个 key、`Paradox Script` 章节的 22 个词法 key、`Paradox Localisation` 与 `Paradox CSV` 的词法 key 均能一一对应。
- `PARADOX_SCRIPT.DEFINITION`、`DEFINITION_REFERENCE`、`DEFINITION_REFERENCE_SNIPPET`、`LOCALISATION_REFERENCE`、`COMPLEX_ENUM_VALUE`、`VARIABLE`、`MACRO`、`SHADER_EFFECT_REFERENCE`、`MESH_LOCATOR_REFERENCE` 等有主题重载的 key，其颜色、下划线/点线/波浪线与粗体/斜体描述与 `ParadoxScriptDefault.xml` / `ParadoxScriptDarcula.xml` 等资源一致。
- `PARADOX_LOCALISATION.ICON` 与 `CONCEPT` 的 `#5C8AE6` / `#008080` 固定颜色、`PARADOX_SCRIPT.VALUE_FIELD` 与 `COMMAND_FIELD` 的 `SASS_VARIABLE` 继承描述均与源码一致。

**需要修正或增补。**

- 文档开头的五个包路径写的是 `icu.windea.pls.*.editor.*HighlighterColors`，当前实际包路径是 `icu.windea.pls.cwt.highlighting.CwtHighlighterColors`、`icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors`、`icu.windea.pls.localisation.highlighting.ParadoxLocalisationHighlighterColors`、`icu.windea.pls.csv.highlighting.ParadoxCsvHighlighterColors`、`icu.windea.pls.lang.highlighting.ParadoxSemanticHighlighterColors`；包整理由 `8a467f2922`（2026-09-03）完成，文档在 2026-10-04 更新时仍未同步。
- 语义章节缺少 `PARADOX_SCRIPT.SEMANTIC_NUMBER`，当前 `ParadoxScriptHighlighterColors.kt:38` 已定义并继承 `DEFAULT_NUMBER`。
- 本地化章节缺少一整组派生/语义 key：`PARADOX_LOCALISATION.SEMANTIC_OPERATOR`、`SEMANTIC_MARKER`、`SEMANTIC_KEYWORD`、`SEMANTIC_STRING`、`DEFINITION_REFERENCE`、`DEFINITION_REFERENCE_SNIPPET`、`LOCALISATION_REFERENCE`、`LOCALISATION_REFERENCE_SNIPPET`、`DYNAMIC_VALUE`、`VARIABLE`、`SYSTEM_COMMAND_SCOPE`、`COMMAND_SCOPE`、`COMMAND_SCOPE_PREFIX`、`COMMAND_FIELD`、`COMMAND_FIELD_PREFIX`、`DATABASE_OBJECT_TYPE`、`DATABASE_OBJECT`，参见 `ParadoxLocalisationHighlighterColors.kt:33-50`。
- CSV 章节缺少派生 key：`DEFINITION_REFERENCE`、`DEFINITION_REFERENCE_SNIPPET`、`ENUM_VALUE`、`COMPLEX_ENUM_VALUE`（外部名为 `PARADOX_CSV.COMPLEX_ENUM`）、`DYNAMIC_VALUE`、`VARIABLE`，参见 `ParadoxCsvHighlighterColors.kt:19-24`。
- `PARADOX_SCRIPT.CONDITION_PARAMETER` 的源码注释写的是 `JAVA_TYPE_PARAMETER`，但文档写的是 `KOTLIN_TYPE_PARAMETER`，而 `ParadoxScriptDefault.xml:3` 的实际颜色 `#20999D` 与文档的 Kotlin 映射一致；应修正源码注释而不是文档值。
- 文档没有区分“有主题 XML 覆盖的 key”和“只使用默认继承的 key”；`colorSchemes/` 只包含 `ParadoxScript*.xml` 与 `ParadoxLocalisation*.xml`，CWT/CSV 及多数 semantic key 依赖平台默认值，建议在文档中加一段说明。

**结论。**

- 文档的 key 与颜色值主体仍然准确，但包路径、`SEMANTIC_NUMBER`、本地化/CSV 派生 key 需要补正；属于小幅修正加中等增补。
- 本周期没有配色方案源码变动，问题主要来自文档未跟上更早的包整理与 key 扩充。

### 2.3 `spellchecking.md`

**依然准确。**

- 关键扩展点方向正确：当前四个语言都通过 `spellchecker.support` 注册 `*SpellcheckerStrategy`，策略基类仍是 `SpellcheckingStrategy`，参见 `chronicle-cwt.xml:41`、`chronicle-script.xml:47`、`chronicle-localisation.xml:38`、`chronicle-csv.xml:29`。
- “跳过空白与存在语言注入的位置”“单行注释委托 `super`”“包含空白的文本用 `PlainTextSplitter`、不含空白的标识符用 `IdentifierSplitter`”“其他情况用 `EMPTY_TOKENIZER`”这些原则与 `CwtSpellcheckerStrategy`、`ParadoxCsvSpellcheckerStrategy` 的实际实现基本一致。
- “仅作文法级拼写检查，不作语义级拼写检查”仍是当前策略的总体定位。

**需要修正或增补。**

- 文档把关键 EP 写成 `com.intellij.spellchecker.tokenizer.SpellcheckingStrategy`，实际注册 EP 是 `com.intellij.spellchecker.support`，`SpellcheckingStrategy` 是基类而非 EP 名；建议改为“EP：`com.intellij.spellchecker.support`；基类：`SpellcheckingStrategy`”。
- “对于单行注释和选项注释，委托到 `super.getTokenizer(element)`”不准确：`CwtSpellcheckerStrategy` 对 `CwtOptionComment` 返回 `EMPTY_TOKENIZER`，只有 `PsiComment` 才委托 `super`，参见 `CwtSpellcheckerStrategy.kt:26-35`。
- “排除参数”只对本地化参数成立；脚本参数、条件参数和参数实参当前使用 `TokenAwareTokenizer` + `IdentifierSplitter` 参与拼写检查，`ParadoxScriptSpellcheckerStrategy.kt:37-48` 明确包含 `ParadoxScriptParameter`、`ParadoxScriptConditionParameter`、`ParadoxScriptArgument`。
- 本地化文本使用平台 `TEXT_TOKENIZER`，本地化属性键与本地化参数返回 `EMPTY_TOKENIZER`；脚本变量引用、本地化图标、文本图标、文本格式使用 `TokenAwareTokenizer` + `IdentifierSplitter`，文档应增加一张按语言划分的行为矩阵。
- 文档没有提到一个源码缺陷：`ParadoxScriptSpellcheckerStrategy.argumentTokenizer` 初始化时传入的是 `CONDITION_PARAMETER_TOKEN`，而不是 `ARGUMENT_TOKEN`，参见 `ParadoxScriptSpellcheckerStrategy.kt:35`；这会导致参数实参的拼写检查使用错误词元类型。
- 当前没有针对四个拼写检查策略的测试；如果文档升级要声明具体行为，建议同时补充最小回归测试。

**结论。**

- 文档原则方向正确，但需要从“概括性历史说明”升级为“按语言的行为矩阵 + EP 名称修正 + 参数行为修正”；属于小幅增补加必要修正。

### 2.4 `tool-integration-tiger.md`

**依然准确。**

- 关键 EP 方向正确：当前仍是 `ChronicleTigerLintInspection`（`LocalInspectionTool` + `ExternalAnnotatorBatchInspection`）与 `ChronicleTigerLintAnnotator`（`ExternalAnnotator`）配对，注册在 `chronicle-inspections.xml:407-411`。
- “执行工具、传入必要参数、得到 JSON 输出、解析并用于代码检查”“仅适用于整个游戏或模组目录”“需要妥善缓存检查结果”这些总体描述仍成立。
- 参考 Javadoc HTML lint 的 `JavadocHtmlLintAnnotator` / `JavadocHtmlLintInspection` 的实现方式仍然与当前结构相似。

**需要修正或增补。**

- 工具集合已经扩大：`TigerLintToolProvider` 注册了 `Ck3`、`Eu5`、`Hoi4`、`Ir`、`Vic3` 五个 provider，`chronicle-integrations.xml:15-19` 全部注册；但 `ChronicleIntegrationSettings` 只声明了 ck3/eu5/hoi4/ir/vic3 五组路径字段，而 `ChronicleIntegrationSettingsConfigurable.getTigerSettingsMap` 只暴露 ck3/ir/vic3 三组 UI，`docs/zh/integrations.md:112-121` 也只写 ck3/ir/vic3。当前有效支持与用户文档一致，但源码存在 eu5/hoi4 的“已注册、未暴露配置”不一致，设计文档应明确这是预留支撑还是待完成功能。
- 配置页面不是“每种工具对应不同的可折叠分组”，而是单一 Lint 分组下的全局 `enableTiger` 复选框、按 ck3/ir/vic3 平铺的路径与 `.conf` 路径行，以及一个独立的 Tiger 高亮映射对话框；模组/游戏设置中另有 `disableTiger` 级别开关，参见 `ChronicleIntegrationSettingsConfigurable.kt:117-164` 与 `ChronicleProfilesSettings.kt:226`。
- 作用范围不是“整个游戏或模组目录”，而是仅限模组根目录：`TigerLintToolService.collectRequest` 在 `rootInfo !is ParadoxRootInfo.Mod` 时直接返回 `null`，参见 `TigerLintToolService.kt:141`。
- 命令行参数应补充为 `--json`、可选 `--config <conf>`、可选 `--game <gamePath>`、最后传入 `modPath`；结果写入 `lintResultsPath` 下的 `<tool>-result@<uuid>.json`，并处理取消、超时、非零退出码，参见 `TigerLintToolProvider.kt:82` 与 `TigerLintToolService.kt:136`。
- 快速修复目前只有针对 `item.wiki` 的 `OpenUrlFix`，没有通用修复；文档中“提供必要的快速修复”应改为“提供 wiki 链接修复，暂不提供通用修复”，参见 `ChronicleTigerLintAnnotator.kt:81`。
- 缓存细节应补充：`TrackingCache` 使用 `maximumSize=128, expireAfterAccess=30m, softValues`，并按 `rootFile`、gameType modification tracker、`ScriptFile`、`LocalisationFile` 合并追踪；请求级 `synchronized` 锁合并重复调用；失败通知按请求去重，参见 `TigerLintToolService.kt:58` 与 `:181`。
- 还应补充 `TigerConfFileListener` 对 `.conf` 文件变更的异步监听、`TigerLintResult` 的 severity × confidence 模型、`TigerHighlightDialog` 的高亮映射配置，以及 `84e9b6447a` 传入模组描述符路径的修正。
- 文档中的旧链接（Issue #128、tslint、JSLint）可以保留作历史参考，但现行设计应以 Tiger 的 JSON 输出格式与 `LintToolProvider`/`LintRequest`/`LintResult` 抽象为主。

**结论。**

- 这是一份历史性集成草案，已明显落后于当前实现；需要较大幅度修订，尤其是工具矩阵、配置 UI、作用范围、缓存、命令参数与快速修复边界。

### 2.5 `mod-import-export.md`

**依然准确。**

- 目的与位置仍然准确：用于游戏或模组设置对话框的模组依赖导入/导出，核心包仍是 `icu.windea.pls.ep.tools`，UI 仍然是 `ParadoxModDependenciesImportPopup` 与 `ParadoxModDependenciesExportPopup`。
- 类层次基本准确：`ParadoxModImporter` / `ParadoxModExporter` 为 EP 接口，`ParadoxJsonBasedModImporter/Exporter` 与 `ParadoxDbBasedModImporter/Exporter` 为两大实现分支；`ParadoxGameJsonImporter/Exporter`、`ParadoxLauncherJsonImporter`、`ParadoxLauncherJsonV2Exporter`、`ParadoxLauncherJsonV3Exporter`、`ParadoxLauncherDbImporter/Exporter`、`ParadoxLauncherBetaDbImporter/Exporter` 均存在，EP 注册见 `chronicle-ep.xml:444-453`。
- 数据库版本描述“V2/V4/V5”与 `Constants.sqlV3Id`、`sqlV4Id`、`sqlV5Id` 以及导入时的 `position` 字符串/整数兼容处理基本一致。

**需要修正或增补。**

- `ParadoxLauncherJsonImporter` 已不是“按版本拆分”的实现，而是通过 `ParadoxMetadataUtil.detectLauncherPlaylistPositionIsInt` 自动探测 `position` 类型：整数按 V3、字符串按 V2，无法探测时先尝试 V2 再回退 V3；文档应补充“统一导入器 + 自动探测”，参见 `ParadoxLauncherJsonImporter.kt:26-33`。
- `ParadoxGameJsonImporter/Exporter` 的文件选择不是简单的“dlc_load.json 或 content_load.json”，而是按 `ParadoxGameTypeConstraint.DescriptorModUsed` 判断：VIC3/EU5 使用 `.metadata/metadata.json` 对应的 `content_load.json`，其他游戏使用 `descriptor.mod` 对应的 `dlc_load.json`，参见 `ParadoxGameJsonImporter.kt:20-30` 与 `ParadoxGameJsonExporter.kt:18-30`。
- 导出只处理启用的模组；官方启动器 JSON 与 DB 导出会排除 `ParadoxModSource.Local` 的本地模组；导入会跳过已存在的模组目录，结果包含 `warning` 与 `actualTotal`，UI 会按结果发通知，参见 `ParadoxLauncherJsonV2Exporter.kt:30`、`ParadoxLauncherDbExporter.kt:50` 与 `ParadoxModDependenciesImportPopup.kt:70`。
- 数据库导出通过 `knex_migrations` 中的 `modifyPositionToInteger` 判断 V4+，再决定 `position` 写入格式；文档可以补充这一检测点，参见 `ParadoxLauncherDbExporter.kt:33-42`。
- `ParadoxDbBasedModExporter.isAvailable` 受 `ChronicleCapabilities.General.includeSqlite` 控制，`ParadoxJsonBasedModExporter.isAvailable` 恒为 `true`；这决定了导入/导出弹窗中哪些条目可见，文档应补充能力门控。
- 本周期唯一实质代码变动是 `34e578eef9`：JSON 导入/导出统一改用 `JsonService.read/write`，从而支持 UTF-8 BOM；文档应补充“读取兼容 BOM”的行为，参见 `ParadoxJsonBasedModImporter.kt:27` 与 `ParadoxJsonBasedModExporter.kt:27`。
- 文档没有提到 `SpecialPathService` 用于定位游戏数据目录、Steam 创意工坊目录与默认 `playlists` 目录，建议补充这一依赖。

**结论。**

- 文档主体结构仍然准确，但需要补充自动探测、游戏类型分支、本地模组排除、能力门控、V4+ 检测与 BOM 兼容；属于小幅到中等幅度的增补。

### 2.6 待验证与风险清单

- `CwtInlineScriptFileConfigContextProvider` 对注入文件直接返回 `null`，Markdown 代码围栏内的内联脚本文件上下文是否完全不可用，当前没有专门测试；按代码路径推断是受限的，建议后续用 `MarkdownTestBase` 增加一个 `common/inline_scripts/...` 围栏用例验证。
- `ParadoxParameterSupport.findContext` 的 `findTopHostElementOrThis` 最终停在 `MarkdownCodeFence` 时，`parentDefinition()` 与 `containingFile as? ParadoxScriptFile` 都可能失败；围栏内参数声明解析需要专门测试才能确认是否完全不可用。
- p20 的参数值用法高亮问题仍标记为“待验证”；`IdentifierHighlightingComputerCodeInjector` 只是局部缓解，不能视为平台缺陷已修复。
- `DocumentWindowImpl.getShreds`、`InjectLanguageAction.findInjectionHost`、`IdentifierHighlightingComputer` 等反射/代码注入点对 263 平台内部实现敏感；升级平台基线时应优先回归这些点。
- `ParadoxScriptSpellcheckerStrategy.argumentTokenizer` 使用错误词元类型、`ChronicleIcons.Configs.ExtendedComplexEnumValue` 指向不存在资源、`ChronicleIconMappings.json` 存在 11 条失效映射，这三项都是本次只读审计发现的源码/资产缺陷，不应在文档中写成已实现行为。
- Tiger 的 eu5/hoi4 provider 已注册但 UI 未暴露配置；当前有效支持仍是 ck3/ir/vic3，设计文档应明确该差异，避免把预留 provider 写成已支持工具。

### 2.7 参考来源

- 代码：`src/main/kotlin/icu/windea/pls/lang/injection/**`、`src/main/kotlin/icu/windea/pls/extensions/markdown/**`、`src/main/kotlin/icu/windea/pls/model/injection/**`、`src/main/kotlin/icu/windea/pls/ep/resolve/config/CwtConfigContextProviders.kt`、`src/main/kotlin/icu/windea/pls/ep/resolve/parameter/ParadoxParameterSupports.kt`、`src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxInlineScriptService.kt`、`src/main/kotlin/icu/windea/pls/inject/injectors/**`。
- 设计文档：`documents/design/script-injection.md`、`icon-design.md`、`color-scheme.md`、`spellchecking.md`、`tool-integration-tiger.md`、`mod-import-export.md`。
- 问题笔记：`documents/notes/issues/p03-inline-script-injection-freeze.md`、`p20-injected-parameter-usage-highlighting.md`、`p21-inline-script-inspections-oom.md`、`p22-markdown-find-usages-unexpected-behavior.md`。
- 路线图与测试规范：`documents/notes/roadmap/v3.1.0.md:58-62`、`documents/notes/guidelines/testing.md:58-70`。
- 用户参考：`docs/zh/extensions.md:110-174`、`docs/zh/integrations.md:112-121`、`docs/zh/ref-config-format.md:1205-1376`。
- GitHub Issues：`#60`（转义多行字符串，仍开放）、`#153`（嵌套内联脚本局部变量，已关闭）、`#244`（递归内联脚本性能，已关闭）、`#289`（内联重构未替换参数，已关闭）、`#412`（内联脚本变量跳转回归，已关闭）、`#418`（本地化文本渲染/日志命令，已关闭）、`#429`（VIC3 参数检测，已关闭）、`#438`（JSON BOM，已关闭）。
