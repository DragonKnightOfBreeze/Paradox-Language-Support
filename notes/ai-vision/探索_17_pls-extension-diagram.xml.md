# 探索_17_pls-extension-diagram.xml

> 文件：`src/main/resources/META-INF/pls-extension-diagram.xml`
> 主题：Diagram 框架扩展与 Action 集成

---

## 总览

- 为多款 Paradox 系游戏提供“事件树/科技树”图表 Provider，挂接到 IntelliJ Diagram 框架。
- 提供图表设置页入口与工具栏按钮，并把“转到相关”能力加入 Diagram 与 PLS 自身的层级菜单。

---

## 扩展（com.intellij）

- `projectConfigurable id="pls.diagram" parentId="pls" key="settings.diagram"`
  - `instance=PlsDiagramSettingsConfigurable`
  - 作用：在 PLS 设置分组下添加 Diagram 配置页。

---

## 扩展（com.intellij.diagram）

- Provider（按游戏与树型）：
  - `StellarisEventTreeDiagramProvider`
  - `StellarisTechTreeDiagramProvider`
  - `Ck2EventTreeDiagramProvider`
  - `Ck3EventTreeDiagramProvider`
  - `Eu4EventTreeDiagramProvider`
  - `Hoi4EventTreeDiagramProvider`
  - `IrEventTreeDiagramProvider`
  - `Vic2EventTreeDiagramProvider`
  - `Vic3EventTreeDiagramProvider`

说明：为不同游戏构建事件/科技实体节点与连边（依赖/触发/解锁等），用于可视化浏览与导航。

---

## Actions（resource-bundle=`messages.PlsDiagramBundle`）

- 引用 `Pls.GotoGroup`：
  - 加入 `Uml.SourceActionsGroup.GoTo` 与 `Uml.NodeCellEditorPopup.GoTo`，位置在 `GotoRelated` 之后。
- 新增 `Pls.Diagram.OpenSettings`（图标 `AllIcons.General.GearPlain`）：
  - 类 `ParadoxDiagramOpenSettingsAction`，加入 `Diagram.DefaultGraphToolbar`，位置在 `Diagram.OpenSettings` 之后。
- 引用 `UML.Group`：
  - 加入 `Pls.DefinitionHierarchyPopupMenu` 与 `Pls.CallHierarchyPopupMenu`。

---

## 观察与建议

- 可用性：针对大型模组的复杂依赖，建议提供“按范围过滤/折叠层级/搜索节点”与状态缓冲，降低渲染与交互成本。
- 互操作：与 `pls-lang.xml` 的导航/层级视图协作，双向跳转；在节点快速文档中集成翻译/AI 概要。
- 诊断：为 Provider 增加统计与耗时指标；提供降级渲染模式（仅关键边/局部展开）。

---

## 下一步

- 小样本验证：对一个小型 Mod 生成事件树/科技树，验证导航/设置按钮/菜单集成是否正常。
