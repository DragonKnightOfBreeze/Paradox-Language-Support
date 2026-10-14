# P4 进行全局代码检查时，偶发误判的 *无法解析的值表达式* 报错

> - 状态：应当已解决

全局代码检查的结果中，可能会报错 *无法解析值表达式 '20'，期望 '{...}'*。
如果重新打开项目，或者更改文件，则报错消失。

```paradox_script
size = 20
```

```cwt
## cardinality = 0..inf
size = int
## cardinality = 0..inf
size = { min = int max = int }
```

2025.11.08
- 可能来自 `ParadoxScriptExpressionBlockMatchOptimizer.optimize` 的实现或调用
- 涉及这种情况的匹配结果有时是不稳定的

2025.11.10
- 在执行后续优化后，`PlsStates.dynamicContextConfigs` 未正确设置导致的
- 需要考虑执行后续优化的时机
