# 探索_8_pls-lang.xml

> 时间：2025-08-27 16:25（本地）
> 来源文件：`src/main/resources/META-INF/pls-lang.xml`
> 目的：聚合并声明跨语言（CWT/Script/Localisation/CSV）的通用语言能力、索引/搜索、提示、导航、重构、文档与动作入口。

---

## 一、扩展点定义（icu.windea.pls.search）

- 扩展点：`configSymbolSearch`、`localScriptedVariableSearch`、`globalScriptedVariableSearch`、`definitionSearch`、`localisationSearch`、`syncedLocalisationSearch`、`filePathSearch`、`dynamicValueSearch`、`complexEnumValueSearch`、`defineSearch`、`inlineScriptUsageSearch`、`parameterSearch`、`localisationParameterSearch`
  - 接口：`com.intellij.util.QueryExecutor`，`dynamic="true"`
- 对应实现（在 `<extensions defaultExtensionNs="icu.windea.pls.search">` 中注册）：`CwtConfigSymbolSearcher`、`Paradox*Searcher` 系列。

> 结论：为“按语义要素搜索”提供统一扩展点命名空间，并在本文件集中完成实现注册。

---

## 二、平台扩展（com.intellij）

- 设置与库
  - `applicationConfigurable`：`pls` → `PlsSettingsConfigurable`
  - `additionalLibraryRootsProvider`：`ParadoxLibraryProvider`
  - `moduleRendererFactory`：`ParadoxModuleRenderFactory`
  - `fileTypeOverrider`（order="first"）：`ParadoxFileTypeOverrider`
  - `utf8BomOptionProvider`：`ParadoxUtf8BomOptionProvider`
  - `vfs.asyncListener`：`ParadoxFileListener`
- 项目视图与图标
  - `projectViewPane`：`CwtConfigFilesViewPane`、`ParadoxFilesViewPane`
  - `projectViewNodeDecorator`：CWT/Paradox 各 1 个
  - `fileIconProvider`（order="first"，CWT/Paradox）与 `openapi.vcs.changes.ui.filePathIconProvider`
  - `iconMapper`：`PlsIconMappings.json`
- 类型/声明/定位
  - `qualifiedNameProvider`
  - `codeInsight.typeInfo`：`CWT`、`PARADOX`、`PARADOX_LOCALISATION`
  - `typeDeclarationProvider`：CWT/Paradox
  - `codeInsight.containerProvider`、`targetElementEvaluator`
  - `declarationRangeHandler`（key=`ParadoxScriptProperty`）→ `ParadoxDefinitionDeclarationRangeHandler`
  - `codeInsight.gotoSuper`（`PARADOX_SCRIPT`）
- 补全/高亮/注解
  - `completion.contributor`：CWT/Script/Localisation/CSV（具体 Provider 在代码中，XML 中以注释列出）
  - `readWriteAccessDetector`、`usageTypeProvider`：CWT/Paradox
  - `annotator`：Script/Localisation/CSV
- Inlay 提示（脚本/本地化/CSV）
  - 脚本：脚本变量、定义信息、定义引用信息/HintText、定位本地化引用、修饰符图标/本地化名、复杂枚举值信息/HintText、动态值信息/HintText、作用域上下文
  - 本地化：图标、引用（默认禁用，`settingsKeyId="ParadoxLocalisationReferenceHintsSettingsKey"`）
  - CSV：定义引用信息/HintText、复杂枚举值信息/HintText
- 行标记/参数信息/模板
  - `lineMarkerProvider`：Script、本地化
  - `codeInsight.parameterInfo`：脚本参数信息
  - `codeInsight.template.postfixTemplateProvider`：后缀模板
- 文档/用法/折叠/结构
  - `platform.backend.documentation.*`：CWT/Paradox 的 `psiTargetProvider` 与 `linkHandler`
  - `findUsagesHandlerFactory`
  - `lang.foldingBuilder`：脚本/本地化（含脚本变量引用折叠）
- 导航聚合
  - `gotoSymbolContributor`：配置符号、定义、脚本变量、本地化、同步本地化
- 重构（支持与命名）
  - `lang.refactoringSupport`、`ParadoxRefactoringSettings` 服务
  - `elementDescriptionProvider`、`nameSuggestionProvider`
  - `renameInputValidator`：脚本变量/本地化属性/参数/本地化参数
  - `automaticRenamerFactory`：相关本地化/图片、生成的修饰符（名称/描述/图标）
  - `inlineActionHandler`：脚本变量/脚本化触发/效果/内联脚本、本地化
  - `refactoring.extractIncludeHandler`：引入本地/全局脚本变量
- 层级/桩/索引
  - `typeHierarchyProvider`、`callHierarchyProvider`
  - `languageStubDefinition`：Script/Localisation；`stubElementRegistryExtension`：对应注册表
  - `stubIndex`：脚本变量名、定义名/类型、本地化名（含 Modifier/Event/Tech）、同步本地化名、定义名（TextFormat）
  - `fileBasedIndex`：`CwtConfigSymbolIndex`、`ParadoxFilePathIndex`、`ParadoxFileLocaleIndex`、`ParadoxMergedIndex`、`ParadoxDefineIndex`、`ParadoxInlineScriptUsageIndex`
- 搜索/引用/路径/注入
  - `searchScopesProvider`、`definitionsScopedSearch`：定义/脚本变量/本地化/文件实现搜索
  - `referencesSearch`：配置符号/配置引用、定义/本地化/文件/参数/本地化参数
  - `psi.referenceContributor`：CWT/Script/Localisation/CSV
  - `pathReferenceProvider`（order="first"）：`ParadoxPathReferenceProvider`
  - `multiHostInjector`：`ParadoxScriptLanguageInjector`
- 生命周期与通知
  - `notificationGroup id="pls"`
  - `postStartupActivity`：`PlsLifecycleListener`
  - `psi.treeChangePreprocessor`：`ParadoxPsiTreeChangePreprocessor`

> 结论：本文件集中注册“跨语言与跨领域”的所有 IDE 能力，覆盖索引、搜索、提示、重构与导航的关键路径。

---

## 三、Registry Key（可运行时调整）

- `pls.settings.refreshOnProjectStartup = true`
- `pls.settings.locFontSize = 18`
- `pls.settings.locTextIconSizeLimit = 36`
- `pls.settings.textLengthLimit = 36`
- `pls.settings.iconHeightLimit = 36`
- `pls.settings.defaultScriptedVariableName = "var"`
- `pls.settings.maxDefinitionDepth = 5`
- `pls.settings.presentableTextLengthLimit = 36`
- `pls.settings.itemLimit = 5`
- `pls.settings.maxImageSizeInDocumentation = 300`

> 作用：控制文档/提示/展示等行为与性能边界。

---

## 四、监听器与事件

- ApplicationListeners：
  - `PlsLifecycleListener` 订阅 `AppLifecycle` 与 `DynamicPlugin`。
  - 针对“默认游戏目录/类型、配置目录/仓库地址、模组类型/设置、游戏设置、根信息”等的变更监听，用于刷新索引、更新库与编辑器通知。
- ProjectListeners：
  - `ParadoxPsiTreeChangePreprocessor$Listener` 监听 `DumbModeListener`。

> 结论：形成“设置/目录变化 → 库/索引刷新 → 编辑器提示”的运行时闭环。

---

## 五、动作（Actions）与菜单

- 文档语言切换：`ChangeQuickDocLocalisationLocaleAction` / `Reset...`（加入 `Documentation.PrimaryGroup`）。
- 外部集成：`OpenInSteam` / `OpenInSteamWebsite`；复制路径/URL 系列；`GoToPath.*`（加入 `FileChooserToolbar`）。
- 导航聚合组 `Pls.GotoGroup`：
  - `GoToFiles`（Ctrl+Alt+F）
  - `GoToDefinitions`（Ctrl+Alt+O）
  - `GoToLocalisations`（Ctrl+Alt+O，同组合键；上下文区分）
  - `GoToRelated*`：Definitions/Localisations（Alt+Shift+P）、Images（Ctrl+Alt+Shift+P）、Configs（Alt+P，含鼠标手势）
- 生成：`GenerateLocalisations*`（加入 `GenerateGroup`）
- 重构：`IntroduceLocalScriptedVariable`（Ctrl+Alt+V）、`IntroduceGlobalScriptedVariable`（Ctrl+Alt+G）
- Diff：Compare Files/Definitions/Localisations（Ctrl+Shift+Alt+F/D/L）
- 层级：Definition/Call Hierarchy 的上下文菜单与操作集
- 本地化/表格操作：替换/生成本地化、CSV 表格列/行增删移等；集中聚合到 `Pls.Tools` 并加入 `ToolsMenu`、`EditorPopupMenu`、`ProjectViewPopupMenu`、`Images.EditorPopupMenu`
- 设置入口：`OpenGameSettings`、`OpenModSettings`（Ctrl+Alt+M）

> 结论：动作体系将常用导航、生成、重构与工具操作聚合到统一菜单与快捷键体系。

---

## 六、与其他 include 的关系（定位）

- 语言定义细节分别在：`pls-cwt.xml`、`pls-script.xml`、`pls-localisation.xml`、`pls-csv.xml`。
- 本文件承担“跨语言聚合与运行时”职责：索引/搜索/提示/导航/重构/文档/动作/监听等统一声明与注册。

---

## 七、注意点与建议

- 若发现图标/路径引用异常，检查 `iconMapper` 与 `fileIconProvider(order="first")` 的覆盖关系。
- 本地化引用的 Inlay 默认关闭，可在设置中启用（`ParadoxLocalisationReferenceHintsSettingsKey`）。
- 两个 `Ctrl+Alt+O`（Definitions/Localisations）可能在某些 keymap 下产生冲突，需依场景分配或用户自定义。
- 大量索引（Stub/FileBasedIndex）可能影响初次扫描性能，建议按需要启用项目范围或排除无关目录。

---

## 八、下一步建议

- 继续阅读：
  - `src/main/resources/META-INF/pls-ep.xml`（扩展点/服务汇总）
  - `src/main/resources/META-INF/pls-inspections.xml`（检查与快速修复）
