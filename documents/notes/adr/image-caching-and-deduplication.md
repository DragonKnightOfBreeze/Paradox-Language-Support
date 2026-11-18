# 图片缓存策略优化、原子落位与去重复物化

> - 模块：后端 > 通用基础设施
> - 版本：v3.0.6
> - 日期：2026-11-18
> - 状态：已实施
> - 标签：`ImageRenderService` / `ImageDescriptor` / `ImageCacheCleaner` / `CommandBasedImageToolProvider` / 内存缓存 / 原子写入 / `JBImageIcon`

## 背景

在 `v3.0.5` 及之前，图片链路已经具备磁盘 PNG 缓存、同步/后台渲染、渲染会话与定期清理，但仍存在几类彼此叠加的问题：

1. **描述符参数冗余**：`ImageDescriptor` 携带 `maxWidth` / `maxHeight`，但生产代码对二者为零引用；同时其语义容易与真正生效的 `heightLimit`（一个“超过阈值即跳过”的展示门槛，而非缩放目标）混淆；
2. **重复物化**：图表、科技卡、快速文档与内嵌提示会在不同时机反复解码同一张图片、反复创建同一份图标对象。既有的 in-flight 去重只覆盖“渲染到磁盘”，不覆盖“解码为 `Image` / 包装为 `Icon`”；
3. **磁盘缓存可能留下半成品**：渲染直接写目标路径时，进程中断或写入异常可能留下截断文件，后续命中后解码失败；
4. **命中判定过重或过轻**：缺少 O(1) 的完整性轻校验，且“保活”若不加区分地刷新 `mtime`，会污染 `Direct` 策略下真实源文件的时间戳；
5. **清理器边界不清**：占位图、工具缓存目录、孤儿临时文件与空子目录缺少明确的保留/回收规则。

## 决策与权衡

### 1. `ImageDescriptor` 参数收敛

- 移除 `maxWidth` / `maxHeight`，将模型简化为 `(sourcePath, targetPath, frameInfo)`：
  - 二者在生产代码中没有调用方，属于预留但未落地的参数；
  - 与 `heightLimit` 并存时命名相近、语义不同（前者若保留会暗示“渲染期缩放”，后者实际只是“超限则放弃物化”），容易诱发误用；
  - 缩放需求已在展示层通过 `Icon.resize` 与 `DocumentationBuilder.resizeImage` 表达，无需在描述符中重复承载。
- **保留** `ImageIoService.scaleImage` 作为通用底层工具：它是与领域无关的 `BufferedImage` 缩放能力，仍可供其他调用方按需使用，不随描述符收敛一并删除。

### 2. 内存级缓存与 `JBImageIcon` 复用（避免重复物化）

在 `ImageRenderService` 中引入两级实例缓存，并统一走 Single-flight：

- **`imageCache`**：以估算像素字节数作为权重（`width * height * 4`），`maximumWeight = 32 MiB`，`expireAfterAccess = 10m`；缓存解码后的 `Image`，直接命中即可跳过元数据读取与磁盘读取；
- **`iconCache`**：`maximumSize = 2048`，`expireAfterAccess = 10m`；缓存包装后的 `Icon`，避免同一图片被反复包装；
- **缓存键**：`ImageCacheKey(sourcePath, sourceSize, sourceModifiedAt, targetPath, frameInfo)`，源文件大小或修改时间变化后自动失效，目标路径仅用于区分不同渲染产物；
- **Single-flight**：渲染侧由 `renderFutures` 合并同一 `ImageDescriptor` 的请求；解码侧由 `imageFutures` 合并同一 `ImageCacheKey` 的并发解码；
- **取消不连累**：等待方若收到的是前置请求传播的取消异常，但通过 `ProgressManager.progressIndicator.isCanceled` 判定 `当前线程自身并未被取消`，则移除已取消的前置请求并独立降级执行一次，避免单个请求的取消波及其他正常并发请求；
- **常量而非设置项**：以上容量与 TTL 直接写死为合理常量，不膨胀为新的设置项。内存缓存属于纯运行期优化，用户难以给出稳定有效的配置，暴露设置反而增加理解与维护成本。

**图标选型依据（经 SDK 字节码验证）**：

- `Icon.resize`（内部走 `IconUtil.toSize`）返回的 `IconSizeWrapper` 只是一个持有原图标与目标尺寸的 3 字段轻量代理，不做任何图像拷贝，因此“缩放”本身并不构成重复物化的成本；
- `CachedImageIcon` 强耦合平台内部图标加载体系（缓存、按需加载、URL 语义等），不适合直接用于“已经解码好的 `Image`”；
- 因此采用公开且稳定的 `image.toIcon()`（即 `IconUtil.createImageIcon` → `JBImageIcon`）完成 HiDPI 适配与对象复用，**无需**给 `createImageIcon` 额外增加尺寸参数；尺寸变化交给轻量代理处理即可。

**元数据读取剪枝**：

- `createImage` 仅在 `heightLimit != null` **且**内存缓存未命中时才读取 `ImageMetadataService`，命中缓存时完全跳过元数据与磁盘访问；
- `applyImageScaling` 优先读取内存缓存图像的宽高，未命中时才回退到元数据读取，避免快速文档场景的重复 IO。

### 3. 磁盘缓存原子落位、合法性校验与节流保活

#### 3.1 采用风格 1：全链路信任模型对称

`ImageRenderService` 与 `CommandBasedImageToolProvider` 统一采用同一套“写入—校验—落位”模型：

1. 写入**随机临时文件**（渲染侧为同目录下带 UUID 的 `.part` 文件；工具侧为 `_tmp` 下的 UUID 临时文件）；
2. 执行 **O(1) 轻量完整性校验**（`ImageIoService.isValidImageFile`）：PNG 校验前 8 字节签名与末 12 字节 `IEND` 块，其余格式只校验存在且非空；
3. 校验通过后以 `ATOMIC_MOVE` **原子落位**（文件系统不支持时回退为 `REPLACE_EXISTING`）；
4. 异常路径删除未落位的目标文件并清理临时文件；运行期解码失败时，对 `Delegated` 策略自动删除缓存并重建一次。

`CommandBasedImageToolProvider` 的流式输入额外使用**输入内容的确定性 SHA-256** 作为缓存键，独立存放于 `images/_tool/{provider}/{sha256}.{sourceFmt}.{targetFmt}`：

- 相同内容只执行一次外部命令；
- 以缓存路径为粒度使用 `Mutex` + double-check，防止并发请求重复拉起外部子进程；
- 缓存路径锁本身也采用有界缓存（`maximumSize = 512`，`expireAfterAccess = 10m`）。

#### 3.2 留档备选：风格 2（`.sha256` 伴随校验文件）

曾评估在 `images/_tool` 中为每个缓存文件额外写入同名 `.sha256` 伴随文件，在命中时读取并比对摘要。**暂不采用**，原因如下：

| 维度 | 风格 1（采用） | 风格 2（暂不采用） |
| --- | --- | --- |
| 文件数量 | 一份产物一个文件 | 产物与校验文件成对，小文件数量翻倍 |
| 信任模型 | 与上层 `ImageRenderService` 对称：都只做 O(1) 轻量完整性校验 | 工具缓存强校验，上层仍为轻校验，形成不对称 |
| 防护目标 | 主要防半成品/截断写入，解码失败可自愈重建 | 可额外发现隐蔽的字节级替换 |
| 复杂度 | 低，规则集中在一处 | 需要维护伴随文件的写入、孤儿配对与清理 |

结论：本地私有缓存区的主要风险是**写入中断导致的半成品**，而非**隐蔽恶意替换**。风格 1 已能覆盖该风险，并与上层保持一致的信任模型；引入风格 2 的边际收益不足以抵消文件数量与规则复杂度的上升，故仅留档备选。

#### 3.3 节流 Touch 红线

`getCachedImagePath` 的保活逻辑必须区分策略：

- **仅 `ImageStrategy.Delegated`** 的派生缓存参与节流刷新：距上次修改超过 1 天时更新 `mtime`，以降低被清理器误回收的概率；
- **严禁触碰 `ImageStrategy.Direct` 的源文件 `mtime`**：`Direct` 直接引用仓库内的真实图片，刷新其时间戳会污染版本库，并改变 `ParadoxImageService.buildCacheUuid` 所依赖的源时间戳，导致缓存 UUID 漂移。

#### 3.4 `ImageCacheCleaner` 边界增强

- **保护 `_placeholder.png`**：占位图不参与任何策略清理；
- **`_tool` 纳入常规清理**：工具缓存与派生缓存共享 TTL/大小/文件数策略；
- **`_tmp` 不作为常规缓存区**：不参与 TTL/大小统计，但回收其中存在超过 1 天的孤儿临时文件；
- **空目录自底向上清除**：清理结束后删除已清空的子目录，同时保留 `imagesPath` 根目录以及 `_tmp`、`_tool` 目录本身；
- `Files.walk` 在返回前显式关闭底层流，避免 Windows 上目录句柄未释放导致空目录删除失败。

## 关键代码位置

- `src/main/kotlin/icu/windea/pls/images/ImageDescriptor.kt`
- `src/main/kotlin/icu/windea/pls/images/ImageRenderService.kt`
- `src/main/kotlin/icu/windea/pls/images/ImageCacheCleaner.kt`
- `src/main/kotlin/icu/windea/pls/core/util/io/ImageIoService.kt`
- `src/main/kotlin/icu/windea/pls/core/ui/UiExtensions.kt`（`Image.toIcon()`）
- `src/main/kotlin/icu/windea/pls/integrations/images/providers/CommandBasedImageToolProvider.kt`
- `src/main/kotlin/icu/windea/pls/base/io/ChronicleDataPathService.kt`
- `src/main/kotlin/icu/windea/pls/base/io/ChronicleDataPathServiceImpl.kt`
- `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxImageService.kt`（`buildCacheUuid` 的源时间戳依赖）
- `src/test/kotlin/icu/windea/pls/images/ImageRenderServiceTest.kt`
- `src/test/kotlin/icu/windea/pls/images/ImageCacheCleanerTest.kt`
- `src/test/kotlin/icu/windea/pls/integrations/images/CommandBasedImageToolProviderTest.kt`
- `src/test/kotlin/icu/windea/pls/lang/presentation/StellarisTechnologyCardBuilderTest.kt`
