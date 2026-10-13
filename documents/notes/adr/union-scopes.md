# 完善作用域的匹配和合并逻辑，兼容并集作用域

> 所属模块：`后端 > 其他语义相关`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.2
> - 日期：2026-08-25
> - 状态：已实施

说明：
- 主要目的是为了兼容 Stellaris 中的 `carrier` 作用域。
- 或者称之为“带继承关系的作用域”。
- 考虑复用并应用作用域规则的字段 `is_subscope_of`。

备注：
- `scopeConfig.is_subscope_of` 应在使用时保证是正确的（这意味着不再检查是否存在意外的递归），如果不正确，应在解析规则时就打印警告日志。
- 基于继承关系的匹配和合并需要递归进行。
- （2026-09-07）~~父作用域不会匹配子作用域。~~
- （2026-09-07）基于继承关系的匹配采用并集策略（这意味着父作用域匹配子作用域，同时子作用域也匹配父作用域）。

作用域的匹配逻辑（scope -> scopeToMatch）：
- scope 是输入的数据（从上下文中获取或推断）。
- scopeToMatch 应来自可信的数据源（规则文件或代码实现）。
- 如果 scope 为 `null`，则直接匹配。
- 如果 scopeToMatch 为 `any`，则直接匹配（scopeToMatch 不应为 `?`，不处理这种情况）。
- 如果 scope 为 `any` 或 `?`，则直接匹配。
- 如果 scope 的 ID 完全匹配 scopeToMatch，则匹配（比较字符串，值相等，不忽略大小写）。
- 如果 scope 存在别名（来自 `scopeConfig.aliases`），则尝试用这些别名完全匹配 scopeToMatch。
- 如果 scope 存在父作用域或子作用域（来自 `scopeConfig.is_subscope_of`），则尝试用这些作用域的 ID 和别名完全匹配 scopeToMatch。这需要递归进行。
- 如果 scope 存在提升（来自 `scopeContext.promotions`，而这来自 `localisationPromotionConfig.supportedScopes`），则尝试用这些提升的 ID 和别名完全匹配 scopeToMatch。

作用域的合并逻辑（scope -> scopeToMerge）：
- scope 是输入的数据（从上下文中获取或推断）。
- scopeToMerge 是另一个输入的数据（从上下文中获取或推断）。
- 如果两者都为 `null`，则返回 `null`。
- 如果其中一项为 `null`，则返回另一项。
- 如果其中一项为 `any`，则返回 `any`。
- 如果其中一项为 `?`，则返回 `?`。
- 如果两者的 ID 完全匹配，则返回第一个（比较字符串，值相等，不忽略大小写）。
- 如果存在别名（来自 `scopeConfig.aliases`），则尝试用这些别名进行完全匹配，返回第一个。
- 如果存在父作用域（来自 `scopeConfig.is_subscope_of`），则尝试使用这些父作用域的 ID 和别名进行完全匹配。这需要递归进行。

额外备注：
- 作用域的匹配方式包括通配匹配（如 `any` `?`）、直接匹配（检查作用域 ID 是否值相等）、间接匹配（别名形式、继承形式等）。
- 对于间接匹配，通过直接检查 `configGroup.scopeModel` 中的属性 `base2MatchedScopes: Int2ObjectOpenHashMap<IntArraySet>`，可以减少不必要的字符串比较，优化性能。
- 目前并未更改快速文档的逻辑：规则文件中（的规则声明中）已经给出了所有必要的信息，不需要通过快速文档再次重复提示。

相关代码位置：
- [x] `icu.windea.pls.lang.scope.ParadoxScopeMatchService`
- [x] `icu.windea.pls.lang.scope.ParadoxScopeMergeService`
- [x] `icu.windea.pls.config.configGroup.CwtScopeModel`
