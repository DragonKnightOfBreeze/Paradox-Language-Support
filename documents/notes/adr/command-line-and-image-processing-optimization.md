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
- [ ] P2 优化插件自身提供的 `CommandLineExecutor` API
  - 考虑默认委托给平台提供的 `ExecUtil` `GeneralCommandLine`，从而有可能通过传入 `ProgressIndicator` 来进行取消检查。

### 范围

主要涉及的接口和语言功能：
- 内嵌提示（部分内嵌提示可能会渲染图标） - 内嵌提示的刷新逻辑位于 EDT 中
- 快速文档（部分快速文档可能会渲染图片/图标） - 快速文档的计算逻辑位于单独的 readAction 中，因此相对来说影响不大
- 基于外部检查工具的代码检查（`ChronicleTigerLintInspection`） - 需要考虑进行取消检查
- GIT 命令相关（`ChronicleGitService`）
- 特殊路径相关（`SpecialPathService`）
- 图片预览（在编辑器中打开 DDS/TGA 图片）

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

### 任务计划草案

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

### 任务计划（AI 协助）

TBD

### 后续修订

> 目前尚未确定是否需要采用这些待办事项。

- [ ] 由于模块依赖问题，`icu.windea.pls.images.ImageFrameInfo` 可能需要移到 `icu.windea.pls` 中的某个子包下
- [ ] 考虑新增 `ParadoxImageService`，将 `ParadoxImageManager` 中的具体解析逻辑移到 `ParadoxImageService`
- [ ] 考虑新增 `ParadoxImageResolveResult`，其中包括必要的信息，以及实际加载图片的/属性（比如可以直接是 `image: Image`）
- [ ] 考虑将 `ParadoxImageManager.isImageFile` 移动到 `ImageService` 中
- [ ] 考虑将 `ImageService.requestRender` 和 `ImageService.render` 的其中一些参数封装到 `ImageRenderConstraint` 或 `ImageRenderSettings` 中
- [ ] `icu.windea.pls.core.execution` 中的代码的后续清理和重构
- [ ] 对于 `ChronicleTigerLintAnnotator`，考虑结合实际的 UX 体验，评估是否需要加上 `runBlockingCancellable` + `withBlackgroundProgresss`

### 后续参考

- `com.intellij.codeInspection.ex.GlobalInspectionContextImpl.runTools` L345
  - 这里声明了一个 `ProgressIndicator`。
  - 也就是说 `com.intellij.lang.annotation.ExternalAnnotator.doAnnotate` 的调用链上层是有 indicator 的，但不能直接获取。
  - 由于 indicator API 已经过时，如果需要明确的状态指示，建议的方式可能仍然是 `runBlockingCancellable` + `withBlackgroundProgresss`。

## 任务（AI 协助）

TBD

## 任务（修订）

TBD

## 参见

### 相关代码位置

- `icu.windea.pls.core.execution.CommandLineExecutor`
- `icu.windea.pls.core.execution.CommandLineService`
- `icu.windea.pls.integrations.images.ImageToolService`
- `icu.windea.pls.integrations.lints.TigerLintToolService`
- `icu.windea.pls.lang.util.ParadoxImageManager`

### 相关代码位置（平台）

- `com.intellij.execution.configurations.GeneralCommandLine`
- `com.intellij.execution.process.CapturingProcessRunner`
- `com.intellij.execution.process.CapturingProcessHandler`
- `com.intellij.execution.process.OSProcessHandler`
  - `com.intellij.execution.process.OSProcessHandler.checkEdtAndReadAction`
- `com.intellij.execution.util.ExecUtil`

### 参考代码位置（平台）

- `com.intellij.codeInspection.javaDoc.JavadocHtmlLintAnnotator` - 这里并没有特别地使用取消检查/状态指示