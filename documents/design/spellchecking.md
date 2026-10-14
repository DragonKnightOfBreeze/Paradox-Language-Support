# 拼写检查机制设计

> 注：本文档记录历史性的设计与实现思路，以后可以考虑进一步补充与完善。

## 概述

关键 EP：`com.intellij.spellchecker.tokenizer.SpellcheckingStrategy`

## 策略与原则

- 仅作文法级别的拼写检查，不作语义级别的拼写检查。
- 跳过空白和存在语言注入的位置。
- 对于单行注释和选项注释，委托到 `super.getTokenizer(element)`。
- 对于包含空白的键/值/表达式（排除参数），以及本地化文本，使用 `PlainTextSplitter`。
- 对于不包含空白的键/值/表达式（排除参数），以及其他标识符，考虑使用 `IdentifierSplitter`（但不强制）。
- 对于其他情况，使用 `EMPTY_TOKENIZER`。
