package icu.windea.pls.lang.inspections

import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.ChronicleAnnotationService
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider
import icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker
import icu.windea.pls.ep.inspections.ParadoxIncorrectSyntaxChecker
import icu.windea.pls.ep.inspections.ParadoxUnresolvedExpressionChecker
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxInspectionService {
    @Optimized
    fun getSuppressedToolIds(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val gameType = definitionInfo.gameType
        val result = mutableSetOf<String>()
        ParadoxDefinitionInspectionSuppressionProvider.EP_NAME.extensionList.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            result += ep.getSuppressedToolIds(definition, definitionInfo)
        }
        return result
    }

    @Optimized
    fun checkIncorrectSyntax(element: PsiElement, context: ParadoxSyntaxInspectionContext, checkers: List<ParadoxIncorrectSyntaxChecker>): Boolean {
        val gameType = context.gameType
        checkers.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            val r = ep.check(element, context)
            if (!r) return false
        }
        return true
    }

    @Optimized
    fun checkIncorrectExpression(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext, checkers: List<ParadoxIncorrectExpressionChecker>): Boolean {
        val gameType = context.gameType
        checkers.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            val r = ep.check(element, config, context)
            if (!r) return false
        }
        return true
    }

    @Optimized
    fun checkUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext, checkers: List<ParadoxUnresolvedExpressionChecker>): Boolean {
        val gameType = context.gameType
        checkers.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            val r = ep.check(element, expectedConfigs, context)
            if (!r) return false
        }
        return true
    }
}
