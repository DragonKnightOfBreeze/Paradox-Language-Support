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
 * 将安全作用域调用转换为显式形式。
 *
 * 检测于文法级别。
 *
 * [CK3/VIC3/EU5]
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
 * 说明：
 * - 对于任意游戏类型和任意安全调用运算符均可用。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToNormalFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.scopeCallStatementToNormalForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        return ParadoxScopeCallStatementManipulationService.convertToNormalForm(element, context.project)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        return ParadoxScopeCallStatementManipulationService.canConvertToNormalForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
