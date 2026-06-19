package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxLocalisationCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

/**
 * 提供图标的名字的代码补全。
 */
class ParadoxLocalisationIconCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement(ICON_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationIcon>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        ParadoxLocalisationCompletionManager.completeIcon(context, result)
    }
}
