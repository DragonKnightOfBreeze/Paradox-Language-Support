# 脚本片段注入机制设计规范

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范 Paradox Chronicle 中针对脚本参数值、默认值、本地化富文本以及 Markdown 代码围栏的**语言注入（Language Injection）**机制、规则上下文推导、嵌套注入绕过策略与已知实现边界。
> - 互链说明：底层 PSI 宿主与 LightElement 体系详见 [`psi-architecture.md`](psi-architecture.md)；文法层与语义层映射详见 [`language-abstractions.md`](language-abstractions.md)；复杂表达式系统详见 [`complex-expressions.md`](complex-expressions.md)。

---

## 一、系统架构与核心组件

### 1.1 三层架构模型

插件的语言注入基础设施由三个核心类协同运作：

```text
               [IntelliJ 平台 MultiHostRegistrar]
                               │
                               ▼
               ParadoxLanguageInjector (注册分派)
                               │
                               ▼
            ParadoxLanguageInjectionService (计算切片与范围)
                               │
                               ▼
            ParadoxLanguageInjectionManager (识别/回溯/切片匹配)
```

1. **`ParadoxLanguageInjector`**：实现了平台的 `MultiHostInjector` 接口，作为注入逻辑的物理分派入口。根据宿主类型与设置开关，将文本切片注入为 `ParadoxScriptLanguage` 或 `ParadoxLocalisationLanguage`。
2. **`ParadoxLanguageInjectionService`**：负责具体的业务逻辑判定，包括向上解析参数上下文、检查参数名称有效性、过滤非法操作符样式、计算文本在宿主内部的精确注入区间（`TextRange`），并生成对应的注入信息载荷。
3. **`ParadoxLanguageInjectionManager`**：提供注入运行期辅助，负责识别注入生成的虚拟文件窗口（`VirtualFileWindow`）、跨层级回溯顶层物理宿主元素、以及依据文档窗口的切片（Shred）信息检索注入元数据。

### 1.2 物理注入与语义规则上下文的分立

在设计上，必须严格区分**物理语言注入**与**语义规则上下文注入**：

- **物理语言注入**：仅利用平台的 `InjectedLanguageManager`，在字符串字面量或参数内部建立语法高亮与 AST/PSI 结构。物理注入本身**不直接包含游戏规则**。
- **语义规则上下文注入**：物理注入完成后，内部生成的虚拟 PSI 文件通过 `CwtConfigContextProvider` 扩展点按优先级链条推导出匹配的 `CwtConfigContext`，从而赋予其代码补全、定义引用解析与代码检查能力。

---

## 二、物理语言注入场景与宿主

### 2.1 现行受支持的注入场景

当前仅对实现 `ParadoxLanguageInjectionHost` 接口的物理 AST 节点开放自动注入：

| 注入场景 | 物理宿主类型 | 注入目标语言 | 触发条件与边界 |
| :--- | :--- | :--- | :--- |
| **参数传入值注入** | `ParadoxScriptString` | `ParadoxScriptLanguage` | 定义调用或内联脚本调用中作为实参的字符串；名称符合合法参数名规范；**不强制要求带有双引号**；排除空字符串与单操作符样式。 |
| **参数默认值注入** | `ParadoxScriptNormalParameterArgument` | `ParadoxScriptLanguage` | 脚本参数默认值语法 `$PARAM\|DEFAULT$` 中的 `DEFAULT` 部分；宿主父节点必须是 `ParadoxScriptParameter`。 |
| **本地化富文本注入** | `ParadoxScriptString` | `ParadoxLocalisationLanguage` | 两侧带引号的字符串字面量；包含颜色代码（`§...§!`）等富文本标记；匹配的规则表达式属于文本相关类型；以合成前缀 `INJECTED: "` 注入以模拟本地化属性值。 |

### 2.2 手动语言注入的明确废弃

在当前架构中，**不再支持用户通过 IDE 手动触发语言注入（Inject Language/Reference）**：
- 插件在 `ParadoxLanguageInjector` 内部显式调用 `InjectionUtils.enableInjectLanguageAction(host, false)` 关闭动作。
- 宿主节点统一实现 `InjectionBackgroundSuppressor` 以抑制平台默认的浅绿色注入背景高亮，保持编辑器视觉整洁。
- 辅助代码注入器 `InjectLanguageActionCodeInjector` 在底层拦截不兼容的操作入口。

---

## 三、语义规则上下文注入与分派管线

当注入的脚本片段进入编辑器或被索引访问时，`CwtConfigContextProvider` 扩展点按照注册优先级从高到低依次尝试提供规则上下文：

```text
CwtConfigContextProvider 优先级分派链：
1. CwtDefineVariableConfigContextProvider      // 定值变量上下文
2. CwtParameterValueConfigContextProvider      // 参数传入值与默认值上下文（核心注入点）
3. CwtInlineScriptUsageConfigContextProvider   // 内联脚本调用处上下文
4. CwtInlineScriptFileConfigContextProvider    // 内联脚本文件内部上下文
5. CwtDefinitionInjectionConfigContextProvider // 定义注入声明上下文
6. CwtDefinitionConfigContextProvider          // 常规定义内部上下文
7. CwtBaseConfigContextProvider                // 兜底基础上下文（跳过未解析检查）
```

### 3.1 参数值注入的上下文恢复机制

`CwtParameterValueConfigContextProvider` 是处理参数注入的核心提供者：

1. **注入信息反查**：通过 `ParadoxLanguageInjectionManager.getParameterValueInjectionInfoFromInjectedFile` 从注入文件的文档切片中反查出 `ParadoxParameterValueInjectionInfo`。
2. **参数元素解析**：根据实参或默认值信息，解析出对应的 `ParadoxParameterLightElement`。
3. **使用处规则反推**：调用 `ParadoxParameterManager.getInferredContextConfigs`，优先从扩展规则 `parameters/{name}` 读取显式上下文配置；若无配置，则自动扫描该参数在目标定义或内联脚本中的所有使用位置，并将各使用位置所要求的 CWT 规则集合并作为当前参数值片段的根上下文规则。
4. **动态标记**：当上下文规则依赖使用处推导时，调用 `markDynamic()` 标记当前上下文，防止不稳定的推导结果被错误缓存在全局规则分组层级。

---

## 四、Markdown 扩展的分工与协作机制

在 Markdown 文档中，Chronicle 提供了代码围栏注入与内联代码符号引用两种截然不同的机制：

### 4.1 Markdown 代码围栏（Code Fence）的物理注入

```markdown
```paradox-script path=stellaris:common/edicts/my_edicts.txt
my_edict = { ... }
```
```

1. **分工契约**：语言注入本身由官方 Markdown 插件根据声明的语言 ID（如 `paradox-script`）完成物理注入；Chronicle 本身**不**注册面向 Markdown 的 `MultiHostInjector`。
2. **文件信息注入**：插件注册了 `InjectedFileProcessor` 扩展点（`MarkdownCodeFenceInjectedFileProcessor`）。在 Markdown 插件完成底层注入后，处理器从围栏属性中解析 `path={gameType}:{path}`，构造 `ParadoxFileInfo` 并注入到虚拟文件的 `VirtualFileWindow` 之上。
3. **上下文接入**：注入了文件信息的代码围栏使得内部脚本能够像项目内的真实物理文件一样，被 `CwtDefinitionConfigContextProvider` 正确识别定义类型，享受完整的语义分析。

### 4.2 Markdown 内联代码（Inline Code）的符号引用模型

```markdown
这是一个 `@my_var` 变量或 `my_definition` 声明。
```

- **非语言注入**：内联代码片段（`MarkdownCodeSpan`）**绝不创建物理注入文件**。
- **符号引用机制**：通过 `MarkdownInlineCodeReferenceProvider`（接入平台 `ImplicitReferenceProvider`）从文本中提取目标符号，并构建 `MarkdownInlineCodeSymbolReference`。
- **轻量符号支持**：解析产物 `MarkdownInlineCodeSymbol` 实现了导航、快速文档、查找用法以及在 3.1.0 中完善的重命名（Rename）支持。

---

## 五、单层注入限制与绕过策略

### 5.1 平台限制：不支持嵌套语言注入

IntelliJ 平台在底层（包括当前基线 `263`）依然严格禁止在已经处于注入状态的 PSI 文本内部再次触发第二层语言注入。

### 5.2 插件的绕过与恢复策略

为了在单层物理注入的约束下支持深度嵌套代码（如内联脚本相互调用或在宏内部使用参数），插件采用了组合绕过策略：

1. **顶层宿主回溯（Top Host Walking）**：
   - 当在注入文件内解析参数或查找定义时，`ParadoxLanguageInjectionManager.findTopHostElementOrThis` 与 `findTopHostFileOrThis` 沿 `VirtualFileWindow.delegate` 逐层向上回溯，直接跳过所有注入边界，回到最外层的物理脚本文件。
   - 这使得嵌套在参数传入值中的参数引用能够正确检索到最外层定义的参数环境。
2. **语义展开模拟物理展开**：
   - 内联脚本的嵌套调用并不触发多次物理注入，而是通过 `ParadoxInlineScriptService.getInferredContextConfigsFromUsages` 在规则推导阶段自底向上合并规则，并借助 `withRecursionCheck` 阻断无限递归展开。

### 5.3 现行已知边界与局限

1. **Markdown 围栏内部不可再次注入**：
   - 代码围栏本身已经是物理注入文件。根据单层注入限制，围栏内部的参数传入值（`ARG = "script"`）无法再次被 `ParadoxLanguageInjector` 捕获，因此围栏内的参数值无法获得专用注入上下文。
2. **转义双引号切片偏移限制**：
   - 当参数值内部包含转义双引号（`\"`）时，由于平台切片（Shred）对转义字符的合并偏移计算特性，可能导致注入信息在特定边缘切片上无法精确对齐。
3. **平台高亮重叠绕过**：
   - 针对平台在不同注入文件间可能错误复用 Markup 缓存导致高亮偏移的问题，插件通过代码注入器 `IdentifierHighlightingComputerCodeInjector` 进行了局部防御性过滤。
