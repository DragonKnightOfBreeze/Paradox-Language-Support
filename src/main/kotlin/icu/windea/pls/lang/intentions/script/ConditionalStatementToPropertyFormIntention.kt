package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptNormalConditionalBlock

/**
 * 将条件化语句转换为属性形式。
 *
 * 检测于文法级别。
 *
 * @see ParadoxConditionalStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ConditionalStatementToPropertyFormIntention : PsiUpdateModCommandAction<ParadoxScriptNormalConditionalBlock>(ParadoxScriptNormalConditionalBlock::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.conditionalStatementToPropertyForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptNormalConditionalBlock, updater: ModPsiUpdater) {
        return ParadoxConditionalStatementManipulationService.convertToPropertyForm(element, context.project)
    }

    override fun isElementApplicable(element: ParadoxScriptNormalConditionalBlock, context: ActionContext): Boolean {
        return ParadoxConditionalStatementManipulationService.canConvertToPropertyForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptNormalConditionalBlock
    }
}
