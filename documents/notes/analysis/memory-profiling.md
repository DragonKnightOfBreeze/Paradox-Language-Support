## A1 内存分析 构建索引时 Stellaris 游戏目录 {#a1}

> - 版本：v3.0.1
> - 日期：2026-08-02+
> - 状态：（跳过）
> - 文件：（跳过）

### 步骤

- 清空缓存，重新打开 IDE。
- 打开 Stellaris 游戏目录，等待索引构建完成。

### 详情

### 分析

## B1 内存分析 完成项目分析后 Stellaris 游戏目录 {#b1}

> - 版本：v3.0.1
> - 日期：2026-08-02+
> - 状态：（跳过）
> - 文件：（跳过）

### 步骤

- 打开 Stellaris 游戏目录。
- 完成项目分析（加载规则分组、构建索引等）后，再附加 profiler。
- 依次打开游戏目录中的以下文件，等到完成文件分析后，再打开下一个。
  - `localisation/simp_chinese/wilderness_l_simp_chinese.yml`
  - `common/armies/00_defense_armies.txt`
  - `common/buildings/00_capital_buildings.txt`
  - `events/first_contact_events.txt`
  - `events/machine_age_crisis_events.txt`
  - `events/shroud_events.txt`
  - `events/gray_goo_events.txt`
  - 其他随机的脚本文件和本地化文件。

### 详情

### 分析