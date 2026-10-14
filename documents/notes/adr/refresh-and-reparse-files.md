# 刷新文件与重解析文件

> - 模块：后端 > 通用基础设施
> - 版本：v2.1.9
> - 日期：2026-05-12
> - 状态：已实施

刷新文件：
- `DaemonCodeAnalyzer.getInstance(project).restart()` - 刷新指定项目的所有文件的高亮和内嵌提示
- `DaemonCodeAnalyzer.getInstance(project).restart(file)` - 刷新指定项目的指定文件的高亮（不会刷新内嵌提示）
- `InlayHintsPassFactoryInternal.clearModificationStamp(editor)` - 刷新指定文件的内嵌提示
- 可以在 readAction 中使用，可以在新开启的协程中使用
- 刷新高亮和内嵌提示

重新解析文件：
- `FileContentUtilCore.reparseFiles` - 重新解析一组文件
- writeAction，通常需要在 edt 中执行
- 之后会自动重建索引、刷新高亮和内嵌提示
- 同时需要适当地清空缓存、更新更改追踪器

相关代码位置：
- `icu.windea.pls.base.analysis.ChronicleAnalysisManager`
- `com.intellij.codeInsight.daemon.DaemonCodeAnalyzer`
- `com.intellij.util.FileContentUtilCore`
- `com.intellij.codeInsight.daemon.impl.InlayHintsPassFactoryInternal`
