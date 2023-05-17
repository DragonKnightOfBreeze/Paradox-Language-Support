@file:Suppress("RemoveExplicitTypeArguments")

package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

object PlsCompletionKeys

val PlsCompletionKeys.completionIdsKey by lazy { keyOf<MutableSet<String>>("paradoxCompletion.completionIds") }
val PlsCompletionKeys.parametersKey by lazy { keyOf<CompletionParameters>("paradoxCompletion.parameters") }
val PlsCompletionKeys.contextElementKey by lazy { keyOf<PsiElement>("paradoxCompletion.contextElement") }
val PlsCompletionKeys.originalFileKey by lazy { keyOf<PsiFile>("paradoxCompletion.originalFile") }
val PlsCompletionKeys.quotedKey by lazy { keyOf<Boolean>("paradoxCompletion.quoted") { false } }
val PlsCompletionKeys.rightQuotedKey by lazy { keyOf<Boolean>("paradoxCompletion.rightQuoted") }
val PlsCompletionKeys.offsetInParentKey by lazy { keyOf<Int>("paradoxCompletion.offsetInParent") }
val PlsCompletionKeys.keywordKey by lazy { keyOf<String>("paradoxCompletion.keyword") }
val PlsCompletionKeys.startOffsetKey by lazy { keyOf<Int>("paradoxCompletion.startOffset") }
val PlsCompletionKeys.isKeyKey by lazy { keyOf<Boolean>("paradoxCompletion.isKey") }
val PlsCompletionKeys.configKey by lazy { keyOf<CwtConfig<*>>("paradoxCompletion.config") }
val PlsCompletionKeys.configsKey by lazy { keyOf<Collection<CwtConfig<*>>>("paradoxCompletion.configs") }
val PlsCompletionKeys.configGroupKey by lazy { keyOf<CwtConfigGroup>("paradoxCompletion.configGroup") }
val PlsCompletionKeys.scopeContextKey by lazy { keyOf<ParadoxScopeContext>("paradoxCompletion.scopeContext") }
val PlsCompletionKeys.scopeMatchedKey by lazy { keyOf<Boolean>("paradoxCompletion.scopeMatched") { true } }
val PlsCompletionKeys.scopeNameKey by lazy { keyOf<String>("paradoxCompletion.scopeName") }
val PlsCompletionKeys.scopeGroupNameKey by lazy { keyOf<String>("paradoxCompletion.scopeGroupName") }
val PlsCompletionKeys.isIntKey by lazy { keyOf<Boolean>("paradoxCompletion.isInt") }

var ProcessingContext.completionIds by PlsCompletionKeys.completionIdsKey
var ProcessingContext.parameters by PlsCompletionKeys.parametersKey
var ProcessingContext.contextElement by PlsCompletionKeys.contextElementKey
var ProcessingContext.originalFile by PlsCompletionKeys.originalFileKey
var ProcessingContext.quoted by PlsCompletionKeys.quotedKey
var ProcessingContext.rightQuoted by PlsCompletionKeys.rightQuotedKey
var ProcessingContext.offsetInParent by PlsCompletionKeys.offsetInParentKey
var ProcessingContext.keyword by PlsCompletionKeys.keywordKey
var ProcessingContext.startOffset by PlsCompletionKeys.startOffsetKey
var ProcessingContext.isKey: Boolean? by PlsCompletionKeys.isKeyKey
var ProcessingContext.config by PlsCompletionKeys.configKey
var ProcessingContext.configs by PlsCompletionKeys.configsKey
var ProcessingContext.configGroup by PlsCompletionKeys.configGroupKey
var ProcessingContext.scopeContext by PlsCompletionKeys.scopeContextKey
var ProcessingContext.scopeMatched by PlsCompletionKeys.scopeMatchedKey
var ProcessingContext.scopeName by PlsCompletionKeys.scopeNameKey
var ProcessingContext.scopeGroupName by PlsCompletionKeys.scopeGroupNameKey
var ProcessingContext.isInt by PlsCompletionKeys.isIntKey


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

//TODO 这个方法存在问题，不要使用
//inline fun completeAsync(parameters: CompletionParameters, crossinline action: () -> Unit) {
//    ProgressManager.checkCanceled()
//    val indicator = parameters.process as? Disposable
//    if(indicator == null) {
//        return action()
//    }
//    val promise = ReadAction.nonBlocking(Callable { action() }).submit(AppExecutorUtil.getAppExecutorService())
//    promise.cancelWhenDisposed(indicator)
//}

