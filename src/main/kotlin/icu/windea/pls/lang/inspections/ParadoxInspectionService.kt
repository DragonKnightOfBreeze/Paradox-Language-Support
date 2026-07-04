package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.ChronicleAnnotationManager
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider
import icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker
import icu.windea.pls.ep.inspections.ParadoxIncorrectSyntaxChecker
import icu.windea.pls.ep.inspections.ParadoxUnresolvedExpressionDecorator
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxInspectionService {
    @Optimized
    fun getSuppressedToolIds(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val gameType = definitionInfo.gameType
        val result = mutableSetOf<String>()
        ParadoxDefinitionInspectionSuppressionProvider.EP_NAME.extensionList.forEachFast f@{ ep ->
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            result += ep.getSuppressedToolIds(definition, definitionInfo)
        }
        return result
    }

    @Optimized
    fun checkIncorrectSyntax(element: PsiElement, context: ParadoxSyntaxInspectionContext, checkers: List<ParadoxIncorrectSyntaxChecker>) {
        val gameType = context.gameType
        checkers.forEachFast f@{ ep ->
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.check(element, context)
        }
    }

    @Optimized
    fun checkIncorrectExpression(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder, checkers: List<ParadoxIncorrectExpressionChecker>) {
        val gameType = config.configGroup.gameType
        checkers.forEachFast f@{ ep ->
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.check(element, config, holder)
        }
    }

    context(_: LocalInspectionTool)
    @Optimized
    fun getDescriptionForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): String? {
        if (expectedConfigs.isEmpty()) return null
        val gameType = expectedConfigs.first().configGroup.gameType
        val decorators = ParadoxUnresolvedExpressionDecorator.EP_NAME.extensionList
        decorators.forEachFast f@{ ep ->
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.getDescription(element, expectedConfigs)?.let { return it }
        }
        return null
    }

    context(_: LocalInspectionTool)
    @Optimized
    fun getHighlightTypeForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): ProblemHighlightType? {
        if (expectedConfigs.isEmpty()) return null
        val gameType = expectedConfigs.first().configGroup.gameType
        val decorators = ParadoxUnresolvedExpressionDecorator.EP_NAME.extensionList
        decorators.forEachFast f@{ ep ->
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.getHighlightType(element, expectedConfigs)?.let { return it }
        }
        return null
    }

    context(_: LocalInspectionTool)
    @Optimized
    fun getFixesForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Array<LocalQuickFix> {
        if (expectedConfigs.isEmpty()) return LocalQuickFix.EMPTY_ARRAY
        val result = mutableListOf<LocalQuickFix>()
        val gameType = expectedConfigs.first().configGroup.gameType
        val decorators = ParadoxUnresolvedExpressionDecorator.EP_NAME.extensionList
        decorators.forEachFast f@{ ep ->
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.collectFixes(element, expectedConfigs, result)
        }
        return result.toArray(LocalQuickFix.EMPTY_ARRAY)
    }
}
