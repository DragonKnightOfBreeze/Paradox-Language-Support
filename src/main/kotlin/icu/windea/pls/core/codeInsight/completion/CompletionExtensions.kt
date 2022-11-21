package icu.windea.pls.core.codeInsight.completion

import com.intellij.util.*

val ProcessingContext.completionType get() = get(PlsCompletionKeys.completionTypeKey)
val ProcessingContext.contextElement get() = get(PlsCompletionKeys.contextElementKey)
val ProcessingContext.quoted get() = get(PlsCompletionKeys.quotedKey)
val ProcessingContext.offsetInParent get() = get(PlsCompletionKeys.offsetInParentKey)
val ProcessingContext.keyword get() = get(PlsCompletionKeys.keywordKey)
val ProcessingContext.isKey: Boolean? get() = get(PlsCompletionKeys.isKeyKey)
val ProcessingContext.configExpression get() = get(PlsCompletionKeys.configExpressionKey)
val ProcessingContext.config get() = get(PlsCompletionKeys.configKey)
val ProcessingContext.configs get() = get(PlsCompletionKeys.configsKey)
val ProcessingContext.configGroup get() = get(PlsCompletionKeys.configGroupKey)
val ProcessingContext.prevScope get() = get(PlsCompletionKeys.prevScopeKey)
val ProcessingContext.isExpectedScopeMatched get() = get(PlsCompletionKeys.isExpectedScopeMatchedKey) ?: true
val ProcessingContext.scopeName get() = get(PlsCompletionKeys.scopeNameKey)
val ProcessingContext.scopeGroupName get() = get(PlsCompletionKeys.scopeGroupNameKey)
val ProcessingContext.isInt get() = get(PlsCompletionKeys.isIntKey) ?: false
val ProcessingContext.valueSetName get() = get(PlsCompletionKeys.valueSetName)
