# 开发笔记B

## 语言支持

### CWT规则文件

### 脚本语言

### 本地化语言

对于本地化语言中引用的命令（`[Root.GetName]`）

* 可以对应内置命令（详见：`Stellaris\logs\script_documentation\localizations.log`）
* 可以对应类型为`scripted_loc`的定义（引用方式：`SCOPE.NAME`，`SCOPE`是作用域名称，`NAME`是定义的名字）
* 重复的左方括号可以进行转义（`[[Text]`，这会被渲染为`[Text]`）

对于本地化语言中引用的图标（`£energy£`）：

* 前后缀必须要匹配，否则会显示为问号图标（原版游戏文件中有许多这样的解析错误）
* 可以对应同名的`gfx/interface/icons`下的dds文件（引用方式：`£NAME£`，`NAME`是不包括文件后缀名的文件名）
* 可以对应名为`GFX_text_NAME`，类型为`sprite_type`的定义（引用方式：`£NAME£`，`NAME`是图标的名字）
* 可以对应名为`GFX_NAME`，类型为`sprite_type`的定义（引用方式：`£NAME£`，`NAME`是图标的名字）

注意事项：

* `localisation_synced`中不能通过`$KEY$`的方式引用`localisation`

[Localisation modding](https://stellaris.paradoxwikis.com/Localisation_modding)

有用的命令：

```
reload text - 重载本地化文本表
switchlanguage $language - 切换使用的语言并且重载本地化文本表（$language为语言名，如：l_english）
```

另见CHANGELOG。

## 应用规则文件

* 解析cwt规则文件（`*.cwt`），用于验证脚本文件
* 日志文件（`*.log`）需要先转换为cwt规则文件后，再进行解析
* 关于scope的处理：适用于`alias[effect]`和`alias[trigger]`

特殊处理：

* stellaris：`alias_name[modifier]`除了匹配`<static_modifier>`，还匹配从日志文件中解析得到生成的/预定义的的`<modifier>`

另见CHANGELOG。

### 作用域处理

作用域处理适用于effect、trigger和modifier_rule（如script_value）。

```
alias[trigger:exists] = scope[any]
```

* `scope[X]`对应links中input_scopes与X的alias匹配的key，scope将会转为output_scope（进栈）

```
## push_scope = country
```

* 选项在subtype声明上时：如果此subtype合法，scope将会设为push_scope的值，以验证此类型
* 选项在alias声明上时：scope将会转为push_scope的值（进栈）

```
## replace_scope = { this = planet root = planet from = ship fromfrom = country }
```

* 对应system scope的scope会被给定的scope所替换

```
## scope == country
## scope = { country planet }
```

* 下面的规则只有在当前scope与其匹配时才合法

```
# scopes.cwt
"Landed title" = {        
    aliases = { landed_title "landed title" }        
    data_type_name = "Title"
}
```

* `Landed title`是需要在文档或提示中显示的文本
* `aliases`表示它如何在规则或脚本中被引用
* `data_type_name`是与localisation scopes的连接
* `is_subscope_of = { province }`用来判断是否可以视为另一个scope

```
# links.cwt
faith = {
    desc = " Unknown, add something in code registration"
    from_data = yes
    type = scope
    data_source = <faith>
    prefix = faith:
    input_scopes = { landed_title province }
    output_scope = faith
}
```

* 类似`root.owner = {...}`或者`root = { owner = {...} }`，其中的`root`和`owner`都是link（不区分大小写）
* `desc`是需要在文档或提示中显示的文本
* `input_scopes`表示可以来自哪些scope
* `output_scope`表示将会转为哪个scope（进栈）
* `from_data`表示是否由其他脚本文件生成
* `data_source`表示表达式`faith:X`中，`X`需要匹配何种CWT数据类型（如`<faith>`）
* `prefix`表示表达式`faith:X`中强制性的前缀`faith:`
* `type`用来判断其是scope还是value(`scope`, `value`, `both`)

扩展：

* `for_definition = pop_faction`：表示此link只能在类型为`pop_faction`的定义的声明中使用，且引用的`data_source`也只能来自该定义的声明中

内置的系统作用域（`system scope`）：

* `this`：当前scope
* `root`：当前声明的初始scope（事件的初始默认scope）
* `prev`：scope栈中的上一个scope
* `prev` `prevprev` `prevprevprev` `prevprevprevprev`
* `from`：调用处的scope（如事件A调用事件B，事件B中的`from`对应事件A中的调用处的scope）（触发该事件的上级scope）
* `fromfrom` `fromfromfrom` `fromfromfromfrom`

链式作用域最多嵌套5层。

### 匹配、提示和解析

* 如果dataType为`localisation`，则需要匹配任意本地化（localisation）。
* 如果dataType为`localisation_synced`，则需要匹配任意同步本地化（localisation_synced）。
* 如果dataType为`localisation_inline`，则当用引号括起时，需要匹配任意同步本地化（localisation_synced），否则视为一个字符串。
* 如果dataType为`abs_file_path`，则需要匹配任意绝对路径。
* 如果dataType为`filepath`，则需要匹配任意路径（相对于游戏或模组根路径）。
* 如果dataType为`filepath[some/path/]`，则需要在加上前缀`some/path/`后，匹配任意路径（相对于游戏或模组根路径）。
* 如果dataType为`filepath[some/path/,.ext]`，则需要在加上前缀`some/path/`和后缀`.ext`后，匹配任意路径（相对于游戏或模组根路径）。
* 如果dataType为`filename`，则需要匹配直接位于脚本文件所在目录下的文件`X`，`X`表示输入值。
* 如果dataType为`filename[some/path]`，则需要匹配位于目录`some/path`中的文件`X`，`X`表示输入值。
* 如果dataType为`icon[gfx/interfaces/ships]`，则需要匹配位于目录`gfx/interfaces/ships`中的DDS文件`X.dds`，`X`表示输入值。
* 如果dataType为`<X>`，则需要匹配类型为`X`的定义。`<X.Y>`需要匹配类型为`X`，子类型包含`Y`的定义。
* 如果dataType为`a_<X>_b`，同上，但是需要带有前缀`a_`和后缀`_b`。
* 如果dataType为`enum[X]`，则需要匹配规则`enum[X] = {...}`中的任意一项（忽略大小写）。
* 如果dataType为`value[X]`，则需要匹配规则`value[X] = {...}`中的任意一项（忽略大小写），或者来自脚本文件（将dataType为`value_set[X]`的输入值视为声明）。
* 如果dataType为`scope[X]`，则需要匹配所拥有的scope_type匹配`X`的任意target。target可能是link_value或link。
  * 如果`X`为`any`，则表示匹配任意scope_type。 
  * scope_type：即作用域类型，需要匹配来自scopes（位于`scopes.cwt`中）的规则。
  * link：类似`root.owner`的格式，即作用域，需要匹配来自`system_scopes`（位于`script_config.pls.cwt`中）和links（位于`links.cwt`中）的规则。
  * link_value：类似`trigger:xxx`的格式。需要匹配来自links（位于`links.cwt`中）的规则（匹配前缀`prefix`且`type`的值是`scope`或`both`）。
* 如果dataType为`scope_field`，则等同于`scope[any]`。
* 如果dataType为`scope_group[X]`，则需要匹配所拥有的scope_type匹配来自`scope_groups`（位于`scope_groups.cwt`中）的规则的target，target可能是link_value或link。
* 如果dataType为`value_field`，则需要匹配数字或者SV表达式。（示例：`value:xxx|xxx|xxx|` `modifier:xxx` `trigger:xxx`）
  * 类似`trigger:xxx`的格式。需要匹配来自links（位于`links.cwt`中）的规则（匹配前缀`prefix`且`type`的值是`value`或`both`）
  * 兼容`value_set[variable]`
* 如果dataType为`int_value_field`，同上，但是SV的值必须是整数类型。
* 如果dataType为`alias_keys_field[X]`，则需要匹配规则`alias[X:Y] = ...`中的`Y`，`Y`可以是任意存在的值。
* 如果key的dataType为`aliasName[X]`，value的dataType为`alias_match_left[X]`，则需要匹配规则`alias[X:Y] = ...`，`Y`可以是任意存在的值。处理时需要内联规则。
* 如果value的dataType为`single_alias_right[X]`，则需要匹配规则`single_alias[X] = ...`。处理时需要内联规则。
* 如果aliasName支持scopes，则需要额外匹配来自`system_scopes`（位于`script_config.pls.cwt`中）和links（位于`links.cwt`中）的规则。
  * 对应的CWT规则示例：`alias[trigger:scope_field] = { alias_name[trigger] = alias_match_left[trigger] }`
* 如果aliasName支持modifiers，则需要额外匹配来自modifiers（位于`modifiers.cwt`中）的规则。
  * 会被添加到别名`modifier`中

在原始CWT规则文件，以下两个规则中的`$parameter`对应scripted_effect/scripted_trigger等类型的定义声明中的参数

```
alias[effect:<scripted_effect>] = {
    ## cardinality = 1..inf
    $parameter = scalar
    ## cardinality = 1..inf
    $parameter = scope_field
}

## scope = any
alias[trigger:<scripted_trigger>] = {
    ## cardinality = 1..inf
    $parameter = scalar
    ## cardinality = 1..inf
    $parameter = scope_field
}
```

```
xxx = {
    some_effect = { PARAM = xxx }
}

some_effect = {
  xxx = $PARAM$
} 
```

### 扩展

插件补充的CWT规则文件。提供缺失的规则和额外的规则。

```cwt
types = {
  type[some_type] = {
    # ...
    localisation = {
      # key可以重名
      # 按优先级排序（从上往下优先级从高到低）
      loc2 = "#desc" # 对应此定义的名为"desc"的属性的值，所对应的本地化
      loc1 = "$_desc" # "$"是定义名称的占位符，对应名为"${definitionName}_desc"的本地化
    }
    images = {
      # key可以重名
      # 按优先级排序（从上往下优先级从高到低）
      # primary # 当其他定义需要基于另一个定义得到对应的DDS图片，而另一个定义有多种对应的DDS图片时，使用此DDS图片
      pic1 = "#icon" # 对应此定义的名为"icon"的属性的值，所对应的DDS图片（基于值的类型）
      pic2 = "#icon|#icon_frame,#frame" # icon_frame，对应此定义的名为"icon_frame"的属性的值，用于对DDS图片进行切割，获取最终需要的DDS图片
      pic3 = "gfx/interfaces/icons/$.dds" # "$"是定义名称的占位符，对应相对于游戏或模组目录，其所对应的DDS图片
      # pic4 = "#" # 如果此定义的值是字符串，则直接基于此定义的值，查找对应的DDS图片
    }
    # ...
  }
}
```

```cwt
enums = {
    complex_enum[pop_faction_parameters] = {
        # ...
        search_scope_type = definition # 查询作用域，例如，这里要求只能引用同一定义声明下的复杂枚举值，目前支持：definition
    }
}
```

```
## suppress = <inspectionToolId>
```

* 放到类型规则、子类型规则上时，不会对对应类型和子类型的定义进行特定的代码检查（目前仅限`ParadoxScriptUnresolvedExpression`）
* 放到定义声明规则中的规则上时，不会读对应的成员或表达式进行特定的代码检查
* 参见：`icu.windea.pls.lang.inspections.ParadoxScriptConfigAwareInspectionSuppressor`（暂未使用，需要验证）

另见CHANGELOG。

## 黑箱探秘

### 默认值

封装变量（scripted_variable）的默认值是0（即不存在的情况）

### 生成器

modifier（修饰符）：

* 游戏运行时会生成日志文件`modifiers.log`
* 对于任意pop_job（职业）`job_xxx`，会在游戏运行时生成modifier（修饰符）`job_xxx_add`，对应的本地化`mod_job_xxx_add`不会自动生成

trigger（触发器）：

* 游戏运行时会生成日志文件`triggers.log`

effect（效果）：

* 游戏运行时会生成日志文件`effects.log`

### 覆盖规则

> 需要进一步确认

对于定义（definition）：

* 相对路径相同的文件中的同一定义，按照加载顺序，后加载的会覆盖先加载的
* 相对路径不同的文件中的同一定义，按照文件名的字母排序顺序，UTF8编码大的会覆盖UTF编码小的（一般来说，中文文件名拥有更高的优先级）

对于本地化文本（localisation）：

* 相对路径相同的文件中的同一本地化文本，按照加载顺序，后加载的会覆盖先加载的
* 相对路径不同的文件中的同一本地化文本，按照文件名的字母排序顺序，UTF8编码大的会覆盖UTF编码小的（一般来说，中文文件名拥有更高的优先级）
* 位于`localisation/replace`目录中的文本化文本，相对于位于`localisation`目录中的本地化文本，拥有更高的优先级
* 相对于`localisation/replace`目录或`localisation`的父路径相同才能进行覆盖（文件名可以不同）

相对路径：相对于游戏根目录或模组根目录的路径，包括文件名

注意：以上规则不准确，存在一些先序覆盖的情况，需要确认

先序加载的定义类型：

* 事件（id为`test.1`和id为`test.01`的事件是同一个）

[Modding - Stellaris Wiki: Overwriting specific elements](https://stellaris.paradoxwikis.com/Modding#Overwriting_specific_elements)
[Modding - Stellaris Wiki: Mod load order](https://stellaris.paradoxwikis.com/Modding#Mod_load_order)

备注：

* 封装修正（`scripted_modifier`）可以覆盖预定义的修正`modifier`

## 其他

### 渲染dds图片

转换dds文件为png文件，然后渲染到文档注释中。

#### 方案1

使用`dds2png`工具（小巧，但是生成需要一定时间，并且生成的PNG文件可能存在错误）

* 在插件的jar的顶级目录下放入一个压缩包`dds2png.zip`，这是一个用于将dds转化为png的小工具
* 定义一个工具类`DdsToPngConverter`
* 第一次使用到这个类时：
  * 确定用户目录（linux是`~`，windows是`C://Users/xxx`，需要确定获取方式）
  * 确定用户目录下存在文件`dds2png/dds2png.exe`，不存在的情况下，将jar包中的`dds2png`解压到用户目录
  * 如果后续执行转化命令时没有转化成功，也进行上述操作
* 需要渲染dds文件时：
  * 确定dds文件的`paradoxPath`，得到对应的png文件的`paradoxPath`，如果`~/dds2png/tmp`目录中存在对应的png文件，则直接使用
  * 如果没有，则需要确定dds文件的绝对路径
  * 执行转化命令：`~/dds2png/dds2png.exe -y <dds_name> <png_name>`
  * png文件保存在`~/dds2png/tmp`这个目录中 
* 未知图标（`unknown.png`，44x44）需要保存到`~/dds2png/tmp`的顶级目录下，以便必要时直接使用

#### 方案2（选用）

使用`DDS4J`（高效，但仍然不兼容某些dds图片）

[vincentzhang96/DDS4J](https://github.com/vincentzhang96/DDS4J)

* 根据图标名解析：基于形如`£unity£`的本地化文本中的图标的名字，查找对应的名称为`GFX_text_${iconName}`、类型为`sprite`或`spriteType`的定义，然后得到名为`textureFile`的属性的值，将其作为相对于游戏或模组目录的路径，得到对应的DDS文件路径。如果无法获取，则查找文件名为`${iconName}.dds`（大小写敏感），位于游戏或模组目录中的DDS文件，得到对应的DDS文件路径。
* 根据类型为`sprite`的定义解析：得到名为`textureFile`（不区分大小写）的属性的值，将其作为相对于游戏或模组根目录的路径，得到对应的DDS文件路径。
* 根据文件名为`${iconName}.dds`（不区分大小写），位于游戏或模组根目录中的DDS文件解析：得到对应的DDS文件路径。
* 得到DDS文件路径后，将其转化为PNG文件，然后保存到`~/.pls/images`目录中，保留相对路径，并在文件名中加上UUID后缀。
* 渲染图标时，使用PNG文件的绝对路径。
