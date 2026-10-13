# 增强对定值（define）的支持

> 所属模块：`后端 > 语言构造支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v2.1.8
> - 日期：2026-05-04
> - 状态：已实施

说明：
- 定值（define）分为定值的命名空间和变量，它们固定声明于游戏目录的 `common/defines` 子目录中的带 `.txt` 扩展名的脚本文件中。
- 引入对应的定值规则（defineConfig），从而为定值的命名空间和变量提供快速文档文本和规则上下文。
- 定值是在（游戏目录中的）脚本文件中声明的。普通导航会跳转到覆盖项，相关规则导航才会跳转到对应的定值规则。
- 理论上，定值是取决于具体的游戏的，并且会在游戏目录中声明并列出所有可用项。
- 但是，插件目前不通过定值规则检查定值的命名空间和变量的名字的合法性（仍然检查其声明结构的合法性，如同定义和定义注入一样）。
- 插件会强制忽略名为 `define` 或 `defines` 的类型规则和声明规则（参见：`icu.windea.pls.ep.config.config.CwtForcedConfigFilterProvider`）。

相关代码位置（核心）：
- [x] `icu.windea.pls.lang.util.ParadoxDefineManager`
- [x] `icu.windea.pls.model.ParadoxDefineInfo`
- [x] `icu.windea.pls.model.ParadoxDefineNamespaecInfo`
- [x] `icu.windea.pls.model.ParadoxDefineVariableInfo`

相关代码位置（规则）：
- [x] `icu.windea.pls.config.config.delegated.CwtDefineConfig`
- [x] `icu.windea.pls.config.config.delegated.CwtDefineNamespaceConfig`
- [x] `icu.windea.pls.config.config.delegated.CwtDefineVariableConfig`

相关代码位置（IDE 功能）：
- [x] `icu.windea.pls.lang.index.ParadoxDefineNamespaceIndex` - 存根索引
- [x] `icu.windea.pls.lang.index.ParadoxDefineVariableIndex` - 存根索引
- [x] `icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch` - 查询
- [x] `icu.windea.pls.lang.search.ParadoxDefineVariableSearch` - 查询
- [x] `icu.windea.pls.lang.codeInsight.navigation.GotoDefineVariablesAction` - 导航动作
- [x] `icu.windea.pls.lang.codeInsight.navigation.GotoDefineVariablesActionHandler` - 导航动作
- [x] `icu.windea.pls.lang.codeInsight.markers.script.ParadoxDefineVariableLineMarkerProvider` - 装订线图标
- [x] `icu.windea.pls.lang.search.searchers.implementations.ParadoxDefineVariableImplementationsSearcher` - 实现查询
- [x] `icu.windea.pls.lang.codeInsight.completion.script.ParadoxDefineNameCompletionProvider` - 代码补全
- [x] `icu.windea.pls.lang.inspections.overrides.OverrideForDefineVariableInspection` - 代码检查（重载）
- [x] `icu.windea.pls.lang.inspections.overrides.IncorrectOverrideForDefineVariableInspection` - 代码检查（不正确的重载）
- [x] `icu.windea.pls.ep.resolve.config.CwtDefineVariableConfigContextProvider` - 提供规则上下文
