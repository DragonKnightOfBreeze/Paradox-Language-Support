package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock

/**
 * 将条件化语句转换为属性形式。
 *
 * 检测于文法级别。
 *
 * @see ParadoxConditionalStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ConditionalStatementToPropertyFormIntention : PsiUpdateModCommandAction<ParadoxScriptConditionalBlock>(ParadoxScriptConditionalBlock::class.java), DumbAware {
    override fun getFamilyName() = ChronicleBundle.message("intention.conditionalStatementToPropertyForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptConditionalBlock, updater: ModPsiUpdater) {
        return ParadoxConditionalStatementManipulationService.convertToPropertyForm(element, context.project)
    }

    override fun isElementApplicable(element: ParadoxScriptConditionalBlock, context: ActionContext): Boolean {
        return ParadoxConditionalStatementManipulationService.canConvertToPropertyForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptConditionalBlock
    }
}
