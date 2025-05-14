package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.icon.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.localisation.psi.*

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
