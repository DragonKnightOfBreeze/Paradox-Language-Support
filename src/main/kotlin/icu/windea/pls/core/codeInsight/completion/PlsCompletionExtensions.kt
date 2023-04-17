package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.*
import com.intellij.openapi.application.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.progress.*
import com.intellij.patterns.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.concurrency.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.util.concurrent.*

val ProcessingContext.completionIds get() = get(PlsCompletionKeys.completionIdsKey)
val ProcessingContext.parameters get() = get(PlsCompletionKeys.parametersKey)
val ProcessingContext.contextElement get() = get(PlsCompletionKeys.contextElementKey)
val ProcessingContext.originalFile get() = get(PlsCompletionKeys.originalFileKey)
val ProcessingContext.quoted get() = get(PlsCompletionKeys.quotedKey) ?: false
val ProcessingContext.rightQuoted get() = get(PlsCompletionKeys.rightQuotedKey)
val ProcessingContext.offsetInParent get() = get(PlsCompletionKeys.offsetInParentKey)
val ProcessingContext.keyword get() = get(PlsCompletionKeys.keywordKey)
val ProcessingContext.isKey: Boolean? get() = get(PlsCompletionKeys.isKeyKey)
val ProcessingContext.config get() = get(PlsCompletionKeys.configKey)
val ProcessingContext.configs get() = get(PlsCompletionKeys.configsKey)
val ProcessingContext.configGroup get() = get(PlsCompletionKeys.configGroupKey)
val ProcessingContext.scopeContext get() = get(PlsCompletionKeys.scopeContextKey)
val ProcessingContext.scopeMatched get() = get(PlsCompletionKeys.scopeMatchedKey) ?: true
val ProcessingContext.scopeName get() = get(PlsCompletionKeys.scopeNameKey)
val ProcessingContext.scopeGroupName get() = get(PlsCompletionKeys.scopeGroupNameKey)
val ProcessingContext.isInt get() = get(PlsCompletionKeys.isIntKey) ?: false

fun PsiElement.getKeyword(offsetInParent: Int): String {
	return text.substring(0, offsetInParent).unquote()
}

fun PsiElement.getFullKeyword(offsetInParent: Int): String {
	return (text.substring(0, offsetInParent) + text.substring(offsetInParent + PlsConstants.dummyIdentifier.length)).unquote()
}

/**
 * 如果不指定[CompletionType]，IDE的默认实现会让对应的[CompletionProvider]相比指定[CompletionType]的后执行。
 */
fun CompletionContributor.extend(place: ElementPattern<out PsiElement>, provider: CompletionProvider<CompletionParameters>) {
	extend(CompletionType.BASIC, place, provider)
	extend(CompletionType.SMART, place, provider)
}

inline fun completeAsync(parameters: CompletionParameters, crossinline action: () -> Unit) {
	ProgressManager.checkCanceled()
	val indicator = parameters.process as? Disposable
	if(indicator == null) {
		return action()
	}
	val promise = ReadAction.nonBlocking(Callable { action() }).submit(AppExecutorUtil.getAppExecutorService())
	promise.cancelWhenDisposed(indicator)
}

