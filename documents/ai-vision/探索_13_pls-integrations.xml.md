# 探索_13_pls-integrations.xml

> 文件：`src/main/resources/META-INF/pls-integrations.xml`
> 主题：PLS 外部工具与生态集成（图像/翻译/Lint）扩展点与实现

---

## 总览

- 定义“集成”相关设置页与监听器，抽象三类集成扩展点（图像工具、翻译工具、Lint 工具），并给出部分默认实现。
- 主要内容：
  - 设置页：`applicationConfigurable` -> `PlsIntegrationsSettingsConfigurable`（`id=pls.integrations`，`key=settings.integrations`）。
  - 异步 VFS 监听：`PlsTigerConfFileListener`（用于 Lint 配置文件变更侦听）。
  - 扩展点（`dynamic=true`）：`imageToolProvider` / `translationToolProvider` / `lintToolProvider`。
  - 实现（`defaultExtensionNs="icu.windea.pls.integrations"`）：`PlsTexconvToolProvider`、`PlsMagickToolProvider`、`PlsTigerLintToolProvider$Ck3/$Ir/$Vic3`，以及（被注释的）翻译工具提供者示例。

---

## 平台扩展（com.intellij）

- `applicationConfigurable`
  - `id="pls.integrations"`，`parentId="pls"`
  - `bundle="messages.PlsBundle"`，`key="settings.integrations"`
  - `instance="icu.windea.pls.integrations.settings.PlsIntegrationsSettingsConfigurable"`
  - 作用：提供“集成”设置页，集中管理外部工具与相关选项。
- `vfs.asyncListener`
  - `implementation="icu.windea.pls.integrations.lints.PlsTigerConfFileListener"`
  - 作用：异步监听（推测）Tiger Lint 相关配置文件的变更，以触发重载/校验。

---

## 扩展点定义（extensionPoints）

- `icu.windea.pls.integrations.imageToolProvider`
  - `interface=icu.windea.pls.integrations.images.tools.PlsImageToolProvider`，`dynamic=true`
- `icu.windea.pls.integrations.translationToolProvider`
  - `interface=icu.windea.pls.integrations.translation.tools.PlsTranslationToolProvider`，`dynamic=true`
- `icu.windea.pls.integrations.lintToolProvider`
  - `interface=icu.windea.pls.integrations.lints.tools.PlsLintToolProvider`，`dynamic=true`

说明：通过动态扩展点解耦具体工具实现，允许按需引入或替换，且可在运行时感知变更。

---

## 集成实现（icu.windea.pls.integrations）

- 图像工具（Image Tool Provider）
  - `PlsTexconvToolProvider`（常见于 DDS/DirectXTex 工具链）
  - `PlsMagickToolProvider`（常见于 ImageMagick 工具链）
  - 关联：与 `pls-images.xml` 中的格式转换/外部编辑动作形成联动能力面。

- 翻译工具（Translation Tool Provider）
  - 注释示例：`PlsTranslationPluginToolProvider`（注释注明“see pls-extension-translation.xml”）
  - 关联：`src/main/resources/META-INF/pls-extension-translation.xml` 存在，可后续阅读以确认翻译插件集成细节。

- Lint 工具（Lint Tool Provider）
  - `PlsTigerLintToolProvider$Ck3`
  - `PlsTigerLintToolProvider$Ir`
  - `PlsTigerLintToolProvider$Vic3`
  - 说明：面向不同变体/游戏的 Lint 支持提供者，配合 `PlsTigerConfFileListener` 实现配置与校验联动。

---

## 观察与推断

- 通过“设置页 + 动态扩展点 + VFS 监听”的三件套，构成可扩展、可配置、可感知的集成子系统。
- 图像工具链（Texconv/Magick）与 `pls-images.xml` 的转换动作形成闭环；运行时可按实际环境选择或优先某实现。
- 翻译工具被拆分至独立 include（`pls-extension-translation.xml`），与本文件解耦，便于模块化与可选依赖。
- Lint 工具多变体的提供者命名（`Ck3/Ir/Vic3`）体现对不同目标环境的适配能力。

---

## 关联与后续

- 直接关联：
  - `src/main/resources/META-INF/pls-images.xml`（图像动作/编辑/转换）
  - `src/main/resources/META-INF/pls-inspections.xml`（Lint 相关）
  - `src/main/resources/META-INF/pls-extension-translation.xml`（翻译扩展）
- 建议下一步：优先阅读 `pls-ai.xml` 或 `pls-inject.xml`；同时可补充阅读 `pls-extension-translation.xml` 以串联翻译生态。
