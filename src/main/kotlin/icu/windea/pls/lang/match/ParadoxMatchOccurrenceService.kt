package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.util.ParadoxDefineManager

object ParadoxMatchOccurrenceService {
    fun evaluate(contextElement: PsiElement, config: CwtMemberConfig<*>): ParadoxMatchOccurrence {
        val project = config.configGroup.project
        val cardinality = config.optionData.cardinality ?: return ParadoxMatchOccurrence(0, null, null)
        val cardinalityMinDefine = config.optionData.cardinalityMinDefine
        val cardinalityMaxDefine = config.optionData.cardinalityMaxDefine
        val occurrence = ParadoxMatchOccurrence(0, cardinality.min, cardinality.max, cardinality.relaxMin, cardinality.relaxMax)
        config.run {
            if (cardinalityMinDefine == null) return@run
            val defineValue = ParadoxDefineManager.getDefineValue(cardinalityMinDefine, contextElement, project)?.castOrNull<Int>() ?: return@run
            occurrence.min = defineValue
            occurrence.minDefine = cardinalityMinDefine
        }
        config.run {
            if (cardinalityMaxDefine == null) return@run
            val defineValue = ParadoxDefineManager.getDefineValue(cardinalityMaxDefine, contextElement, project)?.castOrNull<Int>() ?: return@run
            occurrence.max = defineValue
            occurrence.maxDefine = cardinalityMaxDefine
        }
        return occurrence
    }
}
