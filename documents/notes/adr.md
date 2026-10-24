# 架构决策档案

## 概述

本目录存放插件的自包含架构决策档案（RFC-style ADRs）。

记录技术选型、依赖替换、语言文法与语义支持、规则系统模型扩展，以及未来平台多进程或异步化模型等架构演进的决策因果。文档通常自包含：背景与问题分析、决策方案、关键代码位置、使用说明与实施任务清单。一经实施即封存，永不过期。

## 目录索引

### 构建

#### 依赖

- [将 JSON 序列化框架从 jackson 改为 kotlinx-serialization](adr/replace-jackson-with-kotlinx-serialization.md)

### 后端（语法支持）

#### 通用

#### 脚本文件

- [概述](adr/script-syntax-summary.md)
- [引号相关](adr/script-syntax-quotes.md)
- [字符边界相关](adr/script-syntax-character-edge-cases.md)
- [条件化块相关](adr/script-syntax-conditional-blocks.md)
- [高级插值](adr/script-syntax-advanced-interpolations.md)
- [未整理](adr/script-syntax-misc.md)

#### 本地化文件

- [概述](adr/localisation-syntax-summary.md)
- [引号相关](adr/localisation-syntax-quotes.md)
- [字符边界相关](adr/localisation-syntax-character-edge-cases.md)
- [字符串变体、标签与上下文标签](adr/localisation-syntax-grammatical.md)
- [高级富文本](adr/localisation-syntax-advanced-rich-text.md)

#### CSV 文件

- [概述](adr/csv-syntax-summary.md)
- [引号相关](adr/csv-syntax-quotes.md)

#### CWT 文件

- [概述](adr/cwt-syntax-summary.md)
- [引号相关](adr/cwt-syntax-quotes.md)
- [字符边界相关](adr/cwt-syntax-character-edge-cases.md)

### 后端

#### 通用

- [重构 `CwtDataTypes` `CwtConfigTypes` - 改为偏好使用 lambda 式构建器](adr/cwt-data-types-and-config-types-builder.md)
- [重构作用域和作用域方法 - 改为偏好使用成员方法作为作用域方法，而非带上下文参数的扩展方法](adr/scope-and-scope-methods-refactor.md)
- [重构 path/expression/complexExpression/config/configExpression - 更改解析器与解析方法的代码风格](adr/expression-parser-and-resolve-style-refactor.md)
- [关于上下文对象（context object）](adr/context-object.md)
- [集合选型（collection selection）](adr/collection-selection.md)
- [懒加载的魔法与24字节的摩擦](adr/lazy-value-magic.md)
- [引号模式与引号处理](adr/quote-pattern.md)
- [转义模式与转义处理](adr/escape-pattern.md)
- [颜色处理](adr/color-pattern.md)
- [提供 `PushStream`](adr/push-stream.md)

#### 通用基础设施

- [刷新文件与重解析文件](adr/refresh-and-reparse-files.md)

#### 语言基础设施

- [重新设计 PSI 接口（脚本文件）](adr/script-psi-interface-redesign.md)
- [重新设计 PSI 接口（本地化文件）](adr/localisation-psi-interface-redesign.md)
- [重新设计 PSI 接口（CWT 文件）](adr/cwt-psi-interface-redesign.md)
- [完善脚本文件的 lexer](adr/script-lexer-refactor.md)
- [完善本地化文件的 lexer](adr/localisation-lexer-refactor.md)
- [完善 lexer 的重置逻辑](adr/lexer-reset.md)
- [区分“语法宏”与“语义宏”](adr/macro.md)

#### 规则系统

- [支持并集规则](adr/union-configs.md)
- [并集规则和别名规则的规则展开](adr/expansion-of-union-configs-and-alias-configs.md)
- [重构 `CwtDataExpression` （元数据不应公开可变，以及其他）](adr/data-expression-refactor.md)
- [重命名：`optionData` -> `optionMetadata`](adr/option-metadata.md)
- [模板表达式与模式匹配的相关重构](adr/template-expression-and-pattern-match.md)
- [关于作为通配策略的数据类型的相关重构](adr/wildcard-data-types.md)
- [支持位置感知的数据类型](adr/location-aware-data-types.md)
- [支持片段匹配](adr/snippet-match.md)
- [支持匹配带前缀标签的脚本表达式](adr/prefix-tag-match.md)

#### 索引

- [为合并索引引入线程本地的上下文](adr/merged-index-thread-context.md)

#### 分析

- [优化 rootInfo 和 rootMetadata 的解析逻辑](adr/root-info-and-metadata-resolution.md)

#### 匹配与解析

- [重构规则上下文，以提供更严格的对无法解析的表达式的代码检查](adr/config-context-refactor.md)
- [完善规则匹配的预处理和后续处理逻辑](adr/config-match-processing.md)

#### 其他语义相关

- [完善作用域的匹配和合并逻辑，兼容并集作用域](adr/union-scopes.md)
- [引入 `ParadoxAnnotatedData`](adr/annotated-data-api.md)
- [引入 `ParadoxData` 和 `ParadoxDataSource`](adr/data-api.md)
- [重构 service/manager 层的 relatedItems/presentableItems 相关的方法](adr/related-items-and-presentable-items.md)

#### 语言构造支持

- [增强对定值（define）的支持](adr/define-support.md)
- [增强对数据类型 `ShaderEffect` 和 `MeshLocator` 的支持](adr/shader-effect-and-mesh-locator-support.md)
- [[CK3/VIC3/EU5] 完善对定值引用表达式（defineReferenceExpression）和数组定值表达式（arrayDefineReferenceExpression）的支持](adr/define-reference-expression-support.md)
- [[CK3/VIC3/EU5] 完善对标签集表达式（tagsExpression）的支持](adr/tags-expression-support.md)
- [完善对 CSV 文件与 rowConfig 的支持](adr/csv-and-row-config-support.md)
- [支持内联定义](adr/inline-definition.md)

#### 语言功能支持

- [语句操作服务 & 相关的意向动作和代码检查](adr/statement-operations-intentions-and-inspections.md)
- [不正确的文件编码的代码检查](adr/incorrect-file-encoding-inspection.md)
- [内联脚本文件的文件级别提示](adr/inline-script-file-level-hints.md)
- [重构和完善各个 `ElementDescriptionProvider` 和 `FindUsagesProvider`](adr/element-descriptions.md)
- [重构和完善与 `ItemPresentation` 相关的代码](adr/item-presentations.md)
- [整理部分语言功能的入口，移动到更合适的位置](adr/language-feature-entry-arrangement.md)
- [修复来自 `UnresolvedExpressionInspection` 的误报](adr/unresolved-expression-inspection-fix.md)
- [修复来自 `MissingExpressionInspection` 的误报](adr/missing-expression-inspection-fix.md)
- [修复来自 `TooManyExpressionInspection` 的误报](adr/too-many-expression-inspection-fix.md)

#### 工具

- [特殊路径与链接](adr/special-paths-and-urls.md)

### 测试

#### 通用

- [重构测试作用域方法，提取到 `ChronicleTestScope`](adr/test-scope.md)

#### 基准测试

- [完善基准测试](adr/add-benchmarks.md)

### 分析与设计

#### 调优

- [性能分析](analysis/performance-profiling.md) - 作为分析笔记
- [内存分析](analysis/memory-profiling.md) - 作为分析笔记

#### UI 相关

- [图标设计](../design/icon-design.md) - 作为设计笔记
- [配色方案](../design/color-scheme.md) - 作为设计笔记
