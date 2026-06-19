package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxLocalisationCompletionManager
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiUtil

/**
 * 提供本地化的名字的代码补全。
 */
class ParadoxLocalisationNameCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement(PROPERTY_KEY_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsSettings.getInstance().state.completion.completeLocalisationNames) return

        val position = parameters.position
        if (ParadoxLocalisationPsiUtil.isLocalisationLocaleLike(position)) return
        val element = position.parent as? ParadoxLocalisationPropertyKey ?: return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        ParadoxLocalisationCompletionManager.completeLocalisationName(context, result)
    }
}
