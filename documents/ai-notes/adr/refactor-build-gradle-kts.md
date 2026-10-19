# 重构 `build.gradle.kts`：构建逻辑的封装与约定

> - 模块：构建 > Gradle 构建脚本
> - 版本：v3.0.5
> - 日期：2026-10-19
> - 状态：已完成

## 说明

当前根构建脚本 `build.gradle.kts` 已增长到约 735 行，除了项目本身的声明式配置外，还混入了大量实现细节：

- 基准测试的自定义 task、JMH classpath 拼装、JSON 报告解析、Markdown 表格生成和环境元数据收集；
- Markdown 转 HTML、CHANGELOG 版本段落提取与过滤；
- CWT 配置仓库的下载/解压任务、Jar 内容包含与路径归一化。

这些逻辑本身是稳定的，但放在根脚本中会带来几个问题：

- 根脚本难以快速浏览，项目级声明与工具级实现混杂；
- 逻辑难以复用和测试；
- 修改基准报告或 CWT 下载逻辑时，容易误伤项目配置；
- 未来继续增加基准测试或配置仓库时，根脚本会继续膨胀。

因此需要将这三块逻辑提取到 `buildSrc`，以 Kotlin convention plugin 和工具对象的形式封装，根脚本只保留“项目是什么、依赖什么、启用哪些约定”。

## 目标

- 根脚本保持声明式、可快速浏览；
- 基准测试、Markdown、CWT 配置的具体实现从根脚本中移出；
- 保持现有任务名、命令、报告路径和 CI 行为不变；
- 使用与生产代码/测试代码一致的 JSON 处理方式：`kotlinx.serialization`；
- 为后续可能的构建逻辑测试、版本目录和 included build 留出清晰路径。

## 非目标

- 本次不引入 `gradle/libs.versions.toml`；
- 本次不将构建逻辑迁移到 `gradle/plugins` included build；
- 本次不为构建逻辑新增完整的单元测试/TestKit 测试；
- 本次不改变基准测试的具体场景和性能结论；
- 本次不改变 CWT 配置仓库列表与下载行为；
- 本次不改变根脚本中与项目强相关的配置，例如 `intellijPlatform`、`dependencies`、`sourceSets`、Kotlin 编译参数、归档命名等。

## 现状拆分

| 区块 | 当前大致位置 | 主要复杂度 |
|---|---|---|
| Benchmark | 41–199、682–728 行 | task 类型、classpath、JMH 配置、JSON 报告、Markdown、git/环境元数据 |
| Markdown | 15–20、218–227、445–538 行 | `buildscript` classpath、Markdown 转 HTML、CHANGELOG 解析 |
| CWT Config | 540–603、621–647 行 | 仓库列表、下载/解压任务、Jar 内容与路径归一化 |

实际提取后，根脚本从约 735 行减少到约 310 行。

## 决策

### 1. 使用 `buildSrc` + Kotlin convention plugins

选择 `buildSrc` 而不是以下方案：

- **脚本插件（`apply(from = "gradle/*.gradle.kts")`）**：改动最小，但没有类型安全访问器，task 类和工具类难以共享，长期只是把长文件拆成多个短文件。
- **`gradle/plugins` included build**：更适合多模块复用和构建逻辑测试，但当前项目是单模块，初始成本偏高。

`buildSrc` 的优点：

- 自动参与构建，无需额外发布或 `includeBuild`；
- 可以使用 Kotlin 编写类型安全的 plugin、task 类和工具对象；
- 与现有 Kotlin/Gradle 技术栈一致；
- 后续如需迁移到 included build，代码结构可以基本保留。

### 2. 使用 Kotlin `Plugin<Project>`，而不是预编译脚本插件

基准测试和 CWT 配置都包含自定义 task 类、扩展对象和较多注册逻辑，使用 Kotlin `Plugin<Project>` 更直接：

- 插件 ID 与实现类显式注册；
- 便于在 `buildSrc` 中按包组织；
- 后续可以自然迁移到 included build；
- 避免预编译脚本插件在外部插件应用和自定义 task 类上的额外约束。

计划中的插件：

- `chronicle.benchmark`
- `chronicle.cwt-config`

Markdown 只包含纯函数，不需要插件，保留为工具对象。

### 3. Markdown / CHANGELOG 使用工具对象

提取为：

- `icu.windea.pls.gradle.markdown.MarkdownSupport`
- `icu.windea.pls.gradle.markdown.ChangelogSupport`

根脚本只保留少量调用：

```kotlin
description = MarkdownSupport.toHtml(...)
changeNotes = providers.gradleProperty("pluginVersion").map { ... }
```

Markdown 解析库依赖从根 `buildscript` 移到 `buildSrc/build.gradle.kts`。

### 4. Benchmark 封装为 convention plugin

`chronicle.benchmark` 负责：

- 应用 JMH Gradle Plugin；
- 配置 `jmh { includeTests = false }`；
- 注册 `benchmark` 和 `benchmarkReport`；
- 拼装 benchmark classpath；
- 传递 `chronicle.benchmark.classesDir` / `chronicle.benchmark.reportDir`；
- 处理 `--benchmarks` 参数；
- 从 JSON 生成 Markdown/TEXT 报告和元数据。

根脚本只保留：

- `jmh(...)` 的额外依赖（Guava、fastutil）；
- 可选的 `chronicleBenchmark { ... }` 覆盖项。

### 5. CWT Config 封装为 convention plugin

`chronicle.cwt-config` 负责：

- 应用 `de.undercouch.download`；
- 注册 `prepareCwtConfigs`、`downloadCwtConfig_*`、`unzipCwtConfig_*`；
- 维护默认 CWT 仓库列表；
- 配置 `jar` / `instrumentedJar` 的 CWT 内容与路径归一化；
- 提供 `cwtConfig { ... }` 扩展，允许覆盖默认行为。

根脚本只保留说明性配置和与项目归档相关的其他内容（README、LICENSE、排除项等）。

### 6. JSON 使用 `kotlinx.serialization`

`buildSrc` 中使用 `kotlinx.serialization` 的 `Json.parseToJsonElement` 解析 JMH 结果和 `run.json`，不再使用 Groovy `JsonSlurper`。

这样可以：

- 与生产代码/测试代码的 JSON 处理方式保持一致；
- 避免在 `buildSrc` 中额外引入 Groovy；
- 继续使用 `JsonObject` / `JsonArray` 的显式取值方式，不需要为构建逻辑声明 `@Serializable` 数据类，也不需要额外启用序列化编译器插件。

## 目标结构

```text
buildSrc/
  build.gradle.kts
  src/main/kotlin/icu/windea/pls/gradle/
    benchmark/
      ChronicleBenchmarkPlugin.kt
      ChronicleBenchmarkExtension.kt
      ChronicleBenchmarkTask.kt
      ChronicleBenchmarkReportTask.kt
      BenchmarkReport.kt
    markdown/
      MarkdownSupport.kt
      ChangelogSupport.kt
    cwt/
      CwtConfigPlugin.kt
      CwtConfigExtension.kt
      CwtRepository.kt
      CwtConfigJarSupport.kt
```

根 `build.gradle.kts` 的预期形态：

```kotlin
plugins {
    // Kotlin / IntelliJ Platform / Kover ...
    id("chronicle.benchmark")
    id("chronicle.cwt-config")
}

description = MarkdownSupport.toHtml(...)
changeNotes = ...

dependencies {
    // 项目依赖 + jmh(...) 额外依赖
}

chronicleBenchmark {
    // 可选覆盖；默认值由插件提供
}

cwtConfig {
    // 可选覆盖；默认值由插件提供
}

// intellijPlatform、sourceSets、kotlin、项目特定 tasks ...
```

## 任务

- [x] 新增 ADR 与索引
- [x] 建立 `buildSrc`，迁移 Markdown / CHANGELOG 工具
- [x] 迁移 CWT Config Source Setup
- [x] 迁移 Benchmark 任务与报告
- [x] 运行验证（编译、基准测试、报告生成、插件打包）
- [x] 更新 ADR 勾选、`roadmap.md` 与相关文档

## 关键文件位置

- 根构建脚本：`build.gradle.kts`
- 插件版本：`settings.gradle.kts`
- 构建逻辑：`buildSrc/build.gradle.kts`
- 基准插件：`buildSrc/src/main/kotlin/icu/windea/pls/gradle/benchmark/`
- Markdown 工具：`buildSrc/src/main/kotlin/icu/windea/pls/gradle/markdown/`
- CWT 插件：`buildSrc/src/main/kotlin/icu/windea/pls/gradle/cwt/`
- 基准启动器：`src/jmh/kotlin/icu/windea/pls/test/ChronicleBenchmarkLauncher.kt`
- 版本路线图：`documents/notes/roadmap.md`

## 验证方式

- `./gradlew help`：确认构建脚本与 `buildSrc` 可编译；
- `./gradlew jmhCompileGeneratedClasses`：确认基准源集与 JMH 生成正常；
- `./gradlew benchmark --benchmarks=AntMatcherBenchmark.matches`：确认 `--benchmarks` 与精简 classpath 正常；
- `./gradlew benchmarkReport -x benchmark`：确认 JSON → Markdown/TEXT 报告正常；
- `./gradlew test --tests "icu.windea.pls.core.match.AntMatcherTest"`：确认常规测试未受影响；
- `./gradlew buildPlugin`：确认 CWT 配置仍被正确打包。

## 备选方案与延伸思考

### 关于 `gradle/libs.versions.toml` 与声明式风格

`gradle/libs.versions.toml` 是 Gradle 的版本目录（version catalog）机制：把依赖和插件的坐标、版本集中到一个 TOML 文件，并在构建脚本中通过类型安全访问器引用，例如 `libs.junit`、`libs.versions.kotlin`。

优点：

- 版本集中，减少散落的版本字符串和重复声明；
- 类型安全访问器让依赖声明更接近“声明式”；
- IDE 补全和可发现性更好；
- 多模块项目共享依赖版本时更清晰。

缺点：

- 对当前单模块项目来说，额外引入了一层间接性；
- `buildSrc` 与根构建的版本来源需要额外协调；
- 版本目录更适合“依赖/版本很多、需要统一治理”的场景，而不是“版本少但构建逻辑复杂”的场景；
- 与当前维护者的偏好不完全一致。

因此本次不采用。当前仍以 `gradle.properties` 中的少量版本属性 + 构建脚本中的显式依赖声明为主；如果未来依赖规模或模块数量明显增长，再评估引入版本目录。

### 3 个可选的后续步骤（目前不采用）

1. **引入 `gradle/libs.versions.toml`**
   - 见上一节；本次不采用，后续如果依赖/模块规模明显增长再评估。

2. **为构建逻辑增加单元测试 / TestKit 测试**
   - 优点：可以测试 `MarkdownSupport`、`ChangelogSupport`、报告渲染、CWT 路径归一化等纯逻辑；对 convention plugin 的行为也可以做 TestKit 验证。
   - 暂不采用的原因：当前构建逻辑规模仍可控，且已有端到端命令验证；先保持 `buildSrc` 轻量，等逻辑稳定或出现回归后再补测试。

3. **迁移到 `gradle/plugins` included build**
   - 优点：插件 ID、依赖和测试更隔离；更适合多模块或需要独立发布构建逻辑的场景；对 configuration cache 也更友好。
   - 暂不采用的原因：当前是单模块项目，`buildSrc` 已足够；迁移会引入额外的 `settings.gradle.kts`、插件发布配置和构建入口，收益暂时不明显。

### 其他备选

- **脚本插件拆分**：适合临时降复杂度，但无法提供类型安全的 task 类和工具对象，不作为最终方案。
- **预编译脚本插件**：比 Kotlin plugin 更轻，但自定义 task 类和外部插件应用会更绕；本次选择 Kotlin plugin。
- **把 `intellijPlatform` 配置也封装**：会让构建逻辑依赖 IntelliJ Platform Gradle Plugin 的内部类型，收益低、耦合高；本次保留在根脚本。

## 实施记录

- 根 `build.gradle.kts` 从约 735 行减少到约 310 行。
- 新增 `buildSrc`，注册两个 Kotlin convention plugin：
  - `chronicle.benchmark`：应用 JMH 插件，注册 `benchmark` / `benchmarkReport`，封装 classpath 与报告逻辑。
  - `chronicle.cwt-config`：应用 download 插件，注册 CWT 下载/解压任务，并配置 `jar` / `instrumentedJar` 的配置内容。
- Markdown / CHANGELOG 逻辑迁移为 `MarkdownSupport` / `ChangelogSupport` 工具对象。
- JMH JSON 解析改用 `kotlinx.serialization` 的 `JsonElement` API，未启用序列化编译器插件。
- 插件版本从根 `plugins` 块移到 `settings.gradle.kts` 的 `pluginManagement { plugins { ... } }`。
  - 原因：移除根 `buildscript` 后，Kotlin DSL 对 `plugins` 块中 `providers.gradleProperty(...)` 的解析方式发生变化，导致 `providers` 无法解析；将插件版本集中到 settings 是更稳定的做法，也为后续版本治理留出入口。
- 保持现有任务名、命令、报告路径和 CI 行为不变。
- 已通过：
  - `./gradlew help`
  - `./gradlew jmhCompileGeneratedClasses`
  - `./gradlew benchmark --benchmarks=AntMatcherBenchmark.matches`
  - `./gradlew benchmarkReport -x benchmark`
  - `./gradlew test --tests "icu.windea.pls.core.match.AntMatcherTest" --tests "icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmarkTest"`
  - `./gradlew buildPlugin`
- 已确认 `jar` 中包含 `config/` 下的 CWT 配置内容。

## 备注

- 本项目当前未启用 configuration cache；如果后续启用，应优先把 `System.getProperties()` 的读取改为 `providers` / `CommandLineArgumentProvider`。
- `buildSrc` 是独立构建，不应假设它能直接读取根 `gradle.properties`；版本与默认值应通过扩展属性传入，或在 `buildSrc` 内定义。
- 报告目录中的时间戳报告仍由 `benchmarkReport` 在任务执行时生成，不使用配置文件中的固定时间。
