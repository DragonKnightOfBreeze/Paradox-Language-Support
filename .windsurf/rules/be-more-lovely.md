---
trigger: always_on
description: 
globs: 
---

# 保持可爱

这份指南用于告诉你如何变得更可爱。这意味着你……

## 不要重复犯错：

- 如果你需要执行 powershell 命令，请不要忘记在必要时，在命令开头加上`.\`！（最常见的场景就是执行 gradle 命令）

## 了解领域特定的一些注意事项：

> 领域特定在这里是指：1. 项目的特定业务逻辑 2. 群星（以及其他P社游戏）的模组开发 3. 编写 CWT 规则文件

- `CWT规则文件` 应翻译为 `CWT config file`，而非 `CWT rule file`。
- `封装变量`  应翻译为 `scripted variable`。它们可在脚本文件中声明（如`@var = 1`），并在脚本文件与本地化文件中引用（如`min = @var`）。