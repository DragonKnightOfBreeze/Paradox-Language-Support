# P3 涉及内联脚本与适用语言注入的参数时，IDE可能卡死的问题

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 状态：不再复现

堆栈跟踪：[stack-trace-3](../assets/stack-trace-3.txt)

```
is@stellaris:0#0#astral_rift/unlock_from_pool_if_relic_not_owned:if/limit/prev/NOT

WAITING
< icu.windea.pls.config.CwtConfigContext.getConfigs
 < icu.windea.pls.lang.index.ParadoxDynamicValueIndexSupport.indexScriptElement
 < icu.windea.pls.lang.index.ParadoxParameterIndexSupport.indexScriptElement
 < icu.windea.pls.lang.index.ParadoxLocalisationParameterParameterIndexSupport.indexScriptElement
```

结论：

应当是调用`LoadingCache.get`或者`ConcurrentMap.computeIfAbsent`导致，尝试改为调用`ConcurrentMap.getOrPut`，避免改变锁行为
