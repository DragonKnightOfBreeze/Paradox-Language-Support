package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiService

/**
 * 将链式的作用域调用转换为嵌套形式。
 *
 * 检测于文法级别和语义级别。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToNestedFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = ChronicleBundle.message("intention.scopeCallStatementToNestedForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        // 2.2.0 should use `context.offset` here to get expected behaviour
        val moveTo = ParadoxScopeCallStatementManipulationService.convertToNestedForm(element, context.project, context.offset)
        if (moveTo >= 0) updater.moveCaretTo(moveTo)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        // if property value is a block, caret offset should before or at `{`, so do inline math blocks
        if (!ParadoxScriptPsiService.isBeforeValueLeftBoundEnd(element, context.offset)) return false

        val gameType = selectGameType(context.file)
        return ParadoxScopeCallStatementManipulationService.canConvertToNestedForm(element, gameType)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
