# Issue #451：[Vic3] New quote escape construct breaks the localization parser

> - 模块：语言支持 / 本地化词法与语法分析（`localisation`）
> - 版本：v3.0.6
> - 日期：2026-11-12
> - 状态：已解决
> - 标签：`ParadoxLocalisation` / `Lexer` / `Parser` / `JFlex` / Victoria 3 / Quote Escape / Issue #451

## 背景

- **GitHub Issue**：[#451 - [Vic3] New quote escape construct breaks the localization parser](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/451)
- **提交者**：`kaiser-chris`（长期贡献者，Victoria 3 Mod 作者与脚本调试器开发者）
- **触发用例**：
  在 Victoria 3 本地化文件（`.yml`）中使用了包含引号转义修饰符 `|q` 的本地化函数调用：
  ```yaml
  je_gate_project_change_domain_description: "[Concept('concept_gate_magic_ethic','$concept_gate_magic_ethics|q$')] represent our view of magic in our society and provide massive boosts to ou"
  ```
- **错误表现**：
  IDE 编辑器在 `$concept_gate_magic_ethics|q$` 的闭合 `$` 以及后续字符上抛出高亮红色波浪线与解析错误：
  1. `ARGUMENT_TOKEN, PARAMETER_START or RIGHT_BRACKET expected, got '$'`
  2. `<rich text>, COLORFUL_TEXT_END, STRING_VARIANT_PREFIX, TAG_PART_PREFIX or TEXT_FORMAT_END expected, got ')'`
  导致整个属性值的富文本结构解析崩溃。

## 分析

### 1. 语法机制与业务背景

- **Jomini 本地化函数中的 `|q`**：
  在 Victoria 3（以及 CK3 等 Jomini 引擎游戏）中，本地化支持在方括号内调用数据函数（如 `Concept(...)`、`Concatenate(...)`、`AddTextIf(...)` 等）。
  当函数的单引号字符串参数中嵌入了本地化宏引用 `$key$` 时，若引用的本地化文本展开后含有引号，可能破坏外部的字符串字面量解析边界。
  因此 Paradox 官方引入了格式化修饰符 `|q`（Quote Escape），指示本地化渲染管线在宏展开并注入函数参数时对引号进行逃逸转义。
- **语法的本质归属**：
  `$concept_gate_magic_ethics|q$` 本质上仍然是一个标准的**带有参数修饰符（parameter suffix / argument）的本地化参数引用（`ParadoxLocalisationParameter`）**，其中：
  - 引用名称为 `concept_gate_magic_ethics`；
  - 管道符 `|` 引导参数修饰符；
  - 参数值为 `q`（`ARGUMENT_TOKEN`）。

### 2. 词法与语法分析层面的根本原因

通过对 `ParadoxLocalisation.Text.flex` 和 `ParadoxLocalisation.bnf` 进行深入白盒审查，发现了导致解析崩溃的链式根因：

#### 根因 A：`CommandTextChar` 未排除 `$`

在 `ParadoxLocalisation.Text.flex` 中，命令文本字符集被定义为：
```flex
CommandTextChar = [^\[\]|&:\r\n]
CommandTextBoundChar = [^\[\]|&:\s]
CommandTextToken = {CommandTextBoundChar}({CommandTextChar}*{CommandTextBoundChar})?
```
排除了 `[`、`]`、`|`、`&`、`:` 以及换行，**但唯独漏掉了 `$`**。

#### 根因 B：JFlex 最长匹配（Longest Match）规则导致状态切换失效

在 `IN_COMMAND_TEXT` 状态下，词法分析器原本声明了参数匹配规则：
```flex
<IN_COMMAND_TEXT, IN_COMMAND_ARGUMENT, IN_CONCEPT_NAME> {
    "$" / \S*"$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yybegin(IN_PARAMETER);
        return PARAMETER_START;
    }
    "$" { return getFallbackToken(); }
}
```
然而，当扫描到 `'$concept_gate_magic_ethics|q$'` 时：
- 单字符规则 `"$" / \S*"$"` 的匹配长度仅为 **1**；
- 规则 `{CommandTextToken}` 由于包含 `$`，能够一口气匹配从开头的 `$` 直到 `|` 之前的全部字符（即 `'$concept_gate_magic_ethics`），长度达到 **27**；
- 根据 JFlex 的**最长匹配优先**原则，`{CommandTextToken}` 胜出，开头的 `$` 与后续名称被一并吞成了一个单一的 `COMMAND_TEXT_TOKEN`，词法分析器**未能切入 `IN_PARAMETER` 状态**。

#### 根因 C：多米诺骨牌式的语法错误链

1. **管道符误触发**：`CommandTextToken` 在遇到未包含在字符集内的 `|` 时终止。此时词法分析器仍处于 `IN_COMMAND_TEXT`，读到 `|` 触发了原用于命令修饰符（如 `[Root.GetName|v]`）的规则：
   `"|" { yybegin(IN_COMMAND_ARGUMENT); return PIPE; }`
   词法分析器错误地切入 `IN_COMMAND_ARGUMENT` 状态并产出 `PIPE`。
2. **闭合 `$` 产生非法 Token**：在 `IN_COMMAND_ARGUMENT` 状态下消耗 `q`（作为 `ARGUMENT_TOKEN`）后，紧接着遇到闭合的 `$`。此时由于后方只有 `')]`，无法匹配 `\S*"$"`，于是回退到兜底规则 `getFallbackToken()`，产出了 `TEXT_TOKEN`（值为 `$`）。
3. **语法树 Expectation 冲突**：此时 BNF 语法分析器处于 `command` 规则的 `command_suffix` 节点：
   ```bnf
   command_argument ::= command_argument_name
   private command_argument_name ::= ( ARGUMENT_TOKEN | parameter )+
   ```
   它期望的下一个 Token 集合为 `ARGUMENT_TOKEN`、`PARAMETER_START`（即 `parameter`）或 `RIGHT_BRACKET`（即命令结束闭合）。
   收到突兀的 `TEXT_TOKEN` 后，分析器判定文法非法并报出：
   `ARGUMENT_TOKEN, PARAMETER_START or RIGHT_BRACKET expected, got '$'`。
4. **连锁反应**：解析器在恐慌模式下试图跳过该符号，但紧随其后的 `')` 无法被外部富文本结构认领，再次爆出二次错误，导致整个属性值的语法高亮与 AST 全面崩溃。

### 3. 为什么此前未暴露（兼历史潜伏 Bug 剖析）

检查快照测试 `src/test/testData/localisation/syntax/construct_commands.txt` 发现：
在 `command_parameterized_1:0 "[a_$b$_c]"` 的快照中，`a_$b$_c` 实际上整个被生成为了 `COMMAND_TEXT_TOKEN('a_$b$_c')`，而非预期的 `parameter('$b$')`。
由于此前没有在命令内使用带 `|` 参数修饰符的本地化引用（例如普通的 `$loc_key$` 中间不包含 `|`），`CommandTextToken` 会一路吞到单引号或括号结束，碰巧没有触发 `|` 的切断机制，因而未报出语法解析错误，导致“命令内部无法正确解析参数”这一问题长期潜伏。

## 解决方案

本解决方案遵循最小侵入性、高容错性与完整性原则，无需修改底层业务逻辑架构，仅需在词法定义、容错回退与辅助工具类上进行精准增强。

### 1. 修正 `ParadoxLocalisation.Text.flex` 字符集

在 `ParadoxLocalisation.Text.flex` 中，将 `$` 明确加入 `CommandTextChar` 与 `CommandTextBoundChar` 的排除列表：

```flex
CommandTextChar = [^\[\]|&:\$\r\n] // 排除 `$`，防止命令文本贪婪吞噬参数起始标记
CommandTextBoundChar = [^\[\]|&:\$\s]
CommandTextToken = {CommandTextBoundChar}({CommandTextChar}*{CommandTextBoundChar})?
```

- **效果**：当在命令内部扫描到 `$` 时，`CommandTextToken` 将在此处正常截断；
- JFlex 将匹配 `"$" / \S*"$"` 规则，顺利进入 `IN_PARAMETER` 状态；
- `$concept_gate_magic_ethics|q$` 将被完整解析为标准的 `ParadoxLocalisationParameter`（包含 `PARAMETER_TOKEN`、`PIPE`、`ARGUMENT_TOKEN` 与 `PARAMETER_END`）；
- 解析完成后退出回到 `IN_COMMAND_TEXT`，后续的 `')` 继续作为 `COMMAND_TEXT_TOKEN`，整个命令以 `RIGHT_BRACKET` 正常闭合。

### 2. 增强 `getFallbackToken()` 状态感知容错

在 `ParadoxLocalisation.Text.flex` 的内嵌 Java 逻辑中，改进 `getFallbackToken()`，使其能够感知当前词法状态并返回合理的兜底 Token，避免语法树在遇到非法/孤立单 `$` 时产生无法恢复的硬崩：

```java
private IElementType getFallbackToken() {
    int state = yystate();
    if (state == IN_COMMAND_TEXT) return COMMAND_TEXT_TOKEN;
    if (state == IN_COMMAND_ARGUMENT) return ARGUMENT_TOKEN;
    if (state == IN_CONCEPT_NAME) return CONCEPT_NAME_TOKEN;
    return TEXT_TOKEN;
}
```

### 3. 增强本地化参数修饰符文档（Quick Doc）

在 `ParadoxLocalisationArgumentManager.kt` 中识别 `q` 修饰符，并在属性包中补充描述：

```kotlin
// ParadoxLocalisationArgumentManager.kt
c == 'q' || c == 'Q' -> {
    set += ChronicleDocBundle.message("formattingTag.q")
}
```

并在资源包中补充国际化文案：
- `ChronicleDocBundle.properties`：
  ```properties
  formattingTag.q=<code>q</code> - Quote escape: escapes quotes in the localized text when referenced inside localization functions/commands (e.g. <code>Concept</code>).
  ```
- `ChronicleDocBundle_zh.properties`：
  ```properties
  formattingTag.q=<code>q</code> - 引号转义：在本地化函数/命令（例如 <code>Concept</code>）中引用时，对展开文本中的引号进行转义。
  ```

### 4. 引用解析与 IDE 功能天然支持确认

由于解析修正后 `$concept_gate_magic_ethics|q$` 生成了标准的 `ParadoxLocalisationParameter` AST 节点：
- 其 `idElement` 精确对应 `concept_gate_magic_ethics`；
- `ParadoxLocalisationPsiReferenceProvider` 无需任何改动即可天然挂载 `ParadoxLocalisationParameterPsiReference`；
- IDE 中的 Ctrl+Click 跳转、Find Usages、悬浮快速文档、代码重构均自动完全支持。

## 验证

### 1. 测试用例补充

在 `src/test/testData/localisation/syntax/construct_commands.test.yml` 中新增针对本场景的回归测试用例：
```yaml
 command_parameterized_concept:0 "[Concept('concept_key','$loc_key|q$')]"
 command_parameterized_with_argument:0 "[Root.GetName('$param|arg$')]"
```

并在 `src/test/testData/localisation/syntax/construct_concept_commands_vic3.test.yml` 中补充相关用法。

### 2. 测试执行验证步骤

1. 重新生成 Lexer：
   执行 Grammar-Kit 生成命令，更新 `src/main/gen/icu/windea/pls/localisation/lexer/_ParadoxLocalisationTextLexer.java`；
2. 运行本地化解析测试：
   `./gradlew test --tests "icu.windea.pls.localisation.ParadoxLocalisationParsingTest"`
3. 同步更新 `construct_commands.txt` 中因修复深层潜伏 Bug 而产生的正确 AST 差异（`command_parameterized_1/2/3` 现在将正确展开参数节点）；
4. 运行全量测试套件确认无向后兼容破坏。

## 待评估 / 扩展内容

> 以下几项与本次修复相邻，但本轮**刻意不改动代码**，只记录事实与后续演进评估方向。

### 1. Jomini 数据函数（Data Function）语义支持评估

- **现状**：
  在当前 AST 体系中，`[Concept('concept_key', '$loc_key|q$')]` 仍然被视作普通的 `command`，其内部的 `Concept(...)` 被拆分存放在 `command_text` 中。
- **潜在演进**：
  在 Issue #137 中，Chris 提到了支持更高级的本地化指令分析，包括：
  - `Concept('concept_name', '$display_text$')`
  - `Concatenate(...)`、`Localize(...)`、`AddTextIf(...)`
  - `SelectLocalization(...)`
  未来可评估是否在 CWT 规则体系或 Complex Expression 中为这类函数调用提供：
  1. 函数名与参数签名的代码补全；
  2. 第一个参数对 Concept 定义的直接强引用解析（目前仅支持 Stellaris 风格的 `['concept_name']` 语法）；
  3. 参数个数与参数类型的检查。

### 2. 单引号内特殊字符（如嵌套方括号）的边界评估

- 目前 Jomini 允许在函数中传入复杂的嵌套字符串。如果未来在参数的单引号字符串内部出现未转义的 `[`、`]` 或 `|`，词法分析器是否能通过有状态的引号计数保持在字符串上下文内，值得在未来做进一步的语法边界考察。

## 参考链接

- 关联 Issue：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/451
- 前置 Issue：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/137
