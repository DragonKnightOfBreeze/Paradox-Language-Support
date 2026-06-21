package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiService

/**
 * 将显式作用域调用转换为安全形式。
 *
 * 检测于文法级别。
 *
 * 适用于支持安全（调用）赋值运算符的游戏类型（CK3/VIC3/EU5 使用 `?=`，Stellaris 使用 `? =`）。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToSafeFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.scopeCallStatementToSafeForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        return ParadoxScopeCallStatementManipulationService.convertToSafeForm(element, context.project)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        // if property value is a block, caret offset should before or at `{`, so do inline math blocks
        if (!ParadoxScriptPsiService.isBeforeValueLeftBoundEnd(element, context.offset)) return false

        return ParadoxScopeCallStatementManipulationService.canConvertToSafeForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
