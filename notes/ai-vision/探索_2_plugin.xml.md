# 探索_2_plugin.xml

> 时间：2025-08-27 15:31（本地）
> 来源文件：`src/main/resources/META-INF/plugin.xml`
> 目的：解析插件主配置，梳理依赖、资源包与包含链路（xi:include），为后续逐个阅读子配置建立索引。

---

## 一、插件元信息（观察）

- **idea-plugin（url）**：`https://github.com/DragonKnightOfBreeze/Paradox-Language-Support`
- **category**：`Languages`
- **vendor**：`DragonKnightOfBreeze`（email: `dk_breeze@qq.com`，url: 仓库主页）
- **resource-bundle**：`messages.PlsBundle`（国际化资源前缀）
- （注）本文件内未显式出现 `<id>`、`<name>`、`<version>` 标签；其来源待在其他配置或构建脚本中确认。

## 二、插件依赖（depends）

- **必需模块**：
  - `com.intellij.modules.lang`
  - `com.intellij.platform.images`
- **可选依赖（带 config-file）**：
  - `org.intellij.plugins.markdown` → `pls-extension-markdown.xml`
  - `com.intellij.diagram` → `pls-extension-diagram.xml`
  - `cn.yiiguxing.plugin.translate` → `pls-extension-translation.xml`

说明：当对应可选插件存在时，IDE 会加载相应的 `config-file` 以启用扩展能力。

## 三、包含的配置文件（xi:include）

- `pls-inject.xml`
- `pls-cwt.xml`
- `pls-script.xml`
- `pls-localisation.xml`
- `pls-csv.xml`
- `pls-config.xml`
- `pls-images.xml`
- `pls-lang.xml`
- `pls-integrations.xml`
- `pls-ai.xml`
- `pls-intentions.xml`
- `pls-inspections.xml`
- `pls-ep.xml`

> 猜测含义（基于文件名，待详细阅读验证）：
> - inject：注入/语法注入相关
> - cwt：CWT 规则及其语言支持
> - script/localisation/csv：对应三类语言支持
> - config：设置/配置界面与存储
> - images：图片处理与预览
> - lang：语言/文件类型/lexer/parser/颜色设置等聚合点
> - integrations：与外部工具/服务集成
> - ai：AI 相关动作/服务/设置
> - intentions：意图动作
> - inspections：检查器
> - ep：自定义扩展点

## 四、包含文件体量（用于阅读优先级评估）

> 数据来自目录枚举（字节数，近似反映复杂度）：
- `pls-lang.xml`：47,230 bytes（较大，可能集中声明语言/文件类型/配色/高亮等）
- `pls-inspections.xml`：33,523 bytes（较大，包含众多检查器）
- `pls-ep.xml`：26,934 bytes（扩展点声明较多）
- `pls-script.xml`：5,983 bytes
- `pls-inject.xml`：5,372 bytes
- `pls-images.xml`：4,221 bytes
- `pls-cwt.xml`：3,809 bytes
- `pls-localisation.xml`：7,112 bytes
- `pls-intentions.xml`：8,814 bytes
- `pls-csv.xml`：2,564 bytes
- `pls-config.xml`：1,664 bytes
- `pls-ai.xml`：2,976 bytes
- `pls-integrations.xml`：1,788 bytes
- 可选扩展：
  - `pls-extension-diagram.xml`：2,117 bytes
  - `pls-extension-markdown.xml`：989 bytes
  - `pls-extension-translation.xml`：798 bytes

> 建议阅读优先级：`pls-lang.xml` 与 `pls-ep.xml`（体量与核心程度高）→ `pls-inspections.xml`（检查能力丰富，覆盖面广）→ 其他按主题需要推进。

## 五、架构解读（从 plugin.xml 视角）

- **聚合式主配置**：通过 `xi:include` 将功能按主题拆分为独立 XML，利于维护与模块化演进。
- **条件扩展**：使用 `depends optional + config-file` 模式与外部插件集成（Markdown/Diagram/Translation），在存在时无缝增强。
- **资源束统一**：`messages.PlsBundle` 作为国际化资源前缀，集中管理 UI 文案与提示。
- **能力映射**：README 中提到的语言能力、检查、意图、集成、AI 等，对应分散在上述子配置内的 `actions/services/extensions` 声明。

## 六、与 README 的对应关系（校准）

- 规则驱动（CWT）→ `pls-cwt.xml`
- 三类语言（script/localisation/csv）→ `pls-script.xml`、`pls-localisation.xml`、`pls-csv.xml`
- 图片能力 → `pls-images.xml`
- AI/翻译/外部分析器（Tiger）等集成 → `pls-ai.xml`、`pls-integrations.xml`、可选扩展 config-files
- IDE 能力（检查/意图/语言/扩展点）→ `pls-inspections.xml`、`pls-intentions.xml`、`pls-lang.xml`、`pls-ep.xml`

## 七、开放问题与待确认

- 本文件未见 `<id>/<name>/<version>`：其来源需要在构建脚本或其他配置中确认。
- actions/services/extensions 的具体清单在各子文件中，需逐个阅读以验证 README 的能力矩阵细节。
- 与可选依赖的边界行为（例如未安装 Markdown/Diagram/Translation 插件时功能降级路径）可在相应扩展配置中确认。

## 八、后续阅读计划（待你指定）

- 建议优先项：`pls-lang.xml` 或 `pls-ep.xml`。
- 也可按主题推进：语言（script/localisation/csv）、规则（cwt）、检查（inspections）、意图（intentions）、图片（images）、AI（ai）、集成（integrations）、设置（config）。
