@file:Suppress("RemoveExplicitTypeArguments")

package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*

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

