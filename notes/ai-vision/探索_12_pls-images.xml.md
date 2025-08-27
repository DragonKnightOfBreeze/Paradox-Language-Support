# 探索_12_pls-images.xml

> 文件：`src/main/resources/META-INF/pls-images.xml`
> 主题：PLS 图像子系统的扩展点、文件类型与动作注册

---

## 总览

- 该文件为 PLS 的图像能力提供扩展点与实现，并将 DDS/TGA 文件类型集成到 IntelliJ 平台的查看/编辑工作流中。
- 主要内容：
  - 自定义扩展点 `icu.windea.pls.images.support`（接口 `ImageSupport`）。
  - 基于该扩展点的两种支持实现：默认支持与“工具驱动”的支持。
  - 向 IntelliJ 注册 DDS/TGA 两类文件类型以及对应的 Lookup/文档/编辑器 Provider。
  - 一组围绕图像查看/编辑/转换的动作与菜单编组。

---

## 扩展点与实现

- 扩展点
  - `icu.windea.pls.images.support`（`interface=icu.windea.pls.images.support.ImageSupport`，`dynamic=true`）。
- 实现（`defaultExtensionNs="icu.windea.pls.images"`）
  - `DefaultImageSupport`（`order="last"`）
  - `ToolBasedImageSupport`
- 说明：
  - 通过策略扩展点抽象图像处理能力，默认实现兜底；“工具驱动”实现推测结合外部命令行/库以增强转换或预览能力。

---

## 平台扩展（com.intellij）

- DDS
  - `fileType`：`icu.windea.pls.images.dds.DdsFileType`（`fieldName=INSTANCE`，扩展名 `dds`）
  - `fileLookupInfoProvider`：`DdsLookupInfoProvider`
  - `documentationProvider`：`DdsDocumentationProvider`（标注 `<!--suppress PluginXmlValidity -->`）
  - `fileEditorProvider`：`DdsFileEditorProvider`
- TGA
  - `fileType`：`icu.windea.pls.images.tga.TgaFileType`（`fieldName=INSTANCE`，扩展名 `tga`）
  - `fileLookupInfoProvider`：`TgaLookupInfoProvider`
  - `documentationProvider`：`TgaDocumentationProvider`（同上）
  - `fileEditorProvider`：`TgaFileEditorProvider`
- 作用：为 DDS/TGA 提供查找信息、快速文档与专用编辑器供给，实现 IDE 内部的预览与交互。

---

## 动作与菜单编组

- 根组 `Pls.ImagesRootGroup`
  - `Pls.Images.EditExternally`（`EditExternallyAction`）
    - 快捷键：Ctrl+Alt+F4（默认 Keymap）
    - 菜单：Project 视图弹出菜单中，位于 `EditSource` 之后
  - `Pls.Images.EditExternalEditorPath`（编辑外部图像编辑器路径）
  - 子组 `Pls.Images.ImageViewActions`
    - `Pls.Images.SetBackgroundImage`（同时加入 `ProjectViewPopupMenu` 与 `EditorPopupMenu3`）
  - 加入 `Other.KeymapGroup`（方便在 Keymap 设置中归类）

- 编辑器工具栏组 `Pls.Images.EditorToolbar`
  - 引用现有动作：
    - `Images.ToggleTransparencyChessboard`、`Images.Editor.ToggleGrid`
    - `Images.Editor.ZoomIn/ZoomOut/ActualSize/FitZoomToWindow`
    - `ShowColorPicker`、`Images.ChangeBackground`

- 编辑器弹出菜单组 `Pls.Images.EditorPopupMenu`
  - 组合：`CutCopyPasteGroup`、`FindUsages`、`RefactoringMenu`、`Images.EditorToolbar`、`Images.ShowBorder`、`Pls.Images.SetBackgroundImage`、`VersionControlsGroup`、`Pls.Images.EditExternally`、`Images.EditExternalEditorPath`、`ExternalToolsGroup`

- 转换组 `Pls.Images.ConvertGroup`
  - 动作：
    - `Pls.ConvertImageFormatToPng`（`ConvertImageFormatActions$Png`）
    - `Pls.ConvertImageFormatToDds`（`ConvertImageFormatActions$Dds`）
    - `Pls.ConvertImageFormatToTga`（`ConvertImageFormatActions$Tga`）
  - 挂载位置：`ProjectViewPopupMenu`、`ImagesRootGroup`、`Pls.ImagesRootGroup`、`Images.EditorPopupMenu`、`Pls.Images.EditorPopupMenu`（均 `anchor=last`）

---

## 观察与推断

- 该模块将 PLS 与 IntelliJ 的内建图片查看器生态对齐，通过“引用现有动作”的方式自然融入缩放/网格/取色等操作。
- “外部编辑器”相关动作暗示可配置外部图像编辑器路径，并一键在外部工具中打开当前图像。
- 转换动作覆盖 PNG/DDS/TGA 三向转换，推测具体实现分流至 `DefaultImageSupport` 或 `ToolBasedImageSupport`（后者可能依赖外部工具链，如 ImageMagick 等）。

---

## 关联与后续

- 可能的配置入口与依赖：
  - 外部编辑器路径/外部工具链配置（可在 `pls-integrations.xml` 或设置页中寻找）。
- 建议下一步：阅读 `src/main/resources/META-INF/pls-integrations.xml`，确认图像转换所需的外部依赖与可选集成，并梳理与本模块的联动。
