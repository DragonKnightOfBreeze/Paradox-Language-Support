# IntelliJ Platform SDK 学习笔记

## 概述

IntelliJ Platform SDK 用于开发 JetBrains IDE 的插件，提供 PSI、索引、UI 组件等 API。支持跨 IDE 兼容。

## 核心组件

### PSI (Program Structure Interface)
```java
PsiElement element = PsiUtil.getElementAtCaret(editor);
if (element instanceof PsiIdentifier) {
    // 处理标识符
}
```

### 索引系统
- 文件索引
- 词法索引
- 符号索引

### 编辑器操作
```kotlin
class MyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // 处理操作
    }
}
```

## 调试技巧

1. **热重载插件**：使用 Gradle `runIde` 任务
2. **日志诊断**：`Logger.getInstance("PluginName").info("Message")`
3. **内存分析**：使用 IDE 内置分析工具

## 工程结构与 plugin.xml

```plain
project/
  src/main/kotlin/...                # 源码（Kotlin/Java）
  src/main/resources/META-INF/plugin.xml
  build.gradle.kts
  gradle.properties
```

`src/main/resources/META-INF/plugin.xml` 最小示例：

```xml
<idea-plugin>
  <id>com.example.myplugin</id>
  <name>My Plugin</name>
  <vendor email="dev@example.com">Your Name</vendor>

  <depends>com.intellij.modules.platform</depends>

  <extensions defaultExtensionNs="com.intellij">
    <applicationService serviceImplementation="com.example.services.MyAppService"/>
  </extensions>

  <actions>
    <action id="MyAction"
            class="com.example.actions.MyAction"
            text="Say Hello"
            description="Show greeting dialog">
      <add-to-group group-id="ToolsMenu" anchor="last"/>
    </action>
  </actions>
</idea-plugin>
```

## Gradle 配置（Kotlin DSL）

```kotlin
// build.gradle.kts
plugins {
  kotlin("jvm") version "1.9.24"
  id("org.jetbrains.intellij") version "1.17.3"
}

repositories { mavenCentral() }

intellij {
  version.set("2024.1")
  type.set("IC") // IntelliJ Community
  plugins.set(listOf("java"))
  sandboxDir.set("${project.buildDir}/idea-sandbox")
}

tasks {
  patchPluginXml {
    sinceBuild.set("241")
    untilBuild.set(null as String?)
  }
  runIde {
    // Windows PowerShell 本地运行： .\\gradlew runIde
  }
  buildPlugin {}
  signPlugin {}
  publishPlugin {}
}
```

提示：`gradle.properties` 可配置 `org.gradle.jvmargs`、`kotlin.code.style=official`、`ideaVersion` 等。

## 示例：Action（带 DumbAware）

```kotlin
package com.example.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages

class MyAction : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    Messages.showInfoMessage("Hello from MyAction!", "My Plugin")
  }
}
```

## 示例：Application Service（新风格）

```kotlin
package com.example.services

import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class MyAppService {
  fun greet(name: String) = "Hello, $name"
}
```

## UI DSL（com.intellij.ui.dsl.builder）

```kotlin
import com.intellij.ui.dsl.builder.panel

val ui = panel {
  row("Name:") {
    textField()
  }
  row {
    button("OK") { /* handle click */ }
    button("Cancel") { /* ... */ }
  }
}
```

## 索引与线程模型

- **读/写动作**：在读线程中访问 PSI/索引，在写命令中修改文档。
  - 读：`com.intellij.openapi.application.readAction { /* ... */ }`
  - 写：`com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) { /* ... */ }`
- **Dumb 模式**：索引重建期间避免依赖索引，使用 `DumbAware` 或检查 `DumbService.isDumb(project)`。
- 常见异常：`IndexNotReadyException`，在 Dumb 模式中访问索引导致。

## 动态插件与热重载

- `runIde` 支持快速开发迭代；部分 EP 支持动态加载/卸载（Dynamic Plugins）。
- 大型改动（新增 EP、语言解析器等）建议重启 IDE 验证。

## 签名与发布

- 任务：`buildPlugin`、`signPlugin`、`publishPlugin`。
- 需要机密：`PUBLISH_TOKEN`、`PRIVATE_KEY`、`PRIVATE_KEY_PASSWORD`、`CERTIFICATE_CHAIN`。
- 参考：插件签名 https://plugins.jetbrains.com/docs/intellij/plugin-signing.html

本地/CI 示例（PowerShell）：

```powershell
$env:PUBLISH_TOKEN = "..."
$env:PRIVATE_KEY = "..."            # PKCS#8 PEM
$env:PRIVATE_KEY_PASSWORD = "..."
$env:CERTIFICATE_CHAIN = "..."       # PEM chain
./gradlew buildPlugin signPlugin publishPlugin --info
```

## 常见问题排查

- **IndexNotReadyException**：在 Dumb 模式访问索引；为 Action 实现 `DumbAware` 或延迟到索引完成。
- **EDT 阻塞**：耗时操作放到后台任务（`ProgressManager.runBackgroundableTask`），UI 更新回到 EDT。
- **ClassLoader/版本不兼容**：校验 `sinceBuild`/`untilBuild`，使用 `runPluginVerifier` 覆盖目标 IDE 版本。
- **VirtualFile vs java.io.File**：API 大多基于 `VirtualFile` 与 `Document`，避免直接 `File` 操作.

## 可扩展内容

- **插件架构**：模块化设计、扩展点（EP）建模、服务/组件边界、动态插件能力与限制。
- **PSI/索引进阶**：自定义索引与 `StubIndex`、缓存一致性、Dumb 模式策略与回退。
- **线程与性能**：Read/Write Action 规约、后台任务/取消、性能基线与快照分析。
- **UI/UX**：UI DSL 最佳实践、可访问性（A11y）、设置页与持久化、通知与提示系统。
- **检查与意图**：`LocalInspectionTool`、`IntentionAction`、`QuickFix` 模式与批量修复策略。
- **测试与验证**：`LightPlatformTestCase`/Fixture、UI 测试、`runPluginVerifier` 与兼容矩阵。
- **构建与发布**：签名/发布通道、Gradle 任务编排、CI 模板、跨 IDE 版本/类型兼容性。
- **诊断与观测**：结构化日志、调试与 Profiler、问题最小复现与回归用例。

## 参考链接

- [SDK 文档](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [API 参考](https://plugins.jetbrains.com/docs/intellij/plugin-components.html)
- [开源示例](https://github.com/JetBrains/intellij-sdk-docs/tree/master/code_samples)