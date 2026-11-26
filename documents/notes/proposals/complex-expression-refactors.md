# 复杂表达式 API 集中重构

> - 模块：后端 > 复杂表达式
> - 版本：v3.1.x+
> - 状态：待规划（拆分自原 `roadmap.md`）
> - 标签：`里程碑` / `架构优化` / `ComplexExpression` / `Token` / `Resolver`

## 概述

围绕复杂表达式（Complex Expression）子系统的 API 重构与精确性提升，覆盖更准确的词法切分与解析逻辑、更详细的报错诊断、边界情况处理与测试补充。

关键词：`Token`、`TokenStream`、`Scanner`、`Resolver`、`Visitor`、`Validator`、`Evaluator`。

（注：本主题下的本地化命令词法防御已于 `v3.0.6` 落地，参见 [adr/localisation-command-lexical-defense.md](../adr/localisation-command-lexical-defense.md)。）

## 候选任务

- [ ] P2/MS 重构复杂表达式的 API，尤其是解析逻辑与校验逻辑；考虑将相关代码迁至独立子包 `icu.windea.pls.expression`
- [ ] P2/MS 优化对复杂表达式的支持：优化对动态链接节点中传入的字面量的处理逻辑
- [ ] P2/MS `[VIC3/CK3]` 本地化命令的高级复杂表达式与单引号字面量语义支持
  - 详见子提案：[proposals/localisation-command-advanced-semantic-support.md](localisation-command-advanced-semantic-support.md)
