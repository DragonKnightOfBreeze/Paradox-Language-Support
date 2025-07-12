# 扩展

## Markdown

> [!NOTE]
> 
> 此章节的功能要求同时安装与启用 [Markdown](https://plugins.jetbrains.com/plugin/7793-markdown) 插件

PLS 扩展了与 Markdown 的集成，涉及链接、内联代码、代码块等。

### 链接

通过使用带有特定前缀的特定格式的链接文本，可以将 Markdown 链接解析为匹配的目标引用（定义、本地化等），
从而在编辑器视图中额外提供代码导航、快速文档等语言功能。

这也适用于其他地方的链接，例如 HTML 链接，以及快速文档的原始文本中的用于导航到 PSI 元素的链接。

![](../images/extensions/md_link_1.png)

对于不同类型的引用链接，其格式与示例如下：

**CWT 规则**（目前仅提供有限的支持）

* 格式
  * `cwt:{gameType}/{parts}`
* 示例
  * `cwt:stellaris:types/civic_or_origin`
  * `cwt:stellaris:types/civic_or_origin/origin`

**封装变量**

* 格式
  * `pdx.sv:{name}`
  * `pdx.sv:{gameType}/{name}`
* 示例
  * `pdx.sv:civic_default_random_weight`
  * `pdx.sv:stellaris:civic_default_random_weight`

**定义**

* 格式
  * `pdx.d:{typeExpression}/{name}`
  * `pdx.d:{gameType}/{typeExpression}/{name}`
* 示例
  * `pdx.d:origin_default`
  * `pdx.d:stellaris:origin_default`
  * `pdx.d:civic_or_origin.origin/origin_default`
  * `pdx.d:stellaris:civic_or_origin.origin/origin_default`

**本地化**

* 格式
  * `pdx.l:{name}`
  * `pdx.l:{gameType}/{name}`
* 示例
  * `pdx.l:origin_default`
  * `pdx.l:stellaris:origin_default`

**文件路径**（相对于游戏或模组目录）

* 格式
  * `pdx.p:{path}`
  * `pdx.p:{gameType}/{path}`
* 示例
  * `pdx.p:common/governments/civics/00_origins.txt`
  * `pdx.p:stellaris:common/governments/civics/00_origins.txt`

**修正**

* 格式
  * `pdx.m:{name}`
  * `pdx.m:{gameType}:{name}`
* 示例
  * `pdx.m:job_soldier_add`
  * `pdx.m:stellaris:job_soldier_add`

备注：

* `{gameType}` - 游戏类型ID，目前有以下可选值：`stellaris`, `ck2`, `ck3`, `eu4`, `hoi4`, `ir`, `vic2`, `vic3`（对于共享的规则分组则为`core`）。
* `{typeExpression}` - 定义类型表达式，可以仅包含基础类型（如`civic_or_origin`），也可以包含基础类型以及多个匹配的子类型，用点号分割（如`civic_or_origin.origin`）。

### 内联代码

待编写。

### 代码块

通过在 Markdown 代码块的语言ID后面注入额外的信息，可以为其中的脚本或本地化文件的片段指定游戏类型和文件路径，
插件会根据这些信息来匹配CWT规则，从而提供各种高级语言功能，如同编写实际的脚本或本地化文件一样。

![](../images/extensions/md_code_fence_1.png)

注入信息的格式与示例如下：

* 格式
  * `path={gameType}:{path}`
* 示例
  * `path=stellaris:common/armies/injected_defence_armies.txt`

更加完整的示例：

```paradox_script path=stellaris:common/armies/injected_defence_armies.txt
defense_army = {
    # ...
}
```

备注：

* `{gameType}` - 游戏类型ID，目前有以下可选值：`stellaris`, `ck2`, `ck3`, `eu4`, `hoi4`, `ir`, `vic2`, `vic3`（对于共享的规则分组则为`core`）。
* `{path}` - 模拟的相对于游戏或模组目录的文件路径。必须是合法的脚本或本地化文件的路经。

## 图表

> [!NOTE]
>
> 此章节的功能要求同时安装与启用 Diagrams 插件

> [!WARNING]
> 
> 此章节的功能以及 Diagrams 插件仅在专业版IDE中可用（例如：IntelliJ IDEA Ultimate）

待编写。