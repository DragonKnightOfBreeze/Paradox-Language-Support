# 将插件主仓许可证从 MIT 迁移至 GNU GPL-3.0

> - 模块：工程治理 > 许可证与合规
> - 版本：v3.1.0
> - 日期：2026-11-25
> - 状态：已实施
> - 标签：`GPL-3.0` / `NOTICE` / 许可证迁移 / 版权保护 / Inbound=Outbound

## 背景与战略意图

自立项以来，Paradox Chronicle（原 Paradox Language Support）在宽松的 MIT 许可证下持续迭代，积累了 12 万+ 行复杂 PSI 语法树构建、CWT 规则引擎、多语言注入与模组索引等核心资产。随着插件进入体系化演进阶段，原有的宽松许可模式暴露出治理与防御短板：
1. **防止成果闭源封包与商业化倒卖**：宽松的 MIT 许可允许第三方在不开源、不回馈社区的前提下将核心成果打包为专有商业工具或闭源衍生品，对开源维护者构成结构性不公；
2. **确立权责对等的开源契约**：明确要求任何基于本项目的派生作品、增强版本及外部贡献均须保持同等层级的开源自由度（Inbound = Outbound）；
3. **为后续长期演进确立清晰法权基线**：以 `v3.1.0`“清场换代”为明确的分水岭，为插件后续架构扩展与生态协作奠定稳固的强 Copyleft 法律基石。

## 决策与权衡

### 1. 采用标准 GNU GPL-3.0（GPL-3.0-or-later）

- **版本选择（GPL-3.0 vs GPL-2.0）**：选择 GPL-3.0 而非 GPL-2.0。GPL-3.0 原生与 Apache 2.0 兼容（本项目直接依赖的众多库如 Caffeine、TwelveMonkeys、Javassist、JetBrains 平台均为 Apache-2.0/BSD/MIT），且具备更完备的现代专利保护与反 Tivo 化条款；
- **历史切割与不溯及既往**：本许可证变更自 `v3.1.0`（开发分支 `dev/3.1.0-dev`）起生效。既有已发布版本（`<= v3.0.7`）继续遵循其原有的 MIT 许可证分发，不进行历史追索。

### 2. 标准 `LICENSE` 与 `NOTICE` 署名分治架构

为兼顾平台工具识别与作者合法署名保护，拒绝在标准许可证文本中随意篡改塞入私有行：
- **`LICENSE` 保持纯正官方原文**：放置未经修改的标准 GNU General Public License v3.0 原文（674 行），确保 GitHub、SPDX、JetBrains Marketplace 等合规扫描器能够 100% 准确识别；
- **根目录 `NOTICE` 集中承载版权署名与第 7 条保留声明**：
  - **版权声明**：显式载明 `Copyright (c) 2021-2026 DragonKnightOfBreeze Windea <dk_breeze@qq.com>`；
  - **GPL-3.0 第 7 条（Additional Terms）**：
    - **7(b)**：要求在复制件、衍生品、界面关于对话框中完整保留原作者署名与版权声明；
    - **7(c)**：禁止歪曲派生版本的出处，修改版须有明确的修改标记；
    - **7(e)**：禁止未经书面许可使用项目名称（Paradox Chronicle 等）或作者网名（DragonKnightOfBreeze、Windea）用于商业宣传或背书；
  - **第三方版权与许可登记**：登记 JetBrains Apache-2.0 源码（`DocumentsSynchronizer.java`）与 UI 图标、Microsoft DirectXTex（`texconv.exe`）、iTitus/dds、TwelveMonkeys、Javassist（明确采用 Apache-2.0 分发路径）等资产归属。

### 3. 外部游戏规则仓（`cwt/`）独立性与聚合分发

- 各具体游戏的 CWT 规则仓库（`cwt/cwtools-*-config`）属于独立上游社区仓库，继续维持其各自原有的开源许可（通常为 MIT）；
- 插件打包（`jar`）时将规则与各子仓原有的 `LICENSE` 与 `README.md` 一并内嵌入 `config/<gameId>/`，属于合理的聚合分发（Aggregate Distribution），不强求规则仓库随插件主仓改签 GPL-3.0；
- 插件内置通用核心规则（`cwt/core/`）作为主仓原生组成部分，遵循主仓 GPL-3.0。

### 4. 外部贡献契约（Inbound = Outbound）

在 `CONTRIBUTING.md` 中增设专门条款：任何向主仓提交的 PR（涵盖代码、文档、脚本及核心规则），贡献者均默认以 GNU GPL-3.0 授权合入。

## 实施验证与影响

1. **依赖相容性审计**：全量扫描 `build.gradle.kts` 依赖树与内置资源，确认运行期依赖均为 Apache-2.0 / BSD / MIT / 公有领域，100% 兼容 GPL-3.0；
2. **打包集成验证**：修改 `build.gradle.kts` 的 `jar` 任务，将 `NOTICE` 与 `LICENSE`、`README*` 一同打入产物根目录；执行 `./gradlew jar` 解构验证内嵌完整；
3. **门面文档对齐**：更新 `README.md`、`README_zh.md` 与 `CONTRIBUTING.md`，使外部开发者与用户获得清晰一致的合规预期。
