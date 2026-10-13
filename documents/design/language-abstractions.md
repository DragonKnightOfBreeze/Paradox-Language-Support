# 语言抽象设计规范 (Language Abstractions)

> 本文档规范各语言（Paradox Script, Localisation 等）在文法级别与语义级别的抽象层级与 PSI 模型对应关系。

## 脚本文件 (Paradox Script)

### 文法级别 (Grammar Level)

- **封装变量** (`ParadoxScriptScriptedVariable`)
- **成员** (`ParadoxScriptMember`)
  - **属性** (`ParadoxScriptProperty`)
  - **值** (`ParadoxScriptValue`)
    - 布尔值 (`ParadoxScriptBoolean`)
    - 数字 (`ParadoxScriptNumber`)
    - 字符串 (`ParadoxScriptString`)
    - 颜色 (`ParadoxScriptColor`)
    - 封装变量引用 (`ParadoxScriptScriptedVariable`)
    - 内联数学 (`ParadoxScriptInlineMath`)
    - 块 (`ParadoxScriptBlock`)
- **参数相关**
  - （普通）参数 (`ParadoxScriptNormalParameter`)
  - 内联数学参数 (`ParadoxScriptInlineMathParameter`)
- **条件化块相关**
  - （普通）条件化块 (`ParadoxScriptNormalConditionalBlock`)
  - 内联条件化块 (`ParadoxScriptInlineConditionalBlock`)
  - 条件化表达式 (`ParadoxScriptConditionalExpression`)
  - 条件化参数 (`ParadoxScriptConditionalParameter`)
- **内联数学相关**
  - 内联数学表达式 (`ParadoxScriptInlineMathExpression`)
  - 内联数学操作数 (`ParadoxScriptInlineMathFactor`)
    - 内联数学数字 (`ParadoxScriptInlineMathNumber`)
    - 内联数学封装变量引用 (`ParadoxScriptInlineMathScriptedVariableReference`)
    - 内联数学参数 (`ParadoxScriptInlineMathParameter`)

### 语义级别 (Semantic Level)

- **封装变量** (`scriptedVariable`)
- **定义** (`definition`)
  - 定义的顶级键 (`rootKey`)
  - 定义的类型键 (`typeKey`)
  - 定义的成员 (`definitionMember`)
- **宏** (`macro`)
  - 实现：内联脚本 (`inlineScript`) - Stellaris
  - 实现：定义注入 (`definitionInjection`) - VIC3 / EU5
- **复杂表达式** (`complexExpression`)
  - 实现：作用域字段表达式 (`scopeFieldExpression`)
  - 实现：值字段表达式 (`valueFieldExpression`)
  - ...

## 本地化文件 (Paradox Localisation)

> （待补充与完善）
