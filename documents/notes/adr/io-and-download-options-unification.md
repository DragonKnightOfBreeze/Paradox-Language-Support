# 统一 IO 与下载选项体系、引入可监视流及重试机制

> - 模块：后端 > 通用基础设施
> - 版本：v3.0.6
> - 日期：2026-11-17
> - 状态：已实施
> - 标签：`IoOptions` / `DownloadOptions` / `MonitorableInputStream` / `DownloadService` / API 对齐

## 背景

在为通用服务逐步引入 `*Options` 体系（如 `JsonOptions`、`MarkdownOptions`）的过程中，`core.util.io` 与 `core.util.net` 模块存在若干接口与工程边界问题：
1. `IoService.copy` 仍使用散装的 `bufferSize` 与 `cancellationThreshold` 默认参数；
2. 缺乏用于追踪读写已处理字节数与总长度的流包装器（`ideas.md` 中亦曾讨论是否应与 `CancellableInputStream` / `CancellableOutputStream` 合并）；
3. `ArchiveService.extract` 与 `DownloadService.download` 的选项参数命名为 `settings`，与 `JsonService`/`MarkdownService` 的 `options` 命名不一致；
4. `DownloadOptions` 缺少失败重试策略，`DownloadService` 仅报告单调百分比 `fraction`，未展示已下载字节数、总大小与实时速率，且中断时直接写入目标路径。

## 决策与权衡

### 1. `IoOptions` 保持纯不可变策略配置，单一可选参数

- `IoOptions` 仅承载 `bufferSize`、`cancellable` 与 `cancellationThreshold`，**严禁混入运行时回调闭包或平台进度对象**，避免破坏 `data class` 的值语义与不可变前提；
- `IoService.copy` 统一收敛为单一可选参数 `options: IoOptions = IoOptions.DEFAULT`；
- 将 `ArchiveService.extract` 与 `DownloadService.download` 的参数名一并对齐为 `options`。

### 2. `Monitorable*Stream` 与 `Cancellable*Stream` 保持正交双胞胎关系

拒绝将“监控进度”强行合并进 `CancellableInputStream` / `CancellableOutputStream`：
- 许多底层读取场景（如 `ImageMetadataReader`、`TgaMetadataReader`）仅需要轻量的取消检查，不应背负字节计数与回调字段；
- `MonitorableInputStream` / `MonitorableOutputStream` 与 `Cancellable*Stream` 在命名、构造参数与扩展函数（`asMonitorable` vs `asCancellable`）上保持纯字节阈值（`IO_MONITOR_THRESHOLD = 64 KiB` vs `IO_CANCELLATION_THRESHOLD = 64 KiB`）镜像对称，不引入时间节流参数；
- 需要在关闭流时对小于阈值或尾部未触发的余量补发一次最终回调，且保证 `close()` 幂等；
- 若调用方同时需要监控与取消，可通过流包装器正交组合叠加。

### 3. `DownloadOptions` 重试边界与 `.part` 原子落位

- `DownloadOptions` 新增 `maxRetries: Int = 3` 与 `retryDelay: Duration = 1.seconds`；
- **Fail-Fast 边界**：`CancellationException`（含 `ProcessCanceledException`）、`IllegalArgumentException` 与不可恢复的 HTTP 4xx（408、429 除外）立即终止不重试；网络 `IOException`、超时、限速/停滞触发、5xx 与 SHA-256 不匹配触发线性退避重试；
- **原子落位与状态明细**：单次尝试先写入同目录 `.part` 临时文件，SHA-256 校验通过后 `Files.move(..., REPLACE_EXISTING)` 落位；重试前清理 `.part` 与重置 `RawProgressReporter.fraction(0.0)`；UI 明细文本的时间节流（150ms）留在 `DownloadService` 表现汇报层处理，不污染底层 `IoOptions`。

## 关键代码位置

- `src/main/kotlin/icu/windea/pls/core/util/io/IoOptions.kt`
- `src/main/kotlin/icu/windea/pls/core/util/io/MonitorableInputStream.kt`
- `src/main/kotlin/icu/windea/pls/core/util/io/MonitorableOutputStream.kt`
- `src/main/kotlin/icu/windea/pls/core/util/io/IoStreams.kt`
- `src/main/kotlin/icu/windea/pls/core/util/io/IoService.kt`
- `src/main/kotlin/icu/windea/pls/core/util/net/DownloadOptions.kt`
- `src/main/kotlin/icu/windea/pls/core/util/net/DownloadService.kt`
- `src/test/kotlin/icu/windea/pls/core/util/io/IoServiceTest.kt`
- `src/test/kotlin/icu/windea/pls/core/util/net/DownloadServiceTest.kt`
