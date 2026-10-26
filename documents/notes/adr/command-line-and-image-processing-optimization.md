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

## 任务

- 基础设施
  - TBD
- 重构
  - TBD
- 测试
  - TBD

## 参见

### 相关代码位置

- `icu.windea.pls.core.execution.CommandLineExecutor`
- `icu.windea.pls.core.execution.CommandLineService`
- `icu.windea.pls.integrations.images.ImageToolService`
- `icu.windea.pls.integrations.lints.TigerLintToolService`

### 相关代码位置（平台）

- `com.intellij.execution.configurations.GeneralCommandLine`