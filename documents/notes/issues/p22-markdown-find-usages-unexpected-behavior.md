# P22 对 Markdown 内联代码进行查找用法时的意外行为

> - 版本：v3.0.4
> - 日期：2026-10-03
> - 状态：已解决

Markdown 文件中的部分内联代码（如 `ArrayList()`）可以通过查找用法指向到意外位置（如 `effects.cwt` 中的部分 effect）。

备注：无法在调试环境（IDEA 2025.2）上复现，无论是群星游戏目录，还是新建 Kotlin 项目 - 可能由 Markdown 插件间接导致，考虑更改调试环境（IDEA 20026.2）。

用例：

```
- `ArrayList` ✖
- `ArrayList()`
- `ArrayList(...)`
- `java.util.ArrayList` ✖
- `java.util.ArrayList()`
- `java.util.ArrayList(...)`
- `import ArrayList`
- `import ArrayList()`
- `import ArrayList(...)`
- `import java.util.ArrayList`
- `import java.util.ArrayList()`
- `import java.util.ArrayList(...)`
```

分析：

- 可能来自：`icu.windea.pls.lang.search.searchers.usages.CwtConfigSymbolUsagesSearcher` ✖
- 可能来自：`icu.windea.pls.lang.search.searchers.usages.CwtConfigUsagesSearcher` ✖
- 可能来自：`icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeReferenceProvider` ✖
- 可能来自：`org.intellij.plugins.markdown.model.psi.MarkdownSymbolUsageSearcher` ✖
- 来自：`org.intellij.plugins.markdown.lang.references.backtick.BacktickReference`
  - 直接来自：`icu.windea.pls.lang.navigation.CwtConfigSymbolChooseByNameContributor`

堆栈：

```
com.intellij.codeInsight.navigation.actions.GotoDeclarationOnlyHandler2.gotoDeclaration
>com.intellij.codeInsight.navigation.impl.fromTargetData
>>org.intellij.plugins.markdown.lang.references.backtick.BacktickReference.multiResolve
>>>org.intellij.plugins.markdown.lang.references.backtick.BacktickReference.tryResolve
>>>-org.intellij.plugins.markdown.lang.references.backtick.BacktickReference.resolveWithContributor // shouldSearchInSymbols(name)
>>>->icu.windea.pls.lang.navigation.CwtConfigSymbolChooseByNameContributor.processElementsWithName
```

结论：在 `icu.windea.pls.lang.navigation.CwtConfigSymbolChooseByNameContributor.processElementsWithName` 中，未按参数 `name` 过滤查询结果。
