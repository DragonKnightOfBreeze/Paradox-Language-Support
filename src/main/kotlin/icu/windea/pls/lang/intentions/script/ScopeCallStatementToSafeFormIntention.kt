package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 将显式作用域调用转换为安全形式。
 *
 * ```paradox_script
 * # before
 * exists = owner
 * owner = { ... }
 *
 * # after (CK3/VIC3/EU5)
 * owner ?= { ... }
 *
 * # after (Stellaris)
 * owner? = { ... }
 * ```
 *
 * 说明：
 * - 根据 [ParadoxSyntaxConstraint][icu.windea.pls.model.constraints.ParadoxSyntaxConstraint] 来决定使用 `?=` 还是 `? =`。
 * - `exists = x` 和 `x = y` 之间的注释会在转换后移到安全形式之前。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToSafeFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.scopeCallStatementToSafeForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        val gameType = ParadoxAnalysisManager.selectGameType(element) ?: ParadoxGameType.getDefault()
        return ParadoxScopeCallStatementManipulationService.convertToSafeForm(element, context.project, gameType)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        return ParadoxScopeCallStatementManipulationService.canConvertToSafeForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
