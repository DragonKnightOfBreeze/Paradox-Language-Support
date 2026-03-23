<!-- Context: project-intelligence/technical | Priority: critical | Version: 1.0 | Updated: 2026-03-23 -->

# Technical Domain

**用途**：Paradox Language Support 插件的技术栈、架构与核心开发模式。
**更新时机**：技术栈变更 | 新增核心模式 | 架构决策

## Quick Reference

**受众**：开发者、AI 代理
**关联文件**：`business-domain.md` | `decisions-log.md`

---

## 主要技术栈

| 层次 | 技术 | 说明 |
|------|------|------|
| 语言 | Kotlin（主）/ Java | 插件主体；Java 用于部分平台兼容场景 |
| 平台 | IntelliJ Platform（PSI-based） | 语言支持核心；非 LSP 架构 |
| 构建 | Gradle + IntelliJ Platform Gradle Plugin | JDK 21（`kotlin.jvmToolchain(21)`）|
| 配置驱动 | CWT Config System | 类 JSON Schema，驱动补全/检查/导航等语义功能 |
| AI 集成 | LangChain4j | 本地化翻译/润色工作流 |
| 工具集成 | ImageMagick / Tiger lint / Translation Plugin | 可选依赖，按需启用 |

---

## 项目结构

```
src/main/kotlin|java|resources   # 插件源码
src/test/kotlin|java|resources   # 测试代码与资源
src/test/testData                # 测试数据（CWT/脚本/本地化文件）
cwt/                             # CWT 规则仓库（core + 各游戏类型）
docs/                            # 语言语法与规则格式参考文档
documents/                       # 维护者文档（含 AI 生成文档）
META-INF/pls-*.xml               # 插件注册文件（由 plugin.xml 引入）
```

---

## 核心代码模式

### Manager 模式（典型结构）

```kotlin
// icu.windea.pls.lang.util.ParadoxDefinitionManager
object ParadoxDefinitionManager {
    // 内嵌 Keys 对象统一管理缓存键
    object Keys : KeyRegistry() {                        // icu.windea.pls.core.util.KeyRegistry
        val cachedDefinitionInfo by registerKey<CachedValue<ParadoxDefinitionInfo>>(Keys)
        val cachedDeclaration by registerKey<CachedValue<Any>>(Keys)
    }

    fun getInfo(element: ParadoxDefinitionElement): ParadoxDefinitionInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = ParadoxDefinitionService.resolveInfo(element, file)
                val dependencies = ParadoxDefinitionService.getDependencies(element, file)
                value.withDependencyItems(dependencies)
            }
        }
    }
}
```

### Service 模式（EP 驱动，底层解析）

```kotlin
// Manager 负责缓存，Service 负责实际解析逻辑
// icu.windea.pls.lang.util.ParadoxConfigManager（委托给 ParadoxConfigService）
private fun getConfigContextFromCache(element: ParadoxScriptMember): CwtConfigContext? {
    return CachedValuesManager.getCachedValue(element, Keys.cachedConfigContext) {
        val value = ParadoxConfigService.getConfigContext(element)
        value.withDependencyItems(element, ParadoxModificationTrackers.Resolve)
    }
}
```

### 核心 API 调用模式

```kotlin
// 规则上下文 / 匹配规则
// icu.windea.pls.lang.util.ParadoxConfigManager
ParadoxConfigManager.getConfigContext(element)
ParadoxConfigManager.getConfigs(element, options)

// 作用域匹配
// icu.windea.pls.lang.util.ParadoxScopeManager
ParadoxScopeManager.matchesScope(scopeContext, scopeToMatch, configGroup)

// 定义检索
// icu.windea.pls.lang.resolve.ParadoxDefinitionSearch
ParadoxDefinitionSearch.search(name, gameType, project).findAll()

// 协程作用域 / 规则组
// icu.windea.pls.PlsFacade
PlsFacade.getCoroutineScope(project)
PlsFacade.getConfigGroup(project, gameType)
```

---

## 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名前缀 | 领域名作前缀 | `CwtConfig`、`ParadoxScriptXxx`、`PlsXxx` |
| 抽象类后缀 | `Base` | `ParadoxScriptExpressionSupportBase` |
| 缩写原则 | 避免非前缀缩写 | 用 `context`/`sc`，**不用** `ctx` |
| 测试数据文件 | `snake_case.test.{ext}` | `usage_direct_stellaris.test.txt` |

### 包结构

| 包 | 职责 |
|----|------|
| `icu.windea.pls.core` | 标准库/平台/三方扩展 + 共用工具 |
| `icu.windea.pls.config` | CWT 规则模型 + 服务/解析器/操作器 |
| `icu.windea.pls.lang` | 插件专属、跨语言代码（组件/扩展/工具等）|
| `icu.windea.pls.lang.match` | 语义级匹配（基于索引、引用解析、规则）|
| `icu.windea.pls.lang.resolve` | 语义级解析（同上）|
| `icu.windea.pls.lang.util` | 高层 Manager + 特殊组件（如渲染器）|
| `icu.windea.pls.tools` | 工具类 API（启动器、生成器、日志读取器）|

---

## 代码规范

- 优先用 Kotlin 编写测试和新代码
- 索引策略：文件级数据 → `FileBasedIndex`；PSI 结构数据（不依赖动态数据）→ `StubIndex`；依赖引用解析的数据 → `FileBasedIndex`
- 缓存策略：全局缓存用 strong value + size/TTL；大型规则对象缓存用 soft value；IDE 生命周期绑定缓存用 soft value
- 优先通过已有 EP 和 config 驱动机制扩展，**不硬编码**游戏专属行为
- 修改保持最小化、局部化；新增 EP 实现时遵循命名规范
- 行为变更时添加或更新测试；区分单元测试与集成测试
- KDoc 注释默认用中文；普通注释根据上下文选用；引用类型用 `[PsiElement]` 形式

---

## 安全与稳定性

- **代码注入子系统**：不要轻易修改，除非完全理解影响范围
- 多项目场景下 IDE 工具调用必须传 `project_path` 参数
- IDE 处于 dumb mode 时，先通过 `ide_index_status` 确认就绪
- 可选依赖（Markdown、Diagrams、Translation Plugin）仅在存在时启用

---

## 📂 Codebase References

| 位置 | 说明 |
|------|------|
| `src/main/kotlin/icu/windea/pls/lang/util/` | Manager 层（`ParadoxDefinitionManager` 等）|
| `src/main/kotlin/icu/windea/pls/lang/resolve/` | Service 层（解析逻辑）|
| `src/main/kotlin/icu/windea/pls/config/` | CWT 规则模型与规则组 |
| `src/main/kotlin/icu/windea/pls/core/util/` | 基础工具（`KeyRegistry`、`CacheBuilder` 等）|
| `src/main/resources/META-INF/pls-*.xml` | 插件注册文件 |
| `cwt/` | CWT 规则仓库 |
| `build.gradle.kts` | 构建配置 |
