# 图标设计

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范文件类型、AST 语法节点、CWT 规则元素、装订线、动作与通用图标的设计约定，以及图标资源与 `ChronicleIcons` 的映射关系。

---

## 概述

相关代码位置：
- `icu.windea.pls.ChronicleIcons`

相关文件与目录：
- `src/main/resources/ChronicleIconMappings.json`
- `src/main/resources/icons`
- `src/main/resources/icons/expui`

说明：
- `ChronicleIconMappings.json` 是供 expui 图标组织与维护的映射清单，与 `ChronicleIcons` 的当前路径保持对齐（如 `ExtendedComplexEnumValue` 已指向 `icons/configs/extended/extendedComplexEnumValue.svg`）
- 该映射清单不是运行时图标加载的数据源，权威来源仍是 `ChronicleIcons`
- `icons/expui` 存放 New UI 变体资源，部分图标提供 `_dark.svg` 变体

## 文件类型 (fileTypes)

相关目录：
- [src/main/resources/icons/fileTypes/](/src/main/resources/icons/fileTypes/)
- [src/main/resources/icons/expui/fileTypes/](/src/main/resources/icons/expui/fileTypes/)

说明：
- CWT、Paradox Script、Paradox Localisation 三种自定义语言提供文件类型图标：文件+扩展名标签 / 正方形+字母
- Paradox CSV 使用平台 `AllIcons.FileTypes.Csv`，不单独提供自定义图标
- 规则文件、模组描述符文件：齿轮

详情：
- `cwt.svg` - 绿色+文件+CWT / 绿色+正方形+C
- `paradoxScript.svg` - 紫色+文件+TXT / 紫色+正方形+S
- `paradoxLocalisation.svg` - 红色+文件+YML / 红色+正方形+L
- `cwtConfig.svg` - 灰白色（`#9AA7B0`）+齿轮
- `modDescriptor.svg` - 蓝色（`#5D8DC3`）+齿轮

## 节点 (nodes)

相关目录：
- [src/main/resources/icons/nodes/](/src/main/resources/icons/nodes/)
- [src/main/resources/icons/expui/nodes/](/src/main/resources/icons/expui/nodes/)

说明：
- 图标大小：16x16
- 除了少数特例之外：形状+字
- 绿色：注解、接口、特质、选项、作用域、链接、指令、宏等
- 蓝色：类型、枚举、定义、语言环境等
- 红色：方法、函数、本地化等
- 紫色：属性等
- 橙色：变量、枚举值、动态值等
- 正方形：动态变体
- 菱形：特殊变体
- 花括号：块、分组等

详情（语法层）：
- `option.svg` - 紫色+圆形+O
- `scriptedVariable.svg` - 紫色+圆形+V
- `property.svg` - 紫色+圆形+P
- `value.svg` - 橙色+圆形+V
- `block.svg` - 灰色+花括号
- `parameter.svg` - 橙色+圆形+P
- `conditionalBlock.svg` - 灰色+方括号
- `localisationLocale.svg` - 蓝色+圆形+L
- `localisationProperty.svg` - 红色+圆形+L
- `localisationIcon.svg` - 橙色+圆形+I
- `localisationCommand.svg`- 橙色+圆形+C
- `localisationConceptCommand.svg` - 橙色+菱形+C
- `localisationTextFormat.svg` - 橙色+菱形+F
- `localisationTextIcon.svg` - 橙色+菱形+I

详情（语义层）：
- `type.svg` - 蓝色+圆形+T
- `definitionGroup.svg` - 蓝色+花括号
- `definition.svg` - 蓝色+圆形+D
- `definitionSnippet.svg` - 蓝色+圆形+D & 抽象形式（参考自：abstractClass）
- `localisationGroup.svg` - 红色+花括号
- `localisation.svg` - 红色+圆形+L
- `localisationSnippet.svg` - 红色+圆形+L & 抽象形式（参考自：abstractMethod）
- `defineNamespace.svg` - 橙色+花括号
- `defineVariable.svg` - 橙色+圆形+D
- `enumValue.svg` - 橙色+圆形+E
- `dynamicValue.svg` - 橙色+正方形+V
- `variable.svg` - 橙色+菱形+V
- `modifier.svg` - 紫色+圆形+M
- `trigger.svg` - 紫色+圆形+T
- `effect.svg` - 紫色+圆形+E
- `systemScope.svg` - 绿色+菱形+S
- `staticScope.svg` - 绿色+圆形+S
- `dynamicScope.svg` - 绿色+正方形+S
- `staticValueField.svg` - 绿色+圆形+F
- `dynamicValueField.svg` - 绿色+正方形+F
- `systemCommandScope.svg` - 绿色+菱形+S
- `staticCommandScope.svg` - 绿色+圆形+S
- `dynamicCommandScope.svg` - 绿色+正方形+S
- `staticCommandField.svg` - 绿色+圆形+F
- `dynamicCommandField.svg` - 绿色+正方形+F
- `databaseObjectType.svg` - 蓝色+圆形+T
- `tag.svg` - 绿色+圆形+@
- `macro.svg` - 绿色+正方形+#
- `gameRule.svg` - 蓝色+圆形+R
- `onAction.svg` - 蓝色+圆形+A
- `eventNamespace.svg` - 蓝色+花括号
- `event.svg` - 蓝色+闪电
- `shaderEffect.svg` - 红色+菱形+E
- `meshLocator.svg` - 红色+菱形+L

## 规则 (configs)

相关目录：
- [src/main/resources/icons/configs/](/src/main/resources/icons/configs/)
- [src/main/resources/icons/expui/configs/](/src/main/resources/icons/expui/configs/)

说明：
- 图标大小：16x16
- 除非另有说明，否则与节点图标的设计方案保持一致
- 对于扩展规则，统一使用：灰白色（`#9AA7B0`）

详情（标准规则）：
- `type.svg` - 蓝色+圆形+T
- `row.svg` - 蓝色+正方形+R
- `defineNamespace.svg` - 橙色+花括号
- `defineVariable.svg` - 橙色+圆形+D
- `enum.svg` - 蓝色+圆形+E
- `enumValue.svg` - 橙色+圆形+E
- `complexEnum.svg` - 蓝色+正方形+E
- `union.svg` - 蓝色+圆形+U
- `unionValue.svg` - 橙色+圆形+U
- `dynamicValueType.svg` - 蓝色+正方形+V
- `dynamicValue.svg` - 橙色+正方形+V
- `alias.svg` - 紫色+正方形+A
- `macro.svg` - 绿色+正方形+#
- `link.svg` - 绿色+圆形+L
- `localisationPromotion.svg` - 绿色+圆形+P
- `localisationCommand.svg` - 绿色+圆形+C
- `modifierCategory.svg` - 紫色+花括号
- `modifier.svg` - 紫色+圆形+M
- `trigger.svg` - 紫色+圆形+T
- `effect.svg` - 紫色+圆形+E
- `scope.svg` - 绿色+圆形+S
- `scopeGroup.svg` - 绿色+花括号
- `databaseObjectType.svg` - 蓝色+圆形+T
- `systemScope.svg` - 绿色+菱形+S
- `locale.svg` - 蓝色+圆形+L

详情（扩展规则）：
- `extendedScriptedVariable.svg` - 灰白色（`#9AA7B0`）+圆形+V
- `extendedDefinition.svg` - 灰白色（`#9AA7B0`）+圆形+D
- `extendedGameRule.svg` - 灰白色（`#9AA7B0`）+圆形+R
- `extendedOnAction.svg` - 灰白色（`#9AA7B0`）+圆形+A
- `extendedParameter.svg` - 灰白色（`#9AA7B0`）+圆形+P
- `extendedComplexEnumValue.svg` - 灰白色（`#9AA7B0`）+正方形+E
- `extendedDynamicValue.svg` - 灰白色（`#9AA7B0`）+正方形+V
- `extendedInlineScript.svg` - 灰白色（`#9AA7B0`）+正方形+I

## 装订线 (gutter)

相关目录：
- [src/main/resources/icons/gutter/](/src/main/resources/icons/gutter/)
- [src/main/resources/icons/expui/gutter/](/src/main/resources/icons/expui/gutter/)

说明：
- 图标大小：14x14
- 除非另有说明，否则与节点图标的设计方案保持一致

详情：
- `scriptedVariable.svg` - 紫色+圆形+V
- `definition.svg` - 蓝色+圆形+D
- `localisation.svg` - 红色+圆形+L
- `relatedScriptedVariables.svg` - 紫色+圆形+V & 右下角的回形针
- `relatedDefinitions.svg` - 蓝色+圆形+D & 右下角的回形针
- `relatedLocalisations.svg` - 红色+圆形+L & 右下角的回形针
- `relatedImages.svg` - Markdown 图片图标 & 右下角的回形针
- `relatedDefinitionInjections.svg` - 蓝色+圆形+D - 切面形式（参考自：aroundAdvice） & 右下角的回形针
- `defineVariable.svg` - 橙色+圆形+D
- `inlineScripts.svg` - 脚本图标 & 右下角的回形针
- `definitionInjections.svg` - 蓝色+圆形+D - 切面形式（参考自：aroundAdvice）
- `definitionInjectionTargets.svg` - Rust 目标图标

## 通用 (general)

相关目录：
- [src/main/resources/icons/](/src/main/resources/icons/)

说明：
- 除 `steam.svg` 外，多数通用图标直接复用平台图标，不单独维护自定义资源

详情：
- `steam.svg` - 自定义 Steam 图标
- `GameDirectory` / `ModDirectory` - 平台 `AllIcons.Nodes.Module`
- `EntryDirectory` - 平台 `AllIcons.Modules.SourceRoot`
- `ConfigGroupDirectory` - 平台 `AllIcons.Modules.EditFolder`
- `Library` - 平台 `AllIcons.Nodes.PpLibFolder`
- `Presentation` - 平台 `PlatformImagesIcons.ThumbnailToolWindow`

## 动作 (actions)

相关目录：
- [src/main/resources/icons/actions/](/src/main/resources/icons/actions/)
- [src/main/resources/icons/expui/actions/](/src/main/resources/icons/expui/actions/)

说明：
- 图标大小：16x16
- 除强制刷新动作外，统一使用蓝色（`#5D8DC3`）

详情：
- `gameSettings.svg` - 蓝色+齿轮
- `modSettings.svg` - 蓝色+齿轮
- `refreshConfigGroups.svg` - 蓝色+刷新箭头
- `syncConfigGroupsFromRemote.svg` - 蓝色+克隆/同步图形
- `forceRefreshConfigGroups.svg` - 红色（`#B91919`）+刷新箭头

## 编辑器动作 (editorActions)

相关目录：
- [src/main/resources/icons/editorActions/](/src/main/resources/icons/editorActions/)

说明：
- 图标大小：16x16
- 用于表格编辑，参考自 Markdown 插件

详情：
- `addColumnLeft.svg` - 左侧插入列
- `addColumnRight.svg` - 右侧插入列
- `addRowAbove.svg` - 上方插入行
- `addRowBelow.svg` - 下方插入行
