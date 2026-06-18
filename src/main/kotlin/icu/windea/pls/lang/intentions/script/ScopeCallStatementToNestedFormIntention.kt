package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 将链式作用域调用转换为嵌套形式。
 *
 * 检测于文法级别和语义级别。
 *
 * 说明：
 * - 单步转换（仅展开一层）。
 * - 光标位置决定分割点：找到光标之前最后一个点号，在该点号处分割属性键。
 * - 展开后总是会换行。
 *
 * ```paradox_script
 * # before
 * root.owner = { a = 1 }
 *
 * # after
 * root = {
 *     owner = { a = 1 }
 * }
 * ```
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToNestedFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.scopeCallStatementToNestedForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        val offset = context.offset
        return ParadoxScopeCallStatementManipulationService.convertToNestedForm(element, context.project, offset)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        return ParadoxScopeCallStatementManipulationService.canConvertToNestedForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
