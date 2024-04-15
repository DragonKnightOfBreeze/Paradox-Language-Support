@file:Suppress("RemoveExplicitTypeArguments")

package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

fun PsiElement.getKeyword(offsetInParent: Int): String {
    return text.substring(0, offsetInParent).unquote()
}

fun PsiElement.getFullKeyword(offsetInParent: Int): String {
    return (text.substring(0, offsetInParent) + text.substring(offsetInParent + PlsConstants.dummyIdentifier.length)).unquote()
}

fun PsiElement.isIncomplete(): Boolean {
    val file = containingFile
    val originalFile = file.originalFile
    if(originalFile === file) return false
    val startOffset = startOffset
    file.findElementAt(startOffset)
    val e1 = file.findElementAt(startOffset) ?: return false
    val e2 = originalFile.findElementAt(startOffset) ?: return true
    if(e1.elementType != e2.elementType) return true
    if(e1.textLength != e2.textLength) return true
    return false
}

/**
 * 如果不指定[CompletionType]，IDE的默认实现会让对应的[CompletionProvider]相比指定[CompletionType]的后执行。
 */
fun CompletionContributor.extend(place: ElementPattern<out PsiElement>, provider: CompletionProvider<CompletionParameters>) {
    extend(CompletionType.BASIC, place, provider)
    extend(CompletionType.SMART, place, provider)
}

fun ProcessingContext.initialize(parameters: CompletionParameters) {
    this.parameters = parameters
    this.completionIds = mutableSetOf<String>().synced()
    
    val gameType = selectGameType(parameters.originalFile)
    this.gameType = gameType
    
    if(gameType != null) {
        val project = parameters.originalFile.project
        val configGroup = getConfigGroup(project, gameType)
        this.configGroup = configGroup
    }
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

