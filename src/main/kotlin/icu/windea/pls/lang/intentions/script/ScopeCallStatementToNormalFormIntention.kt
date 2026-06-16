package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SAFE_ASSIGN_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * 将安全作用域调用转换为显式调用形式。
 *
 * CK3/VIC3/EU5:
 * ```paradox_script
 * # before
 * owner ?= { ... }
 *
 * # after
 * exists = owner
 * owner = { ... }
 * ```
 *
 * Stellaris:
 * ```paradox_script
 * # before
 * owner? = { ... }
 *
 * # after
 * exists = owner
 * owner = { ... }
 * ```
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToNormalFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.scopeCallStatementToNormalForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        val gameType = ParadoxAnalysisManager.selectGameType(element) ?: ParadoxGameType.getDefault()
        return ParadoxScopeCallStatementManipulationService.convertToNormalForm(element, context.project, gameType)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        if (!ParadoxScopeCallStatementManipulationService.isSafeForm(element)) return false
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        return when (separatorNode.elementType) {
            SAFE_ASSIGN_SIGN -> ParadoxSyntaxService.isSafeAssignOperatorAllowed(element)
            SAFE_CALL_ASSIGN_SIGN -> ParadoxSyntaxService.isSafeCallAssignOperatorAllowed(element)
            else -> false
        }
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
