{# @pebvariable name="request" type="icu.windea.pls.ai.model.requests.ManipulateLocalisationAiRequest" #}
在本地化文本中，可以使用以下特殊语法：
- 彩色文本
  - 格式：`§{colorId}{richText}§!`
  - 示例：`§R红色文本§!`
  - `{colorId}`为颜色的ID（单字符），无需处理
  - `{richText}`为嵌套的本地化文本，可以嵌套使用特殊语法，需要处理
  - 备注：结束标记`§!`可以单独存在，保留即可
- 参数
  - 格式：`${name}$`，或者`${name}|${args}$`
  - 示例：`$key$`，`$@var$`，`key|R$`
  - `{name}`为参数的名字，用来引用本地化条目、全局封装变量（`@var`）或者预定义的参数，无需处理
  - `{args}`为渲染参数，无需处理
- 命令
  - 格式：`[{text}]`
  - 示例：`[Root.getName]`
  - `{text}`为命令的文本，用来获取动态文本，无需处理
- 图标
  - 格式：`£{id}£`，或者`£{id}|{args}£`
  - 示例：`£unity£`，`£leader_skill|3£`
  - `{id}`为图标的ID，用来引用图标，无需处理
  - `{args}`为渲染参数，无需处理
{% if request.predicates.supportsConceptCommand %}
- 概念命令
  - 格式：`['{expression}']`，或者`['{expression}', {richText}]`
  - 示例：`['concept_id']`，`['concept_id', 替换文本]`
  - `{expression}`为概念的名称、别名或表达式，用来引用概念，无需处理
  - `{richText}`为嵌套的本地化文本，用来替换原始的概念文本，可以嵌套使用特殊语法，需要处理
  - 备注：需要处理的`{richText}`必须位于英文逗号后（忽略前导空白），如果无法找到，视为不存在即可
  - 备注：在游戏中，概念命令会被渲染为超链接，悬浮可以显示详细信息，且能嵌套使用
{% endif %}
{% if request.predicates.supportsTextFormat %}
- 文本格式
  - 格式：`#{expression} {richText}!#`
  - 示例：`#v 文本#!`
  - `{expression}`为文本格式的表达式，用来指定如何渲染文本，无需处理
  - `{richText}`为嵌套的本地化文本，可以嵌套使用特殊语法，需要处理
  - 备注：需要处理的`{richText}`必须位于空白之后，如果无法找到，视为不存在即可
  - 备注：结束标记`!#`可以单独存在，保留即可
{% endif %}
{% if request.predicates.supportsTextIcon %}
- 文本图标
  - 格式：`@{id} {richText}!#`
  - 示例：`@icon!`
  - `{id}`为文本图标的ID，用来引用文本图标，无需处理
{% endif %}