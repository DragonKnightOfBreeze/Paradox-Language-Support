package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiService

/**
 * 将作用域调用转换为普通形式。
 *
 * 检测于文法级别和语义级别。
 *
 * 适用于所有游戏类型和任意安全调用运算符（`?=` 或 `? =`）。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToNormalFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = ChronicleBundle.message("intention.scopeCallStatementToNormalForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        return ParadoxScopeCallStatementManipulationService.convertToNormalForm(element, context.project)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        // if property value is a block, caret offset should before or at `{`, so do inline math blocks
        if (!ParadoxScriptPsiService.isBeforeValueLeftBoundEnd(element, context.offset)) return false

        return ParadoxScopeCallStatementManipulationService.canConvertToNormalForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
