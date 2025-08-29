package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.ep.icon.ParadoxLocalisationIconSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

/**
 * 提供图标名字的代码补全。
 */
class ParadoxLocalisationIconCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationIcon>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return

        ParadoxCompletionManager.initializeContext(parameters, context)
        context.contextElement = element

        ParadoxLocalisationIconSupport.complete(context, result)
    }
}
