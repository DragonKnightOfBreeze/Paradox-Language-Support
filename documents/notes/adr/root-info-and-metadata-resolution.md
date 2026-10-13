# 优化 rootInfo 和 rootMetadata 的解析逻辑

> 所属模块：`后端 > 分析层`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v2.1.8
> - 日期：2026-05-03
> - 状态：已实施

说明：
- 解析这些数据时，不应也不需要依赖 VFS，因此也无需 readAction
- 直接基于 `Path` 定位文件，解析时不要依赖文件的 PSI
- 相关的 EP 也改为直接传入 `Path` 而非 `VirtualFile`
- `ParadoxRootMetadata` 不再使用 `rootFile: VirtualFile` 作为属性，而是改为使用 `rootPath: Path`

相关代码位置：
- `icu.windea.pls.model.ParadoxRootInfo`
- `icu.windea.pls.model.analysis.ParadoxRootMetadata`
- `icu.windea.pls.model.analysis.ParadoxRootMetadataInfo`
- `icu.windea.pls.ep.analysis.ParadoxRootMetadataProvider` - EP
- `icu.windea.pls.lang.analysis.ParadoxRootMetadataUtil` - 主要用于获取路径和解析信息
