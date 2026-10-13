# P18 启动IDE时的明显卡顿

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - IDE 版本：IDEA 2025.2
> - 版本：v3.0.2
> - 日期：2026-09-02
> - 状态：可忽略 & 已优化

线程转储：[thread-dump-1871846717.txt](../assets/thread-dump-1871846717.txt)

分析：
- 可能与规则分组的初始化有关。
- 需要 profiling。

```
(total time)
icu.windea.pls.config.config.CwtFileConfigResolver.resolve(VirtualFile, CwtConfigGroup, String) - 317.88s
icu.windea.pls.config.config.CwtFileConfigResolver.resolve(CwtFile, CwtConfigGroup, String) - 315.441s
icu.windea.pls.config.util.CwtConfigResolverManager.getConfigs(PsiElement, CwtFile, CwtConfigGroup) - 298.793s
icu.windea.pls.config.config.CwtPropertyConfigResolver.resolve(CwtProperty, CwtFile, CwtConfigGroup) - 296.614s
icu.windea.pls.config.util.CwtConfigResolverManager.getOptionConfigs(CwtMember, CwtConfigGroup) - 279.015s
icu.windea.pls.cwt.CwtParserDefinition.createElement(ASTNode) - 267.685ss // 需要研究
-type == OPTION - 267.6s // 疑惑？
icu.windea.pls.ep.config.configGroup.CwtFileBasedConfigGroupProcessor.process(CwtConfigGroup, Continuation) - 71.264s

icu.windea.pls.config.config.CwtFileConfigResolver.resolve(VirtualFile, CwtConfigGroup, String) - 317.88s
>icu.windea.pls.config.config.CwtPropertyConfigResolver.resolve(CwtProperty, CwtFile, CwtConfigGroup) - 296.614s (collapsed)
>-icu.windea.pls.config.util.CwtConfigResolverManager.getOptionConfigs(CwtMember, CwtConfigGroup) - 277.964s
>--icu.windea.pls.core.PlatformExtensionsKt.forEachChild() - 273.248s
>---com.intellij.psi.impl.source.tree.LeafPsiElement.getNextSibling() - 269.343s
>--->com.intellij.psi.impl.source.tree.CompositeElement.getPsi() - 269.343s
>--->-com.intellij.psi.impl.source.tree.CompositeElement.createPsiNoLock() - 267.6s
>--->--icu.windea.pls.cwt.CwtParserDefinition.createElement(ASTNode) - 267.6s
>--->---icu.windea.pls.cwt.psi.CwtElementTypes$Factory.createElement(ASTNode) - 267.6s (cpu time: <<51ms, total time: 267.6s)
// 这是为什么？
// 这样来看，IDE 冻结的根源可能完全不是骨规则分组的初始化，而是其他地方
```

初步结论：
- 经验证，在 IDEA 2026.2.1 上不存在此问题（至少数秒内就能一切就绪）。可能是 IDE 版本兼容性问题。
- 相关的分析笔记：[analysis-startup-freeze-thread-dump-1871846717.md](../../ai-notes/analysis/analysis-startup-freeze-thread-dump-1871846717.md)

后续建议（来自 deepseek）：
- 若需要兼容 2025.2，可考虑防御性优化：把逐文件 `readAction` 合并为分组/批量读动作；项目打开时懒初始化非 core 游戏分组；限制 `forEachConcurrent` 的并发度。
- 验证手段：在 2025.2 上用 async-profiler wall 模式复现，确认 `RunSuspend.await`/`InternalReadAction.readLoop` 的等待占比。
- 若不考虑 2025.2 的兼容性，可参照 P17 的处理方式，将其标记为 IDE 版本兼容性问题而忽略。

后续优化：
- [x] 减少 `CwtFileBasedConfigGroupProcessor` 中的 `readAction` 的数量。每次处理时仅按照步骤分为总计4个。不再在解析和处理文件时，分别对每个文件执行1次 `readAction`。
