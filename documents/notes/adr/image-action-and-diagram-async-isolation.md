# 图片格式转换动作协程化与图表读锁/IO/UI 三段隔离

> - 模块：后端 > 通用基础设施 & 图表扩展
> - 版本：v3.0.6
> - 日期：2026-11-17
> - 状态：已实施
> - 标签：`ConvertImageFormatAction` / `DiagramProvider` / 协程 / 防冻结 / `StellarisTechnologyCardPresentationModel`

## 背景

在以两个限域目标验证并发改造与防冻结治理时，原有实现存在两处典型的锁与线程耦合隐患：
1. **`ConvertImageFormatAction`**：在过时的 `runWriteActionWithCancellableProgressInDispatchThread` 写锁内调用 `executeOnPooledThread { runBlockingCancellable { ... } }.get()`，等价于在持有写锁与 EDT 时同步死等后台图片转码（含外部命令行进程）；同时仅暴露了 `PNG`/`DDS`/`TGA`，且 `ConvertImageFormatDialog` 漏掉了 `newFileNameField` 实例赋值导致单文件改名长期失效。
2. **`diagramProvider` 体系**：
   - `ParadoxDiagramDataModel.refreshDataModel()` 外层包了 `runSmartReadAction`，而子类 `updateDataModel()` 内又调 `runWithModalProgressBlocking`，形成 `readAction` 内嵌套阻塞模态进度的平台死锁隐患；
   - `ElementManager` 与 `StellarisTechTreeDiagramProvider.ColorManager` 在渲染/绘制阶段残留多处 `runSmartReadAction`；
   - `StellarisTechnologyCardBuilder` 直接在 `ElementManager.getItemComponent()` 内拿 `runSmartReadAction` 并 `.get()` 同步阻塞等待 4 张图片解码/转码。

## 决策与权衡

### 1. `ConvertImageFormatAction`：四阶段纯协程分离与带后缀临时文件

- 拆分为清晰的四阶段流转：
  1. **EDT 交互**：通过可覆写钩子 `promptConversionOptions` 收集目标路径与文件名，检查只读状态；
  2. **`readAction` + EDT 冲突预询问**：一次性提取源路径/格式与同名冲突清单，在 `Dispatchers.EDT` 通过 `promptOverwriteChoice` 预先收敛 `SkipOverwriteChoice`（支持 `SKIP`、`SKIP_ALL`、`OVERWRITE`、`OVERWRITE_ALL`）；
  3. **无锁 `Dispatchers.IO` 转码 + 毫秒级 `edtWriteAction` 落位**：在 `withBackgroundProgress` 中先转码至 `imagesTempPath` 下**保留目标扩展名**的临时文件（确保 `texconv`、`magick` 等外部工具正确识别输出格式，同时保证同路径自覆盖安全），仅在最终 `Files.move(REPLACE_EXISTING)` 与刷新 VFS 时进入 `edtWriteAction`；
  4. **EDT 汇报**：发 Warning 通知或错误提示。
- 补齐 `ConvertImageFormatActions.Jpg`（兼容 `.jpg`/`.jpeg` 禁用）与 `Gif`，形成 `PNG / JPG / GIF / DDS / TGA` 五格式动作矩阵。

### 2. `diagramProvider`：去外层读锁 + 适度轻量模型与三段隔离

保持 `v3.0.6` 克制边界（不急于在当前版本敲定跨游戏通用图引擎最终形态），以“消除冻结 + 适度抽象”收口：
- **解除外层锁**：移除 `ParadoxDiagramDataModel.refreshDataModel()` 的外层 `runSmartReadAction`，将精确分段读锁权交还给具体 `DataModel.updateDataModel()`；
- **轻量呈现模型与资产分离**：
  - 引入 `ParadoxDefinitionDiagramNodeModel`（及 `ParadoxDiagramPropertyItem`），在 `DataModel` 的 `readAction` 阶段一次性预加载 `title`、`typeText`、`presentableName`、`subtypes` 与预渲染 `SimpleColoredText` 属性项，挂载于 `DataModel`（而非持久 PSI `element`），使 `ElementManager` 与 `ColorManager` 彻底清零 `runSmartReadAction`；
  - 拆分 `StellarisTechnologyCardBuilder` 为三阶段：`loadModel()`（`readAction` 提取 `StellarisTechnologyCardPresentationModel`）→ `materializeAssets()`（无锁 `Dispatchers.IO` 生成 `StellarisTechnologyCardAssets`）→ `build(model, assets)`（纯内存微秒级 Swing 组装）；
- **防 EDT 阻塞降级**：仅当 `Categories.Presentation` 启用时才在 `DataModel` 后台步骤预物化科技卡资产；若渲染组件时资产尚未就绪且当前位于 EDT 或读锁中，**绝不阻塞等待**，而是原子防重地异步派发 `Dispatchers.IO` 物化并合并节流调用 `schedulePresentationRefresh(builder)`。

## 关键代码位置

- `src/main/kotlin/icu/windea/pls/images/actions/ConvertImageFormatAction.kt`
- `src/main/kotlin/icu/windea/pls/images/actions/ConvertImageFormatActions.kt`
- `src/main/kotlin/icu/windea/pls/images/actions/ConvertImageFormatDialog.kt`
- `src/main/kotlin/icu/windea/pls/extensions/diagram/ParadoxDiagramDataModel.kt`
- `src/main/kotlin/icu/windea/pls/extensions/diagram/ParadoxDiagramModels.kt`
- `src/main/kotlin/icu/windea/pls/extensions/diagram/provider/ParadoxEventTreeDiagramProvider.kt`
- `src/main/kotlin/icu/windea/pls/extensions/diagram/provider/ParadoxTechTreeDiagramProvider.kt`
- `src/main/kotlin/icu/windea/pls/extensions/diagram/provider/ParadoxDefinitionDiagramProviders.Stellaris.kt`
- `src/main/kotlin/icu/windea/pls/lang/presentation/StellarisTechnologyCardModels.kt`
- `src/main/kotlin/icu/windea/pls/lang/presentation/StellarisTechnologyCardBuilder.kt`
- `src/test/kotlin/icu/windea/pls/images/actions/ConvertImageFormatActionTest.kt`
- `src/test/kotlin/icu/windea/pls/extensions/diagram/ParadoxDiagramDataModelTest.kt`
- `src/test/kotlin/icu/windea/pls/extensions/diagram/ParadoxDiagramElementManagerTest.kt`
- `src/test/kotlin/icu/windea/pls/extensions/diagram/ParadoxDiagramModelTest.kt`
- `src/test/kotlin/icu/windea/pls/lang/presentation/StellarisTechnologyCardBuilderTest.kt`
