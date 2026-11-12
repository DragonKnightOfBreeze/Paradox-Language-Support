# 本地化命令的词法防御机制与边界保护

> - 模块：后端（语法支持） > 本地化文件
> - 版本：v3.0.6
> - 日期：2026-11-12
> - 状态：已实施
> - 标签：`ParadoxLocalisation` / `Lexer` / `JFlex` / 词法防御 / 状态机 / Issue #451

## 背景

在 GitHub Issue [#451](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/451) 中，用户报告在 Victoria 3 本地化文件使用新引入的引号转义修饰符 `|q` 时，会导致本地化解析器中断：

```yaml
some_loc_key: "Some text about this [Concept('concept_gate_magic_ethic','$concept_gate_magic_ethics|q$')]"
```

错误表现为在闭合 `$` 及后续字符处报出 `ARGUMENT_TOKEN, PARAMETER_START or RIGHT_BRACKET expected, got '$'`，进而引发连锁富文本解析失败。

排查发现其链式根因如下：
1. **`CommandTextChar` 未排除 `$`**：命令文本字符集 `CommandTextChar = [^\[\]|&:\r\n]` 包含了 `$`；
2. **JFlex 最长匹配贪婪吞噬**：遇到 `'$loc_key|q$'` 时，`{CommandTextToken}` 匹配长度远大于单字符 `"$" / \S*"$"`，导致开头的 `$` 连同 key 名称被吞为单一 `COMMAND_TEXT_TOKEN`，词法分析器未能切入 `IN_PARAMETER` 状态；
3. **连锁语法崩溃**：未包含在字符集内的 `|` 将文本截断，并误触发了命令后缀规则切入 `IN_COMMAND_ARGUMENT`；随后遭遇闭合 `$` 时产生非法的 `TEXT_TOKEN`，致使 BNF 解析器在期望参数或命令闭合处发生文法冲突。
4. **潜伏历史缺陷**：此缺陷此前一直存在，导致命令内部的普通参数引用（如 `[a_$b$_c]`）在 AST 中长期被错误扁平化为单一字符串。

## 决策与权衡

在制定方案时，曾探讨过是否应在 JFlex 内部引入独立的单引号字符串状态机（`IN_COMMAND_STRING`），最终确立了**轻量词法防御 + 状态感知回退**的架构决策：

### 1. 词法层职责边界：保持最简的坚固容器

词法分析器（Lexer）的核心职责是保证底层 PSI 容器与最基础结构边界绝对不崩溃，绝不应在底层状态机中强行耦合上层复杂的业务语义：
- **拒绝在 JFlex 内部硬做单引号状态机**：
  Jomini 引擎中的数据函数参数虽然使用单引号，但其内部包含格式化标签（`#...#!`）、复杂修饰符（`|`）、类型转换（`(CFixedPoint)`）乃至转义换行。若在 JFlex 层面硬啃，不仅状态机极度臃肿，还会与 Stellaris 既有的 `concept_command`（`['...']`）产生歧义打架，且极难优雅处理单引号缺失时的容错；
- **确立“轻量词法防御”**：
  仅需在 `CommandTextChar` 与 `CommandTextBoundChar` 中明确排除 `$`，即可阻断最长匹配贪婪吞噬，让内嵌参数宏恢复正常的词法状态流转。

### 2. 增强 `getFallbackToken()` 状态感知容错

在遇到非成对出现的孤立单 `$`（如 `[Concept('key', '$')]` 或 `[a $ b]`）时，传统全局回退为 `TEXT_TOKEN` 会破坏 `command_text`（仅接受 `COMMAND_TEXT_TOKEN` 与 `parameter`）的文法约束。

决策使 `getFallbackToken()` 感知当前词法状态：
- 处于 `IN_COMMAND_TEXT` 时回退为 `COMMAND_TEXT_TOKEN`；
- 处于 `IN_COMMAND_ARGUMENT` 时回退为 `ARGUMENT_TOKEN`；
- 处于 `IN_CONCEPT_NAME` 时回退为 `CONCEPT_NAME_TOKEN`；
- 其余顶层状态仍回退为 `TEXT_TOKEN`。

从而确保即使出现病态单 `$`，语法树依然能平滑降级，绝不崩溃。

## 任务

- [x] 在 `CommandTextChar` / `CommandTextBoundChar` 中排除 `$`
- [x] 增强 `getFallbackToken()` 的状态感知容错逻辑
- [x] 严格限制 `formattingTag.q` 仅在 `ParadoxGameType.Vic3` 下输出快速文档
- [x] 更新 `ParadoxLocalisationParsingTest` 的 AST 快照基准（纠正历史扁平化缺陷）
- [x] 补充专用回归测试 `Issue451Test`（覆盖引用解析、QuickDoc、PSI 结构及孤立 `$` 容错）

## 关键代码位置

- `src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex`
- `src/main/gen/icu/windea/pls/localisation/lexer/_ParadoxLocalisationTextLexer.java`
- `src/main/kotlin/icu/windea/pls/lang/util/ParadoxLocalisationArgumentManager.kt`
- `src/test/kotlin/icu/windea/pls/test/issues/Issue451Test.kt`

## 参考与关联

- 案卷档案：[documents/notes/issues/issue-451-vic3-quote-escape-parser-break.md](../issues/issue-451-vic3-quote-escape-parser-break.md)
- 进阶语义决策：[documents/notes/adr/localisation-command-advanced-semantic-support.md](localisation-command-advanced-semantic-support.md)
