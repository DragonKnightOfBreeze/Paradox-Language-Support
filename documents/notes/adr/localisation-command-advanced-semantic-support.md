# 本地化命令的高级复杂表达式与单引号字面量语义支持

> - 模块：后端（语义支持） > 复杂表达式
> - 版本：v3.0.7~
> - 日期：2026-11-12
> - 状态：计划中
> - 标签：`ComplexExpression` / `ParadoxLocalisationCommand` / Jomini 数据函数 / 单引号字面量 / Milestone 14 / Issue #137

## 背景

在 Victoria 3、CK3 等 Jomini 引擎游戏中，本地化命令（`[...]`）已从早期的简单作用域调用（如 `[Root.GetName]`）演进为高度动态的数据函数系统（Data Functions / Localization Functions），例如：

- `[Concept('concept_gate_magic_ethic', '$concept_gate_magic_ethics|q$')]`
- `[AddTextIf(Character.IsHistorical, Concatenate(Localize('DATA_CHARACTER_TOOLTIP'), '\n'))]`
- `[SelectLocalization(LessThan_CFixedPoint('(CFixedPoint)$VALUE$', '(CFixedPoint)1'), '#negative_value $VALUE|%0$#!', '#variable $VALUE|%0$#!')]`

这些语法具有以下显著特征：
1. **函数调用嵌套**：支持圆括号多参数传递、逗号分隔与函数多层级递归嵌套；
2. **单引号字符串字面量**：实参普遍使用单引号 `'...'` 包裹，并在字面量内嵌入格式化标签、类型转换、换行转义以及带参数修饰符的本地化宏；
3. **潜在极端边界**：单引号内理论上可能出现包含特殊控制字符的文本（例如 `'[WIP] Concept Title'` 或 `'A | B'`）。

在当前插件架构中，底层语法树仅将命令文本作为 `ParadoxLocalisationCommandText`（实现 `ParadoxLocalisationExpressionElement`）扁平容器收纳，在复杂表达式（Complex Expression）层尚未建立起针对数据函数调用的细粒度表达式节点抽象。

## 决策

根据“文法层负责坚固容器，内部微观语义交由上层复杂表达式”的架构分工，决策**不在 JFlex/BNF 底层文法中过度特化单引号状态机，而是将 Jomini 数据函数及其单引号字符串实参抽象为进阶的复杂表达式节点（Complex Expression Node）**。

### 1. 架构分层模型

```text
PSI / AST 语法层 (BNF & Lexer) ── 保持稳定扁平
  └─ ParadoxLocalisationCommand
       └─ ParadoxLocalisationCommandText (提供底层边界与错误隔离)
            │
            ▼ 延迟解析 / 语义抽象
复杂表达式层 (icu.windea.pls.lang.resolve.complexExpression) ── 细粒度结构建模
  └─ ParadoxLocalisationFunctionCallExpression (如 Concept(...))
       ├─ ParadoxLocalisationFunctionNameExpression (函数名)
       └─ ParadoxLocalisationArgumentListExpression (参数列表)
            ├─ ParadoxLocalisationConceptReferenceExpression ('concept_name')
            └─ ParadoxLocalisationStringLiteralExpression ('$key|q$')
                 └─ ParadoxLocalisationParameterExpression (穿透参数宏)
```

### 2. 单引号字面量节点的语义隔离与容错优势

1. **彻底解耦文法风险**：
   单引号内部即使出现未配对的方括号 `[` / `]` 或管道符 `|`，也只作为 `ParadoxLocalisationStringLiteralExpression` 的内容流，由纯 Kotlin 编写的高层解析器消费，绝不干扰底层 PSI 树的闭合；
2. **弹性容错（Fail-Safe）**：
   即便用户输入未闭合的单引号或参数个数不匹配，仅在语义层由 Inspection / Annotator 报告局部可修复警告，绝不引发整行代码高亮红暴；
3. **IDE 能力跃迁**：
   - 字符串参数可在编辑器中获得专属的字符串字面量配色高亮；
   - 第一个参数可强类型解析至 `game_concept` 定义，支持代码补全、快速跳转、重命名与 Find Usages；
   - 支持针对 Jomini 函数签名、参数数量和参数类型的静态检查。

## 任务

- [ ] 在 `icu.windea.pls.lang.resolve.complexExpression` 下建立 Jomini 数据函数的复杂表达式模型（`ParadoxLocalisationFunctionExpression` 等）
- [ ] 实现针对单引号字符串字面量实参的表达式节点（负责词法隔离、转义处理与富文本嵌套）
- [ ] 实现第一个参数对 `game_concept` 定义的引用解析与代码补全支持
- [ ] 提供针对常见 Jomini 本地化数据函数签名的代码检查与参数提示
- [ ] 补充复杂表达式单测与边界容错用例（单引号内嵌套 `[]`、`|` 等场景）

## 关键代码位置

- `icu.windea.pls.lang.resolve.complexExpression.*`
- `icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupports`
- `icu.windea.pls.lang.inspections.localisation.complexExpression.*`

## 参考与关联

- 词法防御决策：[documents/notes/adr/localisation-command-lexical-defense.md](localisation-command-lexical-defense.md)
- 工作区边界评估：`documents/notes/evaluations/vic3-localisation-single-quote-boundary-evaluation.md`
- 关联 Issue：[#137](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/137)、[#451](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/451)
- 关联里程碑：Milestone 14 (`complex-expression-refactors`)
