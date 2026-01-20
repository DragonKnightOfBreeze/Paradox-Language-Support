# 规则

<!-- TODO 继续改进与润色 -->

## 规则分组 {#config-group}

### 概述 {#config-group-summary}

PLS 基于由 CWT 规则文件组成的规则分组，实现了诸多语言功能。

规则分组可以有不同的来源，而对于同一来源的规则分组，又区分为各自游戏类型的规则分组，以及所有游戏类型共享的规则分组。

你可以在插件的设置页面（`Paradox Language Support > Config`）中按需启用或禁用各类规则分组。
这些规则分组的父目录，以及远程规则分组的仓库地址，也都可以在对应的插件设置页面中进行配置。

参考链接：

- [仓库一览](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/cwt)

### 来源 {#config-group-sources}

#### 内置规则分组 {#config-group-builtin}

- 位置：`config/{gameType}`
- 位于插件压缩包中的插件 jar 包中。
- `{gameType}` 为游戏类型 ID[^1]，对于共享的规则分组则为 `core`。
- 共享的内置规则分组总是会被启用。

#### 远程规则分组 {#config-group-remote}

- 位置：`{configDir}/{dirName}`
- `{configDir}` 为包含所有远程规则分组目录的父目录，可在插件设置页面中自定义。
- `{dirName}` 为仓库目录的名字，对于共享的规则分组则为 `core`。

更改配置后，PLS 会自动从配置的远程仓库中克隆和拉取这些规则分组。

#### 全局的本地规则分组 {#config-group-local}

- 位置：`{configDir}/{gameType}`
- `{configDir}` 为包含所有本地规则分组目录的父目录，可在插件设置页面中自定义。
- `{gameType}` 为游戏类型 ID[^1]，对于共享的规则分组则为 `core`。

其中的规则文件由用户自定义，适用于所有项目。对它们的更改需要手动确认导入。

#### 项目的本地规则分组 {#config-group-project-local}

- 位置：`{configDirName}/{gameType}`
- `{configDirName}` 为项目的本地规则目录的名字，直接位于项目根目录下，默认为 `.config`，可在插件设置页面中自定义。
- `{gameType}` 为游戏类型 ID[^1]，对于共享的规则分组则为 `core`。

其中的规则文件由用户自定义，仅适用于当前项目。对它们的更改需要手动确认导入。

### 覆盖方式 {#config-group-override}

规则会按照文件路径和规则 ID 进行后序覆盖。

读取规则时，插件会依次遍历内置规则分组、全局的本地规则分组以及项目的本地规则分组。
共享的规则分组由所有游戏类型共享，会在对应游戏类型的规则分组之前被遍历。

例如，如果你在项目根目录下的规则文件 `.config/stellaris/modifiers.cwt` 中编写了自定义的规则，它将完全覆盖插件内置的修正规则。
因为插件内置的修正规则位于插件 jar 包中的规则文件 `config/stellaris/modifiers.cwt` 中，它们的文件路径都是 `modifiers.cwt`。
如果此自定义的规则文件中没有任何内容，应用后插件将无法解析脚本文件中的任何修正。

## 规则文件 {#config-file}

### 概述 {#config-file-summary}

CWT 规则文件使用一种特殊的文件格式，可以视为 Paradox 脚本语言的变种。它的文件扩展名为 `.cwt`。

### 语法 {#config-file-syntax}

CWT 规则文件的基本语法如下所示：

```cwt
# both equal sign (`=`, `==`) and not equal sign (`<>`, `!=`) can be used as the k-v separator (also available in options)
# options and values can be mixed in option clauses (`{...}`)
# properties and values can be mixed in clauses (`{...}`)

### documentation comment
## option = option_value
## option_0 = { k = v }
## option_value
prop = {
    # line comment
    k = v
    v
}
```

关于更加详细的语法说明，可以参考：

- [附录：语法参考](ref-syntax.md)中的[对应章节](ref-syntax.md#cwt)

## 自定义 {#customization}

### 编写规则文件 {#write-config-files}

关于每种 CWT 规则的更加详细的编写规范，目前可以参考：

- 插件内置的规则文件。它们分别位于插件仓库的 `cwt/core` 目录下，以及各个规则仓库中。
- [附录：规则格式参考](ref-config-format.md)
- CWTools 的[指引文档](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)

### 导入规则文件 {#import-config-files}

你可以选择启用[远程规则分组](#config-group-remote)，其中的规则文件来自各个远程规则仓库。
或者，你也可以选择启用[本地规则分组](#config-group-local)或[项目的本地规则分组](#config-group-project-local)，并在对应的规则目录下编写自定义规则文件。  
这些文件可用于完善插件内置的规则，或是增强插件功能。

当检测到有变更时，编辑器右上角的悬浮工具栏会出现刷新按钮。  
点击确认导入后，这些自定义规则文件的更改将会被应用。
之后，IDE 会在后台重新解析已打开的文件。  

注意：如果规则文件的更改会影响索引逻辑（如新增定义类型、修改某定义类型的匹配条件等），你可能需要重新索引整个项目（这可能需要数分钟），以确保在涉及到这些更改的场合，插件能正常工作。

[^1]: 目前有以下可选值：`stellaris`, `ck2`, `ck3`, `eu4`, `eu5`, `hoi4`, `ir`, `vic2`, `vic3`。对于共享的规则分组则为 `core`。