# P21 对内联脚本目录执行全局代码检查时的 OOM

> - 版本：v3.0.3
> - 日期：2026-09-22
> - 状态：已解决

如果在 Stellaris 游戏目录中尝试对内联脚本目录（`common/inline_scripts`）执行全局代码检查，可能会导致 OOM，需要排查，尝试优化。

Classes:

| Class                                                             | Count      | Shallow   | Retained  |
|-------------------------------------------------------------------|------------|-----------|-----------|
| icu.windea.pls.config.config.CwtPropertyConfigDelegate            | 39,537,988 | 1.27 GB   | 1.25 GB   |
| icu.windea.pls.config.config.CwtPropertyConfigImplWithConfigs     | 10,083,624 | 564.68 MB | 2.73 GB   |
| icu.windea.pls.lang.match.ParadoxMatchCandidate                   | 18,005,920 | 432.14 MB    | 3.43 GB   |
| icu.windea.pls.config.config.CwtPropertyConfigImpl                | 8,095,274  | 387.61 MB | 658.83 MB |
| icu.windea.pls.config.config.CwtPropertyConfigDelegateWithConfigs | 2,745,372  | 109.81 MB | 299.35 MB |
| ...                                                               | ...        | ...       | ...       |

Biggest Objects:

| Biggest Objects                                                                                                                                                                              | Shallow | Retained  | Comment                                         |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|-----------|-------------------------------------------------|
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:38) 0x7245885d0               | 24 B    | 1.25 GB   | size=7972436, elementType=ParadoxMatchCandidate |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:38) 0x716923fc8               | 24 B    | 892.63 MB | size=5314957, elementType=ParadoxMatchCandidate |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:38) 0x748c4da10               | 24 B    | 809.96 MB | size=5314957, elementType=ParadoxMatchCandidate |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:38) 0x741edd9d0               | 24 B    | 783.43 MB | size=5314957, elementType=ParadoxMatchCandidate |
| icu.windea.pls.config.configGroup.CwtConfigGroupService 0x7045d57f8                                                                                                                          | 24 B    | 23.24 MB  |                                                 |
| icu.windea.pls.config.configGroup.CwtConfigGroupService 0x7066b5950                                                                                                                          | 24 B    | 21.19 MB  |                                                 |
| com.google.common.collect.RegularImmutableList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collect(ParadoxMatchCandidateService.kt:18) 0x719ca4680          | 16 B    | 12.78 MB  | size=163328, elementType=CwtPropertyConfig?     |
| com.google.common.collect.RegularImmutableList, GC Root: Java Frame: icu.windea.pls.lang.inspections.script.scope.IncorrectScopeInspection.check(IncorrectScopeInspection.kt:45) 0x7d9750948 | 16 B    | 8.58 MB   | size=33176, elementType=CwtPropertyConfig?      |
| com.google.common.collect.RegularImmutableList, GC Root: Java Frame: icu.windea.pls.lang.inspections.script.scope.IncorrectScopeInspection.check(IncorrectScopeInspection.kt:45) 0x7ef0d85f8 | 16 B    | 5.21 MB   | size=25338, elementType=CwtValueConfig?         |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:36) 0x719ca4668               | 24 B    | 5.19 MB   | size=40963, elementType=ParadoxMatchCandidate   |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:36) 0x7444ea810               | 24 B    | 4.89 MB   | size=40963, elementType=ParadoxMatchCandidate   |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:36) 0x7471b3d10               | 24 B    | 4.83 MB   | size=40963, elementType=ParadoxMatchCandidate   |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:36) 0x753f66868               | 24 B    | 2.67 MB   | size=27308, elementType=ParadoxMatchCandidate   |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:36) 0x7bc8391f8               | 24 B    | 2.22 MB   | size=18205, elementType=ParadoxMatchCandidate   |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectMatched(ParadoxMatchCandidateService.kt:44) 0x7feec3e08                      | 24 B    | 1.45 MB   | size=12136, elementType=ParadoxMatchCandidate   |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:36) 0x75dd07858               | 24 B    | 1.19 MB   | size=12136, elementType=ParadoxMatchCandidate   |
| com.intellij.util.SmartList, GC Root: Java Frame: icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry(ParadoxMatchCandidateService.kt:36) 0x76d2fa8b0               | 24 B    | 1.02 MB   | size=8090, elementType=ParadoxMatchCandidate    |
| ...                                                                                                                                                                                          | ...     | ...       | ...                                             |

分析：
- 初步分析是由于内存中驻留的规则对象（以及 `ParadoxMatchCandidate`）过多导致的。
- 可能与内联脚本（inline script）以及别名规则（alias config） 导致的内联操作有关。

关键代码入口：
- `icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry` L38

堆栈追踪：

```
icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectFromAliasEntry
-icu.windea.pls.config.manipulation.CwtConfigManipulationService.inlineAlias(icu.windea.pls.config.config.CwtPropertyConfig, java.util.Collection<java.lang.String>) // 断点：result.size >= 5000
--icu.windea.pls.config.manipulation.CwtConfigManipulationService.deepCopyConfigs // 断点：result.size >= 5000
---icu.windea.pls.config.manipulation.CwtConfigManipulationService.doDeepCopyConfigs // 断点：result.size >= 5000
-icu.windea.pls.lang.match.ParadoxExpressionMatchService.getMatchedAliasKeys // configGroup.aliasGroups["effect"]?.keys -> size=1056
--icu.windea.pls.lang.match.ParadoxExpressionMatchService.matchScriptExpression
---icu.windea.pls.ep.match.expression.ParadoxScriptConstantExpressionMatcher.match
----icu.windea.pls.model.expressions.ParadoxExpressionBase.matchesRegex

icu.windea.pls.lang.resolve.ParadoxConfigService.flattenConfigsForConfigContext
icu.windea.pls.lang.resolve.ParadoxConfigService.flattenConfigsForConfigContext L266 `val parentConfigs = parentContext.getConfigs(options)` // parentConfigs.size=163328

0 = {CwtPropertyConfigDelegate@63208} (property) position = int
1 = {CwtPropertyConfigDelegate@63209} (property) order_by = value_field
2 = {CwtPropertyConfigDelegate@63210} (property) inverse = bool
```

线程转储：[thread-dump-202609212154.txt](../assets/thread-dump-202609212154.txt)

结论：
- 在 `icu.windea.pls.lang.match.ParadoxExpressionMatchService.getMatchedAliasKeys` 中，由于兼容带参数的表达式，但未预先通过正则过滤来匹配 aliasKey，导致展开后的规则的规模过高。

后续优化：
- [x] 在 `ParadoxConfigExpansionService` 中，新增 `expandAndMatchUnion` `expandMatchedUnion` `expandAndMatchAliasKeys` `expandMatchedAliasKeys` 并应用，移除旧的相关方法
- [x] 在 `ParadoxConfigExpansionService.expandAndMatchAliasKeys` 中，如果表达式带参数，预先通过正则匹配来过滤 aliasKey（通过遍历 `configGroup.aliasModel.name2ConstKeys[aliasName]`），以降低时间复杂度
