# 外部检查工具集成：ck3-tiger (Tool Integration: Tiger)

> 注：本文档记录历史性的设计与实现思路，以后可以考虑进一步补充与完善。

## 概述

整合 ck3-tiger（以及 vic3-tiger、imperator-tiger），提供相应的配置页面，基于简化和优化后的检查结果，提供相应的代码检查和快速修复。

- 新增配置页面，每种工具对应不同的可折叠分组，提供必要的配置项（如工具路径）。
- 新增代码检查，放到新增的检查工具分组中，并作为同一个代码检查。
- 标记文件中的错误位置，并提供必要的快速修复。
- 执行工具，传入必要的参数，得到 JSON 输出结果，解析并用于代码检查。
- 注意检查工具仅能作用于整个游戏或模组目录。
- 代码实现可以参考 JSLint 和 TSLint 插件的实现。
- 需要妥善缓存检查结果，从而优化性能。

## 关键 EP 类

- `com.intellij.codeInspection.LocalInspectionTool`
- `com.intellij.lang.annotation.ExternalAnnotator`
- `com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection`

## 参考代码

- `com.intellij.codeInspection.javaDoc.JavadocHtmlLintAnnotator`
- `com.intellij.codeInspection.javaDoc.JavadocHtmlLintInspection`

## 相关链接

- [Issue #128: [VIC3/CK3] Add ck3-tiger validator support to Plugin](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/128)
- [amtep/tiger (GitHub)](https://github.com/amtep/ck3-tiger)
- [palantir/tslint (GitHub)](https://github.com/palantir/tslint)
- [JetBrains/intellij-plugins/tslint](https://github.com/JetBrains/intellij-plugins/tree/master/tslint)
