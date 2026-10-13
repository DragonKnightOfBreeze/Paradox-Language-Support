# P9 对于对应多个 PSI 引用的脚本表达式（如 `religion:catholic`），光标位置的用法高亮可能不正确

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 状态：已解决

```
# {link} = {link}:{definition}
religion = religion:catholic
```

分析：
- 光标放到 `religion:` 上时，可能会错误地高亮 `catholic`
- 光标放到 `catholic` 上时，可能会错误地高亮 `religion:`
- 光标放到 `religion:catholic` 的结尾时，可以正确地高亮 `catholic`
- ~~似乎仅在涉及到对定义的引用时，才可能出现这个问题~~ - 即使对于 `event_target:xxx` 也可能出现
- 可能与 `PsiReference.isReferenceTo` 有关？
- 可能与 `referencesSearch` 有关？
- IDE 有时会报错 `expressionReferences` 缓存不一致，可能与这个有关？

可能与这个堆栈有关：[stack-trace-9](../assets/stack-trace-9.txt)

即使对以下文件片段中的命令（如 `GetIcon`），也有可能会错误地高亮。

```
l_simp_chinese:
 job_evosymbiont_plural_with_icon: "[calculator_physicist.GetIcon][calculator_biologist.GetIcon][calculator_engineer.GetIcon]$job_evosymbiont_plural$"
```

最终解决方案：
- 添加 `icu.windea.pls.inject.injectors.fix.IdentifierHighlightingComputerCodeInjector`
