# 支持位置感知的数据类型

> - 模块：后端 > 规则系统
> - 状态：已变更，参见 [#snippet-match](##snippet-match)
> - 相关问题：[#340](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/340) [#413](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/413)

说明：
- 支持 *位置感知的数据类型*，适用于定义和本地化的匹配（如 `localisation|$_name,$_desc`），并替换掉现有的 *后缀感知的数据类型* （如 `localisation|_name,_desc`）。
- 例如，如果脚本表达式 `test` 匹配的数据表达式是 `localisation|$_name,$_desc`，则仅当同时存在本地化 `test_name` 和 `test_desc` 时才认为完全匹配。
- 对于匹配这类数据类型的脚本表达式，目前暂不提供对应的代码补全和用法查询。
- 对于匹配这类数据类型的脚本表达式，认为进行引用解析时无法直接解析为对应的定义/本地化化，而是将之视为一种“相关项”。
- 对于匹配这类数据类型的脚本表达式，考虑提供这类“相关项”的导航动作、装订线图标（lineMarkerProvider）和自动重命名（automaticRenamer）。

新增数据类型：
- `DefinitionWithLocations` - 替换 `SuffixAwareDefinition` - 示例：`<job>|$_name,$_desc`
- `LocalisationWithLocations` - 替换 `SuffixAwareLocalisation` - 示例：`localisation|$_name,$_desc`
- `SyncedLocalisationWithLocations` - 替换 `SuffixAwareSyncedLocalisation` - 示例：`localisation_synced|$.name,$.desc`

任务：
- ~~考虑统一位置表达式（`CwtLocationExpression`）使用的格式与这里使用的格式。~~ - 忽略或搁置
