# 字符串变体、标签与上下文标签

> - 模块：后端（语法支持） > 完善本地化文件的语法支持
> - 版本：v3.0.2+
> - 日期：2026-09-05+
> - 状态：部分实施（待完善）

计划（总览）：
- 一些本地化文件的本地化文本中存在 `&!`。这在 `99_README_GRAMMAR.txt` 中有被部分说明（*4. Tagging Localization Strings* 章节）。需要研究。
  - 例如，对于 `Empress&!fem,vowel`， `&!` 可视为 `TAG_PART_PREFIX`，`,` 可视为 `COMMA`，`fem` 和 `vowel` 可视为 `TAG_TOKEN`。
- 一些本地化文件的本地化文本中存在 `|||`。这在 `99_README_GRAMMAR.txt` 中有被部分说明（*5. String Variants* 章节）。需要研究。
  - 例如，对于 `A|||B|||t1:C|||t2,t3:D`，`|||` 可视为 `STRING_VARIANT_PREFIX`，`,` 可视为 `COMMA`，`:` 可视为 `COLON`，`t1` `t2` 可视为 `TAG_TOKEN`。
  - 字符串变体可以以0个、1个或多个标签开始，自身的字符串也可以（在末尾）使用标签。
  - 因此，对于普通的本地化文本的PSI的命名，可能需要从 `ParadoxLocalisationText` 回退为 `ParadoxLocalisationString`？
- 一些本地化文件的本地化命令中存在 `::suffix`，这在 `99_README_GRAMMAR.txt` 中有被部分说明（*7. Tag-sensitive text* 章节）。需要研究。
  - 例如，对于，`[From.GetAdj::<a_an> <1> plot]`，`::` 可视为 `TAG_SENSITIVE_TEXT_PREFIX`。
  - `::` 不应能嵌套使用本地化命令（或其变体）。
- 一些本地化文件的本地化命令中存在 `&suffix`，这在 `99_README_GRAMMAR.txt` 中有被部分说明（*8. Context tags* 章节）。需要研究。
  - 例如，对于 `[From.GetName&dative]`，`&` 可视为 `CONTEXT_TAG_PART_PREFIX`，`dative` 可视为 `CONTEXT_TAG_TOKEN`。
  - 例如，对于 `Empire of $1&genitive$`，`&` 可视为 `CONTEXT_TAG_PART_PREFIX`，`dative` 可视为 `CONTEXT_TAG_TOKEN`。（啊？）
  - 对于上述例子，参数名在语义上可能仅允许使用 `1`，但在词法层面应兼容任何参数名。
  - 目前认为仅能指定单上下文标签（contextTag）。

备注：
- 目前认为这些特殊语法在所有游戏类型（至少所有主要游戏类型）中都可以使用，因此不在 lexer 层面检查游戏类型（同样的，也不在更高层面检查）。

参考：
- `references/documents/game/99_README_GRAMMAR.txt` - 来自 Stellaris 游戏目录中的 `localisation/99_README_GRAMMAR.txt`

---

计划（初步兼容，初步的语言基础设施，2026-09-05）：
- [x] 更新 `ParadoxLocalisation.Text.flex`。
- [x] 更新 `ParadoxLocalisation.bnf`。
- [x] 更新 `ParadoxLocalisationHighlighterColors` `ParadoxLocalisationHighlighterColorSets` `ParadoxLocalisationColorSettingsPage` `配色方案.md`。
- [x] 更新 `ParadoxLocalisationSyntaxHighlighter`。
- [x] 更新 `ParadoxLocalisationBraceMatcher`（对于 `<PARAM>`）。
- [x] 更新 `ParadoxCommandExpression` 以及相关代码，解析时不再需要兼容 `command_suffix`（`::` 或 `&` 之后的部分不再被识别为 `COMMAND_TEXT` 的一部分）。
- [x] 更新 `ParadoxLocalisationManager.isRichText`。
- [x] 补充并通过 `ParadoxLocalisationParsingTest`。
- [ ] （v3.0.4+）进一步补充并通过 `ParadoxLocalisationParsingTest`（语言构造、边界情况等）。

附加（2026-09-07）：
- [x] 字符串变体（`STRING_VARIANT`）中应仅将开始的第一个 `xxx:` 识别为标签部分（`STRING_VARIANT_TAG_PART`），之后的 `xxx:` 应正常识别为普通文本。

设计（初步兼容，初步的语言基础设施， 2026-09-05）：
- `<example>` -> `<token name>` -> `<highlighter color>`
- `&!fem,vowe` -> `TAG_PART`
  - `&!` -> `TAG_PART_PREFIX` -> `MARKER`
  - `,` -> `COMMA` -> `MARKER`
  - `few` `vowe` -> `TAG_TOKEN`（宽松的标识符，允许以数字开始） -> `TAG`
- `A|||B|||t1:C|||t2,t3:D`
  - `A` -> `TEXT`
  - `|||B|||t1:C|||t2,t3:D` -> `STRING_VARIANT_SET`
    - `|||B` `|||t1:C` `|||t2,t3:D` -> `STRING_VARIANT`
    - `|||` -> `STRING_VARIANT_PREFIX` -> `MARKER`
    - `t1:` `t2,t3:` -> `STRING_VARIANT_TAG_PART`
    - `,` -> `COMMA` -> `MARKER`
    - `:` -> `COLON` -> `OPERATOR`
    - `t1` `t2` `t3` -> `TAG_TOKEN`
- `[From.GetAdj::<a_an> <1> plot]`
  - `::<a_an> <1> plot` -> `TAG_SENSITIVE_TEXT`（暂时没有其他更好的命名方案，就这样吧）
    - `::` -> `TAG_SENSITIVE_TEXT_PREFIX` -> `MARKER`
    - `<a_an>` `<1>` -> `TAGGED_PARAMETER`
    - `<` `>` -> `TAGGED_PARAMETER_START` / `TAGGED_PARAMETER_END` -> `MARKER`
- `[From.GetName&dative]`
  - `&dative` -> `CONTEXT_TAG_PART`（可以与 `|ARG` 混用，且认为需要放到之前）
    - `&` -> `CONTEXT_TAG_PART_PREFIX` -> `MARKER`
    - `dative` -> `CONTEXT_TAG_TOKEN`（宽松的标识符，允许以数字开始，区别于 `TAG_TOKEN`） -> `TAG`
- `Empire of $1&genitive$`
  - `&genitive` -> `CONTEXT_TAG_PART`（可以与 `|ARG` 混用，且认为需要放到之前）
    - `&` -> `CONTEXT_TAG_PART_PREFIX` -> `MARKER`
    - `genitive` -> `CONTEXT_TAG_TOKEN`（宽松的标识符，允许以数字开始，区别于 `TAG_TOKEN`） -> `TAG`

---

计划（附加，v3.0.4+）：
- [ ] 标签（tag）的数量不应超过8个，因此需要考虑提供对应的代码检查。考虑使用 `IncorrectSyntaxInspection`。
- [ ] 标签（tag）应当被视为动态引用（仅区分读写访问，不存在明确的声明处）。
- [ ] 上下文标签（context tag）应当被视为动态引用（仅区分读写访问，不存在明确的声明处）。
- [ ] 将 `<a_an>` `<1>` 视为本地化参数的一种变体（tagged parameter），其引用解析逻辑基本上是一样的。显著区别是仅能在特定上下文（tag-sensitive text）使用，且 `<` `>` 之间仅能是参数名（`PARAMETER_TOKEN`）
- [ ] 将 `ParadoxLocalisationParameter` 重命名为 `ParadoxLocalisationNormalParameter`
- [ ] 引入 `ParadoxLocalisationParameter`，其子接口包括 `ParadoxLocalisationNormalParameter`（`$PARAM$`） 和 `ParadoxLocalisationTaggedParameter`（`<PARAM>`）
- [ ] 为标签（tag）和上下文标签（context tag）提供快速文档、查找用法等语言功能支持
- [ ] 同步修改 `ParadoxLocalisationManager.isSpecialLocalisation` 的逻辑：兼容带字符串变体（stringVariant）的本地化文本（可以考虑扩展为 EP）
- [ ] 与此同时，渲染本地化文本时，仍然兼容带字符串变体（stringVariant）的本地化文本，但仅渲染非变体部分
