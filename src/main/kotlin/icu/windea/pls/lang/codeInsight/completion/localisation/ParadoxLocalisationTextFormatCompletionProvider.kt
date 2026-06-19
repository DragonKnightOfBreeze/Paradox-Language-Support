package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxLocalisationCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.model.ParadoxGameType

@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3, ParadoxGameType.Eu5)
class ParadoxLocalisationTextFormatCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement(TEXT_FORMAT_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationTextFormat>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        ParadoxLocalisationCompletionManager.completeTextFormat(context, result)
    }
}
