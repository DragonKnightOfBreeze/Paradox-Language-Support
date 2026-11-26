# 机制分析、现场排查与性能剖析

## 概述

本目录属于**不可变历史卷宗**（除长期跟进的性能与内存剖析记录外，其余单次现场排查与专题分析皆为特定版本下的历史定格快照）。

涵盖：
- **性能与内存长期剖析**：按测试轮次（如 A1~A7、B1 等）追踪真实大型游戏环境下的索引耗时、堆内存开销与 JMH 基准数据；
- **现场排查快照**：针对特定 Bug、死锁、线程卡顿或栈溢出（SOF）的线程转储与堆栈现场剖析；
- **底层领域机制分析**：针对规则系统匹配、覆盖策略、作用域推断与模组导入导出等底层机制的专题调研。

## 备注

笔记中应当包含必要的元数据，例如：版本、日期、状态，等等。

格式示例如下：

```
# （一级标题）

> - 版本：v3.0.0
> - 日期：2026.01.01

（笔记内容）
```

## 目录索引

### 性能与内存剖析

- [性能分析](analysis/performance-profiling.md) - 构建索引与完成项目分析时的耗时剖析与优化轮次
- [内存分析](analysis/memory-profiling.md) - 构建索引与完成项目分析时的堆内存占用与优化轮次
- [3.0.5 基准测试分析](analysis/benchmarks-3.0.5.md)

### 现场排查快照

- [与 `ParadoxLocalisationFindUsagesProvider` 有关的 SOF（P19）](analysis/p19-localisation-find-usages-sof.md)
- [IDE 启动冻结（P18，线程转储 1871846717）](analysis/startup-freeze-thread-dump-1871846717.md)
- [代理内置 shell 执行 Gradle 命令的行为（Windows）](analysis/opencode-windows-gradle-timeout.md)

### 底层机制专题

- [规则系统 覆盖规则 — 更新记录](analysis/cwt-override-rules-updates.md)
- [规则系统 覆盖规则](analysis/cwt-override-rules.md)
- [规则系统 规则](analysis/cwt-rules.md)
- [模组导入导出](analysis/mod-import-export.md)
- [模组开发 作用域](analysis/mod-scopes.md)
