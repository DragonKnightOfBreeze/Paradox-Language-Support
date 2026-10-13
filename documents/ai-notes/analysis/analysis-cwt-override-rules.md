# 领域：规则系统 覆盖规则

> 目标：梳理 PLS「覆盖规则」（`overrides` / 原「优先级规则」）的语义、来源与数据获取方式，并记录各游戏覆盖策略的确定程度与依据。
>
> - 版本：3.0.4-dev
> - 日期：2026-10-02
> - 参考来源：**各游戏官方英文 wiki + IronyModManager (IMM)**（不再使用第三方翻译/转述）
> - 状态：探索性笔记（`ai-notes` 按原样存档；后续项目状态可能变化）

---

## 1. 背景与定位

覆盖规则用于指定**目标目录（相对于入口目录）**的覆盖策略，影响目标的**生效顺序**与**查询结果排序**（流式查询除外），并被用于覆盖相关的代码检查。

- 数据模型：`icu.windea.pls.config.model.CwtConfigGroupDataModel.overrides: Map<String, CwtOverrideConfig>`
- 配置对象：`icu.windea.pls.config.config.detached.CwtOverrideConfig`（`filePath` + `strategy`）
- 策略枚举：`icu.windea.pls.model.overrides.ParadoxOverrideStrategy`
- 解析入口：`icu.windea.pls.ep.config.configGroup.CwtConfigGroupProcessors.FileBased`（读取 `overrides = { ... }`）
- 策略提供者：`icu.windea.pls.ep.overrides.ParadoxBaseOverrideStrategyProvider`（经 `ParadoxFilePathMapBasedOverrideStrategyProvider` 匹配路径）
- 规则文件：
  - 通用：`cwt/core/00_overrides.cwt`
  - 各游戏：`cwt/cwtools-<game>-config/config/overrides.cwt`
- 文档：`docs/zh/ref-config-format.md#config-override`、`docs/en/ref-config-format.md#config-override`

### 四种覆盖策略

| 策略 | 含义 | 近似引擎行为 |
| --- | --- | --- |
| `fios` | First In, Only Served | 只读一次；先加载者生效，后者被忽略 |
| `lios` | Last In, Only Served | 后读覆盖（默认） |
| `dupl` | Duplicates | 整文件覆盖；必须用同路径文件整体替换 |
| `ordered` | Ordered | 顺序读取；按序新增或合并，不覆盖既有条目 |

> 注意：插件的策略是**查询/检查语义的近似**，并非引擎内部数据结构的精确复刻。例如「不可覆盖」只能以 `fios` 近似。

---

## 2. 数据来源与获取方式

引擎行为没有公开的权威规范，因此只能从官方 wiki 与 IMM 推断。两者可交叉验证：

### 2.1 官方英文 wiki（人工维护，逐目录）

- Stellaris：`https://stellaris.paradoxwikis.com/Modding` →「Overwriting specific elements」→「Common folder」有一张**逐目录**的 `Overwrite` 表（值为 FIOS / LIOS / DUPL / NO，另有错误日志说明）。
- VIC3：`https://vic3.paradoxwikis.com/Mod_compatibility` →「Exceptions」与「List of folders」。
- EU5：`https://eu5.paradoxwikis.com/Mod_compatibility` → 与 VIC3 结构一致。
- CK3：`https://ck3.paradoxwikis.com/Modding` →「Override rules」（Full file override / Single object override）。
- HOI4：`https://hoi4.paradoxwikis.com/Modding` →「Loading files」（无逐目录表）。
- IR / EU4：wiki 的 Modding 页**未提供**覆盖/加载顺序的逐目录信息。

> 官方 wiki 站点带有反爬挑战，普通 HTTP 抓取会返回挑战页（约 3KB）。需使用真实浏览器（本项目使用 Paseo 浏览器工具）访问并提取正文。

### 2.2 IronyModManager (IMM)

IMM（`https://github.com/bcssov/IronyModManager`）为 Stellaris 提供了可直接解析的数据：

- `src/IronyModManager.IO/Mods/InfoProviders/StellarisDefinitionInfoProvider.cs` 的 `FIOSPaths`：只读一次目录。
- `References/CopyAll/Maps/StellarisParserMap.json`：逐目录解析器映射。
- `BaseDefinitionInfoProvider` 的输出命名规则：FIOS 用 `!!!_` 前缀 + 升序（先加载者胜出）；LIOS 用 `zzz_` + 降序；本地化写入 `replace/`。
- HOI4 有解析器映射，但其 provider 未实现（`FIOSPaths` 抛异常），只能作弱参考。

### 2.3 复现脚本

`scripts/util/fetch_override_strategies.py`：

- 将 IMM 解析到**固定 commit**（默认 `master`，脚本内记录 SHA）；
- 解析 `FIOSPaths` 与 `StellarisParserMap.json`；
- 按固定规则推断每个目录的策略；
- 与现有 `overrides.cwt` 做差异对比。

```powershell
python scripts/util/fetch_override_strategies.py --out build/overrides.md
```

本次运行基准：IMM `master` @ `6a8861d734382a9e98f7394ea404fac7a2b884b3`（2026-10-02）。

---

## 3. IMM 推断规则

| IMM 解析器 | 推断策略 | 说明 |
| --- | --- | --- |
| 命中 `FIOSPaths` | `fios` | 先加载者胜出 |
| `*WholeTextParser` | `dupl` | 整文件作为整体处理 |
| `StellarisOverwrittenObjectSingleFileParser` | `dupl` | 整体替换的「单文件对象」 |
| `*OverwrittenParser` | `lios` | 键级对象替换 |
| `*KeyParser` / `DefaultParser` / `*DefinesParser` | `lios` | 默认后读覆盖 |
| `GenericLocalizationParser` | `lios` | 本地化；惯例是放入 `replace/` 目录 |

> 局限：`WholeText` 往往是 IMM「无法解析」时的兜底，不能直接等同于引擎的整文件覆盖。例如 `common/on_actions` 在 IMM 中是 `GenericWholeTextParser`，但引擎实际是**自动合并**（`ordered`）。
>
> 冲突处理：以官方 wiki 为准，IMM 作为佐证；官方 wiki 未覆盖但 IMM 明确的，标注较低可信度。

---

## 4. 各游戏结论

### 4.1 Stellaris（官方 wiki 逐目录表 + IMM，可信度最高）

已写入 `cwt/cwtools-stellaris-config/config/overrides.cwt`。要点：

- `fios`：`common/component_sets`、`common/component_templates`、`common/event_chains`、`common/global_ship_designs`、`common/governments/authorities`、`common/scripted_loc`、`common/scripted_variables`、`common/section_templates`、`common/ship_behaviors`、`common/solar_system_initializers`、`common/special_projects`、`common/start_screen_messages`、`events`。
- `dupl`：`common/inline_scripts`、`common/name_lists`、`common/observation_station_missions`、`common/strategic_resources`、`common/terraform`、`common/traits`。
- `ordered`：`common/on_actions`。
- 含 `DUPL/LIOS` 或版本差异者保守取 `lios` 并注明：`common/buildings`、`common/districts`、`common/ethics`、`common/opinion_modifiers`、`common/planet_classes`、`common/pop_jobs`、`common/species_archetypes`、`common/technology`。

对比旧配置的**修正**（基于官方 wiki）：

| 目录 | 旧值 | 新值 | 依据 |
| --- | --- | --- | --- |
| `common/star_classes` | `fios` | （默认 `lios`） | wiki：LIOS |
| `common/static_modifiers` | `fios` | （默认 `lios`） | wiki：LIOS |
| `common/relics` | `fios` | （默认 `lios`） | wiki：LIOS |
| `common/ship_sizes` | `dupl` | （默认 `lios`） | wiki：LIOS |
| `common/starbase_modules` | `dupl` | （默认 `lios`） | wiki：LIOS |
| `common/diplomatic_actions` | `dupl` | （默认 `lios`） | wiki：LIOS |
| `common/technology` | `dupl` | `lios` | wiki：LIOS* |
| `common/governments/authorities` | `dupl` | `fios` | wiki：FIOS |
| `common/strategic_resources` | `fios` | `dupl` | wiki：DUPL |
| `common/traits` | `dupl` | `dupl`（不变，IMM 为 FIOS） | wiki：DUPL/NO |
| `common/observation_station_missions` | `dupl`/`lios` | `dupl` | wiki：entire override only |

### 4.2 Victoria 3（官方 wiki `Mod compatibility`）

- `common/on_actions = ordered`：events/on_actions/random_events 追加；trigger/effect 需整文件覆盖。
- `events = fios`：先加载者胜出。
- `gui = fios`：GUI type/template 不可覆盖，先加载者胜出。
- `localization = fios`：后加载的键不覆盖先前的键；使用 `replace/` 目录。
- `common/dna_data = ordered`：同名 DNA 不覆盖而产生重复；只有整文件覆盖才会替换。

> 旧配置中的 `common/history/* = ordered` 无 wiki 依据（wiki 未列出 history 例外，默认按隐式替换 `lios`），已移除。

### 4.3 Europa Universalis V（官方 wiki `Mod compatibility`）

- `common/on_actions = ordered`。
- `events = fios`：先加载者胜出。
- `localization = fios`：后加载的键不覆盖先前的键；`replace/` 目录优先。
- GUI type/template 在 EU5 中为**后加载者胜出（LIOS）**，故无需条目（与 VIC3 不同）。

> 旧配置中的 `gui = fios` 无 wiki 依据（EU5 实为 LIOS），已移除；`setup/start`、`common/attribute_columns` 亦无来源，已移除。

### 4.4 Crusader Kings III（官方 wiki `Override rules`）

- `common/on_action = ordered`：events/random_events/on_actions 追加；trigger/effect 覆盖但报错。
- `gui = fios`：UI type/template 先加载者优先。
- `common/holdings = dupl`：添加建筑必须整文件覆盖 holdings 文件。
- 通用「单对象覆盖」为 LIOS（后 ASCII 文件名者胜出）；`common/defines` 为命名空间级替换（LIOS）；事件/角色自 1.20 起可用 `id_override_priority` / `history_override_priority`。

### 4.5 Hearts of Iron IV（官方 wiki `Loading files`）

- 未提供逐目录策略表。通则：同名同路径文件整体替换；其余按求值顺序决定优先级（不同类型的重复处理不一致）。
- 故不添加逐目录条目；未命中者默认 `lios`。

### 4.6 Imperator: Rome / Europa Universalis IV / Crusader Kings II / Victoria II

- 官方 wiki 的 Modding 页未提供逐目录覆盖数据；IMM 也未实现相应 provider。
- 暂不添加游戏级 `overrides.cwt`，沿用通用规则与默认 `lios`。

---

## 5. 冲突与注意事项

- `traits`：wiki 为 `DUPL/NO`，IMM 为 FIOS；以 wiki 为准取 `dupl`。
- `strategic_resources`：wiki 为 `DUPL`，IMM 的 `FIOSPaths` 亦包含它；以 wiki 为准取 `dupl`。
- `section_templates`：wiki 列 `NO DUPL/FIOS`，IMM 为 FIOS；保守取 `fios`。
- `star_classes` / `static_modifiers`：wiki 明确为 LIOS，IMM 解析器为 DefaultParser，与 wiki 一致；旧配置的 `fios` 已修正。
- 覆盖键目前应为**具体目录路径**（匹配方向为「配置键 匹配 类型路径模式」）；不确定是否支持通配键，待核实。

---

## 6. 后续工作

- [ ] 核实官方 wiki 对 `common/technology` 的 `LIOS*` 语义，以及 `common/opinion_modifiers` 的 `DUPL/LIOS`。
- [ ] 评估内联脚本（`common/inline_scripts`）在各游戏中的覆盖语义。
- [ ] 为 IR / EU4 / CK2 / VIC2 寻找官方来源；确认其 wiki 是否给出逐目录列表。
- [ ] 核实覆盖键与路径模式（ANT）的匹配规则说明。
- [ ] 待官方 wiki 更新后，重新运行 `scripts/util/fetch_override_strategies.py` 并同步。

---

## 7. 参考

- PLS 规则格式参考手册（覆盖规则）：`docs/zh/ref-config-format.md#config-override`
- IronyModManager：<https://github.com/bcssov/IronyModManager>
- 官方 wiki：
  - Stellaris：<https://stellaris.paradoxwikis.com/Modding>
  - Victoria 3：<https://vic3.paradoxwikis.com/Mod_compatibility>
  - Europa Universalis V：<https://eu5.paradoxwikis.com/Mod_compatibility>
  - Crusader Kings III：<https://ck3.paradoxwikis.com/Modding>
  - Hearts of Iron IV：<https://hoi4.paradoxwikis.com/Modding>
- 复现脚本：`scripts/util/fetch_override_strategies.py`
- 规则系统规则领域笔记：`documents/ai-notes/domain/领域_规则系统_规则.md`
