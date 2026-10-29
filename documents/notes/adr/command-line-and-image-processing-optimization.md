## 优化命令行执行逻辑和图片处理逻辑

> - 模块：后端 > 通用
> - 版本：v3.0.5
> - 日期：2026-10-27
> - 状态：WIP

## 说明

为了避免意外的、可能的 IDE 冻结，需要优化插件代码中的命令行执行逻辑（以及图片处理逻辑）。
尽可能地遵循平台开发的最佳实践，沿用平台提供的相关基础设施，并在必要时自行创建相关基础设施。

另外，也要考虑重构插件代码中的命令行执行逻辑。
例如直接委托给平台提供的相关基础设施，直接传入命令分组，等等。

相关 Issue：
- [#396](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/396)

## 笔记

### 计划

- [ ] P2 优化命令行执行逻辑和图片处理逻辑
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
  - 在 `icu.windea.pls.base.settings` 中新增 `ChronicleImageSettings` 和 `ChronicleImageSettingsConfigure`，提供必要的配置项
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
    - 这要求在测试时，临时更改 `ChronicleIntegrationsSettings` 中的配置项。
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
- 从 `ExternalToolDownloadService` 提取 `ArchiveService`、`ProxyService`、`DownloadService` 及 `ArchiveType` / `ArchiveSettings` / `DownloadSettings` / `DownloadResult`。
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
  - `ProgressCheckingInputStream` / `ProgressCheckingOutputStream` 每累计 64 KiB 调用一次 `ProgressManager.checkCanceled()`。
  - `readImage` / `writeImage` / `copyImageStream` / `copyImageFile` / `ImageMetadataService` / `DefaultImageSupport` 使用上述检查。
- 新增测试：
  - `ImageCacheCleanerTest`：`None`、`ByTtl`、`BySize`、`ByTtlAndSize`。
  - `ImageRenderServiceTest`：同步/异步策略、显式策略覆盖、三种失败策略及渲染会话失败处理。
  - `ImageIoExtensionsTest`：输入/输出流按字节阈值检查取消，以及复制路径。

## 任务（AI 协助）

### 主要 & 次要

- [x] 基础设施
  - [x] 新增 `ProcessExecutor`、`ExecutionTimeouts`；弃用旧命令行 API
  - [x] 新增 `ImageMetadataService`
  - [x] 扩展 `ImageService`：缓存、渲染、`ImageRenderSession`、`ImageCacheCleaner`
  - [x] 新增 `ImageCacheCleanupPolicy`、`ImageRenderFailurePolicy`、`ImageRenderPolicy` 及文案
- [ ] 重构
  - [x] 迁移 Git、Texconv、Magick、Tiger、SpecialPath、CwtConfigService
  - [x] 新增 `ParadoxImageService`/`ParadoxImageResolveResult`，保留 `ParadoxImageManager` facade
  - [x] 快速文档：`toDocumentation` 移出 RA，等待 `ImageRenderSession`
  - [x] 内嵌提示：`Sync`/`Async` 渲染；`Async` 完成后刷新内嵌提示
  - [x] 注入器：EDT/RA 跳过外部工具并 warning
  - [x] Tiger：`TigerLintToolService` + `TrackingCache` + `TigerLintResult` 扩展
- [ ] 测试
  - [x] `ProcessExecutor`：超时、取消、stderr、退出码、编码
  - [x] `ImageMetadataService`/`ImageService`：元数据、缓存、渲染、清理
  - [x] `ImageCacheCleanupPolicy` / `ImageRenderFailurePolicy` / `ImageRenderPolicy` 的逻辑与设置项
  - [x] 图片 IO 取消检查（按字节阈值）
  - [x] issue #396 回归测试 - `Issue396Test`
  - [x] Tiger 检查逻辑与 `ChronicleTigerLintInspection` 进阶测试 - `Issue396TigerLintTest`
  - [x] 进阶测试：`includeExternalTools` + `build/external-tools`
- [ ] 清理与文档
  - [ ] 清理旧 API、注释、常量
  - [ ] 修订 ADR
  - [ ] 补充 AI note

### 主要 & 次要（修订）

#### 2026-10-28 - step 1

- [x] 修订 `core.execution` 中旧代码：也弃用 `CommandLineExecutionException`，更新快速文档（`## 弃用声明`）
- [x] 修订 `core.execution` 中的新增代码：添加 `@file:Suppress("unused")`
- [x] 应使用 `ProcessExecutor`（对齐 `CapturingProcessHandler`），而非 `ProgressExecutor`。
- [x] 提取 `icu.windea.pls.core.util.system.WindowsRegistryService`（方法：`getStringValue`）。
- [x] 将 `CwtConfigService.getContainingConfigGroupForRepo` 中获取 remoteUrls 的方法提取到 `ChronicleGitService`。
- [x] 需要为 `CwtConfigService.getContainingConfigGroupForRepo` 补充测试（集成测试）。应当可以从测试数据根目录（`src/test/testData`）向上找到项目的根目录，从而检查 `cwt/` 中的那些子目录。
- [x] 需要确认 `ChronicleGitService` 中那些需要执行命令行的方法可能会抛出哪些常见异常，并且同步更新这些方法以及 `ProcessExecutor.execute` 等相关方法的注解和文档注释。
  - 常见异常包括：`ExecutionException` `IlegalStateException` 

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

`ExternalToolDownloadService` 相关：

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
  - `icu.windea.pls.util.io.ArchiveService` - 也已补充测试（冒烟，简单场景）
  - `icu.windea.pls.util.net.ProxyService` - 也已补充测试（冒烟，简单场景）
  - `icu.windea.pls.util.net.DownloadService` - 也已补充测试（冒烟，简单场景）
  - 一些通用的 model/enum/constant，也一并提取到对应位置（同包下/直接作为类成员或内部类）。
  - 需要允许更加灵活的配置，并提供一个合理的默认配置。例如提供默认参数 `settings: DownloadSettings`（速度检测/超时检测/等等）。

#### 2026-10-29 - step 6 - post revision

- [ ] 不能假定：总是存在外部解压工具的可执行文件路径（如 7z），本地环境总是存在系统代理（如 clash） - 需要结合使用 Assume
- [ ] `ImageMetadataService` - 考虑同时提供 `getMetadata(...): ImageMetadata` 和 `getMetadataOrNull(...): ImageMetadata?`，同时考虑提取 `ImageMetadataReader`
- [ ] `ImageRenderServiceTest` - 需要拆分出 `ParadoxImageServiceTest`
- [ ] `CommandBasedImageToolProvider.read` - 需要考虑如何进行有效的缓存
- [ ] `ParadoxImageManager` 的清理和重构
- [ ] 考虑将 `ParadoxImageManager.isImageFile` 移动到 `ImageService` 中
- [ ] `icu.windea.pls.integrations.lints.TigerLintToolService.collectRequest` - 获取 `modPath` 时不应硬编码（改为基于 EP）
- [ ] `LintToolProviderExternalTest` - 暂不确定 - 仅出于对齐的目的，因为 `Issue396TigerLintTest` 中已经覆盖了
- [ ] `XxxSettingsConfigure` -> `SettingsConfigurable`
- [ ] 重命名：`ProgressCheckingInputStream` -> `CancellableInputStream` `ProgressCheckingOutputStream` -> `CancellableOutputStream`
- [ ] 引入扩展方法：`InputStream.cancellable()` `OutputStream.cancellable()`

#### 2026-10-30 - step 6 - post doc

- [ ] 作为总结，补充 ADR（AI 编写，`documents/ai-notes/adr/`）
  - 暂时搁置
- [ ] 关于网络代理的问题：需要考虑补充解决方案笔记（AI 编写，`documents/ai-notes/solutions/`）
  - 暂时搁置
- [ ] 编写参考文档 `images.md`
  - 暂不确定

### 评估

#### 2026-10-28 - step 0

- [x] ~~考虑将 `icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext` 移到 `icu.windea.pls.lang.codeInsight.hints.InlayHintsContext`~~
  - 与 `DocumentationContext` 对齐 + 移出属性 `settings`（因为这是语义相关的）
  - 结论：目前没有必要
- [x] ~~由于模块依赖问题，`icu.windea.pls.images.ImageFrameInfo` 可能需要移到 `icu.windea.pls` 中的某个子包下~~
  - 结论：目前没有必要
- [x] 新增 `ParadoxImageService`，将 `ParadoxImageManager` 中的具体解析逻辑移到 `ParadoxImageService`
- [x] 新增 `ParadoxImageResolveResult`，目前包含来源路径、缓存路径、帧数信息和尺寸限制；实际加载图片的逻辑仍留在渲染/展示层
- [ ] 考虑 `ParadoxImageManager` 和 `ParadoxImageService` 的进一步的清理和重构
- [ ] 对于 `ChronicleTigerLintAnnotator`，考虑结合实际的 UX 体验，评估是否需要加上 `runBlockingCancellable` + `withBlackgroundProgresss`

#### 2026-10-28 - step 1

- [x] 考虑为 `ChronicleGitService` 补充测试。需要考虑如何获取一个可用的 Git 仓库路径。
- [x] 考虑为 `ChronicleSqliteService` 补充测试。

#### 2026-10-29 - step 2

- [x] `ImageMetadata` 不直接复用平台图片索引：平台 API 需要 `byte[]` 或只覆盖标准图片文件类型，DDS/TGA 仍需自定义读取器。
- [x] 渲染代码按职责拆分：`ImageService` 保留格式转换/文件类型/SPI 生命周期，`ImageRenderService` 负责缓存、渲染和清理。
- [x] 原始高度不可获取时直接跳过图标，不使用 `icon.iconHeight` 作为回退。

#### 2026-10-30 - step 6

- [ ] `ExternalToolDownloadTestService` 中的 downloadDescriptor 可能不应该硬编码，理想情况下应可以自动确保是最新的
  - 现状：不是那么必要，留待后续评估
- [ ] 外部工具诸如 tiger/magick 可能也适用于 非 Windows/Linux 的 OS，考虑评估，并对应地更新生产和测试代码
  - 现状：不是那么必要，留待后续评估

### 分析

#### 2026-10-29

- [x] 无法正确地从 DDS/PNG 转换图片格式为 TGA
  - 参见：`ConvertImageFormatAction` 
  - 原因：`DefaultImageSupport` 使用 `ImageReader.getNumImages(false)` 判断帧数；JPEG/DDS 等读取器在无法预知帧数时会返回 `-1`，导致 `repeat(-1)` 不执行任何转换，最终产出空文件。
  - 修复：当帧数不确定（`<= 0`）时至少转换第一帧；同时将图片转换为标准颜色模型，避免 CMYK 等自定义颜色模型导致 `ImageIO.write` 静默失败；`ImageService` 对空产出继续尝试下一个 `ImageSupport`。
- [x] 无法正确地从 DDS/PNG 转换图片格式为 TGA（进阶）
  - 参见：`ConvertImageFormatAction`
  - 备注：需要验证（且仅验证/单独验证）使用基于外部工具的 imageSupport 时，是否也可以正确工作。

### 修订

#### 人工修订

- [ ] `XxxSettings()` -> `XxxSettings.DEFAULT`
- [ ] 从 `XxxService.kt` 中分离出代码到对应的文件（这更符合个人偏好）

### 验证

#### 人工验证

- [ ] 需要验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），是否可以正常转换图片格式（`ConvertImageFormatAction`）
- [ ] 需要验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），是否可以正常在编辑器中渲染图片
- [ ] 需要验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），是否可以正常在快速文档中渲染图片
- [ ] 需要验证在各种策略和格式下（TwelveMonkeys/texconv/magick X jpg/png/dds/tga），是否可以正常在内嵌提示中渲染图片
- [ ] 需要验证基于外部检查工具的代码检查（`ChronicleTigerLintInspection`），是否可以正常工作
- [ ] 需要结合调式断点，对于命令行执行以及外部图片工具操作，确保不会发生在不正确的上下文中

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
- [x] `icu.windea.pls.images.ImageCacheCleaner` - 新增
- [x] `icu.windea.pls.images.ImageRenderRequest` - 新增
- [x] `icu.windea.pls.images.ImageRenderSession` - 新增
- [x] `icu.windea.pls.model.policies.ImageCacheCleanupPolicy` - 新增
- [x] `icu.windea.pls.model.policies.ImageRenderFailurePolicy` - 新增
- [x] `icu.windea.pls.model.policies.ImageRenderPolicy` - 新增
- [x] `icu.windea.pls.base.settings.ChronicleImageSettings` - 新增
- [x] `icu.windea.pls.base.settings.ChronicleImageSettingsConfigurable` - 新增
- [x] `icu.windea.pls.core.util.io.ProgressCheckingInputStream` / `ProgressCheckingOutputStream` - 新增
- [x] `icu.windea.pls.integrations.images.ImageToolService` - 已加入 EDT/RA 保护
- [x] `icu.windea.pls.integrations.lints.TigerLintToolService` - 已重构为 app-level + `TrackingCache`
- [x] `icu.windea.pls.integrations.lints.TigerLintRequest` - 新增
- [x] `icu.windea.pls.lang.util.ParadoxImageManager` - 已重构为门面
- [x] `icu.windea.pls.lang.resolve.ParadoxImageService` - 新增
- [x] `icu.windea.pls.lang.resolve.ParadoxImageResolveResult` - 新增
- [x] `icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationTarget` - 快速文档等待渲染会话
- [x] `icu.windea.pls.lang.codeInsight.documentation.CwtDocumentationTarget` - 快速文档等待渲染会话
- [x] `icu.windea.pls.integrations.ExternalToolDownloadService` - 新增
- [x] `icu.windea.pls.util.io.ArchiveService` - 新增
- [x] `icu.windea.pls.util.net.ProxyService` - 新增
- [x] `icu.windea.pls.util.net.DownloadService` - 新增
- [x] `icu.windea.pls.images.actions.ConvertImageFormatAction` - 提取内部转换方法，支持在后台执行外部工具
- [x] `icu.windea.pls.integrations.images.providers.TexconvToolProvider` - 支持自定义路径，默认使用外部工具目录
- TBD

测试代码：

- [x] `icu.windea.pls.core.execution.ProcessExecutorTest` - 常见命令/区分OS/区分编码/取消行为/超时行为/编码/stderr/退出码
- [x] `icu.windea.pls.config.configGroup.CwtConfigFileServiceTest` - `getGameTypeFromRepoFile` 集成测试（含各游戏规则仓库）
- [x] `icu.windea.pls.images.ImageMetadataServiceTest`
- [x] `icu.windea.pls.images.ImageServiceTest` - 格式转换/往返转换
- [x] `icu.windea.pls.images.ImageRenderServiceTest` - 渲染、同步/异步请求、渲染策略、失败策略、渲染会话
- [x] `icu.windea.pls.images.ImageCacheCleanerTest` - 全部缓存清理策略
- [x] `icu.windea.pls.core.util.io.ImageIoExtensionsTest` - 图片 IO 取消检查阈值与复制
- [x] `icu.windea.pls.integrations.ExternalToolDownloadServiceTest` - `check` 行为
- [x] `icu.windea.pls.integrations.ExternalToolDownloadTest` - 下载与冒烟测试（Windows/Linux）
- [x] `icu.windea.pls.integrations.ExternalToolDownloadTestService` - 共享 `build/external-tools`
- [x] `icu.windea.pls.images.ImageFormatConversionTest` - 各种策略和格式下的图片格式转换
- [x] `icu.windea.pls.images.actions.ConvertImageFormatActionTest` - 动作级别的图片格式转换
- [x] `icu.windea.pls.util.io.ArchiveServiceTest` - Zip/TarGz/SevenZip 冒烟
- [x] `icu.windea.pls.util.net.ProxyServiceTest` - 代理配置解析
- [x] `icu.windea.pls.util.net.DownloadServiceTest` - 本地 HTTP 下载冒烟
- [x] `icu.windea.pls.integrations.images.ImageToolProviderExternalTest` - 图片工具冒烟
- [x] `icu.windea.pls.base.io.ChronicleGitServiceTest` - 本地仓库冒烟与常见场景
- [x] `icu.windea.pls.base.io.ChronicleSqliteServiceTest` - 临时 SQLite 数据库
- [x] `icu.windea.pls.test.issues.Issue396Test` - 外部图片工具上下文回归测试
- [x] `icu.windea.pls.test.issues.Issue396TigerLintTest` - Tiger 服务与 `ChronicleTigerLintInspection` 端到端测试
- [x] ~~`icu.windea.pls.integrations.lints.RunTigerTest`~~ - 已移除（因为已存在 `ExternalToolDownloadTest`）
- ...

参考：

- `icu.windea.pls.core.cache.CancellableCache`
- `icu.windea.pls.core.cache.TrackingCache`
- `icu.windea.pls.core.util.MergedModificationTracker`
- `icu.windea.pls.core.util.io.ProgressCheckingInputStream`
- `icu.windea.pls.core.util.io.ProgressCheckingOutputStream`
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

网络代理相关：

- `com.intellij.util.net.ProxySettings`
- `com.intellij.util.net.ProxyConfiguration`
- `com.intellij.util.net.JdkProxyProvider`

外部检查相关：

- `com.intellij.codeInspection.javaDoc.JavadocHtmlLintAnnotator` - 这里并没有特别地使用取消检查/状态指示
