# 性能与内存分析 (Performance & Memory Profiling)

## 概述 / Overview

本目录存放由维护者人工长期记录与跟进的系统体质分析笔记。

不同于 `ai-notes/analysis/` 下针对单次偶发 Bug 或特定报错的现场排查快照，本目录下的分析是**持续维护的活笔记（Living Profiling Records）**，主要按测试轮次（如 A1~A7、B1 等）记录插件在真实大型游戏环境（如 Stellaris 游戏目录）下的性能、耗时与内存开销走势。

---

This directory contains long-term performance and memory profiling records maintained manually by the project maintainers.

Unlike issue-driven snapshots in `ai-notes/analysis/`, documents here are **living profiling records** tracking index latency, throughput, and memory consumption across versions and testing rounds.

## 索引 / Index

- [性能分析 (Performance Profiling)](performance-profiling.md) - 构建索引与完成项目分析时的耗时剖析与优化轮次
- [内存分析 (Memory Profiling)](memory-profiling.md) - 构建索引与完成项目分析时的堆内存占用与优化轮次
