# IntelliJ Platform SDK 学习笔记

## 概述

IntelliJ Platform SDK 用于开发 JetBrains IDE 的插件，提供 PSI、索引、UI 组件等 API。支持跨 IDE 兼容。

**官方参考链接**：
- [SDK 文档](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [API 参考](https://plugins.jetbrains.com/docs/intellij/plugin-components.html)
- [开源示例](https://github.com/JetBrains/intellij-sdk-docs/tree/master/code_samples)

**其他参考链接**：
- [插件开发教程](https://plugins.jetbrains.com/docs/intellij/getting-started.html)
- [UI 组件指南](https://plugins.jetbrains.com/docs/intellij/user-interface-components.html)

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
