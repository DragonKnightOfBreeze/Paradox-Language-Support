# Issue #459：IDEA 堆栈报错（EP 缓存在类初始化阶段的 `ExceptionInInitializerError`）

> - 模块：扩展点（EP）缓存 / 类初始化（`<clinit>`）/ `LazyValue`
> - 版本：v3.1.0
> - 日期：2026-11-27
> - 状态：已解决
> - 标签：`ExceptionInInitializerError` / `ProcessCanceledException` / `ReadAction.CannotReadException` / `ExtensionPointName` / `LazyValue` / 并发与初始化 / Issue #459

## 背景

- **GitHub Issue**：[#459 - idea堆栈报错](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/459)
- **报告者**：`E1ectror`
- **标签**：`bug`、`ide-compatibility`
- **错误表现**：
  在 IDEA 日志中出现与类初始化相关的堆栈报错，典型首行为
  `java.lang.ExceptionInInitializerError`，其 `Caused by` 为
  `com.intellij.openapi.progress.ProcessCanceledException` 或
  `com.intellij.openapi.application.ReadAction.CannotReadException`。
  堆栈顶层落在若干 EP 伴生对象的缓存计算函数中（最早可见于
  `ParadoxScriptExpressionMatcher.computeCache()`）。

## 分析

### 1. 触发链路

- 多数 EP 接口采用如下写法：`companion object INSTANCE` 中持有
  `LazyValue<...> CACHE`，并在 `init { ... }` 中**急切**调用
  `CACHE.initialize { computeCache() }`（或 `CACHE.reinitialize { compute() }`）。
- `companion object` 的 `init { ... }` 会在伴生类的 `<clinit>`（静态初始化）阶段执行。
  该阶段并未处于可读上下文（read action）中，也可能处于线程被取消的状态。
- 缓存计算内部需要读取 `EP_NAME.extensionList`。在不满足可读条件时，平台会抛出
  `ReadAction.CannotReadException`；线程被取消时抛出 `ProcessCanceledException`。
- 这两个异常从 `<clinit>` 逸出后，JVM 会把该类标记为初始化失败，并包装为
  `ExceptionInInitializerError`。此后**同一 JVM 中再次访问该类都会直接抛出
  `NoClassDefFoundError`**，且无法通过“重试”恢复——这是本 Issue 危害较大的根本原因。

### 2. 为什么必须改为惰性初始化

- `<clinit>` 每个类生命周期内**只会执行一次**。任何在该阶段失败的一次性初始化都具有
  永久性，无法被后续重试纠正。
- `LazyValue.initialize` 本身已经是“初始化器成功返回后才写入值”的语义：
  ```kotlin
  inline fun <T> initialize(crossinline initializer: () -> T): T {
      if (isInitialized()) return get()
      synchronized(this) {
          if (isInitialized()) return get()
          val value = initializer() // 若此处抛出（如 ProcessCanceledException），不会写入
          this.value = value
          return value
      }
  }
  ```
  因而只要把初始化调用从 `<clinit>` 移到**实际读取缓存的方法**中，失败就不会污染
  `LazyValue` 的已初始化状态，后续对同一读取入口的调用可以正常重试。

### 3. `get()` 与 `getOrNull()` 的语义区分

- 早期 `LazyValue` 实现了 `Supplier<T?>`，`get()` 实际返回可空值，语义容易与
  `getOrNull()` 混淆。
- 重构后：
  - `get()`：仅在确认已初始化后使用，返回非空 `T`；
  - `getOrNull()`：未初始化时返回 `null`，不做强制断言；
  - `value` 属性：未初始化时返回 `null`（兼容旧用法）。
- 注意：非 `inline` 的 `getOrNull()` 中的 `_value as? T` 会因泛型擦除退化为
  `as? Any?`，从而在未初始化时把内部哨兵值 `UNINITIALIZED` 当作结果返回。因此
  `getOrNull()` 必须先判断 `isInitialized()`，再执行 `_value as T`（本次一并修正）。
- 读取入口在调用 `CACHE.initialize { ... }` 之后，缓存必然已初始化，后续应使用
  `get()`；仅“可能未初始化”的场景才使用 `getOrNull()`。

## 解决方案

分成三个提交完成（均关联 `#459`）：

1. **`c3db479c09`** `[fix] Refactor LazyValue`：区分 `get()` / `getOrNull()`，
   统一初始化器返回非空 `T` 的契约。
2. **`589a31e704`** `[fix] ... part 1`：以 `ParadoxScriptExpressionMatcher` 为样板，
   将 `init { ... }` 中的急切初始化移除，只在读取入口通过 `CACHE.initialize { ... }`
   懒初始化；`init { ... }` 仅保留
   `EP_NAME.addExtensionPointListener { CACHE.reinitialize { ... } }`。
3. **part 2（本次）**：将同样的改造推广到其余 18 个采用相同模式的 EP 类。

改造后的统一形态：

```kotlin
companion object INSTANCE {
    @JvmField val EP_NAME = ExtensionPointName<X>("...")

    fun getAll(): List<X> = getAllFromCache()

    // region Implementations

    @JvmField val CACHE = LazyValue<List<X>>()

    init {
        // 只注册监听，不在此处读取 EP_NAME.extensionList
        EP_NAME.addExtensionPointListener { CACHE.reinitialize { compute() } }
    }

    private fun getAllFromCache(): List<X> {
        return CACHE.initialize { compute() }
    }

    private fun compute(): List<X> = EP_NAME.extensionList.optimized()

    // endregion
}
```

### 纳入 part 2 的 EP 类清单

`src/main/kotlin/icu/windea/pls/ep/`：

- `analysis`：
  - `ParadoxRootMetadataProvider`
  - `ParadoxInferredGameTypeProvider`
  - `ParadoxIgnoredFileProvider`
- `inspections`：
  - `ParadoxUnresolvedExpressionChecker`
  - `ParadoxIncorrectSyntaxChecker`
  - `ParadoxIncorrectExpressionChecker`
  - `ParadoxDefinitionInspectionSuppressionProvider`
- `match`：
  - `ParadoxPatternMatcher`
  - `ParadoxScriptExpressionMatchOptimizer`
  - `ParadoxCsvExpressionMatcher`
- `resolve.config`：
  - `CwtConfigContextProvider`
  - `CwtDeclarationConfigContextProvider`
  - `CwtOverriddenConfigProvider`
  - `CwtRelatedConfigProvider`
- `resolve.expression`：
  - `ParadoxCsvExpressionSupport`
  - `ParadoxLocalisationExpressionSupport`
  - `ParadoxPathReferenceExpressionSupport`
  - `ParadoxScriptExpressionSupport`

此外，`ParadoxScriptExpressionMatcher` 已在 part 1（`589a31e704`）完成改造。

### 核对结论

- 上述 EP 的 `CACHE` 字段均只在其伴生对象内部使用，不存在跨类的
  `Xxx.CACHE.get()` / `getOrNull()` 外部调用点，因此读取入口统一收敛到
  `getAll()` / `get()` / `getAllFromCache()` / `getFromCache()`。
- `init { ... }` 中不再残留任何 `CACHE.initialize` / `CACHE.reinitialize` 的急切调用。

## 验证

### 1. 回归测试

- `src/test/kotlin/icu/windea/pls/test/issues/Issue459Test.kt`：
  - `testLazyValue_doesNotMarkInitializedWhenInitializerCanceled`：
    初始化器抛出 `ProcessCanceledException` 后 `isInitialized()` 仍为 `false`、
    `getOrNull()` 为 `null`，重试可正常完成并写入值。
  - `testEpCache_isInitializedLazily`：
    访问代表性 EP（`CwtRelatedConfigProvider`）的 `CACHE` 触发 `<clinit>` 后
    `isInitialized()` 仍为 `false`；调用 `getAll()` 后变为 `true` 且能取到已注册扩展。
- `LazyValueTest` 补充 `get()` / `getOrNull()` 的语义用例。

### 2. 执行命令

```bash
./gradlew compileKotlin compileTestKotlin test \
  --tests "icu.windea.pls.test.issues.Issue459Test" \
  --tests "icu.windea.pls.core.util.values.LazyValueTest" \
  --no-daemon --console=plain
```

## 参考链接

- 关联 Issue：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/459
- 里程碑：`v3.1.0`
- 相关提交：
  - `c3db479c09` `[fix] Refactor LazyValue`
  - `589a31e704` `[fix] ... part 1`
  - part 2（本提交）
