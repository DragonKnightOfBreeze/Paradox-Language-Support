# Appendix: Additional Resources Reference

<!--
@doc-meta
This document introduces additional resources in the project that are not directly part of the plugin code or user documentation,
but are still useful for mod authors and interested developers — including audit/statistics scripts and syntax highlighting definitions.

@see scripts/
@see docs/.vuepress/highlighters/
@see docs/.vuepress/plugins/
@see docs/.vuepress/configs/highlighters.ts
-->

## Overview {#overview}

This document introduces the **additional resources** bundled with the project — they are not directly part of the plugin source code or user-facing documentation, but still hold reference value for mod authors, config file co-maintainers, and anyone interested in the plugin project.

Currently, two categories of resources are covered:

- **Audit and statistics scripts**: Python scripts under `scripts/`, used for statistical analysis and hotspot auditing of plugin code, CWT config files, and game files.
- **Syntax highlighting definitions**: Highlighting grammar definition files under `docs/.vuepress/highlighters/`, providing code block syntax highlighting for CWT, Paradox Script, Paradox Localisation, and Paradox CSV.

## Audit and Statistics Scripts {#scripts}

<!-- @see scripts/ -->

The project provides a set of Python scripts under `scripts/` for multidimensional statistical analysis of the project itself and the Paradox game ecosystem. Most of these scripts can be run independently, require no additional dependencies (only the Python standard library, with the exception of `mod_insights.py`), and support multiple output modes.

### Common Features {#scripts-common}

All audit scripts share the following features:

- **Output modes**: Default output to console; `--summary` for a condensed summary; `--markdown` to generate a full Markdown report file (saved by default to a timestamped directory under `tmp/reports/`).
- **Output redirection**: `--output FILE` to specify the output file path.
- **Auto-generated file detection**: Some scripts identify and separately report auto-generated files (e.g. filenames containing `.gen.` or paths containing `/generated/`).

### Script Overview {#scripts-list}

#### code_stats.py {#script-code-stats}

<!-- @see scripts/code_stats.py -->

**Plugin code statistics**. Walks Kotlin (`.kt`) and Java (`.java`) source files under `src/main` and `src/test`, reporting per-directory totals for lines, blank lines, comment lines, and code lines, as well as min/max/average lines per file.

```shell
python scripts/code_stats.py [--summary] [--markdown] [--output FILE]
```

#### code_hotspots.py {#script-code-hotspots}

<!-- @see scripts/code_hotspots.py -->

**Plugin code distribution and hotspot audit**. Provides two complementary views: per-package file count and line distribution; and a list of large-file hotspots exceeding a configurable threshold (default 500 lines).

```shell
python scripts/code_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]
```

#### config_stats.py {#script-config-stats}

<!-- @see scripts/config_stats.py -->

**CWT config file statistics**. Walks `.cwt` files across config repositories under `cwt/`, reporting per-repository line metrics. CWT comment classification follows the language specification: line comments (`#`) and doc comments (`###`) are counted as comment lines, while option comments (`##`) are counted as code lines.

```shell
python scripts/config_stats.py [--summary] [--markdown] [--output FILE]
```

#### config_hotspots.py {#script-config-hotspots}

<!-- @see scripts/config_hotspots.py -->

**CWT config file distribution and hotspot audit**. Reports per-subdirectory `.cwt` file distribution, lists large-file hotspots exceeding a configurable threshold (default 1000 lines), and separately reports the proportion of auto-generated files.

```shell
python scripts/config_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]
```

#### game_file_stats.py {#script-game-file-stats}

<!-- @see scripts/game_file_stats.py -->

**Game file statistics**. Auto-detects locally installed Paradox games (via Steam registry or default paths), walks each game's entry directories, and reports line metrics for script files (`.txt`, `.gfx`, `.gui`, etc.), localisation files (`.yml`), and CSV files (`.csv`). Game types, entry info, and path detection logic are synced from the plugin's Kotlin sources.

```shell
python scripts/game_file_stats.py [--summary] [--markdown] [--output FILE]
```

#### game_file_hotspots.py {#script-game-file-hotspots}

<!-- @see scripts/game_file_hotspots.py -->

**Game file distribution and hotspot audit**. Reports per-first-level-subdirectory file distribution, lists large-file hotspots exceeding a configurable threshold (default 10000 lines), and separately reports auto-generated files.

```shell
python scripts/game_file_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]
```

#### mod_insights.py {#script-mod-insights}

<!-- @see scripts/mod_insights.py -->

**Mod ecosystem insights**. Scans Steam Workshop mods for locally installed Paradox games, detects the development environment (IDEA / VSCode / Unknown), and fetches view counts, subscriptions, and favorites via the public Steam Workshop API.

Development environment detection is based on filesystem heuristics (such as `.idea/`, `.vscode/`, `_cwtools/`, `.config/*.cwt`, etc.) and is not guaranteed to be fully accurate — many mod authors exclude IDE config directories when uploading to the Workshop.

```shell
python scripts/mod_insights.py [--markdown] [--output FILE]
```

#### run_reports.py {#script-run-reports}

<!-- @see scripts/run_reports.py -->

**Batch report generator**. Runs the six audit scripts above (excluding `mod_insights.py`) in `--markdown` mode, saving reports to `documents/reports/{timestamp}/`.

```shell
python scripts/run_reports.py
```

## Syntax Highlighting Definitions {#highlighters}

<!-- @see docs/.vuepress/highlighters/ -->

The project provides syntax highlighting definition files for four languages — CWT, Paradox Script, Paradox Localisation, and Paradox CSV — enabling syntax coloring in Markdown code blocks. These definitions are used by the project's documentation site (built on VuePress) and can also be reused in other environments that support the corresponding formats.

### Highlighting Schemes {#highlighters-schemes}

Syntax highlighting definitions are provided in three formats, covering the major code highlighting engines:

- **TextMate grammars** (`docs/.vuepress/highlighters/text-mate/`): Standard `.tmLanguage.json` format, serving as the foundational grammar definitions for the other two schemes. TextMate grammars are widely supported — VS Code, Shiki, GitHub, and others are all based on this format.
- **Shiki language registrations** (`docs/.vuepress/highlighters/shiki/`): TypeScript modules that load the corresponding TextMate grammar files and adapt them to Shiki's `LanguageRegistration` interface.
- **Prism language definitions** (`docs/.vuepress/highlighters/prism/`): JavaScript modules that independently define grammar rules using Prism's token pattern API.

Each of the four languages has a complete set of definition files:

| Language | TextMate | Shiki | Prism |
|---|---|---|---|
| CWT | `cwt.tmLanguage.json` | `shiki-cwt.ts` | `prism-cwt.js` |
| Paradox Script | `paradox-script.tmLanguage.json` | `shiki-paradox-script.ts` | `prism-paradox-script.js` |
| Paradox Localisation | `paradox-localisation.tmLanguage.json` | `shiki-paradox-localisation.ts` | `prism-paradox-localisation.js` |
| Paradox CSV | `paradox-csv.tmLanguage.json` | `shiki-paradox-csv.ts` | `prism-paradox-csv.js` |

### VuePress Integration {#highlighters-vuepress}

<!-- @see docs/.vuepress/plugins/ -->
<!-- @see docs/.vuepress/configs/highlighters.ts -->

The project documentation site is built with [VuePress](https://vuepress.vuejs.org/) + [vuepress-theme-hope](https://theme-hope.vuejs.press/). To support syntax highlighting for the four languages above in documentation code blocks, the project implements corresponding VuePress plugins (under `docs/.vuepress/plugins/`), in both Prism and Shiki variants:

- **Prism plugins** (`docs/.vuepress/plugins/prism/`): Register Prism language definitions in the `extendsMarkdown` hook.
- **Shiki plugins** (`docs/.vuepress/plugins/shiki/`): Wrap `@vuepress/plugin-shiki` with custom language registration injection.

The choice of highlighting engine is controlled by the `usePrism` flag in `docs/.vuepress/configs/highlighters.ts`. **Shiki is currently used** as the highlighting engine (`usePrism = false`). Switching engines only requires changing this flag — the corresponding plugins and language registrations will switch automatically.

### Reuse Notes {#highlighters-reuse}

These highlighting definition files can be reused outside the project documentation site:

- **TextMate grammars** (`.tmLanguage.json`) can be used directly in VS Code extensions, Shiki integrations, GitHub Linguist, and any other scenario that supports TextMate grammars.
- **Prism language definitions** can be registered in any website or tool using Prism.js via `registerXxx(Prism)`.
- **Shiki language registrations** can be imported directly into any project using Shiki via their factory functions.
