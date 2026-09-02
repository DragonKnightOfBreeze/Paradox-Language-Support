# 分析：与 `ParadoxLocalisationFindUsagesProvider` 有关的 SOF（P19）

> 版本：v3.0.2
> 日期：2026-09-02
> 相关：`documents/notes/问题追踪.md` 中的 P19
> 素材：`documents/notes/assets/stack-trace-p19.txt`
> 平台：IDEA 2025.2（`platformVersion=2025.2`，源码为 `ideaIU-252.23892.409-sources.jar`）

基于 P19 的堆栈跟踪与 IDEA 2025.2 平台源码，分析 `ParadoxLocalisationFindUsagesProvider` 相关 StackOverflowError（SOF）的根本原因。本次仅作分析报告，未更改任何代码。

## 一、问题背景

- 现象：一次调试途中意外出现与 `ParadoxLocalisationFindUsagesProvider` 有关的 SOF。
- 疑问：这个问题看起来不应该存在——`canFindUsagesFor` 覆盖的元素（`ParadoxLocalisationProperty` 与本地化语言的 `LightElementBase`）似乎都已被 `ElementDescriptionProvider` 覆盖。
- 状态：尚未发现稳定复现的方式。

## 二、分析过程

### 2.1 堆栈跟踪解读

堆栈跟踪（截断后）呈现出清晰的循环结构：

```
ParadoxLocalisationFindUsagesProvider.getType (kt:28)
  → ElementDescriptionUtil.getElementDescription (ElementDescriptionUtil.java:21)
    → UsageViewTypeLocation$1.getElementDescription (UsageViewTypeLocation.java:44)
      → LanguageFindUsages.getType (LanguageFindUsages.java:68)
        → LanguageFindUsages.getFromProviders (LanguageFindUsages.java:94)
          → ParadoxLocalisationFindUsagesProvider.getType (kt:28) → 无限循环
```

堆栈顶部的 Maven 帧（`MavenDomUtil.isMavenFile` → `getContainingFile` → `checkCanceled`）只是循环中某次迭代恰好执行到 Maven 插件注册的 EP provider（对非 Maven 文件返回 null，但先做了一次无用的 ReadAction 检查），不影响结论。

### 2.2 平台源码事实（IDEA 2025.2）

1. `ElementDescriptionUtil.java:12-27`：先遍历 `ElementDescriptionProvider.EP_NAME.getExtensionList()`，全部返回 null 后调用 `location.getDefaultProvider()`。
2. `UsageViewTypeLocation.java:25-50`：默认 provider 对非 `PsiMetaOwner`/`PsiFile`/`PsiDirectory` 元素调用 `LanguageFindUsages.getType(psiElement)`（:44）。虽然 :48 有 `TypePresentationService.getTypePresentableName` 兜底，但**永远走不到**——我们的 `getType` 永不返回空串。
3. `LanguageFindUsages.java:87-100`：`getFromProviders` **不再检查 `canFindUsagesFor`**，对该语言注册的每个 provider 无条件调用 getter。即任何本地化语言元素都会触发我们的 `getType`，而不仅限于 `canFindUsagesFor` 返回 true 的两类元素。
4. `UsageViewNodeTextLocation.java:39`：默认 provider 调用 `LanguageFindUsages.getNodeText(element, true)` —— `getNodeText` 存在同款递归。
5. `UsageViewLongNameLocation` 的默认 provider 最终落到 `UsageViewShortNameLocation` → `PsiNamedElement.getName()` → 会终止，所以 `getDescriptiveName` 是安全的。

### 2.3 为什么"看起来不应该存在"

- `ParadoxLocalisationFindUsagesProvider.canFindUsagesFor` 只对 `ParadoxLocalisationProperty` 和本地化语言的 `LightElementBase` 返回 true。
- 这两类恰好都被 EP provider 覆盖：`ParadoxLocalisationElementDescriptionProvider` 无条件覆盖 property（`getElementType` 中 `is ParadoxLocalisationProperty -> ChronicleBundle.message("cwt.type.property")`）；`ParadoxElementDescriptionProvider` 覆盖 `ParadoxLocalisationParameterLightElement` 等各类 light element。
- 按旧平台（`getFromProviders` 带 `canFindUsagesFor` 门槛）的逻辑，递归确实永远不会发生——设计上看似闭环。
- 但 2025.2 移除了门槛。`ParadoxLocalisationPropertyKey`（生成接口仅 `extends NavigatablePsiElement`，**不是** `PsiNamedElement`）、`locale`、`parameter`、`command_text`、`concept_name`、`text`、`property_value` 等本地化元素全部未覆盖 → 一旦被查询类型就必然递归。

### 2.4 平台实际触发点（2025.2 中调用 `getType` 的位置）

| 位置 | 场景 |
|---|---|
| `FindUsagesManager.java:512` | 每次查找用法构建 `UsageViewPresentation`（`setTargetsNodeText(UsageViewUtil.getType(...))`，后台 ReadAction） |
| `CommonFindUsagesDialog.java:65` | 查找用法对话框打开时（EDT，触发即弹错） |
| `PsiElement2UsageTargetAdapter.java:208` | Recent Find Usages 历史记录生成（`getLongDescriptiveName`） |
| `ShowUsagesAction.java:350` | Ctrl+Click 弹出框头部 |
| `FindUsagesManager.java:564/570` | "未找到用法" 提示（F3 查找下一处） |
| `UsageInfo2UsageAdapter.java:580` | psiFile 为空或二进制文件的 usage 渲染（`clsType`/`clsName`） |

触发条件统一为：**primary element 是未覆盖的本地化元素**（未经 `TargetElementUtil` 提升到 property 的流程，例如以 key token、`$param$`、command text 直接作为搜索目标）。

### 2.5 为什么难以稳定复现

- 常规光标流中，`TargetElementUtil`（`ELEMENT_NAME_ACCEPTED`）沿父链提升到第一个 `PsiNamedElement`——即 `ParadoxLocalisationProperty`（key token 不是 named element）→ 被覆盖 → 安全。日常操作因此几乎从不触发。
- 部分触发路径（`createPresentation`、`getLongDescriptiveName`）运行在 `ReadAction.nonBlocking` 后台线程，SOF 仅被 error handler 记入 idea.log，UI 可能只是"查找用法无反应"，用户无感知；调试时恰好注意到。
- 可能还有版本因素：P17/P18 均验证过在 IDEA 2026.2.1 上不复现（2025.2 特有行为），P19 或属同类——2026.x 平台内部实现可能又有变化。建议在 2026.2.1 上验证一次。

## 三、结论

1. **根本原因**：`ParadoxLocalisationFindUsagesProvider.getType`/`getNodeText` 无条件委托 `ElementDescriptionUtil.getElementDescription`，而平台 2025.2 的 `UsageViewTypeLocation`/`UsageViewNodeTextLocation` 默认 provider 又会回调 `LanguageFindUsages.getType`/`getNodeText` → 回到本 provider，形成无出口的相互递归。
2. **关键平台变化**：`LanguageFindUsages.getFromProviders` 移除了 `canFindUsagesFor` 门槛，导致"未被覆盖的本地化元素"（如 key token、parameter、command text 等）也会进入递归，而设计者按旧门槛的逻辑认为闭环。
3. **触发条件**：任何本地化语言的、未被 EP `elementDescriptionProvider` 覆盖的元素成为查找用法的 primary element（或 usage 位于空/二进制文件）时，平台对 `getType`/`getNodeText` 的查询即触发 SOF。
4. **同款隐患**：`ParadoxScriptFindUsagesProvider`、`CwtFindUsagesProvider`、`ParadoxCsvFindUsagesProvider` 是完全相同的委托模式，语言中未覆盖的元素一旦成为 primary element 同样会递归。修复时应一并处理。

## 四、相关上下文

- [P19（问题追踪）](../../notes/问题追踪.md)：本问题的记录入口。
- [P17、P18（问题追踪）](../../notes/问题追踪.md)：同为"IDEA 2025.2 特有、2026.2.1 不复现"的版本兼容性问题的参照。
- 相关代码位置：
  - `icu.windea.pls.localisation.findUsages.ParadoxLocalisationFindUsagesProvider`（`getType`/`getDescriptiveName`/`getNodeText` 委托 `ElementDescriptionUtil`，ParadoxLocalisationFindUsagesProvider.kt:28/32/36）
  - `icu.windea.pls.localisation.findUsages.ParadoxLocalisationElementDescriptionProvider`（仅覆盖 property）
  - `icu.windea.pls.lang.findUsages.ParadoxElementDescriptionProvider`（覆盖 property 与各 light element，`getElementType`）
  - `icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey`（生成接口仅 `extends NavigatablePsiElement`，不是 `PsiNamedElement`）
  - 同款模式：`ParadoxScriptFindUsagesProvider`、`CwtFindUsagesProvider`、`ParadoxCsvFindUsagesProvider`
- 平台源码（2025.2）：
  - `com.intellij.psi.ElementDescriptionUtil`（:12-27）
  - `com.intellij.usageView.UsageViewTypeLocation`（:25-50，:44 为递归点）
  - `com.intellij.usageView.UsageViewNodeTextLocation`（:39 为递归点）
  - `com.intellij.lang.findUsages.LanguageFindUsages`（:87-100，无 `canFindUsagesFor` 门槛）

## 五、后续建议（仅记录，未做任何更改）

- 最稳妥：`getType`/`getNodeText` 不再无条件委托；对 EP 未覆盖的元素返回自带默认值（如 `TypePresentationService` 结果或固定字符串）。
- 或：在 `ParadoxLocalisationElementDescriptionProvider` 中为 key/locale/parameter/command_text/concept_name/text/value 等元素增加兜底非空描述。
- 或：在 `getType`/`getNodeText` 入口加线程局部递归保护（最小改动，但治标）。
- 其他三个语言的 provider（脚本/CWT/CSV）同步处理。
- 复现手段：以 key token 或 `$param$` 作为 primary element 直接触发查找用法（如在调试器中调用 `FindManager.findUsages(keyElement, ...)`，或从 Recent Find Usages 弹窗中打开一个以非 property 元素为目标的条目），观察 `CommonFindUsagesDialog` / `FindUsagesManager.createPresentation` 的 `getType` 调用即可确定性复现。
