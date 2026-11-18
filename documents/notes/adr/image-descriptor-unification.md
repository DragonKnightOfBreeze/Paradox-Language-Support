# 统一 `ImageDescriptor` 模型并分离图片层与语义层职责

> - 模块：后端 > 通用基础设施
> - 版本：v3.0.6
> - 日期：2026-11-17
> - 状态：已实施
> - 标签：`ImageDescriptor` / `ImageStrategy` / `ImageRenderService` / `ParadoxImageService` / 分层解耦

## 背景

在之前的实现中，图片领域与语义层各自维护了一套几乎完全镜像的模型类：
- 图片层：`icu.windea.pls.images.ImageRenderRequest`
- 语义层：`icu.windea.pls.lang.resolve.ParadoxImageResolveResult`

两者携带相同的 5 个核心字段（`sourcePath`、`targetPath`、`frameInfo`、`maxWidth`、`maxHeight`），每次渲染均需调用 `toRenderRequest()` 转换。更重要的是，`ParadoxImageResolveResult` 暴露的 `canResolve()`、`resolveUrl()`、`createImage()`、`createImageIcon()` 并不依赖任何 Paradox PSI/CWT 语义，却通过 `ParadoxImageService.resolveUrl` 反向耦合了 `ImageRenderService`、`ImageRenderSession` 与 `ChronicleImageSettings`。

## 决策与权衡

### 1. 收敛为 `ImageDescriptor` 与 `ImageStrategy`

- 在图片层（`icu.windea.pls.images`）引入统一的数据类 `ImageDescriptor`，并移除 `ImageRenderRequest` 与 `ParadoxImageResolveResult`；
- 引入 `ImageStrategy` 枚举（`Direct` / `Delegated`），由 `targetPath == sourcePath` 自动推导策略与 `renderRequired` / `targetFormat`；
- 保留 `data class` 扁平结构，确保直接作为 `ImageRenderService.renderFutures`（`ConcurrentHashMap`）的键时拥有天然的结构化 `equals` / `hashCode`。

### 2. 职责单向纠偏（`lang -> images`）

- 将 `canResolve(descriptor: ImageDescriptor)` 与 `resolveUrl(descriptor: ImageDescriptor, project: Project)` 正式迁入 `ImageRenderService`；
- `ImageDescriptor` 的便捷方法直接委托给 `ImageRenderService`；
- `ParadoxImageService` 退回纯粹的语义层解析职责（根据定义、文件或文件路径解析得到 `ImageDescriptor?`），彻底移除对 `ImageRenderService`、`ImageRenderSession` 及图片失败策略配置的反向依赖。

### 3. 附：`CwtSeparatorType.DoubleEqual` 默认匹配影响评估结论

同期核查全仓 `cwt/` 各游戏规则库中显式 `==` 的覆盖分布后确认：CK2（0）、Vic2（0）、EU4（1）、HOI4（37）等规则仓远未完善 `==` 标注。因此决定在 `v3.0.6` **不在规则匹配层默认启用 `DoubleEqual` 硬过滤**，仅保持在显式启用 `ChronicleConfigSettings.features.checkComparisonOperators` 时用于语法检查（结论已注记于 `ParadoxSyntaxService.isComparisonOperatorValid`）。

## 关键代码位置

- `src/main/kotlin/icu/windea/pls/images/ImageDescriptor.kt`
- `src/main/kotlin/icu/windea/pls/images/ImageRenderService.kt`
- `src/main/kotlin/icu/windea/pls/images/ImageRenderSession.kt`
- `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxImageService.kt`
- `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxSyntaxService.kt`
- `src/test/kotlin/icu/windea/pls/images/ImageRenderServiceTest.kt`
- `src/test/kotlin/icu/windea/pls/lang/resolve/ParadoxImageServiceTest.kt`
