# `StatefulPresentation` 被标记为内部 API 导致 IDEA 2026.3 兼容性问题（改用 `BasePresentation` 自定义实现）（2026-11-03）

> - 模块：构建
> - 版本：v3.0.5
> - 日期：2026-11-03

## 背景

- 插件 3.0.5 在 JetBrains Plugin Verifier 针对 IntelliJ IDEA 2026.3 (263.7307.112) 的兼容性验证中报告 10 处 internal API 使用，全部集中在 `com.intellij.codeInsight.hints.presentation.StatefulPresentation` 及其内部类 `StateMark`：
  - 内部类：`StatefulPresentation`（2）、`StatefulPresentation.StateMark`（2）
  - 内部方法：`getState()`（2）、`getPresentation()`（1）、`setState(S)`（1）
  - 内部构造器：`StatefulPresentation.<init>(...)`（2）、`StatefulPresentation.StateMark.<init>(String)`（1）
- 同一插件在 2026.2.4 上验证时未报告该项。

## 分析

### 原因

- `StatefulPresentation` 位于 `platform/lang-impl`。对比平台源码可见，261 分支尚未标注该类型，262 分支起其类本身及其嵌套的 `StateMark` 均被标注为 `@ApiStatus.Internal`。
- 插件在 `AsyncIconPresentation` 中继承 `StatefulPresentation<Icon?>`，用于在异步物化图片完成后安全地更新内嵌提示的 presentation。

### 替代方案评估

- `BiStatePresentation`：类本身公开，但状态固定为布尔值，且其 `State` / `STATE_MARK` 仍为内部 API，无法承载 `Icon?` 状态。
- `DynamicDelegatePresentation`：同样为 `@ApiStatus.Internal`。
- `StaticDelegatePresentation`：公开，但 presentation 在构造后不可替换，不满足异步更新需求。
- 本地复制 `StatefulPresentation`：可作为兜底方案，但会复制平台内部实现并长期跟随平台演进，仅在缺少可用公开 API 时使用。
- `BasePresentation` + `InlayPresentation`：`BasePresentation` 公开；`InlayPresentation` 为 `@ApiStatus.Experimental`（插件原本已在使用）。两者足以实现本场景所需的最小状态管理，因此优先采用此方案，不引入本地副本。

### 解决方案

- `AsyncIconPresentation` 改为直接继承 `BasePresentation`。
- 自行维护 `state: Icon?`、当前 presentation 及其 `PresentationListener`；状态变化时重建 presentation，并触发 size/content 更新事件。
- 在 `updateState(previousPresentation)` 中，若同一元素上的旧 presentation 仍是 `AsyncIconPresentation`，先把已物化的图标状态转移到新实例，再委托给底层 presentation 的 `updateState`，从而保留跨 pass 的状态，避免图标闪烁和重复渲染。
- 不再引用 `StatefulPresentation` / `StateMark`，也未引入本地副本。

### 注意事项

- `InlayPresentation` 仍为实验性 API，保留 `@Suppress("UnstableApiUsage")`。
- 已在最低支持版本 2025.2 上通过 `./gradlew compileKotlin --no-daemon --console=plain`。

## 参见

### 相关代码位置

- `icu.windea.pls.lang.codeInsight.hints.AsyncIconPresentation`
- `com.intellij.codeInsight.hints.presentation.BasePresentation.BasePresentation`

### 参考链接

- 261 分支（无 `@Internal`）：https://github.com/JetBrains/intellij-community/blob/261/platform/lang-impl/src/com/intellij/codeInsight/hints/presentation/StatefulPresentation.kt
- 262 分支（`@ApiStatus.Internal`）：https://github.com/JetBrains/intellij-community/blob/262/platform/lang-impl/src/com/intellij/codeInsight/hints/presentation/StatefulPresentation.kt
- 263 分支（`@ApiStatus.Internal`）：https://github.com/JetBrains/intellij-community/blob/263/platform/lang-impl/src/com/intellij/codeInsight/hints/presentation/StatefulPresentation.kt
- 263 `BasePresentation`：https://github.com/JetBrains/intellij-community/blob/263/platform/lang-api/src/com/intellij/codeInsight/hints/presentation/BasePresentation.kt
- 263 `InlayPresentation`：https://github.com/JetBrains/intellij-community/blob/263/platform/lang-api/src/com/intellij/codeInsight/hints/presentation/InlayPresentation.kt
- 263 `BiStatePresentation`：https://github.com/JetBrains/intellij-community/blob/263/platform/lang-impl/src/com/intellij/codeInsight/hints/presentation/BiStatePresentation.kt
- 263 `DynamicDelegatePresentation`：https://github.com/JetBrains/intellij-community/blob/263/platform/lang-impl/src/com/intellij/codeInsight/hints/presentation/DynamicDelegatePresentation.kt
- 263 `StaticDelegatePresentation`：https://github.com/JetBrains/intellij-community/blob/263/platform/lang-impl/src/com/intellij/codeInsight/hints/presentation/StaticDelegatePresentation.kt
