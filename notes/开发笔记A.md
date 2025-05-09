# 开发笔记A

## 全局

### 杂项

关于作用域`Modules with dependents`，如果需要在查找引用时包含依赖中的文件，需要导出依赖（在依赖菜单中勾选`Export`）

### 异常

* `ProcessCanceledException` - 可能会在任何操作中发生，这时操作将会取消。在代码中应尽可能频繁地调用`ProgressManager.checkCanceled()`以避免卡顿。不要尝试捕获该类异常。
* `IncorrectOperationException` - 不支持的操作。例如，当元素无法被重命名时，这时IDE会自动提示：`Cannot perform refactoring...`。

### 缓存

方案1：（对于`PsiElement`）使用`CachedValuesManager.getCachedValue(element, key, provider)`。

注意事项：

* 需要提供方案验证结果的等效性。
* 避免无限循环调用。

方案2：（对于`CompositePsiElement`）声明带有`volatile`修正的字段，重载`subtreeChanged`方法，在里面清空缓存。

### UI

关于UI DSL

* 当使用`validation` `validationXxx`时，必须配套使用`bindText(observableMutableProperty)`，否则无法正确验证修改后的值
* `com.intellij.ui.dsl.builder.Cell.onApply` - 调用这个方法时，对应的属性必定已经被修改（但是，如果修改了多个属性并且存在重复的对饮的回调，似乎仍然会重复执行回调）

## 自定义语言

### 语法解析

关于bnf文件：

* 规则定义可以忽略空白
* 直接导向token的rule，在flex文件中需要使用对应的token
* 即使idea提示不需要转义，一些字符在regexp中仍然需要转义

### 重命名

最终决定是否可以进行重命名的是这个方法（在弹出对话框之前）：

* `com.intellij.refactoring.rename.RenameHandlerRegistry.hasAvailableHandler`

需要从`DataContext`得到`PsiElement`，然后再去判断是否可以重命名：

* `com.intellij.refactoring.actions.BaseRefactoringAction.getPsiElementArray`
* `com.intellij.refactoring.actions.BaseRefactoringAction.getElementAtCaret`

最终解决方案：

* 重载`com.intellij.refactoring.rename.PsiElementRenameHandler.isAvailableOnDataContext`

### 代码补全

方案1：自定义`com.intellij.codeInsight.completion.CompletionContributor.CompletionContributor`

方案2：实现`com.intellij.psi.PsiReference.getVariants`（存在某些注意事项）

这里操作可能被取消：`com.intellij.util.indexing.FileBasedIndexImpl.ensureUpToDate`

IDEA进行代码补全时，显示的提示项的最大数目存在限制，由以下系统属性决定：

```
ide.completion.variant.limit=1000
```

### 索引

可以使用stubIndex。

* 一个stubElementType可以对应多个stubIndex
* 一个stub可以存储多个属性，但基于key/name构建索引

### 刷新文件更改到硬盘

[Action doesn't see changes in xml file – IDEs Support (IntelliJ Platform) | JetBrains](https://intellij-support.jetbrains.com/hc/en-us/community/posts/206791625-Action-doesn-t-see-changes-in-xml-file)

尝试使用：

```
FileDocumentManager.saveDocument(FileDocumentManager.getDocument(VirtualFile))
```

## 其他

### 备注

```
// 为什么这里是HashMap而非LinkedHashMap？
com.intellij.refactoring.rename.RenameDialog.myAutoRenamerFactories
```

```
getName 确定进行重构和导航时显示的PsiElement的名字
setName 确定进行重命名时的逻辑
getTextOffset 确定选中一个PsiElement时，哪一部分会高亮显示
getReference 确定选中一个PsiElement时，哪些其他的PsiElement会同时高亮显示
```