# 命令行执行与图片处理优化：最终总结

> - 模块：性能优化 > 命令行执行、图片处理
> - 版本：v3.0.5
> - 日期：2026-11-03
> - 状态：已完成（人工验证通过）

## 背景

本任务最初针对 issue [#396](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/396)：内嵌提示渲染 DDS/TGA 图标时，可能通过 ImageIO 注入器在 EDT 或 readAction 中调用外部图片工具，导致 IDE 卡死。

随着实现推进，任务范围扩大为：

- 统一命令行执行入口，支持取消与超时；
- 清理图片读取、渲染、缓存和格式转换的基础设施；
- 重新划分快速文档与内嵌提示的图片物化边界；
- 补充图片 IO、外部工具和设置相关的测试；
- 清理不再需要的渲染策略和重复设置。

人工编写的 ADR 位于 [`documents/notes/adr/command-line-and-image-processing-optimization.md`](../../notes/adr/command-line-and-image-processing-optimization.md)。本文以最终状态为主，补充实现细节、备选方案和后续可评估事项。

## 最终目标

- 命令行和外部工具操作不在 EDT/readAction 中执行。
- 图片读取、渲染、解码和缓存写入具备取消检查与超时边界。
- 内嵌提示的图片物化不阻塞收集阶段，也不在 EDT 中懒加载 Icon。
- 快速文档在 readAction 中只收集请求，在 RA 外等待图片完成。
- 图片格式转换在可取消的后台上下文中执行。
- 设置项按“图片处理”和“展示”职责拆分，命名明确。
- 测试能够发现上下文、取消和超时方面的回归。

## 最终架构

### 1. 命令行执行

- `ProcessExecutor` 基于 `GeneralCommandLine` / `CapturingProcessHandler`。
- 支持 `ProgressIndicator`、超时、超时销毁进程和取消。
- `ExecutionTimeouts` 提供常用超时档位。
- 在 EDT/readAction 中执行时默认警告；测试可通过 `ChronicleCapabilities.Test.strictContext` 将警告升级为异常。
- 旧的 `CommandLineExecutor` / `CommandLineService` 已弃用并迁移。

### 2. 图片 IO

- `IoService`：路径/流复制，支持取消检查。
- `ImageIoService`：图片读写、缩放、切分、格式转换和同格式快速复制。
- `ImageMetadataService` / `ImageMetadataReader`：
  - 对 DDS/TGA 优先使用自定义读取器或索引；
  - 其他格式使用 `ImageIO`；
  - 专用读取器不可用时回退到通用读取；
  - 提供 `getMetadata`（非空）和 `getMetadataOrNull`（可空）两种语义。

### 3. 图片渲染

- `ImageRenderService` 负责：
  - 磁盘缓存检查；
  - 同步/异步渲染；
  - `ImageRenderSession` 会话；
  - 内存 `Image`/`Icon` 物化；
  - 快速文档文本图标缩放；
  - 图片渲染失败策略；
  - 缓存定期清理。
- `ImageRenderSession` 使用 `ConcurrentLinkedQueue` 记录任务、请求和失败请求。
- `ImageRenderContext` 通过 userData 将渲染会话附加到通用上下文，避免基础上下文依赖 images 模块。
- 快速文档：
  - `DocumentationContext` 显式传递；
  - RA 中收集图片请求；
  - `awaitAll` 在 RA 外等待；
  - 渲染失败策略和文本图标缩放都在等待后应用。
- 内嵌提示：
  - `HintsContext` 显式传递；
  - `AsyncIconPresentation` 继承 `StatefulPresentation`；
  - 后台协程通过 `coroutineToIndicator` 物化图片；
  - 完成后在 EDT 更新占位 presentation；
  - 不再调用 `ImageMetadataService`、同步 `resolveUrl` 或 URL 懒加载 Icon。
- 内嵌提示直接使用 `factory.smallScaledIcon(icon)`；快速文档则按文档文本高度缩放文本图标。

### 4. 外部图片工具

- `ImageToolProvider` 的转换操作是 suspend。
- `ImageToolService` 负责工具发现、上下文检查、调度、日志和异常传播。
- `ImageService` / `ImageSupport` 的转换操作同样是 suspend。
- `ImageToolProvider.read` 由于 `ImageReader` SPI 是同步 API，保留同步语义；其外部工具路径通过 `runBlockingCancellable` 桥接。
- `ConvertImageFormatAction` 将转换放到后台线程，并避免在 writeAction 中直接执行外部命令。

### 5. 设置

`ChronicleImageSettings` 负责图片处理：

- 缓存清理策略、TTL、最大大小、最大文件数；
- 渲染失败策略；
- 渲染超时。

`ChroniclePresentationSettings` 负责展示阈值：

- 展示文本长度限制；
- 快速文档本地化文本字体基准大小；
- 快速文档图片最大显示尺寸；
- 快速文档文本图标高度限制；
- 内嵌提示本地化文本长度限制；
- 内嵌提示文本图标高度限制。

`ParadoxHintsSettings` 只保留 provider 级选项，不再包含数字阈值。

`ImageRenderPolicy` 已移除，`ParadoxImageService.resolveUrl` 不再区分同步/异步策略。

## 关键决策

### 基础上下文 + userData，而不是让基础模块依赖 images

可选的方案有：

1. 在基础模块定义 `HintsContext` / `DocumentationContext`，直接持有图片请求和渲染会话；
   - 缺点是基础模块会依赖 images，破坏现有分层。
2. 在语义模块直接使用 `ParadoxHintsContext`/`DocumentationContext`；
   - 缺点是 CWT 与 Paradox 快速文档需要共享同一类上下文，容易继续分裂。
3. **采用**：基础模块只定义通用上下文和 `UserDataHolderBase`；images 模块通过 `Key` 附加 `ImageRenderSession`。
   - 优点：基础模块不依赖 images；语义层可以按需读取图片数据；未来还可以附加其他可选数据。

### 快速文档使用显式 `DocumentationContext`

- 移除隐式 `ThreadLocal` 作为主要入口。
- 目标类只负责：
  1. 创建 context/session；
  2. 在 RA 中构建文档并记录请求；
  3. 在 RA 外等待；
  4. 应用缩放和失败策略；
  5. 生成最终文档。
- `ImageRenderSession` 暂时仍作为 context 的字段，兼容现有 manager/renderer 的调用方式；未来可以完全移除 `withRenderSession` 的 ThreadLocal 兼容层。

### 内嵌提示使用 `StatefulPresentation`

- 收集阶段只添加占位 presentation，不读取图片像素。
- 后台物化成功后在 EDT 更新状态。
- 避免整文件刷新，也避免在 `InlayHintsPass` 的 apply 阶段触发懒加载 Icon。
- 不引入全局强引用 icon 缓存：icon 的生命周期由 presentation 持有，任务完成后不额外保留强引用。

### 文本图标缩放

- 内嵌提示已经通过 `factory.smallScaledIcon(icon)` 处理，不需要额外缩放。
- 快速文档中，图片请求在 `awaitAll` 后统一处理：
  - 高度不超过 `quickDocTextIconHeightLimit` 的图片按文档文本高度缩放；
  - 更大的图片保持原始尺寸，避免把本地化文本中的大图片强行当作文本图标。
- 文本高度优先使用 `getDocumentationFontSize().size`；无法获取时回退到 `quickDocLocalisationTextFontSize`。

### 移除 `ImageRenderPolicy`

- 内嵌提示不再使用同步/异步策略选择；它始终采用“缓存或后台物化 + presentation 更新”。
- 快速文档使用渲染会话等待，不依赖策略。
- `ImageRenderPolicy` 的设置项、枚举、文案和测试已移除。

## 测试与验证

已覆盖：

- `ProcessExecutorTest`：超时、取消、stderr、退出码、编码。
- `Issue396Test`：外部图片工具上下文保护，以及严格上下文断言。
- `ImageMetadataServiceTest`：文件/路径元数据、可空 API、异常场景。
- `ImageServiceTest` / `ImageFormatConversionTest`：格式转换、往返转换、外部工具矩阵。
- `ImageRenderServiceTest`：渲染、同步/异步请求、渲染会话、失败策略、内存 Icon、文本图标缩放。
- `ParadoxImageServiceTest`：PNG/DDS/TGA、帧数信息、非图片文件、缓存命中、失败策略。
- `ImageCacheCleanerTest`：全部缓存清理策略。
- `ArchiveServiceTest` / `ProxyServiceTest` / `DownloadServiceTest`：解压、代理、下载。
- `ExternalToolDownloadTest` / `LintToolProviderExternalTest`：Tiger 等外部工具下载和 provider 校验。
- `buildPlugin`：通过。

人工验证已覆盖快速文档、内嵌提示、图片转换动作、图片读取和外部检查工具。

## 待评估与可扩展内容

### 1. 图片物化的架构继续收敛

- `ImageRenderService` 同时保留同步、异步和会话 API，仍有继续收敛空间。
- `withRenderSession` 的 ThreadLocal 兼容层可以在所有调用点显式传递 context 后移除。
- `ParadoxImageResolveResult.resolveUrl` 仍有同步 fallback；未来可以统一为 context + suspend API。

### 2. 内嵌提示的进度与批处理

- 当前内嵌提示图片物化默认不显示状态指示。
- 如果实际使用中出现大量图标，可以考虑：
  - 每个 editor/file 一个渲染协调器；
  - 一个后台进度任务批量处理请求；
  - 基于时间或数量去重/合并请求。
- `StatefulPresentation` 状态更新的生命周期仍依赖 project 检查；未来可以引入 editor/session 级别的 generation token。

### 3. 内存 icon 缓存

- 当前没有全局强引用缓存，依赖 presentation 持有 icon。
- 如果发现频繁重解码或同一图片被大量复用，可以再评估：
  - 会话内 in-flight 去重；
  - 有界、soft/weak value 的缓存；
  - 按 editor/project 生命周期清理。
- 不建议无界全局强引用 `BufferedImage`。

### 4. 外部工具 suspend 化的边界

- `ImageToolProvider.read` 受同步 `ImageReader` SPI 限制，不能简单 suspend。
- 可进一步把外部工具读取路径与 ImageIO 内置实现路径拆分，避免同步桥接。
- `ConvertImageFormatAction` 仍存在 EDT 等待后台转换的残余；更理想的是：
  1. 后台转换到临时文件；
  2. writeAction 中只做文件移动、删除和 VFS 刷新；
  3. 避免在 writeAction 中等待外部进程。

### 5. 设置与展示策略

- `quickDocMaxImageDisplaySize` 与 `quickDocTextIconHeightLimit` 的边界仍可继续细化。
- 可以评估是否为快速文档提供“只缩放文本图标，不缩放其他图片”的显式类型标记。
- `ChronicleSettings` / `ChronicleSettingsConfigurable` 仍包含较多配置，未来可按主题拆分。

### 6. 网络代理问题

- 外部工具下载依赖网络，测试环境中的系统代理（例如本地代理软件）可能导致：
  - 本地 HTTP 下载测试被代理拦截；
  - 外部工具下载失败或超时。
- `DownloadServiceTest` 已通过 `Assume` 在检测到系统代理时跳过本地下载测试。
- 具体代理排查与 Gradle/JVM 信任库问题另见：
  - [`documents/ai-notes/solutions/download-service-system-proxy.md`](../solutions/download-service-system-proxy.md)
  - [`documents/ai-notes/solutions/gradle-download-ssl-handshake-failure.md`](../solutions/gradle-download-ssl-handshake-failure.md)

## 备选方案

### 方案 A：在内嵌提示中同步渲染并等待

- 实现简单，但会在 EDT/readAction 中阻塞，无法根本上解决 #396。
- 不采用。

### 方案 B：仅把 `ImageRenderPolicy` 默认值改为 `Async`

- 仍会在 collect 中读取 metadata，并在下一次 collect 中懒加载 Icon。
- 不能解决 EDT/readAction 中的图片 IO。
- 不采用。

### 方案 C：全局内存 icon 缓存

- 可以避免重复解码，但容易引入长期 retention 和生命周期问题。
- 当前优先保持 presentation 持有 icon；只有性能分析证明必要时再加有界缓存。

### 方案 D：把上下文直接放在语义层

- 可以让 `HintsContext` / `DocumentationContext` 直接拥有图片字段。
- 但会让基础模块依赖 images，破坏分层；当前采用基础上下文 + userData。

## 参考位置

- `src/main/kotlin/icu/windea/pls/core/execution/`
- `src/main/kotlin/icu/windea/pls/core/util/io/`
- `src/main/kotlin/icu/windea/pls/images/`
- `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxImageService.kt`
- `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxImageResolveResult.kt`
- `src/main/kotlin/icu/windea/pls/lang/codeInsight/hints/AsyncIconPresentation.kt`
- `src/main/kotlin/icu/windea/pls/lang/codeInsight/documentation/`
- `src/main/kotlin/icu/windea/pls/base/settings/ChronicleImageSettings.kt`
- `src/main/kotlin/icu/windea/pls/base/settings/ChroniclePresentationSettings.kt`
- `src/main/kotlin/icu/windea/pls/integrations/images/`
- `src/main/kotlin/icu/windea/pls/integrations/lints/`
- `src/main/kotlin/icu/windea/pls/images/actions/ConvertImageFormatAction.kt`
- `src/test/testData/images/`
