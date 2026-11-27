# Issue #464：部分位置快速文档的图标异常

> - 模块：定义解析 / 相关图片与相关本地化 / 图片缩放 / 子类型匹配
> - 版本：v3.1.0（回归，部分源自 3.0.x 之后的优化提交）
> - 日期：2026-11-27
> - 状态：已解决
> - 标签：`bug`、`regression`、`language-features`、`civic`、`scripted_modifier`、Issue #464

## 背景

- **GitHub Issue**：[#464 - 部分位置快速文档的图标异常](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/464)
- **标签**：`bug`、`regression`、`language-features`
- **错误表现**：
  1. 路径正确但未读取到 civic 图标，而 councilor 图标可以正常读取。
  2. 快速文档中部分图片（定义图标 / 相关图片）过小。
  3. 评论区补充：`hidden` 的 `scripted_modifier` 仍然提示需要定义本地化。
- **复现线索**：截图中的 Stellaris Mod 定义了没有显式 `icon` 属性的 `civic_TWEO_hospice`，其图标只能依赖 `subtype[civic]` 下的推断路径 `gfx/interface/icons/governments/civics/$.dds`；`councilor` 的推断规则位于直接规则之前，因此表现正常。

## 只读核查

### 1. 规则与数据

- `cwt/cwtools-stellaris-config/config/common/governments.cwt` 中，`type[civic_or_origin]` 的 `images` 先声明直接规则 `icon = icon # filepath`，再在 `subtype[civic]` 下声明推断规则 `icon = "gfx/interface/icons/governments/civics/$.dds" # inferred`。
- 同一文件中的 `type[councilor]` 则把推断规则 `icon = "gfx/interface/icons/governments/civics/$.dds|$civic"` 放在前面，因此不依赖子类型也能命中。
- `cwt/cwtools-stellaris-config/config/common/scripted_modifiers.cwt` 中，本地化规则为 `localisation = { subtype[!hidden] = { name = "mod_$" } }`，即 `hidden` 定义不应要求本地化。
- 截图中的 `civic_TWEO_hospice` 没有显式 `icon` 属性，只能通过 `subtype[civic]` 的推断路径定位到 `civic_TWEO_hospice.dds`。

### 2. 相关链路

- 定义 → 相关图片：`ParadoxDefinitionInfo.images` → `ParadoxDefinitionService.resolveRelatedImageInfos(...)` → `CwtTypeImagesConfig.getLocationConfigs(subtypes)`。
- 定义 → 相关本地化：`ParadoxDefinitionInfo.localisations` → `ParadoxDefinitionService.resolveRelatedLocalisationInfos(...)` → `CwtTypeLocalisationConfig.getLocationConfigs(subtypes)`。
- 快速文档缩放：`ParadoxDocumentationTarget.computeDocumentation(...)` → `ImageRenderService.applyImageScaling(...)`。
- 缺失本地化检查：`MissingLocalisationInspection` → `ParadoxLocalisationCodeInsightContextFactory.fromDefinition(...)` → `definitionInfo.localisations`。

## 根因

### 1. 子问题 1 与子问题 3：构造期急切初始化（已由 `f0243c563f` 修复）

- 提交 `b16a1a17e3` 曾把 `ParadoxDefinitionInfo` 的 `localisations`、`images`、`modifiers`、`primaryLocalisations`、`primaryImages` 由计算属性改为构造期立即初始化。
- `ParadoxDefinitionInfo` 的 `element` 由 `ParadoxDefinitionService.resolveInfo(...)` 在构造完成后才赋值，因此这些属性在初始化时基于空 `element` 解析出空子类型并永久缓存。
- 对 `civic_or_origin` 而言，空子类型只保留 `icon = icon`，丢失 `subtype[civic]` 下的推断规则；没有显式 `icon` 的 civic 因此无法显示图标。
- 对 `scripted_modifier` 而言，`subtype[!hidden]` 在空子类型下反而匹配成功，于是 `hidden` 定义也被要求提供本地化。
- `councilor` 的推断规则不依赖子类型，因此不受影响，这与截图中 councilor 图标正常、civic 图标缺失的现象一致。
- `#458` 的修复提交 `f0243c563f` 已把上述派生属性恢复为计算 getter，因此子问题 1 与子问题 3 在规则与代码层面均已修复；本次新增回归测试用于锁定该行为。

### 2. 子问题 2：文本图标缩放误伤普通图片（本次修复）

- `ImageRenderService.applyImageScaling(...)` 原先遍历渲染会话中的所有图片请求，并把高度不超过 `quickDocTextIconHeightLimit`（默认 36）的图片统一按文档文本高度缩放。
- 快速文档中的定义图标与相关图片也可能低于该阈值（例如 25x25 的 civic `.dds`），于是被误判为内联 `textIcon` 并缩小。
- 真正的内联文本图标只来自本地化富文本的 `renderIcon(...)`，应当只有这部分图片参与缩放。

## 解决方案

1. 保留 `f0243c563f` 的计算 getter 语义，子问题 1 与子问题 3 无需额外代码改动。
2. 在图片渲染会话中显式标记文本图标。
   - `ImageRenderSession` 新增 `markTextIcon(...)` 与 `textIconRequests()`。
   - `ImageRenderService.resolveUrl(...)` 新增 `textIcon` 参数，并在存在渲染会话时标记对应请求。
   - `ParadoxLocalisationTextQuickDocRenderer` 与 `ParadoxLocalisationTextHtmlRenderer` 的 `renderIcon(...)` 以 `textIcon = true` 解析 URL。
   - `applyImageScaling(...)` 仅遍历被标记的文本图标请求，继续遵守 `quickDocTextIconHeightLimit`。
3. 按新语义更新既有 `ImageRenderServiceTest`，并补充“非文本图标即使低于高度限制也不缩放”的用例。

## 回归测试

- 新增 `src/test/kotlin/icu/windea/pls/test/issues/Issue464Test.kt`。
- 新增夹具 `src/test/testData/issues/464/`：
  - `.config/stellaris/governments.test.cwt`：最小化的 Stellaris 规则，保留 `civic_or_origin` 的直接/子类型图片规则，以及 `scripted_modifier` 的 `subtype[hidden]` 与 `subtype[!hidden]` 本地化规则。
  - `common/governments/civics/test_civics.txt`：没有显式 `icon` 属性的 civic。
  - `common/scripted_modifiers/test_scripted_modifiers.txt`：一个 `hidden` 定义与一个非 `hidden` 定义。
  - `gfx/interface/icons/governments/civics/test_civic.dds`：25x25 的二进制图片夹具，复制时直接链式调用 `.injectFileInfo(...)`。
- 覆盖：
  1. 快速文档缩放只作用于被标记的文本图标，非文本图标保持原始尺寸。
  2. `civic` 子类型的推断图片规则被保留，并能解析到对应的 `.dds` 文件。
  3. `hidden` 的 `scripted_modifier` 不再要求本地化，而非 `hidden` 定义仍然要求本地化。

## 验证

```powershell
./gradlew compileKotlin compileTestKotlin test --tests "icu.windea.pls.test.issues.Issue464Test" --no-daemon --console=plain
./gradlew test --tests "icu.windea.pls.images.ImageRenderServiceTest" --tests "icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextQuickDocRendererTest" --tests "icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextHtmlRendererTest" --tests "icu.windea.pls.lang.resolve.ParadoxImageServiceTest" --no-daemon --console=plain
```

结果：全部通过。

## 经验

- 派生属性若依赖构造完成后才注入的可变状态（如 `ParadoxDefinitionInfo.element`），不应在构造期立即求值；应使用计算属性或显式惰性缓存。
- 对“按尺寸阈值推断语义”的逻辑要保持警惕：尺寸只是弱信号，若调用方能提供真实语义（如“这是文本图标”），应优先传递显式标记。
- 平台测试中内存 VFS 的 `VirtualFile.toNioPath()` 可能不可用，回归断言应直接验证子类型图片规则能解析到目标 `.dds` 文件，而不是依赖 `resolveByDefinition(...)`。
