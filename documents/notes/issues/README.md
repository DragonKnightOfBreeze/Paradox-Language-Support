# 问题追踪与疑难分析 (Issues & Problem Tracking)

## 概述 / Overview

本目录记录来自日常开发调试、GitHub 仓库反馈以及 CWTools 相关仓库的待办问题清单，并对其中的疑难复杂问题（主要涉及 PSI 引用、索引一致性、缓存竞态与并发死锁等）进行深度追踪与分析。

---

This directory tracks active issue backlogs from debugging and GitHub, and archives in-depth diagnostic analyses for hard bugs (P1~Pnn).

## 问题记录

### 来自调试

- [x] 需要兼容整行或多行参数值中对双引号的特别转义
- [x] 需要兼容本地化文本中对左方括号的转义（`[[`）
- [ ] 在`PARAM = xxx`后面输入回车会产生不正确的自动缩进（应当与语言注入有关）
- [ ] `QuoteLiteralIntention`和`UnquoteLiteralIntention`不直接适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""`中的`\"v\"`）
- [ ] 对于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""`中的`\"v\"`），如果用引号括起，由于已被转义一次，会导致中断规则匹配 - IDEA自身BUG或者IDEA设计如此
- [ ] 进行代码补全时，考虑优先补全那些不需要访问索引的，能够比较快速地计算出结果的项
- [ ] 有时脚本文件中的路径引用会无法解析，不知为何？

### 来自 Github 仓库

- [x] 无法及时检测项目本地的规则文件的更改？ - 基本修复？
- [x] 无法正确注入`on_action`中事件引用的事件类型？ - 应当已解决
- [x] 重新打开项目后，IDE无法完成对脚本文件的解析？ - 未复现，不再考虑 
- [ ] `scope[species]`应当始终可以匹配`this` - 如果无法准确匹配，需要直接匹配同类型的第一个规则 - 待验证
- [x] 作用域上下文的内嵌提示显示有问题 - 应当显示在`possible = {`后面，且要求`{`之后不存在空白以外的字符
- [x] `some_trigger = value`中的`value`不应当被特别高亮

### 来自 CWTools 的 Github 仓库

- [x] [The tool cannot recognize in-script flag variables (Vic3) · Issue #76 · cwtools/cwtools-vscode (github.com)](https://github.com/cwtools/cwtools-vscode/issues/76)
- [x] [[Stellaris\] Could support tech@level grammar? · Issue #58 · cwtools/cwtools-vscode (github.com)](https://github.com/cwtools/cwtools-vscode/issues/58)
- [x] [Parsing issues in Vic3 · Issue #53 · cwtools/cwtools (github.com)](https://github.com/cwtools/cwtools/issues/53)


## 疑难问题深度分析 / Problem Analyses (P1~Pnn)

- [P1 索引时报错 `Indexing process should not rely on non-indexed file data.`](p01-indexing-rely-on-non-indexed-data.md)
- [P2 `ParadoxExpressionManager.getExpressionReferences` 中使用的缓存可能不一致的问题](p02-expression-references-cache-inconsistency.md)
- [P3 涉及内联脚本与适用语言注入的参数时，IDE可能卡死的问题](p03-inline-script-injection-freeze.md)
- [P4 进行全局代码检查时，偶发误判的 *无法解析的值表达式* 报错](p04-unresolved-expression-false-positive.md)
- [P5 索引时的偶发报错，涉及解析定义的子类型和内联脚本](p05-indexing-subtype-and-inline-script-error.md)
- [P6 来自的 `ParadoxTextBasedTargetSearcher` 和 fastutil 的索引越界异常](p06-target-searcher-fastutil-index-out-of-bounds.md)
- [P7 `Indexing process should not rely on non-indexed file data.` 对于子定义](p07-indexing-rely-on-non-indexed-data-for-subdefinitions.md)
- [P8 定义信息的缓存不一致的问题](p08-definition-info-cache-inconsistency.md)
- [P9 对于对应多个 PSI 引用的脚本表达式（如 `religion:catholic`），光标位置的用法高亮可能不正确](p09-multi-psi-reference-usage-highlighting.md)
- [P10 表达式 PSI 引用的缓存不一致的问题](p10-expression-references-cache-inconsistency.md)
- [P11 表达式 PSI 引用的缓存不一致的问题 & `Issue142Test.testVarInEffect` 可能无法通过](p11-expression-references-cache-inconsistency-issue-142.md)
- [P12 java.lang.Throwable: Write-unsafe context! Model changes are allowed from write-safe contexts only.](p12-write-unsafe-context-model-changes.md)
- [P13 表达式 PSI 引用的缓存不一致的问题 & `Issue284Test.testScopeLinkInEffect` 可能无法通过](p13-expression-references-cache-inconsistency-issue-284.md)
- [P14 在事件脚本文件中，变更文件内容后，其中特定部分的作用域推断会发生误报](p14-event-script-scope-inference-false-positive.md)
- [P15 测试过程中的 `java.lang.AssertionError: Indexing timeout`](p15-testing-indexing-timeout.md)
- [P16 测试过程中的 `java.lang.NoSuchMethodError: 'kotlin.sequences.Sequence kotlin.sequences.SequencesKt.sequenceOf(java.lang.Object)'`](p16-testing-nosuchmethoderror-sequences.md)
- [P17 启动IDE时报错 `Cannot find scheme: Islands Dark from plugins: icu.windea.pls;icu.windea.pls`](p17-cannot-find-scheme-islands-dark.md)
- [P18 启动IDE时的明显卡顿](p18-startup-freeze-profiling.md)
- [P19 与 `ParadoxLocalisationFindUsagesProvider` 有关的 SOF](p19-localisation-find-usages-sof.md)
- [P20 对于适用代码注入的参数值，光标位置的用法高亮可以不正确](p20-injected-parameter-usage-highlighting.md)
- [P21 对内联脚本目录执行全局代码检查时的 OOM](p21-inline-script-inspections-oom.md)
- [P22 对 Markdown 内联代码进行查找用法时的意外行为](p22-markdown-find-usages-unexpected-behavior.md)
