package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.ProblemsHolder
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider
import icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

object PlsInspectionService {
    fun getSuppressedToolIds(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val gameType = definitionInfo.gameType
        val result = mutableSetOf<String>()
        ParadoxDefinitionInspectionSuppressionProvider.EP_NAME.extensionList.forEach { ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@forEach
            result += ep.getSuppressedToolIds(definition, definitionInfo)
        }
        return result
    }

    fun checkIncorrectExpression(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        val gameType = config.configGroup.gameType
        ParadoxIncorrectExpressionChecker.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.check(element, config, holder)
        }
    }
}
