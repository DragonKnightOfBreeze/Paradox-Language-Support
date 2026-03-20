# 附录：附加资源参考

<!--
@doc-meta
本文档介绍项目中不直接属于插件代码或用户文档、但对模组作者和感兴趣的开发者仍有实用价值的附加资源。
包括审计与统计脚本、语法高亮定义文件等。

@see scripts/
@see docs/.vuepress/highlighters/
@see docs/.vuepress/plugins/
@see docs/.vuepress/configs/highlighters.ts
-->

## 概述 {#overview}

本文档介绍项目中附带的**附加资源**——它们不直接属于插件源代码或面向用户的文档，但对于模组作者、规则文件协助维护者以及对插件项目感兴趣的人仍然具有参考价值。

目前涵盖两类资源：

- **审计与统计脚本**：位于 `scripts/` 目录下的 Python 脚本，用于对插件代码、CWT 规则文件和游戏文件进行统计分析和热点审计。
- **语法高亮定义**：位于 `docs/.vuepress/highlighters/` 目录下的高亮语法定义文件，为 CWT、Paradox Script、Paradox Localisation 和 Paradox CSV 提供代码块语法高亮。

## 审计与统计脚本 {#scripts}

<!-- @see scripts/ -->

项目在 `scripts/` 目录下提供了一组 Python 脚本，用于对项目自身和 Paradox 游戏生态进行多维度的统计分析。这些脚本大部分均可独立运行，无需安装额外依赖（仅使用 Python 标准库，`mod_insights.py` 除外），并支持多种输出模式。

### 通用特性 {#scripts-common}

所有审计脚本共享以下特性：

- **输出模式**：默认输出到控制台；`--summary` 输出精简摘要；`--markdown` 生成完整的 Markdown 报告文件（默认保存到 `tmp/reports/` 下的带时间戳目录）。
- **输出重定向**：`--output FILE` 可指定输出文件路径。
- **自动生成文件检测**：部分脚本会识别并单独统计自动生成的文件（如文件名含 `.gen.` 或路径含 `/generated/`）。

### 脚本一览 {#scripts-list}

#### code_stats.py {#script-code-stats}

<!-- @see scripts/code_stats.py -->

**插件代码统计**。遍历 `src/main` 和 `src/test` 下的 Kotlin（`.kt`）和 Java（`.java`）源文件，按目录统计总行数、空行数、注释行数和代码行数，以及每个文件的最小/最大/平均行数。

```shell
python scripts/code_stats.py [--summary] [--markdown] [--output FILE]
```

#### code_hotspots.py {#script-code-hotspots}

<!-- @see scripts/code_hotspots.py -->

**插件代码分布与热点审计**。提供两个互补视图：按包统计文件数和行数分布；列出超过阈值（默认 500 行）的大文件热点。

```shell
python scripts/code_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]
```

#### config_stats.py {#script-config-stats}

<!-- @see scripts/config_stats.py -->

**CWT 规则文件统计**。遍历 `cwt/` 目录下各规则仓库中的 `.cwt` 文件，按仓库统计行数指标。CWT 的注释分类遵循语言规范：行注释（`#`）和文档注释（`###`）计为注释行，选项注释（`##`）计为代码行。

```shell
python scripts/config_stats.py [--summary] [--markdown] [--output FILE]
```

#### config_hotspots.py {#script-config-hotspots}

<!-- @see scripts/config_hotspots.py -->

**CWT 规则文件分布与热点审计**。按子目录统计 `.cwt` 文件分布，列出超过阈值（默认 1000 行）的大文件热点，并单独报告自动生成文件的比例。

```shell
python scripts/config_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]
```

#### game_file_stats.py {#script-game-file-stats}

<!-- @see scripts/game_file_stats.py -->

**游戏文件统计**。自动检测本地安装的 Paradox 游戏（通过 Steam 注册表或默认路径），遍历每个游戏的入口目录，统计脚本文件（`.txt`、`.gfx`、`.gui` 等）、本地化文件（`.yml`）和 CSV 文件（`.csv`）的行数指标。游戏类型、入口信息和路径检测逻辑与插件 Kotlin 源代码同步。

```shell
python scripts/game_file_stats.py [--summary] [--markdown] [--output FILE]
```

#### game_file_hotspots.py {#script-game-file-hotspots}

<!-- @see scripts/game_file_hotspots.py -->

**游戏文件分布与热点审计**。按一级子目录统计文件分布，列出超过阈值（默认 10000 行）的大文件热点，并单独报告自动生成文件。

```shell
python scripts/game_file_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]
```

#### mod_insights.py {#script-mod-insights}

<!-- @see scripts/mod_insights.py -->

**模组生态洞察**。扫描本地安装的 Paradox 游戏的 Steam 创意工坊模组，检测开发环境（IDEA / VSCode / 未知），并通过 Steam 创意工坊公开 API 获取浏览量、订阅数和收藏数等统计信息。

开发环境的检测基于文件系统特征（如 `.idea/`、`.vscode/`、`_cwtools/`、`.config/*.cwt` 等），不保证完全准确——许多模组作者在上传时会排除 IDE 配置目录。

```shell
python scripts/mod_insights.py [--markdown] [--output FILE]
```

#### run_reports.py {#script-run-reports}

<!-- @see scripts/run_reports.py -->

**批量报告生成器**。一键运行上述六个审计脚本（不含 `mod_insights.py`），以 `--markdown` 模式生成报告，保存到 `documents/reports/{timestamp}/` 目录下。

```shell
python scripts/run_reports.py
```

## 语法高亮定义 {#highlighters}

<!-- @see docs/.vuepress/highlighters/ -->

项目为 CWT、Paradox Script、Paradox Localisation 和 Paradox CSV 四种语言提供了语法高亮定义文件，用于在 Markdown 文档的代码块中实现语法着色。这些定义文件供项目文档站点（基于 VuePress）使用，也可在其他支持相应格式的环境中复用。

### 高亮方案 {#highlighters-schemes}

语法高亮定义以三种格式提供，覆盖主流的代码高亮引擎：

- **TextMate 语法**（`docs/.vuepress/highlighters/text-mate/`）：标准的 `.tmLanguage.json` 格式，是其他两种方案的基础语法定义。TextMate 语法被广泛支持——VS Code、Shiki、GitHub 等均基于此格式。
- **Shiki 语言注册**（`docs/.vuepress/highlighters/shiki/`）：TypeScript 模块，加载对应的 TextMate 语法文件并适配为 Shiki 的 `LanguageRegistration` 接口。
- **Prism 语言定义**（`docs/.vuepress/highlighters/prism/`）：JavaScript 模块，使用 Prism 的 token 模式 API 独立定义语法规则。

四种语言各有一套完整的定义文件：

| 语言 | TextMate | Shiki | Prism |
|---|---|---|---|
| CWT | `cwt.tmLanguage.json` | `shiki-cwt.ts` | `prism-cwt.js` |
| Paradox Script | `paradox-script.tmLanguage.json` | `shiki-paradox-script.ts` | `prism-paradox-script.js` |
| Paradox Localisation | `paradox-localisation.tmLanguage.json` | `shiki-paradox-localisation.ts` | `prism-paradox-localisation.js` |
| Paradox CSV | `paradox-csv.tmLanguage.json` | `shiki-paradox-csv.ts` | `prism-paradox-csv.js` |

### VuePress 集成 {#highlighters-vuepress}

<!-- @see docs/.vuepress/plugins/ -->
<!-- @see docs/.vuepress/configs/highlighters.ts -->

项目文档站点使用 [VuePress](https://vuepress.vuejs.org/) + [vuepress-theme-hope](https://theme-hope.vuejs.press/) 构建。为了在文档代码块中支持上述四种语言的语法高亮，项目实现了对应的 VuePress 插件（位于 `docs/.vuepress/plugins/`），分为 Prism 和 Shiki 两套：

- **Prism 插件**（`docs/.vuepress/plugins/prism/`）：在 `extendsMarkdown` 钩子中注册 Prism 语言定义。
- **Shiki 插件**（`docs/.vuepress/plugins/shiki/`）：封装 `@vuepress/plugin-shiki`，注入自定义语言注册。

高亮引擎的选择通过 `docs/.vuepress/configs/highlighters.ts` 中的 `usePrism` 标志控制。**当前使用 Shiki** 作为高亮引擎（`usePrism = false`）。切换引擎只需修改该标志，对应的插件和语言注册会自动切换。

### 复用说明 {#highlighters-reuse}

这些高亮定义文件可以在项目文档站点之外复用：

- **TextMate 语法**（`.tmLanguage.json`）可直接用于 VS Code 扩展、Shiki 集成、GitHub Linguist 等任何支持 TextMate 语法的场景。
- **Prism 语言定义**可在任何使用 Prism.js 的网站或工具中通过 `registerXxx(Prism)` 注册使用。
- **Shiki 语言注册**可在任何使用 Shiki 的项目中通过工厂函数直接引入。
