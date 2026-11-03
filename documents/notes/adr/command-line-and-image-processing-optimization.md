## 优化命令行执行逻辑和图片处理逻辑

> - 模块：后端 > 通用
> - 版本：v3.0.5
> - 日期：2026-10-27 ~ 2026-11-03
> - 状态：已完成（告一段落）
> - 另见：`documents/notes/adr/command-line-and-image-processing-optimization.md`

## 说明

为了避免意外的、可能的 IDE 冻结，需要优化插件代码中的命令行执行逻辑（以及图片处理逻辑）。
尽可能地遵循平台开发的最佳实践，沿用平台提供的相关基础设施，并在必要时自行创建相关基础设施。

另外，也要考虑重构插件代码中的命令行执行逻辑。
例如直接委托给平台提供的相关基础设施，直接传入命令分组，等等。

相关 Issue：
- [#396](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/396)

## 笔记

### 计划

- [x] P2 优化命令行执行逻辑和图片处理逻辑
  - 适用取消检查和超时限制，以避免可能的 IDE 冻结。
  - 避免在 EDT 中直接执行（预期存在一定耗时的）命令行，以避免可能的 IDE 冻结。
  - 执行命令行时，通常需要考虑进行取消检查（抵达时间间隔阈值时）。
  - 执行命令行时，通常需要考虑指定超时。
  - 进行图片IO（渲染/转换/等等）时，也需要考虑进行取消检查（抵达字节大小阈值时）。
- [x] P2 优化插件自身提供的 `CommandLineExecutor` API
  - 考虑默认委托给平台提供的 `GeneralCommandLine`/`CapturingProcessHandler`，从而有可能通过传入 `ProgressIndicator` 来进行取消检查。

### 范围

主要涉及的接口和语言功能：
- 内嵌提示（部分内嵌提示可能会渲染图标） - 内嵌提示的刷新逻辑位于 EDT 中
- 快速文档（部分快速文档可能会渲染图片/图标） - 快速文档的计算逻辑位于单独的 readAction 中，因此相对来说影响不大
- 基于外部检查工具的代码检查（`ChronicleTigerLintInspection`） - 需要考虑进行取消检查
- GIT 命令相关（`ChronicleGitService`）
- 特殊路径相关（`SpecialPathService`）
- 图片预览（在编辑器中打开 DDS/TGA 图片）

目标（主要）：
- 重构命令行执行的基础设施（统一入口、取消检查、超时限制、命令数组，等等） - `ProcessExecutor` 等
- 重构图片处理的基础设施（取消检查、可能的异步渲染） - `ImageToolService` `ImageService` 等
- 重构相关代码，从而确保解决 Issue [#396](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/396)
- 重构相关代码，从而尽力确保命令行执行发生在正确的上下文中（非 EDT、非 readAction）
- 应用这些重构后的基础设施，重构和迁移相关代码

目标（次要）：
- 重构图片处理中涉及语义的那部分解析逻辑 - `ParadoxImageManager` `ParadoxImageService` `ParadoxImageResolveResult` 等
- 重构相关代码，从而尽力解决一些向东容易处理的次要问题
- 补充测试（包括使用特定断言逻辑的回归测试、针对常见场景的冒烟测试、可自动下载相关工具到 build 目录的进阶测试）

非目标：
- 移除图片处理相关的代码注入器（如 `DDSImageReaderCodeInjector`） - 因为需要支持直接在编辑器中渲染 DDS/TGA 图片，目前无法这样做
- 内置图片解析代码，移除对外部库的依赖，移除相关的代码注入器 - 尽管这是更理想的选择，但是会带来更多的复杂性，存在无法避免的调研、维护和测试成本

需要评估：
- 内置所有图片工具（以及检查工具），或者提供便捷入口以在使用插件时一键下载对应的工具 - 完成主要和次要目标后，再另行评估

### 堆栈分析

关键调用堆栈：
```
icu.windea.pls.core.execution.CommandLineExecutor
-icu.windea.pls.core.executeCommandLine
--icu.windea.pls.base.io.ChronicleGitService
--icu.windea.pls.lang.tools.SpecialPathService
--icu.windea.pls.integrations.images.providers.CommandBasedImageToolProvider - 图片处理工具
---icu.windea.pls.integrations.images.ImageToolService.read - 图片渲染
----icu.windea.pls.inject.injectors.DDSImageReaderCodeInjector / icu.windea.pls.inject.injectors.TGAImageReaderCodeInjector
---->icu.windea.pls.images.ImageService.convertImageFormat
---->>icu.windea.pls.images.actions.ConvertImageFormatAction - 用于转换图片格式的动作
---->icu.windea.pls.images.ImageService.sliceImage
---->-icu.windea.pls.lang.util.ParadoxImageManager.doCreateImageFile
---->icu.windea.pls.lang.util.ParadoxImageManager.doCreateImageFile - 通过 `ImageIO.write` 写入图片
---->-icu.windea.pls.lang.util.ParadoxImageManager.doResolveUrl - 解析图片地址
---icu.windea.pls.integrations.images.ImageToolService.convertImageFormat
--icu.windea.pls.integrations.lints.providers.CommandBasedLintToolProvider - 检查工具
---icu.windea.pls.integrations.lints.providers.TigerLintToolProvider.validateRootDirectory
----icu.windea.pls.integrations.lints.TigerLintToolService.resolveTigerLintResultForRootDirectory
-----icu.windea.pls.integrations.lints.TigerLintToolService.getTigerLintResultForRootDirectoryFromCache
------icu.windea.pls.integrations.lints.TigerLintToolService.resolveTigerLintResultForFile
-------icu.windea.pls.integrations.lints.TigerLintToolService.resolveTigerLintResultForFileFromCache
--------icu.windea.pls.integrations.lints.TigerLintToolService.getTigerLintResultForFile
---------icu.windea.pls.lang.inspections.lints.ChronicleTigerLintAnnotator.doAnnotate - 外部注解器
--------->icu.windea.pls.lang.inspections.lints.ChronicleTigerLintInspection - 基于外部注解器的代码检查
--icu.windea.pls.config.config.CwtConfigService.getGameTypeFromRepoFile
```

图片处理工具和检查工具可能是基于命令行的，而当前的命令行执行逻辑可能导致意外的 IDE 冻结。

---

来自 #396 的关键调用堆栈：

```
com.intellij.diagnostic.Freeze
	at java.base@25.0.3/java.lang.ProcessImpl.waitForInterruptibly(Native Method)
	at java.base@25.0.3/java.lang.ProcessImpl.waitFor(ProcessImpl.java:555)
	at icu.windea.pls.core.execution.CommandLineExecutor.doExecute(CommandLineExecutor.kt:73)
	at icu.windea.pls.core.execution.CommandLineExecutor.execute(CommandLineExecutor.kt:59)
	at icu.windea.pls.integrations.images.providers.TexconvToolProvider.doConvertImageFormat(TexconvToolProvider.kt:90)
	at icu.windea.pls.integrations.images.providers.TexconvToolProvider.convertImageFormat(TexconvToolProvider.kt:44)
	at icu.windea.pls.integrations.images.providers.CommandBasedImageToolProvider.convertImageFormat(CommandBasedImageToolProvider.kt:50)
	at icu.windea.pls.integrations.images.providers.CommandBasedImageToolProvider.read(CommandBasedImageToolProvider.kt:37)
	at icu.windea.pls.integrations.images.ImageToolService.read(ImageToolService.kt:22)
	at icu.windea.pls.inject.injectors.DDSImageReaderCodeInjector.read(DDSImageReaderCodeInjector.kt:25)
	at java.base@25.0.3/java.lang.invoke.DirectMethodHandle$Holder.invokeSpecial(DirectMethodHandle$Holder)
	at java.base@25.0.3/java.lang.invoke.LambdaForm$MH/0x0000000017008000.invoke(LambdaForm$MH)
	at java.base@25.0.3/java.lang.invoke.LambdaForm$MH/0x000000001160e000.invokeExact_MT(LambdaForm$MH)
	at java.base@25.0.3/jdk.internal.reflect.DirectMethodHandleAccessor.invokeImpl(DirectMethodHandleAccessor.java:157)
	at java.base@25.0.3/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base@25.0.3/java.lang.reflect.Method.invoke(Method.java:565)
	at icu.windea.pls.inject.CodeInjectorContext.applyInjection(CodeInjectorContext.kt:169)
	at java.base@25.0.3/java.lang.invoke.DirectMethodHandle$Holder.invokeStatic(DirectMethodHandle$Holder)
	at java.base@25.0.3/java.lang.invoke.LambdaForm$MH/0x0000000016a80000.invoke(LambdaForm$MH)
	at java.base@25.0.3/java.lang.invoke.Invokers$Holder.invokeExact_MT(Invokers$Holder)
	at java.base@25.0.3/jdk.internal.reflect.DirectMethodHandleAccessor.invokeImpl(DirectMethodHandleAccessor.java:158)
	at java.base@25.0.3/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base@25.0.3/java.lang.reflect.Method.invoke(Method.java:565)
	at com.twelvemonkeys.imageio.plugins.dds.DDSImageReader.read(DDSImageReader.java)
	at com.intellij.ui.icons.IconUtilKt.loadRasterImage(iconUtil.kt:234)
	at com.intellij.ui.icons.IconUtilKt.loadPngFromClassResource(iconUtil.kt:195)
	at com.intellij.ui.icons.ImageCacheKt.doLoadByDescriptor(imageCache.kt:250)
	at com.intellij.ui.icons.ImageCacheKt.loadByDescriptorWithoutCache(imageCache.kt:148)
	at com.intellij.ui.icons.ImageCacheKt.loadImage(imageCache.kt:111)
	at com.intellij.ui.icons.ImageCacheKt.loadImage$default(imageCache.kt:64)
	at com.intellij.ui.icons.ImageDataByUrlLoader.loadImage(ImageDataByUrlLoader.kt:24)
	at com.intellij.ui.icons.CachedImageIcon.loadImage-Xbo1f9k$intellij_platform_util_ui(CachedImageIcon.kt:386)
	at com.intellij.ui.icons.ScaledIconCache.loadIcon-GvFvY-8(ScaledIconCache.kt:66)
	at com.intellij.ui.icons.ScaledIconCache.getCachedIcon-BVbfYdY(ScaledIconCache.kt:47)
	at com.intellij.ui.icons.CachedImageIcon.resolveActualIcon(CachedImageIcon.kt:196)
	at com.intellij.ui.icons.CachedImageIcon.getIconWidth(CachedImageIcon.kt:157)
	at com.intellij.codeInsight.hints.presentation.ScaledIconPresentation.getWidth(ScaledIconPresentation.kt:32)
	at com.intellij.codeInsight.hints.presentation.InsetPresentation.getWidth(InsetPresentation.kt:24)
	at com.intellij.codeInsight.hints.presentation.StaticDelegatePresentation.getWidth(StaticDelegatePresentation.kt:15)
	at com.intellij.codeInsight.hints.presentation.StaticDelegatePresentation.getWidth(StaticDelegatePresentation.kt:15)
	at com.intellij.codeInsight.hints.presentation.StatefulPresentation.getWidth(StatefulPresentation.kt:64)
	at com.intellij.codeInsight.hints.presentation.StaticDelegatePresentation.getWidth(StaticDelegatePresentation.kt:15)
	at com.intellij.codeInsight.hints.presentation.InsetPresentation.getWidth(InsetPresentation.kt:24)
	at com.intellij.codeInsight.hints.presentation.ScaledDelegatedPresentation.getWidth(ScaleAwarePresentationFactory.kt:116)
	at com.intellij.codeInsight.hints.presentation.InsetPresentation.getWidth(InsetPresentation.kt:24)
	at com.intellij.codeInsight.hints.presentation.StaticDelegatePresentation.getWidth(StaticDelegatePresentation.kt:15)
	at com.intellij.codeInsight.hints.presentation.StaticDelegatePresentation.getWidth(StaticDelegatePresentation.kt:15)
	at com.intellij.codeInsight.hints.presentation.DynamicInsetPresentation.getWidth(DynamicInsetPresentation.kt:28)
	at com.intellij.codeInsight.hints.presentation.InsetPresentation.getWidth(InsetPresentation.kt:24)
	at com.intellij.codeInsight.hints.presentation.ScaledDelegatedPresentation.getWidth(ScaleAwarePresentationFactory.kt:116)
	at com.intellij.codeInsight.hints.presentation.StaticDelegatePresentation.getWidth(StaticDelegatePresentation.kt:15)
	at com.intellij.codeInsight.hints.presentation.RecursivelyUpdatingRootPresentation.getWidth(RecursivelyUpdatingRootPresentation.kt:56)
	at com.intellij.codeInsight.hints.LinearOrderInlayRenderer.calcWidthInPixels(LinearOrderInlayRenderer.kt:88)
	at com.intellij.openapi.editor.impl.InlineInlayImpl.doUpdate(InlineInlayImpl.java:79)
```

部分内嵌提示（如 `ParadoxDefinitionHintTextHintsProvider`）中可能需要渲染图标（渲染本地化文本时，也需要渲染其中的图标）。
刷新内嵌提示时，平台内部的处理逻辑会尝试计算图标宽度（`CachedImageIcon.getIconWidth`），而这又会导致加载图片缓存（`ImageCacheKt.loadImage`）。

如果在插件的集成设置中，启用了 texconv/magick（`ChronicleIntegrationsSettings.ImageState`），
插件会改为通过命令行执行的方式读取图片（DDS/TGA），而这里的命令行执行逻辑可能因为图片过大或者其他意外原因，花费比预期更长的时间，最终导致 IDE 冻结。
否则，插件会基于 TwelveMonkeys ，通过原生的 ImageIO 读取图片，如果图片过大，ImageIO 的读写也可能花费比预期更长的时间，最终也可能导致 IDE 冻结。

目前来看，需要重点优化的仍然是命令行执行逻辑。
对于图片处理逻辑，也要尝试优化，而这可能引申出一些超出范围的重构任务。
比如：由插件内置解析代码，移除对外部库的依赖，移除相关的代码注入器（这里的代码注入器实在是让人难受……）。

### 补充

内嵌提示的刷新逻辑位于 EDT 中，而在 EDT 中进行取消检查是没有意义的。
因此需要考虑异步操作（异步加载、异步刷新、批量、防抖），而这又会到来额外的复杂性。

### 参考

- `com.intellij.codeInspection.ex.GlobalInspectionContextImpl.runTools` L345
  - 这里声明了一个 `ProgressIndicator`。
  - 也就是说 `com.intellij.lang.annotation.ExternalAnnotator.doAnnotate` 的调用链上层是有 indicator 的，但不能直接获取。
  - 由于 indicator API 已经过时，如果需要明确的状态指示，建议的方式可能仍然是 `runBlockingCancellable` + `withBlackgroundProgresss`。

### 探讨

关于初步的任务计划：
- 命令行执行逻辑的相关重构：弃用 `CommandLineService` 等，新增 `ExecutionService`，提供存在多种变体的方法 `execute`
  - 例如：`ExecutionService.execute(commandLine: GeneralCommandLine): ProcessOutput`
- 解决卡死与隐患（通过 metadata 获取图片尺寸，图片无需切分时也缓存到磁盘，等等）
  - 需要提取 `icu.windea.pls.images.ImageMetadataService` 以统一地获取图片的元数据（获取的应是原始的尺寸）
- 命令行执行的进一步重构和优化（不超出当前任务范围）
  - 一些次要的问题修复和代码重构
- 图片处理的进一步重构和优化（不超出当前任务范围）
  - 一些次要的问题修复和代码重构
- 进一步的清理和重构（包括人工修订）
- 补充测试（考虑包括使用特定断言逻辑的回归测试、针对常见场景的冒烟测试、可自动下载相关工具到 build 目录的进阶测试）
- 作为总结，补充 ADR（两份：AI编写 + 人工编写）

备注：
- 目前不能移除注入器，因为我们需要通过 ImageIO + SPI 提供图片预览（直接在编辑器中打开 DDS/TGA 图片）。
- 需要保证图片渲染是在后台进程中执行的，且采用更好的缓存策略（暂时不处理由插件代码处理的，异步渲染逻辑）。
- 对于内嵌提示中的图标，可以考虑首先改为直接获取缓存后的图标（如 `IconUtil.createImageIcon`），而不是一个延迟加载的图表（如 `CachedImageIcon`）。之后再考虑异步加载策略。
- 注入器中本身可能并不需要EDT断言，因为 `OSProcessHandler.checkEdtAndReadAction` 中已经存在更严格的断言。但是这个断言在一些情况下可能会被忽略，而我们可能仍然需要给出警告。
- 这里涉及的一些常量可以首先作为 kotlin 常量，以后再考虑是否作为能力项（`ChronicleCapabilities`）或配置项。

关于命令行执行的断言问题：
- `OSProcessHandler.checkEdtAndReadAction` 中存在额外的断言策略。由于开发环境和用户环境是有可能启用 internal 模式的。我们需要尽可能兼容这里的断言策略。
- 这意味着在一些地方，比如内嵌提示、快速文档、tiger等，我们需要保证命令行执行既不发生在 EDT 中，也不发生在 readAction 中。
- 我们目前有一些地方并不符合这样的断言策略，因此后续还需要考虑确保是异步执行的（考虑使用 `withBackgroundProgress`）。
- 参见 `ParadoxImageManager`。

关于命令行执行的入口：
- 理想的方案是仅提供一个（或者少数几个）入口，而如果采用 `ExecutionService.execute(...)`，由于存在多种参数组合，难以避免存在多个入口。
- 更合适的方案是，不再新增 `ExecutionService`，仅提供 `ProcessExecutor(commandLine)` 最为唯一入口。
- 之后可以再通过类似 `.withTimeout(...)` 的方式，添加更多配置。

关于图片渲染与图片解析：
- 对于基础设施层的图片渲染与解析逻辑（如 `ImageService`），应当采用合适的缓存策略（基础缓存策略+过期/上限），即使其底层是基于命令行的。
- 对于语义层的图片渲染与解析逻辑（如 `ParadoxImageService`），应当支持懒加载（例如仅直接返回 resolveResult，而不会直接写入图片）。

关于“基于策略的实现细节”：
- 这里涉及到了一些可能需要准备多种“策略”的代码实现。
- 目前来说，最理想的方式是，在 `icu.windea.pls.model.policies` 提供对应的“策略/policy”枚举，并且允许在插件设置页面自定义。
- 但是这相对来说不是那么重要，因此我们可以将其作为“需要评估的后续任务”。一开始仅“记录决策、添加注释、预先封装”，到时候再进一步优化即可。

关于“策略/policy”枚举：
- 策略枚举应统一位于 `icu.windea.pls.model.policies` 中。它们可以涉及不同的模块/层次。
- `Paradox` 前缀意味着是“（需要依赖游戏/模组文件的）语义的”，然而有些策略是直接发生在基础设施层的（`images` 模块），并且未来可能会“扩展语义”。
- 因此更合适的命名应分别是：
  - `ImageCacheCleanupPolicy`
    - 这直接发生在基础设施层，而缓存计算策略可能取决于具体场景。
  - `ImageRenderFailurePolicy`
    - 这适用于快速文档。但也可能适用于内嵌提示（同步加载的情况，并且仅适用于部分策略选项）。
  - `ImageRenderPolicy` - `Sync` + `Async`，默认 `Sync`。
    - 我们预期内嵌提示中要渲染的图片仅包括“文本图标”，并且存在“尺寸限制”。
    - 因此直接发生在 `ParadoxHintsProvider.collectFromElement` 中是可行的，但这本身可能发生在 readAction 中，可能需要另开线程/协程来渲染。
    - 超时设置可以与 `iconHeightLimit` 对齐，参见 `ParadoxHintsSettings`。

## 探讨（修订与收尾）

关于后续优化：
- 与 `imagePolicy` 相关的优化和测试
  - 涉及 `ImageCacheCleanupPolicy` `ImageRenderFailurePolicy` `ImageRenderPolicy`
  - 在 `icu.windea.pls.base.settings` 中新增 `ChronicleImageSettings` 和 `ChronicleImageSettingsConfigurable`，提供必要的配置项
  - 实现每一种 `imagePolicy` 的选项的具体逻辑。
  - 补充专门的测试，以验证这些具体逻辑的行为是正确的。
- 图片处理（渲染/读取/写入）时的取消检查
  - 在理想情况下，进行图片IO/读写时，也需要进行取消检查（例如，抵达字节大小阈值时）。
  - 也需要补充专门的测试。

关于后续测试：
- 与 `ExternalToolDownloadService` 相关的测试
  - 提供 `icu.windea.pls.integrations.ExternalToolDownloadService`
  - 其中的 check 方法要求指定路径为空或不存在
  - 其中的 download 方法是 suspend 方法，通过 `reportProgressScope` 提供妥善的状态指示（下载地址/进度/等等），不预先绑定到 `withBackgroundProgress`/`withModalProgress`
  - 暂时不在生产代码中使用这两个 `toolDownloadService`，先补充测试
  - 需要补充测试，验证各种外部工具能够正确地下载（在 `build/external-tools` 目录下给定一个临时目录作为下载位置，它们在不同测试用例之间应是统一的）
    - 测试代码中的外部工具下载逻辑是统一的，因此，可以在测试代码中再封装 `LintToolDownloadTestService` 等
  - 需要补充测试，验证各种外部工具能通过冒烟测试（例如 `--version` 命令），直接使用 `ProcessExecutor` 验证。
    - 这要求首先下载对应的外部工具。
  - 需要补充测试，验证在各种策略下（TwelveMonkeys/texconv/magick），图片转换操作能够符合预期地成功/失败。
    - 使用 `ImageToolProvider`。
    - 这要求在测试时，临时更改 `ChronicleIntegrationSettings` 中的配置项。
    - 这要求首先下载对应的外部工具。
- 为 `ChronicleGitService` 与 `ChronicleSqliteService` 补充测试。
  - 这要求需要一个测试环境/测试数据。
  - 一个可能的方案是，在 `build` 目录下给定一个独特的临时目录（如 `build/tmp/base-io-git-test`），在测试时自行创建、构建和更新测试环境/测试数据。
  - 包括必要的冒烟测试和一些常见场景的测试。
- issue #396 回归测试
  - 这涉及内嵌提示中的图片渲染，以及对上下文状态（EDT / readAction）的断言。
  - 相较于人工验证，基于测试的验证可能存在一定难度。需要研究具体如何达成。
  - 需要在 ADR/注释/文档注释 中，补充关于测试的实现思路和代码实现的更多细节。
- 验证 tiger 的检查逻辑和对应的外部代码检查（`ChronicleTigerLintInspection`）
  - 实际上我们只需要在测试时，提供一个简单的模组，同时预先下载 tiger 工具即可。
  - 相较于人工验证，基于测试的验证可能存在一定难度。需要研究具体如何达成。
  - 需要在 ADR/注释/文档注释 中，补充关于测试的实现思路和代码实现的更多细节。

### 任务计划草案（AI 协助）

> 初版计划；执行过程中在 `任务计划 （修订）` 章节记录调整。

- 阶段 1：命令行执行基础设施
  - 新增 `ProcessExecutor`，作为唯一执行入口；默认绑定当前 indicator，通过 `withIndicator`/`withoutIndicator`/`withTimeout`/`withDestroyOnTimeout` 做可变配置（与 `GeneralCommandLine` 对齐）。
  - 弃用 `CommandLineExecutor`/`executeCommandLine`/`CommandLineService`/`CommandType`；旧测试保留。
  - 迁移 Git、Texconv、Magick、Tiger、SpecialPath、CwtConfigService；移除 `isValid` 中的版本命令。
  - 执行时不硬禁止 EDT/RA，仅 warning；协程内通过 `coroutineToIndicator` 提供 indicator。
- 阶段 2：图片元数据与基础设施缓存
  - 新增 `ImageMetadataService`；统一获取原始尺寸，替换 `ImageIO.read(...).height`。
  - `ImageService`：磁盘 PNG 缓存（文件名 UUID 即索引；`@h36` 缩略图）+ 内存缓存（TTL/上限）+ `ImageCacheCleaner`。
  - 基础缓存策略：TTL 30 天、总大小 1 GiB、文件数 10k、清理间隔 1 小时。
- 阶段 3：图片语义层与渲染
  - `ParadoxImageService` 返回 `ParadoxImageResolveResult`，支持懒加载；`ParadoxImageManager` 暂时保留为 facade。
  - 快速文档：`ImageService` 线程本地 `ImageRenderSession`，在 RA 外 `awaitAll`；默认失败策略为 `AcceptBroken`。
  - 内嵌提示：默认 `ImageRenderPolicy.Sync`；缓存命中直接使用；需要渲染时在非 RA 线程执行；`Async` 后续。
  - 注入器保留；EDT/RA 时跳过外部工具并 warning。
- 阶段 4：Tiger
  - `TigerLintToolService` 使用 app-level service；`TrackingCache<TigerLintRequest, TigerLintResult>`。
  - `TigerLintResult` 增加 `request` 与 `modificationTracker`；结合 `VirtualFile` 和 `MergedModificationTracker`；不依赖 project/PSI。
  - 暂不增加 `withBackgroundProgress`；仅设置超时。
- 阶段 5：外围
  - SpecialPath 使用 JNA（回退 `reg query`）；Git 使用非交互参数 + 超时；CwtConfigService 直接读取 `.git/config`。
- 阶段 6：测试与收尾
  - 新增 `includeExternalTools`，默认启用；工具下载到 `build/external-tools`。
  - 补充单元、平台、回归、冒烟和进阶测试；清理代码；修订 ADR；后续补充 AI note。

关键决策：
- `ProcessExecutor` 采用可变配置，不是线程安全对象。
- 图片缓存文件名中的 UUID 即索引，不引入集中索引文件。
- `ImageCacheCleanupPolicy` 默认 `TtlAndSize`；`ImageRenderFailurePolicy` 默认 `AcceptBroken`；`ImageRenderPolicy` 默认 `Sync`。
- 策略枚举放在 `icu.windea.pls.model.policies`（不含 `Paradox` 前缀），文案同步补充。
- `TigerLintToolService` 为 app-level，使用 `TrackingCache`，不依赖 project/PSI。
- `ParadoxImageManager` 暂时保留，后续再评估是否拆分/重命名。
- 测试工具默认启用。

### 任务计划（AI 协助）

#### 2026-10-28 - step 1

- 完成阶段 1 的基础设施部分：新增 `ProcessExecutor`、`ExecutionTimeouts`，并将 `CommandLineExecutor`/`executeCommandLine`/`CommandLineService`/`CommandType` 标记为弃用。
- `ProcessExecutor` 默认绑定当前 indicator；提供 `withIndicator`/`withoutIndicator`/`withTimeout`/`withDestroyOnTimeout`。
- 新增 `ProcessExecutorTest`，覆盖冒烟、区分 OS、UTF-8、退出码/stderr、超时、显式 indicator 取消、当前 indicator 取消。
- 完成阶段 1 的迁移：Git、Texconv、Magick、Tiger、SpecialPath、CwtConfigService；移除 `isValid` 中的版本命令；Git 使用非交互参数 + 超时；SpecialPath 使用 JNA（回退 `reg query`）；CwtConfigService 直接读取 `.git/config`。
- 旧命令行 API 暂时保留（已弃用）；旧测试仅补充 `@Suppress("DEPRECATION")`；生产调用点已迁移到 `ProcessExecutor`。
- 将 remote URLs 的读取提取到 `ChronicleGitService.getRemoteUrls`；新增 `CwtConfigFileServiceTest` 覆盖真实项目根目录下的 `cwt/core` 识别。
- 更新 `ProcessExecutor.execute` 与 `ChronicleGitService` 的异常注解和文档注释（主要是 `ExecutionException`）。

#### 2026-10-28 - step 2

- 新增 `ImageMetadataService`，统一获取原始图片尺寸；替换内嵌提示/快速文档中的 `ImageIO.read(...).height`。
- 扩展 `ImageService`：缓存检查、同步/后台渲染、`ImageRenderSession`、`ImageCacheCleaner`。
- 新增 `ImageCacheCleanupPolicy`、`ImageRenderFailurePolicy`、`ImageRenderPolicy` 及 bundle 文案。
- 新增 `ImageMetadataServiceTest`、`ImageServiceTest`、`ImageCacheCleanerTest`。

#### 2026-10-29 - step 3

- 新增 `ParadoxImageService` 与 `ParadoxImageResolveResult`；`ParadoxImageManager` 保留为门面，解析阶段不写文件。
- 缓存路径的 UUID 基于源文件路径、大小、修改时间、帧数信息和目标格式；不引入集中索引文件。
- 快速文档在 `DocumentationTarget` 中创建 `ImageRenderSession`，在 RA 内记录渲染请求，在 RA 外等待完成后才 `toDocumentation()`；默认失败策略为 `AcceptBroken`。
- 内嵌提示使用 `ImageRenderPolicy.Sync`；缓存未命中时同步渲染，原始高度不可获取时直接跳过，不使用 `icon.iconHeight` 回退。
- `ImageToolService.read` 在 EDT/RA 中跳过外部工具并 warning，交回内置实现。
- `ImageRenderService.requestImageRender` 不再使用 `Task.Backgroundable`；同步入口委托异步入口，异步入口使用项目协程作用域 + `withBackgroundProgress`。
  - `runBlockingCancellable` 依赖当前线程已有的 Job/indicator，直接使用会在没有进度上下文的调用点抛异常，因此同步入口改为等待项目协程中的异步任务。
- 新增 `ImageRenderServiceTest` 的 `requestImageRender` 与 `ImageRenderSession` 场景。

#### 2026-10-29 - step 4 & 5

- 新增 `TigerLintRequest`；`collectInformation` 返回请求，`doAnnotate` 直接基于请求获取结果，不再在 `doAnnotate` 中开启读操作。
- `TigerLintToolService` 改为 app-level，使用 `TrackingCache<TigerLintRequest, TigerLintResult>`；不依赖 project/PSI。
- `TigerLintResult` 增加 `request` 与 `modificationTracker`；依赖 `VirtualFile`、脚本/本地化文件追踪器和设置追踪器，通过 `MergedModificationTracker` 合并。
- `TigerLintToolProvider.validateRootDirectory(request)` 不再接收 `VirtualFile`；命令执行使用 `ProcessExecutor` 和超时。
- 暂不增加 `withBackgroundProgress`；后续根据实际 UX 评估。
- 阶段 5 的外围任务（Git、SpecialPath、CwtConfigService）已在 step 1 完成，无需额外改动。

#### 2026-10-29 - step 6 - test & wrap-up（部分）

- 新增 `ExternalToolDownloadService`：`check` 判断目标为空或不存在；`download` 为 suspend，使用 `reportProgressScope` / `reportRawProgress` 报告状态。
- 下载使用 JDK `HttpClient`，自动应用 IDE 静态代理、系统环境变量代理、`ProxySelector` 和 JVM 系统属性；带连接/读取超时、最低速度检查和总时长限制。
- 下载支持直接文件、Zip、TarGz 和 SevenZip；SevenZip 在 Commons Compress 不支持多流编码器时回退到外部 `7z` / `7za` / `7zr`。
- 新增 `ExternalToolDownloadTestService`：共享 `build/external-tools`，使用 `.downloaded-tool` 标记判断版本；提供 texconv、magick、tiger 的跨 OS 描述和冒烟测试。
- 新增 `ExternalToolDownloadServiceTest`、`ExternalToolDownloadTest`、`ImageToolProviderExternalTest`。
- 支持在设置页配置 texconv 路径，默认使用 `{path}/external-tools/texconv.exe`；`TexconvToolProvider` 不再依赖内置资源。
- 新增 `ChronicleGitServiceTest`（本地临时 Git 仓库）与 `ChronicleSqliteServiceTest`（临时 SQLite 数据库）。
- 从 `ExternalToolDownloadService` 提取 `ArchiveService`、`ProxyService`、`DownloadService` 及 `ArchiveType` / `ArchiveOptions` / `DownloadOptions` / `DownloadResult`。
- 新增 `ImageFormatConversionTest` 与 `ConvertImageFormatActionTest`，覆盖 TwelveMonkeys/texconv/magick × jpg/png/dds/tga 的成功/失败组合。
- `convertImageFormat` 在 `ImageService`/`ImageToolService`/`ImageSupport`/`ImageToolProvider` 中加入同格式 NOP 处理（复制而非转换）。
- `ConvertImageFormatAction` 在 EDT 中执行转换时转到后台线程，避免外部工具被 EDT 保护跳过。
- `includeExternalTools` 默认启用；Tiger 真实模组验证、issue #396 回归和 ADR 总结仍暂缓。

#### 2026-10-30 - step 6 - regression & tiger

- 新增 `Issue396Test`：使用记录调用的测试 `ImageToolProvider`，验证 `ImageToolService` 在 EDT / readAction 中跳过外部工具，并在受限上下文之外正常调用外部工具。
  - 该测试对应 `DDSImageReaderCodeInjector.read` -> `ImageToolService.read` 的关键路径，避免内嵌提示计算图标宽度时同步执行命令行。
- 新增 `Issue396TigerLintTest` 和测试数据 `src/test/testData/issues/396/tiger-mod`：
  - 简单 CK3 模组包含 `descriptor.mod` 和一个预期存在报错的 `scripted_trigger`。
  - 使用已下载的 `ck3-tiger` 和本地 CK3 游戏目录；如果本地环境不满足则跳过。
  - 验证 `TigerLintToolService.collectRequest`、`TigerLintResult` 解析，以及 `ChronicleTigerLintInspection` 的端到端高亮。
- `TigerLintRequest` 增加 `modPath`，Tiger 命令行现在传入模组描述符（`descriptor.mod` 或 `.metadata/metadata.json`），而不是模组目录。

#### 2026-10-30 - step 7 - image policies & io cancellation

- 新增 `ChronicleImageSettings` 与图片设置页 `ChronicleImageSettingsConfigurable`（沿用现有 `Configurable` 命名），提供：
  - `ImageCacheCleanupPolicy` 及 TTL（天）、最大大小（MB）、最大文件数。
  - `ImageRenderPolicy`（`Sync`/`Async`）。
  - `ImageRenderFailurePolicy`（`AcceptBroken`/`RemoveImage`/`Placeholder`）。
- `ImageCacheCleaner` 支持全部缓存清理策略，并可通过构造参数覆盖 TTL、大小和文件数；`ImageRenderService` 定期清理时读取设置。
- 图片渲染策略：
  - `Sync`：缓存未命中时同步等待渲染。
  - `Async`：在后台渲染，不阻塞调用方；渲染成功后刷新内嵌提示。
  - 未显式指定策略时使用 `ChronicleImageSettings.state.renderPolicy`。
- 图片渲染失败策略：
  - `AcceptBroken`：保留目标 URL（接受损坏图片）。
  - `RemoveImage`：移除图片（快速文档中会移除对应的 `<img>`）。
  - `Placeholder`：使用插件生成的占位图。
  - 快速文档渲染会话会记录失败请求，并在 `awaitAll` 后统一应用失败策略。
- 图片 IO 取消检查：
  - `CancellableInputStream` / `CancellableOutputStream` 每累计 64 KiB 调用一次 `ProgressManager.checkCanceled()`。
  - `readImage` / `writeImage` / `copyImageStream` / `copyImageFile` / `ImageMetadataService` / `DefaultImageSupport` 使用上述检查。
- 新增测试：
  - `ImageCacheCleanerTest`：`None`、`ByTtl`、`BySize`、`ByTtlAndSize`。
  - `ImageRenderServiceTest`：同步/异步策略、显式策略覆盖、三种失败策略及渲染会话失败处理。
  - `IoServiceTest`：输入/输出流按字节阈值检查取消、流/路径复制。

#### 2026-11-01 - step 8 - contexts & image materialization

目标：把“解析”和“图片物化（metadata、渲染、解码、Icon 创建）”分开，彻底避免在 EDT/readAction 中触发图片 IO 或懒加载 Icon。

决策：

- 在 core/base 层引入基础上下文，避免基础上下文反向依赖 images 或 lang 模块：
  - `HintsContext`：持有 `file`、`editor`、`project`，并提供 `UserDataHolder` 能力。
  - `DocumentationContext`：持有 `element`、`originalElement`、`hint`，并提供 `UserDataHolder` 能力。
  - CWT 与 Paradox 的快速文档共用同一个 `DocumentationContext`，不再拆分两套上下文类型。
  - images 模块通过自定义 `Key` 把图片渲染会话/请求附加到上下文；基础上下文本身不依赖 images。
- 图片渲染请求仍然由 `ImageRenderService` 管理；但不再依赖 `ThreadLocal` 隐式传递渲染会话。
  - 上下文只保存请求和 session；实际渲染在后台协程中执行。
  - 同步渲染的 `ImageRenderPolicy` 不再作为内嵌提示的主路径；后续评估移除。
- 快速文档：
  - `computeDocumentation` 中先 `readAction` 收集 `DocumentationContext` 的请求，再在 RA 外等待完成。
  - 失败策略在等待完成后统一应用。
- 内嵌提示：
  - `getCollectorFor` 创建 `HintsContext` 和图片渲染会话。
  - `collectFromElement` 只记录渲染请求，并添加占位 `StatefulPresentation`。
  - 后台协程完成物化后，在 EDT 上更新占位 presentation；不再调用 `ImageMetadataService`/同步渲染/URL 懒加载 Icon。
  - 不引入全局强引用 icon 缓存；初始只使用会话内的 in-flight 去重，icon 生命周期由 presentation 持有。
- 工具方法：
  - 优先把可 suspend 的 external tool 操作改为 suspend；service 负责 dispatcher、进度、日志、上下文检查。
  - `ImageToolProvider.read` 受同步 `ImageReader` SPI 约束，不能简单 suspend；该路径继续只允许内置实现/缓存。

#### 2026-11-01 - step 9 - presentation settings

- 新增展示层设置（暂定 `ChroniclePresentationSettings`），统一快速文档与内嵌提示的文本/图片展示阈值：
  - `presentableTextLimit`
  - 快速文档：本地化字体大小、图片显示尺寸、文本图标尺寸限制
  - 内嵌提示：本地化文本长度限制、图标高度限制
- `ChronicleImageSettings` 仅保留图片处理相关项，例如渲染失败策略、缓存策略、渲染超时和源文件大小限制。
- `ParadoxHintsSettings` 只保留 provider 级开关；数字阈值从内嵌提示 provider 设置页移出。
- `ChronicleInternalSettings` 中的 Registry 阈值项已移除，由持久化设置取代，避免双重 source of truth。

#### 2026-11-01 - step 10 - suspend tool providers

- 将 lint/translation/图片转换等 external tool provider 的操作方法逐步改为 suspend。
- 已迁移：ImageToolProvider.convertImageFormat、TranslationToolProvider.translate、LintToolProvider.validateRootDirectory。
- provider 只实现具体操作，不检查 EDT/readAction；service 负责上下文检查、进度、超时和日志。
- `TigerLintToolService.collectTigerLintResult` 的 `runWithModalProgressBlocking`/`withBackgroundProgress` 分支作为显式用户操作的参考。
- `ImageToolProvider.read` 由于同步 `ImageReader` SPI 限制，保留同步语义；在 EDT/RA 中不得调用外部工具。

## 任务（AI 协助）

### 主要 & 次要

- [x] 基础设施
  - [x] 新增 `ProcessExecutor`、`ExecutionTimeouts`；弃用旧命令行 API
  - [x] 新增 `ImageMetadataService`
  - [x] 扩展 `ImageService`：缓存、渲染、`ImageRenderSession`、`ImageCacheCleaner`
  - [x] 新增 `ImageCacheCleanupPolicy`、`ImageRenderFailurePolicy` 及文案
- [x] 重构
  - [x] 迁移 Git、Texconv、Magick、Tiger、SpecialPath、CwtConfigService
  - [x] 新增 `ParadoxImageService`/`ParadoxImageResolveResult`，保留 `ParadoxImageManager` facade
  - [x] 快速文档：显式 `DocumentationContext` + `ImageRenderSession`，在 RA 外等待渲染
  - [x] 内嵌提示：`HintsContext` + 后台图片物化 + `StatefulPresentation` 占位更新
  - [x] 基础上下文使用 userData 附加图片数据，避免 core/base 依赖 images
  - [x] 展示阈值迁移到 `ChroniclePresentationSettings`；图片渲染超时迁移到 `ChronicleImageSettings`
  - [x] 清理展示/图片设置命名和描述
  - [x] 快速文档文本图标按文档文本高度缩放；内嵌提示使用 `smallScaledIcon`
  - [x] 移除 `ImageRenderPolicy`
  - [x] 移除不再需要的渲染策略设置项和测试
  - [x] 注入器：EDT/RA 跳过外部工具并 warning
  - [x] Tiger：`TigerLintToolService` + `TrackingCache` + `TigerLintResult` 扩展
- [x] 测试
  - [x] `ProcessExecutor`：超时、取消、stderr、退出码、编码
  - [x] `ImageMetadataService`/`ImageService`：元数据、缓存、渲染、清理
  - [x] `ImageCacheCleanupPolicy` / `ImageRenderFailurePolicy` 的逻辑与设置项
  - [x] 图片 IO 取消检查（按字节阈值）
  - [x] issue #396 回归测试 - `Issue396Test`
  - [x] Tiger 检查逻辑与 `ChronicleTigerLintInspection` 进阶测试 - `Issue396TigerLintTest`
  - [x] 进阶测试：`includeExternalTools` + `build/external-tools`
- [x] 清理与文档
  - [x] 清理旧 API、注释、常量
  - [x] 修订 ADR
  - [x] 补充 AI note

### 主要 & 次要（修订）

#### 2026-10-28 - step 1

- [x] 修订 `core.execution` 中旧代码：也弃用 `CommandLineExecutionException`，更新快速文档（`## 弃用声明`）
- [x] 修订 `core.execution` 中的新增代码：添加 `@file:Suppress("unused")`
- [x] 应使用 `ProcessExecutor`（对齐 `CapturingProcessHandler`），而非 `ProgressExecutor`。
- [x] 提取 `icu.windea.pls.core.util.system.WindowsRegistryService`（方法：`getStringValue`）。
- [x] 将 `CwtConfigService.getContainingConfigGroupForRepo` 中获取 remoteUrls 的方法提取到 `ChronicleGitService`。
- [x] 需要为 `CwtConfigService.getContainingConfigGroupForRepo` 补充测试（集成测试）。应当可以从测试数据根目录（`src/test/testData`）向上找到项目的根目录，从而检查 `cwt/` 中的那些子目录。
- [x] 需要确认 `ChronicleGitService` 中那些需要执行命令行的方法可能会抛出哪些常见异常，并且同步更新这些方法以及 `ProcessExecutor.execute` 等相关方法的注解和文档注释。

#### 2026-10-29 - step 2

- [x] 修订 `icu.windea.pls.images.supports.DefaultImageSupport.doConvertImageFormat`，确保妥善关闭 stream/reader。
- [x] 将 `getContainingConfigGroup` 从 `CwtConfigManager` 移到 `CwtConfigFileService`。
  - 目的：避免内存泄露（如果要在 `VirtualFile` 上缓存数据，当插件卸载时，必须确保妥善清空）。
  - `ChronicleFacade` `CwtConfigService` 等处也要重构。
- [x] 需要考虑进一步整理和重构 `ImageMetadata` 以及相关代码。
  - 决策：保留基于流式读取的 `ImageMetadataService`，不直接采用 `ImageInfoIndex`/`ImageInfoReader`。
  - `ImageInfoReader` 只接受 `byte[]`，对大文件不必要；`ImageInfoIndex` 主要覆盖平台 `ImageFileType`/SVG，不覆盖 DDS/TGA。
  - 同时将 `ImageMetadata` 收敛为 `width`/`height`，移除未使用的 `format`/`bpp`。
- [x] 在 `icu.windea.pls.config.configGroup.CwtConfigFileServiceTest` 中，除了验证 `cwt/core` 目录外，也需要验证 `cwt/cwtools-stellaris-config` 等目录。
  - 各种游戏类型对应的子目录都需要验证。
  - 如果要获取 `core` 以外的规则分组，必须先在集成测试中显式初始化。因此，我们在这里的测试中，实际上只需要验证 `getGameTypeFromRepoFile` 即可。
- [x] 将与图片渲染相关的代码（包括使用 `ImageRenderRequest`/`ImageRenderSession` 作为参数的）从 `ImageService` 提取到 `ImageRenderService`。
- [x] `ImageRenderService`/`ImageMetadataService` 需要实现接口 `Disposable`，并妥善地实现方法 `dispose`。
- [x] 如果无法从 `ImageMetadata` 获取图片尺寸，可能需要直接返回，而不是作为回退尝试从 `icon.iconHeight` 获取。
  - 这可能会意外触发图片加载，获取的也可能不是原始高度。
  - 决策：直接跳过图标渲染；同时先读取元数据再加载 icon，避免无意义的图片加载。
- [x] 与图片处理相关的测试，如果是图片格式敏感的，应覆盖支持的标准图片格式（jpg/png/dds/tga）。
  - 这可能需要先转换图片格式。
- [x] 与图片处理相关的测试，其测试数据（主要是各种格式的图片）应位于 `src/test/testData/images/` 目录下。
  - 原始图片可以来自各个本地游戏目录中的某个真实图片，或者来自 `src/test/resources/images/mod_unknown.png` 等。
- [x] 需要补充测试，以验证图片格式转换的正确性和幂等性（转换/转换+反向转换）。
  - 需要基于 `ImageService`/`ImageRenderService`。

#### 2026-10-29 - step 6 - optimization & test

- [x] 将 `ParadoxImageService` `ParadoxImageResolveResult` 移到 `icu.windea.pls.lang.resolve`
- [x] 将 `icu.windea.pls.images.policies` 中的代码移动到 `icu.windea.pls.model.policies`
- [x] 需要补充测试，验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），`ConvertImageFormatAction` 能够符合预期地成功/失败
  - 需要尽可能全面，覆盖各种组合。
- [x] 需要补充测试，验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），图片转换操作能够符合预期地成功/失败
  - 需要尽可能全面，覆盖各种组合。
- [x] `convertImageFormat` 中的 NOP 处理：如果 `sourceFormat` 等同于 `targetFormat`，可能需要直接返回，不执行转换操作
  - 可能需要适用于多个级别（toolProvider/toolService/imageSupport/imageService）
- [x] 与 `imagePolicy` 相关的优化和测试
- [x] 图片处理（渲染/读取/写入）时的取消检查
- [x] 与 `ExternalToolDownloadService` 相关的测试
- [x] 为 `ChronicleGitService` 与 `ChronicleSqliteService` 补充测试
- [x] issue #396 回归测试 - `Issue396Test`
- [x] 验证 tiger 的检查逻辑和对应的外部代码检查（`ChronicleTigerLintInspection`） - `Issue396TigerLintTest`

#### 2026-10-29 - step 6 - external tools

- [x] 在测试代码中调用 `ExternalToolDownloadService.download` 时，需要尽可能地确保下载的是最新版本
  - 如果不是，则移除临时目录中内容，并重新下载。
- [x] 改为可以在插件页面中更改 texconv 的路径，默认使用 `~/.windea/chronicle/external-tools/texconv.exe`
  - 将 `ChronicleDataPathService.texconvExePath` 更改到此路径。
- [x] `ExternalToolDownloadTestService` 和 `ExternalToolDownloadTestService` 中需要适当加上一些日志输出
  - 下载地址/文件名/目标目录/下载进度指示/等等。
  - 测试代码中直接使用 `println` 输出日志。
- [x] 除了 texconv 之外，外部工具诸如 tiger/magick 也可以在 非 Windows 的 OS（如 Linux）下执行
  - 需要在测试与生产代码中兼容它们。
  - 主要是 `ExternalToolDownloadService` 以及相关的测试。
- [x] 重构 `CommandBasedImageToolProvider`
  - 提取公开方法 `getExecutablePath` `validateExecutablePath`（通常来自插件设置，有些也有固定的默认值）
- [x] 将 `icu.windea.pls.core.io` 中的代码移动到 `icu.windea.pls.core.util.io` - 出于对齐的目的（如 `com.intellij.util.io.DataInputOutputUtil`）
- [x] 从 `DefaultImageSupport` 提取公共扩展方法到 `DataIoExtensions.kt`
- [x] 为了保持整洁的代码架构，便于后续查阅和重构，需要从 `ExternalToolDownloadService` 提取通用代码
  - `icu.windea.pls.core.util.io.ArchiveService` - 也已补充测试（冒烟，简单场景）
  - `icu.windea.pls.core.util.net.ProxyService` - 也已补充测试（冒烟，简单场景）
  - `icu.windea.pls.core.util.net.DownloadService` - 也已补充测试（冒烟，简单场景）
  - 一些通用的 model/enum/constant，也一并提取到对应位置（同包下/直接作为类成员或内部类）。
  - 需要允许更加灵活的配置，并提供一个合理的默认配置。例如提供默认参数 `settings: DownloadSettings`（速度检测/超时检测/等等）。

#### 2026-10-30 - step 7 - manual revision

- [x] 重命名：`cancelable` -> `cancellable`
- [x] P3 重命名：`Cache.cancelable` -> `Cache.asCancellable`
- [x] 移动：将 `StdlibExtensions.kt` 中的部分方法移到 `icu.windea.pls.core.util.io` 中的 `PathExtensions.kt` 和 `FileExtensions.kt`（并且移除/补充一些方法）
- [x] 移动：`icu.windea.pls.util` -> `icu.windea.pls.core.util`
- [x] 移动：将 `ArchiveService` 中的方法 `findExecutable` `makeExecutable` 移到 `PathService`
- [x] 重命名：`ArchiveOptions` -> `ArchiveOptions`，`DownloadOptions` -> `DownloadOptions`（更符合命名约定）
- [x] 从 `XxxService.kt` 中分离出代码到对应的文件（更符合个人偏好）
- [x] `ImageRenderSession` - 对于属性 `jobs` 和 `failedRequests`，改为使用 `ConcurrentLinkedQueue`，抹平心智负担，统一代码风格
- [x] 重构 `TigerLintToolService.collectRequest` 和 `TigerLintRequest`（统一属性类型 + 移除不必要的属性）
- [x] 重命名：`ProgressCheckingInputStream` -> `CancellableInputStream`，`ProgressCheckingOutputStream` -> `CancellableOutputStream`
- [x] 引入扩展方法：`InputStream.asCancellable()` `OutputStream.asCancellable()`
- [x] 提取 `IoService` 和 `ImageIoService`，其中的 IO 读写方法支持取消检查
- [x] 清理和优化 IO 读写的相关代码
- [x] 将 `icu.windea.pls.images.ImageFrameInfo` 移到 `icu.windea.pls.core.util.ImageFrameInfo`（对齐 `RangeInfo` 和 `Template`，以后可能还会新增 `ImageLayerInfo` 等）
- [x] 将方法 `scaleImage` `sliceImage` 从 `ImageService` 移动到 `ImageIoService`（因为它们不依赖于 imageSession/imageRequest）
- [x] 修复：需要补充 `DdsImageWriterSpi` 和 `TgaImageWriterSpi` 并在 `ImageService` 中显式挂载（避免转换图片格式时失败）
- [x] 在 `TigerLintToolService` 中添加并应用 `lintResultStatuses`（直到成功之前，避免多次发送警告通知）
- [x] 优化 `TigerLintToolService.getTigerLintResult`，如果结果状态为已取消，需要确保显式抛出 `ProcessCanceledException`
- [x] 按照以下思路重构 `TranslationToolProvider` `LintToolProvider` `ImageToolProvider`
  - [x] 其中的操作方法（operation methods）以及服务类中的对应操作方法可能抛出 `UnsupportedOperationException`（例如，找不到工具，工具不支持特定操作）
  - [x] 其中的操作方法（operation methods）以及服务类中的对应操作方法也可能抛出其他异常（例如，命令行执行失败）
  - [x] 确保在服务类中的操作方法中记录日志（success/failed + warn/debug）
- [x] 从 `ImageService` 分理出 `ImageContext` 以及 `ImageLifecycleListener`
- [x] 在 `ImageService` 中提取方法 `isImageFileType` `isBasicImageFileType` `isExtendedImageFileType`

#### 2026-10-29 - step 7 - revision

- [x] `ImageMetadataService` - 提供 `getMetadata(...): ImageMetadata` 和 `getMetadataOrNull(...): ImageMetadata?`，并提取 `ImageMetadataReader`
  - `ImageMetadataReader` 负责 DDS/TGA 索引/读取器及通用 `ImageIO` 读取；索引或专用读取器不可用时回退到通用读取。
- [x] 不能假定：总是存在外部解压工具的可执行文件路径（如 7z），本地环境总是存在系统代理（如 clash） - 需要结合使用 Assume
  - `ArchiveServiceTest` 的 7z 测试显式禁用外部工具回退。
  - `DownloadServiceTest` 检测到系统代理时通过 `Assume` 跳过本地下载测试。
  - `LintToolProviderExternalTest` 通过 `ChronicleAssume.includeExternalTools()` 控制外部工具测试。
- [x] `ImageRenderServiceTest` - 拆分出 `ParadoxImageServiceTest` 和 `ParadoxImageManagerTest`（并补充更多）
  - `ParadoxImageServiceTest` 覆盖 PNG/DDS/TGA、帧数信息和非图片文件。
  - `ParadoxImageManagerTest` 覆盖 `isImageFile`、`canResolve`、缓存命中、`Sync`/`Async` 渲染策略及失败策略。
- [x] `LintToolProviderExternalTest` - 仅验证 toolProvider，不验证 inspection
  - 覆盖 ck3-tiger、imperator-tiger、vic3-tiger 的启用状态、可用性、可执行文件和 conf 路径校验。
- [x] 为 `IoService` `ImageIoService` `PathService` 补充测试
  - `IoServiceTest` 覆盖路径/流复制、相同路径跳过和取消检查。
  - `ImageIoServiceTest` 覆盖格式判断、读写、缩放、切分、同格式复制和格式转换。
  - `PathServiceTest` 覆盖目录/父目录创建、类路径文件创建、可执行文件查找。
- [x] 为 `FileExtensions.kt` `PathExtensions.kt` 补充测试
  - 覆盖 `toFile`/`toFileOrNull`、`toPath`/`toPathOrNull`、`parsePath` 和 `clean`。
- [x] 除了 ck3-tiger 之外，也为 vic3-tiger 和 imperator-tiger 补充相关测试
  - `ExternalToolDownloadTestService` 新增 `vic3Tiger`、`irTiger` 描述符。
  - `ExternalToolDownloadTest` 新增对应下载与冒烟测试。
  - `LintToolProviderExternalTest` 覆盖对应 toolProvider。

#### 2026-11-01 - step 8 - contexts & image materialization

- [x] 新增 core 层 `HintsContext`，`ParadoxHintsContext` 继承它。
- [x] 新增 core 层 `DocumentationContext`，CWT 与 Paradox 快速文档共用。
- [x] 新增 images 层 `ImageRenderContext`，通过 userData 附加 `ImageRenderSession`。
- [x] `DocumentationTarget` 使用显式 `DocumentationContext`，在 RA 外等待渲染。
- [x] 快速文档的本地化图标不再在 RA 中读取 metadata/Icon；改为记录图片请求和使用渲染后的 URL。
- [x] 内嵌提示的图标不再在 collect 中读取 metadata、调用同步 `resolveUrl` 或触发懒加载 Icon。
- [x] 新增 `AsyncIconPresentation`：占位 `StatefulPresentation`，后台物化完成后在 EDT 更新。
- [x] 新增 `ImageRenderService.renderImageIcon`：在后台读取 metadata、渲染并解码为内存 Icon。

#### 2026-11-01 - step 9 - presentation settings

- [x] 新增 `ChroniclePresentationSettings`，统一快速文档/内嵌提示的文本和展示阈值。
- [x] 将展示文本长度、快速文档字体/图片/文本图标限制、内嵌提示文本长度/文本图标高度限制移入该设置并统一命名。
- [x] `ParadoxHintsSettings` 只保留 provider 级开关；provider 设置页移除数字阈值。
- [x] 从 `ChronicleInternalSettings` 移除对应 Registry 阈值属性，统一使用新设置。
- [x] 新增 `ChroniclePresentationSettingsConfigurable` 并注册到设置页。
- [x] `ChronicleImageSettings` 新增 `renderTimeoutSeconds`，用于内嵌提示图片物化超时。

#### 2026-11-01 - step 10 - suspend tool providers

- [x] 将可 suspend 的 external tool provider 操作迁移为 suspend（图片转换、lint、translation）。
- [x] service 层统一负责 dispatcher、超时、上下文检查和日志。
- [x] 新增 `ChronicleCapabilities.Test.strictContext`，测试中可将 EDT/readAction 上下文转为异常。
- [x] `Issue396Test` 增加严格上下文断言。
- [x] `ImageToolProvider.read` 保留同步 SPI 语义；EDT/RA 中只允许内置实现/缓存。

#### 2026-11-01 - post revision

- [x] 清理 `ImageToolProvider` 和 `ImageSupport`
- [x] 清理 `ImageToolService` 和 `ImageService`
- [x] 清理 `ParadoxImageManager` 和 `ParadoxImageService`
  - 移除 `ParadoxImageManager`，将其中所有方法移到 `ParadoxImageService`。
  - 为 `ParadoxImageResolveResult` 新增方法 `canResolve` `resolveUrl`，委托给 `ParadoxImageService` 中的对应方法。
- [x] 完善 `TigerLintResult`
  - 重命名字段（从而移除 `@SerialName`），补充缺失的字段。
- [x] 清理 `ChronicleImageSettings` 和 `ChroniclePresentationSettings` 中的配置项
  - 更加准确的命名和描述（适用对象是快速文档还是内嵌提示，普通图标还是文本图标，等等）。
- [x] 落实渲染快速文档/内嵌提示时，文本图标的缩放策略
  - 对于内嵌提示，因为已经使用了 `factory.smallScaledIcon(icon)`，预期不需要进行额外处理。
  - 对于快速文档，对于视为“文本图标”的图标（本地化文本中实际上也允许渲染大图片），需要基于文本高度缩放，以获得更好的 UI 效果。
- [x] 移除已不再用于 hints 的 `ImageRenderPolicy`

#### 2026-11-02 - step 11 - final revision

- [x] 清理展示设置命名：明确快速文档/内嵌提示、普通图片/文本图标、显示尺寸/高度限制等语义。
- [x] 清理图片设置中的 `ImageRenderPolicy` 字段、设置页、文案和相关测试。
- [x] 移除 `ImageRenderPolicy` 枚举及 `ParadoxImageService.resolveUrl` 中的策略分支。
- [x] 快速文档文本图标缩放：
  - 渲染会话中记录所有相关图片请求，包括不需要实际转换的图片。
  - `awaitAll` 后在 RA 外读取目标图片 metadata。
  - 高度不超过 `quickDocTextIconHeightLimit` 的图片按文档文本高度缩放；更大的图片保持原始尺寸。
- [x] 内嵌提示继续使用 `factory.smallScaledIcon(icon)`，不额外处理文本图标缩放。
- [x] `ParadoxImageServiceTest` 清理与渲染策略相关的测试。
- [x] 重构 `StellarisTechnologyCardBuilder`，以隔离数据读取与UI渲染逻辑，避免在EDT中直接渲染图片（这可能会执行命令行）
  - ~~改为使用协程并在 `runWithModalProgressBlocking` 中执行。~~ - 这会导致无限刷新，目前的临时解决方案仍会阻塞 EDT，以后再考虑优化。
  - 首先读取通用数据，接着读取非通用数据，最后构建UI组件。

#### 2026-10-30 - post doc

- [x] 作为总结，补充 ADR（AI 编写，`documents/ai-notes/adr/`）
- [x] 关于网络代理的问题：需要考虑补充解决方案笔记（AI 编写，`documents/ai-notes/solutions/`）
- [x] 编写参考文档 `images.md`

### 评估

#### 2026-10-28 - step 0

- [x] ~~考虑将 `icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext` 移到 `icu.windea.pls.lang.codeInsight.hints.InlayHintsContext`~~
  - 与 `DocumentationContext` 对齐 + 移出属性 `settings`（因为这是语义相关的）
  - 结论：目前没有必要
- [x] 由于模块依赖问题，`icu.windea.pls.images.ImageFrameInfo` 可能需要移到 `icu.windea.pls` 中的某个子包下
  - 结论：移动到 `icu.windea.pls.core.util.ImageFrameInfo`
- [x] 新增 `ParadoxImageService`，将 `ParadoxImageManager` 中的具体解析逻辑移到 `ParadoxImageService`
- [x] 新增 `ParadoxImageResolveResult`，目前包含来源路径、缓存路径、帧数信息和尺寸限制；实际加载图片的逻辑仍留在渲染/展示层

#### 2026-10-28 - step 1

- [x] 考虑为 `ChronicleGitService` 补充测试。需要考虑如何获取一个可用的 Git 仓库路径。
- [x] 考虑为 `ChronicleSqliteService` 补充测试。

#### 2026-10-29 - step 2

- [x] `ImageMetadata` 不直接复用平台图片索引：平台 API 需要 `byte[]` 或只覆盖标准图片文件类型，DDS/TGA 仍需自定义读取器。
- [x] 渲染代码按职责拆分：`ImageService` 保留格式转换/文件类型/SPI 生命周期，`ImageRenderService` 负责缓存、渲染和清理。
- [x] 原始高度不可获取时直接跳过图标，不使用 `icon.iconHeight` 作为回退。

#### 2026-10-30 - step 7

- [x] 考虑 `ParadoxImageManager` 和 `ParadoxImageService` 的进一步的清理和重构
- [x] ~~考虑将 `ParadoxImageManager.isImageFile` 移动到 `ImageService` 中~~
  - 结论：不采用
- [x] 对于 `ChronicleTigerLintAnnotator`，考虑结合实际的 UX 体验，评估是否需要加上 `runBlockingCancellable` + `withBlackgroundProgresss`
  - 结论：加上，因为检查结果的缓存失效时机并不是那么显而易见
- [ ] `CommandBasedImageToolProvider.read` - 可能需要考虑如何进行有效的缓存

#### 2026-11-01 - step 8 & 9

- [x] 使用 core 层通用上下文 + userData 附加图片数据，避免基础模块依赖 images。
- [x] 内嵌提示不再在 collect/EDT 中读取 metadata、同步渲染或懒加载 Icon；改用后台物化和占位 presentation。
- [x] 快速文档使用显式 DocumentationContext，图片渲染请求在 RA 中收集、在 RA 外等待。
- [x] 展示阈值统一到 ChroniclePresentationSettings；图片渲染超时放到 ChronicleImageSettings。

#### 2026-11-01 - step 10

- [ ] 为 hints/docs 上下文补充专门的回归测试（验证 collect/EDT 不发生图片 IO）。
  - 现在：已人工验证，可评估如何编写测试
- [x] 需要评估渲染快速文档/内嵌提示时，文本图标的缩放策略。可能需要人工验证。
  - 备注：需要区分“普通图标”和“文本图标”：对于内嵌提示和渲染器中渲染的图标，应全部视为“文本图标”，并基于文本高度进行缩放
  - 结论：必须处理快速文档中的，否则 UI 效果会不理想
  - 状态：已完成

#### 2026-10-30 - post

- [ ] `ExternalToolDownloadTestService` 中的 downloadDescriptor 可能不应该硬编码，理想情况下应可以自动确保是最新的
  - 现状：不是那么必要，留待后续评估
- [ ] 外部工具诸如 tiger/magick 可能也适用于 非 Windows/Linux 的 OS，考虑评估，并对应地更新生产和测试代码
  - 现状：不是那么必要，留待后续评估
- [ ] 内置所有图片工具（以及检查工具），或者提供便捷入口以在使用插件时一键下载对应的工具
  - 结论：非当前目标，需要另行评估
- [ ] 考虑将图片处理库改为 [kotlin-graphics/gli](https://github.com/kotlin-graphics/gli)
  - 结论：非当前目标，需要另行评估
- [ ] 使用 `DefaultImageSupport`（TwelveMonkeys）时，无法写入 DDS 图片（例如，从 PNG 转为 DDS）
  - 结论：应当是依赖库自身的缺陷
- [ ] 如果不需要切分图片（PNG/DDS/TGA/等等），解析图片时不会先转换图片格式（DDS/TGA -> PNG）
  - 这意味着 `~/.windea/chronicle/images/` 中不会生成本地图片缓存
  - 参见：`icu.windea.pls.lang.resolve.ParadoxImageService.doResolve`
  - 参见：`icu.windea.pls.lang.resolve.ParadoxImageResolveResult.ParadoxImageResolveResult`
  - 现状：暂不确定，可能需要更改行为细节
- [x] 考虑将各种 externalToolProvider 中的操作方法（operation methods）全部改为挂起方法（suspend methods），以暗示应当在后台线程中执行
  - 状态：已完成（除了 `ImageToolProvider.read`）
- [x] 评估 `ChronicleSettings` / `ChronicleSettingsConfigurable` 的进一步拆分和迁移。
  - 结论：非当前目标，需要另行评估
- [x] 考虑重构代码以移除 `ParadoxDefinitionPresentationUtil` 中的 `.toFileUrl().toIconOrNull()`
  - 状态：已完成
- [ ] 考虑将 `ImageRenderRequest`/`ParadoxImageResolveResult` 合并为 `ImageDescriptor`
  - 更加通用，不限于“渲染”，也可用于“描述”和“解析”。
  - 提供图片层的方法 `ImageDescriptor.createImage` `ImageDescriptor.createImageIcon` `ImageDescriptor.resolveUrl`。
  - 考虑区分渲染和解析策略：`Direct` `Delegated`（converted/sliced/convertedAndSliced）。
- [ ] 考虑物化图片/图标时的缓存策略（读取/渲染/解析）
  - 相关代码位置：`ImageService.createImage` `ImageService.createImageIcon`
  - 参见：`com.intellij.util.IconUtil`
  - 参见：`com.intellij.ui.icons.CachedImageIcon`
- [ ] UI Presentation API 的进一步重构
  - 相关代码位置：`ParadoxDefinitionPresentation` `ParadoxDefinitionPresentationProvider` `ParadoxDefinitionPresentationService`
  - 关键代码位置：`StellarisTechnologyCardBuilder.build`
  - 思路：预先加载所有必要的数据，渲染所有必要的图表，尽可能消除 `ParadoxDefinitionDiagramProvider.ElementManager` 中的 readAction
  - 备注：这个 Diagrams 官方插件本身就有 readAction 相关的 BUG，而且 API 设计也不太合理……
  - 结论：非当前目标，需要另行评估
- [ ] 图片转换动作的 EDT 阻塞路径进一步改造
  - 关键代码位置：`ConvertImageFormatAction.convertImageFormat`
  - 思路：后台转换 + EDT 提交
  - 结论：非当前目标，需要另行评估

### 分析

#### 2026-10-29

- [x] 无法正确地从 DDS/PNG 转换图片格式为 TGA
  - 参见：`ConvertImageFormatAction` 
  - 原因：`DefaultImageSupport` 使用 `ImageReader.getNumImages(false)` 判断帧数；JPEG/DDS 等读取器在无法预知帧数时会返回 `-1`，导致 `repeat(-1)` 不执行任何转换，最终产出空文件。
  - 修复：当帧数不确定（`<= 0`）时至少转换第一帧；同时将图片转换为标准颜色模型，避免 CMYK 等自定义颜色模型导致 `ImageIO.write` 静默失败；`ImageService` 对空产出继续尝试下一个 `ImageSupport`。
- [x] 无法正确地从 DDS/PNG 转换图片格式为 TGA（进阶）
  - 参见：`ConvertImageFormatAction`
  - 备注：需要验证（且仅验证/单独验证）使用基于外部工具的 imageSupport 时，是否也可以正确工作。

#### 2026-10-31

- [x] 不正确的上下文状态（EDT / readAction） - 更新内嵌提示时
  - 堆栈：[stack-trace-202610311612.txt](.assets/stack-trace-202610311612.txt) - EDT
  - 堆栈：[stack-trace-202610312033.txt](.assets/stack-trace-202610312033.txt) - RA
  - 断点：`icu.windea.pls.core.execution.ProcessExecutor.warnIfIncorrectContext`
  - 相关代码位置：`icu.windea.pls.lang.codeInsight.hints.localisation.ParadoxLocalisationIconHintsProvider.collectFromElement`
  - 临时解决方案：~~调用 `icon.iconHeight`，确保图片加载~~ -> 仍然会在 readAction 中
  - 状态：已解决
- [x] 使用 `DefaultImageSupport`（TwelveMonkeys）时，图片格式转换失败 - DDS -> TGA（至少）
  - 堆栈：[stack-trace-202610311808.txt](.assets/stack-trace-202610311808.txt)
  - 解决方案：需要补充 `DdsImageWriterSpi` 和 `TgaImageWriterSpi` 并在 `ImageService` 中显式挂载
  - 状态：已解决
- [x] 需要重新设计图片渲染和解析的流程，避免重复渲染，以及不正确的上下文状态
  - 避免 `.toFileUrl().toIconOrNull()`，如果确实需要渲染图片，这可能是不必要的
  - 状态：已解决

### 验证

#### 人工验证

- [x] 需要验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），是否可以正常转换图片格式（`ConvertImageFormatAction`）
  - 限制：使用 `DefaultImageSupport`（TwelveMonkeys）时，无法转换为 DDS
  - 状态：已验证（3 X 4 X 4）
- [x] 需要验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），是否可以正常在编辑器中渲染图片
  - 状态：已验证（3 X 4）
- [x] 需要验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），是否可以正常在快速文档中渲染图片
  - 状态：已简单验证
- [x] 需要验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），是否可以正常在内嵌提示中渲染图片
  - 状态：已简单验证
- [x] 需要验证基于外部检查工具的代码检查（`ChronicleTigerLintInspection`），是否可以正常工作
  - 状态：已简单验证
- [x] 需要结合调式断点，对于命令行执行以及外部图片工具操作，确保不会发生在不正确的上下文中 - #396
  - 状态：已简单验证

## 参见（AI 协助）

### 相关代码位置

生产代码：

- [x] `icu.windea.pls.core.execution.CommandLineExecutor` - 已弃用
- [x] `icu.windea.pls.core.execution.ProcessExecutor` - 新增
- [x] `icu.windea.pls.core.execution.ExecutionTimeouts` - 新增
- [x] `icu.windea.pls.core.execution.CommandLineService` - 已弃用
- [x] `icu.windea.pls.core.execution.CommandType` - 已弃用
- [x] `icu.windea.pls.base.io.ChronicleGitServiceImpl` - 已迁移
- [x] `icu.windea.pls.config.config.CwtConfigService` - 已迁移
- [x] `icu.windea.pls.integrations.images.providers.TexconvToolProvider` - 已迁移
- [x] `icu.windea.pls.integrations.images.providers.MagickToolProvider` - 已迁移
- [x] `icu.windea.pls.integrations.lints.providers.TigerLintToolProvider` - 已迁移
- [x] `icu.windea.pls.lang.tools.SpecialPathServiceImpl` - 已迁移
- [x] `icu.windea.pls.images.ImageService` - 基础设施已扩展；保留格式转换与 SPI 生命周期
- [x] `icu.windea.pls.images.ImageRenderService` - 新增
- [x] `icu.windea.pls.images.ImageMetadataService` - 新增
- [x] `icu.windea.pls.images.ImageMetadataReader` - 新增
- [x] `icu.windea.pls.core.util.io.IoService` / `ImageIoService` / `PathService` - 新增/提取
- [x] `icu.windea.pls.images.ImageCacheCleaner` - 新增
- [x] `icu.windea.pls.images.ImageRenderRequest` - 新增
- [x] `icu.windea.pls.images.ImageRenderSession` - 新增
- [x] `icu.windea.pls.model.policies.ImageCacheCleanupPolicy` - 新增
- [x] `icu.windea.pls.model.policies.ImageRenderFailurePolicy` - 新增
- [x] ~~`icu.windea.pls.model.policies.ImageRenderPolicy`~~ - 已移除（已不再用于内嵌提示）
- [x] `icu.windea.pls.base.settings.ChronicleImageSettings` - 新增；包含渲染超时
- [x] `icu.windea.pls.base.settings.ChronicleImageSettingsConfigurable` - 新增
- [x] `icu.windea.pls.base.settings.ChroniclePresentationSettings` - 新增
- [x] `icu.windea.pls.base.settings.ChroniclePresentationSettingsConfigurable` - 新增
- [x] `icu.windea.pls.core.codeInsight.hints.HintsContext` - 新增
- [x] `icu.windea.pls.core.codeInsight.documentation.DocumentationContext` - 新增
- [x] `icu.windea.pls.images.ImageRenderContext` - 新增
- [x] `icu.windea.pls.lang.codeInsight.hints.AsyncIconPresentation` - 新增
- [x] `icu.windea.pls.core.util.io.CancellableInputStream` / `CancellableOutputStream` - 新增
- [x] `icu.windea.pls.integrations.images.ImageToolService` - 已加入 EDT/RA 保护；同步转换桥接 suspend provider
- [x] `icu.windea.pls.integrations.images.ImageToolProvider` - 转换操作改为 suspend；
ead 保留同步 ImageReader SPI 语义
- [x] `icu.windea.pls.integrations.images.providers.CommandBasedImageToolProvider` - 转换/read 桥接调整
- [x] `icu.windea.pls.integrations.lints.TigerLintToolService` - 已重构为 app-level + `TrackingCache`
- [x] `icu.windea.pls.integrations.lints.TigerLintRequest` - 新增
- [x] `icu.windea.pls.lang.util.ParadoxImageManager` - 已重构为门面
- [x] `icu.windea.pls.lang.resolve.ParadoxImageService` - 新增
- [x] `icu.windea.pls.lang.resolve.ParadoxImageResolveResult` - 新增
- [x] `icu.windea.pls.integrations.ExternalToolDownloadService` - 新增
- [x] `icu.windea.pls.core.util.io.ArchiveService` - 新增
- [x] `icu.windea.pls.core.util.net.ProxyService` - 新增
- [x] `icu.windea.pls.core.util.net.DownloadService` - 新增
- [x] `icu.windea.pls.images.actions.ConvertImageFormatAction` - 提取内部转换方法，支持在后台执行外部工具
- [x] `icu.windea.pls.integrations.images.providers.TexconvToolProvider` - 支持自定义路径，默认使用外部工具目录
- TBD

生产代码（快速文档/内嵌提示/渲染器）：
- [x] `icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationTarget` - 快速文档等待渲染会话
- [x] `icu.windea.pls.lang.codeInsight.documentation.CwtDocumentationTarget` - 快速文档等待渲染会话
- [x] `icu.windea.pls.lang.codeInsight.hints.script.ParadoxModifierIconHintsProvider` - 使用 `HintsContext` + `AsyncIconPresentation`
- [x] `icu.windea.pls.lang.codeInsight.hints.localisation.ParadoxLocalisationIconHintsProvider` - 使用 `HintsContext` + `AsyncIconPresentation`
- [x] `icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextHtmlRenderer`
- [x] `icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextQuickDocRenderer`
- [x] `icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer` - 使用 `AsyncIconPresentation`，collect 中不加载 icon

测试代码：

- [x] `icu.windea.pls.core.execution.ProcessExecutorTest` - 常见命令/区分OS/区分编码/取消行为/超时行为/编码/stderr/退出码
- [x] `icu.windea.pls.config.configGroup.CwtConfigFileServiceTest` - `getGameTypeFromRepoFile` 集成测试（含各游戏规则仓库）
- [x] `icu.windea.pls.images.ImageMetadataServiceTest` - 文件/路径元数据、`getMetadata`/`getMetadataOrNull` 和异常场景
- [x] `icu.windea.pls.images.ImageServiceTest` - 格式转换/往返转换
- [x] `icu.windea.pls.images.ImageRenderServiceTest` - 渲染、同步/异步请求、渲染会话、失败策略、内存 Icon 物化、文本图标缩放
- [x] `icu.windea.pls.images.ImageCacheCleanerTest` - 全部缓存清理策略
- [x] `icu.windea.pls.lang.resolve.ParadoxImageServiceTest` - PNG/DDS/TGA、帧数信息、非图片文件、图片判断、缓存命中、失败策略
- [x] `icu.windea.pls.core.util.io.IoServiceTest` - 路径/流复制、同路径跳过、取消检查
- [x] `icu.windea.pls.core.util.io.ImageIoServiceTest` - 格式判断、读写、缩放、切分、复制和转换
- [x] `icu.windea.pls.core.util.io.PathServiceTest` - 目录创建、类路径文件、可执行文件查找
- [x] `icu.windea.pls.core.util.io.FileExtensionsTest` / `PathExtensionsTest`
- [x] `icu.windea.pls.integrations.ExternalToolDownloadServiceTest` - `check` 行为
- [x] `icu.windea.pls.integrations.ExternalToolDownloadTest` - texconv/magick/ck3-tiger/vic3-tiger/imperator-tiger 下载与冒烟测试（Windows/Linux）
- [x] `icu.windea.pls.integrations.ExternalToolDownloadTestService` - 共享 `build/external-tools`
- [x] `icu.windea.pls.images.ImageFormatConversionTest` - 各种策略和格式下的图片格式转换
- [x] `icu.windea.pls.images.actions.ConvertImageFormatActionTest` - 动作级别的图片格式转换
- [x] `icu.windea.pls.core.util.io.ArchiveServiceTest` - Zip/TarGz/SevenZip 冒烟
- [x] `icu.windea.pls.core.util.net.ProxyServiceTest` - 代理配置解析
- [x] `icu.windea.pls.core.util.net.DownloadServiceTest` - 本地 HTTP 下载冒烟
- [x] `icu.windea.pls.integrations.images.ImageToolProviderExternalTest` - 图片工具冒烟
- [x] `icu.windea.pls.integrations.lints.LintToolProviderExternalTest` - Tiger toolProvider 可用性和路径校验
- [x] `icu.windea.pls.base.io.ChronicleGitServiceTest` - 本地仓库冒烟与常见场景
- [x] `icu.windea.pls.base.io.ChronicleSqliteServiceTest` - 临时 SQLite 数据库
- [x] `icu.windea.pls.test.issues.Issue396Test` - 外部图片工具上下文回归测试（含严格上下文断言）
- [x] `icu.windea.pls.test.issues.Issue396TigerLintTest` - Tiger 服务与 `ChronicleTigerLintInspection` 端到端测试
- [x] ~~`icu.windea.pls.integrations.lints.RunTigerTest`~~ - 已移除（因为已存在 `ExternalToolDownloadTest`）
- ...

参考：

- `icu.windea.pls.core.cache.CancellableCache`
- `icu.windea.pls.core.cache.TrackingCache`
- `icu.windea.pls.core.util.MergedModificationTracker`
- `icu.windea.pls.core.util.io.CancellableInputStream`
- `icu.windea.pls.core.util.io.CancellableOutputStream`
- `icu.windea.pls.model.policies.*`
- `icu.windea.pls.images.actions.ConvertImageFormatAction`
- ...

### 相关代码位置（平台）

通用：

- `com.intellij.openapi.progress.coroutinesKt.coroutineToIndicator`
- `com.intellij.codeInsight.daemon.impl.InlayHintsPassFactoryInternal.clearModificationStamp`
- `com.intellij.platform.ide.progress.withBackgroundProgress`

命令行相关：

- `com.intellij.execution.configurations.GeneralCommandLine`
- `com.intellij.execution.process.CapturingProcessRunner`
- `com.intellij.execution.process.CapturingProcessHandler`
  - `com.intellij.execution.process.CapturingProcessHandler.runProcess(int, boolean)`
  - `com.intellij.execution.process.CapturingProcessHandler.runProcessWithProgressIndicator`
- `com.intellij.execution.process.OSProcessHandler`
  - `com.intellij.execution.process.OSProcessHandler.checkEdtAndReadAction`
- `com.intellij.execution.util.ExecUtil`

图片处理相关：

- `org.intellij.images.index.ImageInfoIndex`
- `org.intellij.images.util.ImageInfoReader`
- `org.intellij.images.util.imageio.CommonsImagingImageReaderSpi`
- `com.intellij.util.IconUtil`
- `com.intellij.ui.icons.CachedImageIcon`

网络代理相关：

- `com.intellij.util.net.ProxySettings`
- `com.intellij.util.net.ProxyConfiguration`
- `com.intellij.util.net.JdkProxyProvider`

外部检查相关：

- `com.intellij.codeInspection.javaDoc.JavadocHtmlLintAnnotator` - 这里并没有特别地使用取消检查/状态指示
