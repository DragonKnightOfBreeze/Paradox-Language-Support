# P7 `Indexing process should not rely on non-indexed file data.` 对于子定义

> - 状态：已解决

堆栈跟踪：[stack-trace-6](../assets/stack-trace-6.txt)

```
	at icu.windea.pls.lang.resolve.ParadoxDefinitionService.resolveSubtypeConfigs(ParadoxDefinitionService.kt:74) <- 可能需要访问尚未索引的父定义
	at icu.windea.pls.lang.resolve.ParadoxDefinitionService.resolveDeclaration(ParadoxDefinitionService.kt:80)
	at icu.windea.pls.model.ParadoxDefinitionInfo.doGetDeclaration(ParadoxDefinitionInfo.kt:96)
	at icu.windea.pls.model.ParadoxDefinitionInfo.getDeclaration(ParadoxDefinitionInfo.kt:67)
	at icu.windea.pls.ep.configContext.CwtBaseConfigContextProvider.getCacheKey(CwtConfigContextProviders.kt:79)
```

结论：

`ParadoxDefinitionService.resolveSubtypeConfigs` 中需要检查 `ParadoxMatchOptions.SkipIndex`，如果已设置则需要避免访问其他索引
