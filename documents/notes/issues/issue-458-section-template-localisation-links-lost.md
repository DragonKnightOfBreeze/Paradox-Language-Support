# Issue #458：舰船区段（`section_template`）本地化链接丢失

> - 模块：定义解析 / 相关本地化与相关图片 / 子类型匹配
> - 版本：v3.1.0（回归，源自 3.0.x 之后的优化提交）
> - 日期：2026-11-27
> - 状态：已解决
> - 标签：`bug`、`regression`、`language-features`、`ParadoxDefinitionInfo`、`section_template`、Issue #458

## 背景

- **GitHub Issue**：[#458 - 舰船区段本地化链接丢失](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/458)
- **标签**：`bug`、`regression`、`language-features`
- **错误表现**：
  1. 在 `common/section_templates` 中定义的区段（`ship_section_template`）不再显示本地化内嵌提示/快速文档中的本地化文本；
  2. 在本地化文件中，对应的键（如 `lglr_tundra_a`）也无法通过装订线图标/查找用法索引回区段定义。
- **复现线索**：原版 `common/section_templates/battleship.txt` 与 `localisation/english/ship_sections_l_english.yml`。

## 只读核查

### 1. 原版数据与规则

- 原版 `common/section_templates/battleship.txt` 使用：
  ```txt
  ship_section_template = {
      key = "BATTLESHIP_BOW_L1M1S2"
      ship_size = battleship
      # ...
  }
  ```
- 原版 `localisation/english/ship_sections_l_english.yml` 以 `key` 的值（如 `BATTLESHIP_BOW_L1M1S2`）作为本地化键。
- `cwt/cwtools-stellaris-config/config/common/section_templates.cwt` 的关键规则为：
  ```cwt
  type[section_template] = {
      name_field = "key"
      path = "game/common/section_templates"
      path_extension = .txt
      subtype[needs_locs] = {
          ship_size = <ship_size.designable>
      }
      localisation = {
          subtype[needs_locs] = {
              name = "$"
          }
      }
  }
  ```
  即：区段本地化**只声明在 `needs_locs` 子类型下**，而该子类型要求引用的 `ship_size` 定义匹配 `ship_size.designable`。`battleship` 没有显式写 `is_designable = yes`，但规则中该属性为可选（`## cardinality = 0..1`），因此“未声明”仍可匹配 `designable`。

### 2. 双向链路

- **定义 → 本地化**：`ParadoxDefinitionManager.getRelatedLocalisations(...)` → `ParadoxDefinitionService.resolveRelatedLocalisations(...)`，依赖 `definitionInfo.localisations`；
- **本地化 → 定义**：`ParadoxLocalisationManager.getRelatedDefinitions(...)` → `ParadoxLocalisationService.resolveRelatedDefinitions(...)`，逐一检查候选定义的 `definitionInfo.localisations`；
- 两者都取决于 `ParadoxDefinitionInfo.localisations`（主键相关逻辑还依赖 `primaryLocalisations`）。

## 根因

### 1. 回归提交

- 提交 `b16a1a17e3`（`[infra/fix/refactor] Optimize the command-line execution logic and image processing logic to avoid possible IDE freezes - post revision (cleanup)`）把 `ParadoxDefinitionInfo` 中的若干派生属性从**计算属性（getter）**改成了**构造期立即初始化**：
  ```kotlin
  val localisations = ParadoxDefinitionManager.getRelatedLocalisationInfos(this)
  // images / modifiers / primaryLocalisations / primaryImages 同理
  ```
- 然而 `ParadoxDefinitionInfo` 的 `element` 是在构造完成后才由 `ParadoxDefinitionService.resolveInfo(...)` 赋值：
  ```kotlin
  return ParadoxDefinitionInfo(...).also { it.element = element }
  ```

### 2. 失效链路

1. 构造 `ParadoxDefinitionInfo` 时，`element` 仍为 `null`；
2. `localisations` 初始化会调用 `resolveRelatedLocalisationInfos(...)`，其中需要 `definitionInfo.subtypes`；
3. `subtypes` 通过 `getSubtypeConfigs(...)` 解析，而后者需要 `element`；`element == null` 时返回空子类型列表；
4. 对于 `section_template`，`localisation` 规则挂在 `needs_locs` 子类型下，空子类型导致 `getLocationConfigs(emptyList())` 不返回任何位置规则；
5. 于是 `localisations` 被永久缓存为空列表（属性为 `val`），即使之后 `element` 已赋值也不会重算；
6. 双向本地化链接因此同时失效。

> 说明：直接声明本地化（不依赖子类型）的类型不受影响，所以该回归最初只在 `section_template` 这类“本地化挂在子类型下”的定义上明显暴露。

## 解决方案

将依赖 `element`/子类型的派生属性恢复为计算属性（与 `b16a1a17e3` 之前的语义一致）：

- `localisations`
- `images`
- `modifiers`
- `primaryLocalisations`
- `primaryImages`

`memberPath` 不依赖 `element`（仅依赖 `source`、`rootKeys`、`typeKey`），保留构造期缓存不变。

对应改动：`src/main/kotlin/icu/windea/pls/model/ParadoxDefinitionInfo.kt`。

## 回归测试

- 新增 `src/test/kotlin/icu/windea/pls/test/issues/Issue458Test.kt`
- 新增夹具 `src/test/testData/issues/458/`：
  - `.config/stellaris/section_templates.test.cwt`：最小化的 Stellaris 规则，保留 `name_field = "key"`、`needs_locs` 子类型与子类型下的本地化；
  - `common/ship_sizes/00_ship_sizes.txt`、`common/section_templates/battleship.txt`、`localisation/english/ship_sections_l_english.yml`。
- 覆盖：
  1. `definitionInfo` 的 `type` / `name` / `subtypes` / `localisations`；
  2. 定义侧的 `getRelatedLocalisations`（含 `onlyPrimary`）与 `getPrimaryLocalisationKey`；
  3. 本地化侧的 `getRelatedDefinitions`，确保能反向索引回区段定义。

## 验证

```powershell
./gradlew test --tests "icu.windea.pls.test.issues.Issue458Test" --no-daemon --console=plain
```

结果：全部通过。

## 经验

- 派生属性若依赖“构造后才注入”的可变状态（如 `element`），不应在构造期立即求值并缓存；应使用计算属性或显式的惰性缓存。
- 优化“计算属性 → 立即缓存”时，需要确认所有依赖在构造完成时已就绪。
