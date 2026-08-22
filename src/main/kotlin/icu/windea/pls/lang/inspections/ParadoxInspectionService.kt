package icu.windea.pls.lang.inspections

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimizedIfEmpty
import icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider
import icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker
import icu.windea.pls.ep.inspections.ParadoxIncorrectSyntaxChecker
import icu.windea.pls.ep.inspections.ParadoxUnresolvedExpressionChecker
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.orSpecific

@Optimized
object ParadoxInspectionService {
    fun getSuppressedToolIds(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val gameType = definitionInfo.gameType
        val result = mutableSetOf<String>()
        val eps = ParadoxDefinitionInspectionSuppressionProvider.getAll()
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            result += ep.getSuppressedToolIds(definition, definitionInfo)
        }
        return result.optimizedIfEmpty()
    }

    fun applyIncorrectSyntaxCheckers(context: ParadoxSyntaxInspectionContext, element: PsiElement): Boolean {
        val gameType = context.gameType
        val checkers = ParadoxIncorrectSyntaxChecker.getAll()
        checkers.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, context)
            if (!r) return false
        }
        return true
    }

    fun applyIncorrectExpressionCheckers(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        val gameType = context.gameType
        val checkers = ParadoxIncorrectExpressionChecker.getAll()
        checkers.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, config, context)
            if (!r) return false
        }
        return true
    }

    fun applyUnresolvedExpressionCheckers(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        val gameType = context.gameType
        val checkers = ParadoxUnresolvedExpressionChecker.getAll()
        checkers.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, expectedConfigs, context)
            if (!r) return false
        }
        return true
    }
}
