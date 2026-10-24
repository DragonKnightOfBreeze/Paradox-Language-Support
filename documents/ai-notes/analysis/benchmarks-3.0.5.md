# 3.0.5 基准测试分析

> 类型：基准测试归档与分析
> 日期：2026-10-24
> 报告归档：`documents/reports/benchmarks/20261024_214112/`
> 分支：`dev/3.0.5-dev`
> 提交：`4bf80a7faa`（运行时工作区为 dirty 状态）

## 概述

本文档分析 3.0.5 阶段已有的全部 JMH 基准测试结果，并给出当前可复用的性能结论。

- 基准框架：JMH `1.37`
- Java：JetBrains Runtime `21.0.6`
- OS：Windows 10（amd64），16 个处理器
- Gradle：`9.8.0`
- 结果模式：`AverageTime`
- 报告目录：`documents/reports/benchmarks/20261024_214112/`

本次覆盖的基准测试类：

- `StdlibFastExtensionsBenchmark`
- `StdlibFastExtensionsScalingBenchmark`
- `StdlibFastExtraExtensionsBenchmark`
- `CollectionFastBuildersBenchmark`
- `CollectionFastExtensionsBenchmark`
- `CollectionFastExtensionsHotPathBenchmark`
- `CollectionFastExtensionsScalingBenchmark`
- `CollectionFastExtraExtensionsBenchmark`
- `CollectionFastExtraExtensionsHotPathBenchmark`
- `CollectionFastExtraExtensionsScalingBenchmark`
- `AntMatcherBenchmark`
- `AntFromRegexBenchmark`
- `GlobMatcherBenchmark`
- `PathMatcherBenchmark`
- `TextMatcherBenchmark`
- `SimilarityMatchServiceBenchmark`

### 数据说明

- 第一轮全量运行达到 shell 时限，在 `AntMatcher` 中途被终止；随后单独重跑了 `AntMatcher`、`GlobMatcher`、`PathMatcher`、`SimilarityMatchService`、`TextMatcher`，再将两批 JSON 合并生成最终报告。
- 因而归档数据来自同一环境，但不是严格意义上的单次连续 JVM 运行；跨类比较时应保留这一限制。
- `SimilarityMatchServiceBenchmark` 使用 `@Fork(1)`，其余基准通常使用 `@Fork(2)`。
- 报告包含时间主指标，不包含 GC profiler 数据。内存结论来自此前带有 `-Dchronicle.benchmark.profilers=gc` 的专项运行，不在本归档中重复。
- 1M 规模、集合过滤和前缀/后缀匹配等场景的误差可能较大；结论优先看方向，不应把单次绝对值当作精确常量。

## 匹配器

### `AntMatcher` vs `AntFromRegex`

| 场景 | AntMatcher（ns/op） | AntFromRegex（ns/op） | 观察 |
|---|---:|---:|---|
| `matches` | 216.950 | 214.441 | 基本持平 |
| `matchesIgnoreCase` | 167.905 | 218.818 | AntMatcher 约快 23% |
| `matchesDeepMemberPath` | 435.548 | 683.607 | AntMatcher 约快 36% |
| `matchesLongFilePath` | 784.959 | 2264.889 | AntMatcher 约快 2.9 倍 |
| `matchesFirstInPatternSet` | 1168.270 | 775.399 | Regex 参考约快 34% |
| `matchesMiss` | 137.248 | 179.726 | AntMatcher 约快 24% |
| `matchesUniquePatterns` | 148.148 | 852.853 | AntMatcher 约快 5.8 倍 |

结论：

- 纯 Kotlin 实现在深层路径、长路径、唯一模式和 miss 路径上优势明显。
- 在“多个简单模式中较早命中”的场景下，预编译正则参考更快；这说明不能把“纯 Kotlin 一定更快”作为绝对结论。
- `AntMatcher` 的主要价值还包括无正则依赖、语义可控和缓存行为可预测。

### `GlobMatcher`

| 场景 | GlobMatcher（ns/op） | Regex 参考（ns/op） | 观察 |
|---|---:|---:|---|
| `matches` | 11.039 | 57.162 | 约快 5.2 倍 |
| `matchesIgnoreCase` | 17.329 | 55.793 | 约快 3.2 倍 |
| `matchesLongInput` | 359.715 | 919.917 | 约快 2.6 倍 |
| `matchesMiss` | 9.946 | — | 快速失败路径很短 |

短输入和常规匹配下，`GlobMatcher` 明显优于正则参考；`ignoreCase` 有额外成本，但仍保持明显优势。

### `PathMatcher`

| 场景 | PathMatcher（ns/op） | 直接实现（ns/op） |
|---|---:|---:|
| `matchesStringExact` | 1.455 | 1.450 |
| `matchesStringPattern` | 10.585 | 10.399 |
| `matchesStringAny` | 2.058 | 1.307 |
| `matchesListExact` | 14.669 | 11.753 |
| `matchesListPattern` | 64.207 | 54.951 |
| `matchesListAny` | 19.093 | — |
| `relative` | 9.903 | 8.248 |

结论：

- 精确匹配和模式匹配与直接实现基本持平。
- `any`、列表封装和 `relative` 有约 20%～57% 的额外开销，但绝对值仍然很低。
- 当前没有迫切的重构必要，除非这些路径在真实 profiling 中成为热点。

### `TextMatcher`

| 场景 | 当前实现（ns/op） | 惯用/参考实现（ns/op） |
|---|---:|---:|
| `matchesInt` | 4.620 | `toIntOrNull`：6.391；正则：41.239 |
| `matchesIntWithoutLeadingUnary` | 3.339 | — |
| `matchesFloat` | 6.264 | `toDoubleOrNull`：15.488；正则：75.963 |
| `matchesFloatStrictDot` | 6.272 | 正则：48.964 |
| `matchesIntPercentageField` | 4.180 | `dropLast + toIntOrNull`：16.143 |
| `matchesFloatPercentageField` | 5.704 | `dropLast + toDoubleOrNull`：28.403 |
| `matchesDateField` | 125.503 | 预检查 + formatter：245.122 |
| `matchesDateFieldValid` | 204.771 | — |
| `matchesDateFieldInvalid` | 13.188 | — |

结论：

- 整数、浮点数、百分比字段的手写扫描器都明显快于常见惯用形式和正则，保留这些实现是合理的。
- `matchesDateField` 加入形态预检查后，混合输入和无效输入显著改善；当前归档中混合输入约 **125.5 ns/op**，无效输入约 **13.2 ns/op**。
- 当前 `matchesDateFieldWithPrecheck` 是“外部预检查 + 生产实现已有预检查”的重复检查，已不再适合作为生产实现的对照；后续可清理这个基准方法。
- `matchesDateFieldValid` 约 **204.8 ns/op**，相比历史探测值偏高；可能与预检查、缓存服务和运行方差有关，值得在最终性能收尾时复核。

### `SimilarityMatchService`

| candidateSize | Direct（us/op） | Warm（us/op） | Warm 相对 Direct |
|---:|---:|---:|---:|
| 2000 | 127.682 | 45.050 | 约快 2.8 倍 |
| 5000 | 342.097 | 131.880 | 约快 2.6 倍 |
| 10000 | 699.035 | 263.035 | 约快 2.7 倍 |

结论：

- 候选规模从 2k 增长到 10k 时，耗时约按线性增长。
- 缓存收益在不同规模下保持稳定，约 2.6～2.8 倍。
- 该结果支持保留缓存路径；后续如果调整缓存容量，需要同时关注容量边界测试。

## 字符串 `@Fast` 扩展

### `equalsFast`

| 场景 | `@Fast`（ns/op） | 标准实现（ns/op） | 观察 |
|---|---:|---:|---|
| ASCII 相等 | 6.478 | 4.147 | 约慢 56% |
| ASCII `ignoreCase` | 7.024 | 7.522 | 约快 7%，但误差较大 |
| 非 ASCII 相等 | 7.148 | 3.657 | 约慢 95% |

长字符串：

| 场景 | 长度 | `@Fast`（ns/op） | 标准实现（ns/op） |
|---|---:|---:|---:|
| 完全相等 | 100 | 27.876 | 4.630 |
| 完全相等 | 10000 | 2375.134 | 190.841 |
| 完全相等 | 1000000 | 235951.183 | 20637.104 |
| 末位不匹配 | 1000000 | 247040.986 | 21589.936 |
| `ignoreCase` 混合 | 1000000 | 742537.230 | 763133.929 |

结论：

- 标准 `String.equals` 在短 ASCII 和长 ASCII 上都明显更快，长字符串下约 9～11 倍。
- `ignoreCase` 在短字符串和长字符串上都基本持平，没有稳定优势。
- 这进一步验证了 `equalsFast` 和 `equalsAnyFast` 的弃用决定；生产调用已回退到标准实现。

### `trimFast` / `splitFast` / `joinToStringFast`

| 场景 | `@Fast`（ns/op） | 标准实现（ns/op） | 观察 |
|---|---:|---:|---|
| `trimFast` | 12.011 | 12.719 | 约快 6% |
| `splitFast` | 34.920 | 42.565 | 约快 18% |
| `splitFast(limit=2)` | 24.824 | 28.765 | 约快 14% |
| `joinToStringFast` | 60.575 | 71.126 | 约快 15% |
| `joinToStringFast` + transform | 97.447 | 95.877 | 基本持平 |

结论：

- `trimFast` 收益较小；`splitFast` 和 `joinToStringFast` 在对应场景下仍有稳定收益。
- 带 transform 的 `joinToStringFast` 没有优势，使用时需要区分场景。

## 集合 `@Fast` 扩展

### 固定规模 64

| 场景 | `@Fast`（ns/op） | 标准实现（ns/op） | 观察 |
|---|---:|---:|---|
| `forEachFast` | 33.129 | 35.345 | 约快 6% |
| `forEachIndexedFast` | 35.982 | 38.012 | 约快 5% |
| `forEachReversedFast` | 33.322 | 33.477 | 基本持平 |
| `mapFast` | 163.392 | 238.241 | 约快 31% |
| `mapNotNullFast` | 416.026 | 249.925 | 约慢 66% |
| `flatMapFast` | 1836.317 | 2141.942 | 约快 14% |
| `filterFast` | 383.296 | 223.848 | 约慢 71% |
| `filterNotNullFast` | 357.001 | 243.800 | 约慢 46% |
| `filterIsInstanceFast` | 224.084 | 182.955 | 约慢 22% |
| `findFast` | 19.191 | 20.272 | 约快 5% |
| `findLastFast` | 21.633 | 22.156 | 基本持平 |
| `allFast` | 36.158 | 41.775 | 约快 13% |
| `anyFast` | 1.687 | 1.770 | 基本持平 |
| `noneFast` | 35.703 | 36.643 | 基本持平 |
| `processFast` | 33.785 | 37.628 | 约快 10% |
| `dropFast(0)` | 0.738 | 29.327 | 约快 40 倍 |
| `dropFast(1)` | 162.121 | 177.329 | 约快 9% |
| `dropFast(nearEnd)` | 2.996 | 2.785 | 标准实现略快 |

结论：

- `mapFast`、`flatMapFast`、`dropFast(0)` 是固定规模下的明确赢家。
- `filterFast`、`filterNotNullFast`、`mapNotNullFast`、`filterIsInstanceFast` 在单次 64 元素基准中仍然较慢；后续已针对 `when` 分支顺序做过优化，但当前归档仍显示这些方法的整体收益不稳定。
- `dropFast(0)` 的收益来自直接返回原列表；标准 `drop(0)` 仍会复制。
- `dropFast(nearEnd)` 没有收益，标准实现已经走 O(1) 路径。

### 小规模热路径

模拟 `X * Y * Z` 的重复小集合操作，每批处理 256 个小集合。核心结果：

| 场景 | size 0 | size 1 | size 4 | size 64 |
|---|---:|---:|---:|---:|
| `forEachFast` / `forEach` | 0.480 / 0.523 | 2.716 / 4.916 | 5.522 / 7.018 | 37.111 / 39.579 |
| `mapFast` / `map` | 0.563 / 3.586 | 5.143 / 13.730 | 25.122 / 34.761 | 385.386 / 433.892 |
| `filterFastNone` / `filterNone` | 0.531 / 4.081 | 3.332 / 5.800 | 5.213 / 8.843 | 35.562 / 54.043 |
| `filterFastHalf` / `filterHalf` | 0.560 / 3.894 | 5.500 / 10.015 | 20.161 / 24.407 | 223.293 / 324.909 |
| `dropFast(0)` / `drop(0)` | 0.349 / 0.577 | 0.339 / 3.959 | 0.358 / 18.194 | 0.414 / 32.120 |
| `dropFast(1)` / `drop(1)` | 0.579 / 0.611 | 0.641 / 0.604 | 14.981 / 19.879 | 130.151 / 166.817 |
| `dropFastHalf` / `dropHalf` | 0.861 / 0.908 | 0.940 / 4.253 | 13.325 / 16.151 | 76.675 / 100.067 |

结论：

- 热路径下 `mapFast`、`filterFast`、`dropFast(0)` 的优势非常明确。
- `forEachFast` 在 1/4 元素时有优势；0 和 64 元素时收益较小。
- `filterFast` 在“多数为空/少量结果”的热路径中表现良好，不能仅根据单次大集合结果否定它。
- `dropFast(0)` 仍然是时间上的特例。

### 规模变化

| 场景 | size=100 | size=10000 | size=1000000 |
|---|---:|---:|---:|
| `mapFast` / `map` | 230.305 / 347.021 | 35891.011 / 48700.047 | 10296429.722 / 11485440.222 |
| `filterFastHalf` / `filterHalf` | 576.853 / 353.839 | 33961.513 / 34222.813 | 4519968.231 / 4970567.093 |
| `filterFastNone` / `filterNone` | 53.432 / 53.424 | 4809.980 / 5811.748 | 920369.420 / 937483.956 |
| `filterFastAll` / `filterAll` | 766.238 / 411.810 | 45742.108 / 49037.638 | 9865688.865 / 10506151.733 |
| `forEachFast` / `forEach` | 54.438 / 52.665 | 4296.074 / 4554.864 | 905256.538 / 907555.958 |
| `dropFast(0)` / `drop(0)` | 0.610 / 40.476 | 0.616 / 4218.678 | 0.624 / 5738716.793 |
| `dropFast(1)` / `drop(1)` | 224.964 / 261.080 | 22625.896 / 34375.737 | 8471393.436 / 9213873.560 |
| `dropFastHalf` / `dropHalf` | 101.131 / 165.676 | 10856.627 / 16157.816 | 994827.545 / 1607177.958 |

结论：

- `mapFast` 的收益随规模增大而缩小，但到 1M 仍有约 10%～34% 的时间优势。
- `filterFast` 的时间表现依赖命中率和规模；小规模高命中率时可能更慢，热路径/低命中率时更有优势。
- `dropFast(0)` 在所有规模下都保持常数时间；标准实现随规模线性增长。
- `dropFast(1)` 和 `dropFastHalf` 在大规模下仍有收益，但收益幅度与数据规模、GC 和缓存状态有关。

### 构建器

| 场景 | size 0 | size 1 | size 4 | size 64 |
|---|---:|---:|---:|---:|
| `buildImmutableList` / `List(size)` | 0.594 / 2.491 | 1.742 / 6.907 | 10.177 / 13.568 | 87.288 / 137.445 |
| `buildImmutableSet` / `buildSet` | 0.607 / 4.352 | 1.740 / 5.280 | 59.619 / 6.998 | 573.899 / 50.464 |
| `asImmutableList` / `toList` | 1.947 / 0.610 | 1.727 / 2.386 | 1.749 / 27.079 | 1.741 / 54.318 |

结论：

- `buildImmutableList` 在所有非空规模下都优于 `List(size)`，当前实现值得保留。
- `buildImmutableSet` 在 0/1 元素时更快，但 4/64 元素时分别慢约 8.5 倍和 11.4 倍；这验证了弃用决定。
- `asImmutableList` 是非空列表的零拷贝包装，优势显著；空数组时标准空列表更快，符合实现特性。

### 前缀/后缀切片

固定规模 64 的热路径和规模变化结果：

| 场景 | size=4 | size=64 | size=100 | size=10000 | size=1000000 |
|---|---:|---:|---:|---:|---:|
| `removePrefixOrNullOne` / 参考 | 20.649 / 22.315 | 137.929 / 299.901 | 194.900 / 262.006 | 22544.539 / 31264.754 | 4120524.185 / 5453146.928 |
| `removePrefixOrNullHalf` / 参考 | 19.229 / 20.081 | 126.964 / 152.375 | 149.400 / 175.388 | 18215.992 / 17599.648 | 2072957.233 / 2843111.051 |
| `removeSuffixOrNullOne` / 参考 | 20.643 / 25.093 | 171.071 / 231.581 | 203.473 / 316.282 | 21020.203 / 33840.033 | 4064948.071 / 6129306.606 |
| `removeSuffixOrNullHalf` / 参考 | 20.083 / 23.718 | 145.170 / 171.203 | 149.153 / 229.113 | 17662.909 / 22397.174 | 2091157.160 / 2880330.930 |

结论：

- 短前缀/短后缀和大部分成功匹配场景在较大规模下有稳定收益。
- 在 0/1 元素时两者都很快，差异主要来自固定开销，不能把 fast 版本当作小切片优化。
- 失败路径的收益较小，甚至可能略慢；这与预检查/分支和 `subList` 访问有关。
- 当前结果支持保留 `removePrefixOrNull` / `removeSuffixOrNull`，但不建议把它们用于所有切片场景。

## 结论与后续行动

### 已确认的结论

- 标准 `String.equals` / `equalsIgnoreCase` 不应被自定义 ASCII 实现替代；`equalsFast`、`equalsAnyFast` 已弃用并迁移。
- `buildImmutableSet` 在 4+ 元素场景下明显不如 `buildSet`，已弃用并建议使用标准实现。
- `buildImmutableList`、`asImmutableList` 仍值得保留。
- `mapFast`、`flatMapFast`、`dropFast(0)` 是高价值优化。
- `forEachFast`、`forEachIndexedFast` 适合小集合热路径，但不是所有规模的通用优化。
- `filterFast`、`filterNotNullFast`、`mapNotNullFast`、`filterIsInstanceFast` 需要按调用形态判断，不能仅凭单一规模下结论。
- `TextMatcher` 的整数/浮点/百分比扫描器明显优于惯用实现和正则；日期预检查显著改善无效/混合输入。
- `AntMatcher`、`GlobMatcher` 在深层、长路径、唯一模式等场景具有明确优势；但正则参考在“简单模式集合快速命中”时可能更快。
- `SimilarityMatchService` 的缓存收益稳定，候选规模增长时仍保持约 2.6～2.8 倍。

### 后续建议

- 清理 `TextMatcherBenchmark.matchesDateFieldWithPrecheck`：生产实现已经内置预检查，该基准现在是双重检查，不再代表真实对照。
- 复核 `matchesDateFieldValid` 相对历史值的偏高情况；必要时补充 GC profiler 和更长时间测量。
- 对 1M 规模和过滤类基准，后续如果需要最终报告，可提高 fork 数或测量时间，降低大误差。
- 为内存敏感路径（`forEachFast`、`mapFast`、`filterFast`、`dropFast`）保留专项 `gc` profiler 结果；本次归档不包含 GC 数据。
- 将本报告作为 3.0.5 阶段的性能基线；跨机器/跨 JVM 不直接比较绝对值。

## 复现

```bash
# 完整基准（本次因时限拆分为两批执行）
./gradlew benchmarkReport --no-daemon --console=plain

# 仅生成报告（输入目录已有 JSON 时）
./gradlew benchmarkReport -x benchmark \
  -Dchronicle.benchmark.reportDir=<benchmark-json-dir> \
  --no-daemon --console=plain
```

报告归档：`documents/reports/benchmarks/20261024_214112/`

- `summary.md`：所有结果的汇总表
- `metadata.md`：环境、Git 与运行信息
- `*.json`：JMH 原始结果
- `*.txt`：按类拆分的 Markdown 表格
