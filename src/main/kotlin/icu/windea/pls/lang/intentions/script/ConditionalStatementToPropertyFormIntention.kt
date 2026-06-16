package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition

/**
 * 将条件构造转换为属性形式。
 *
 * ```paradox_script
 * # before
 * [[PARAM] PARAM = $PARAM$ ]
 *
 * # after
 * PARAM = $PARAM|no$
 * ```
 *
 * @see ParadoxConditionalStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ConditionalStatementToPropertyFormIntention : PsiUpdateModCommandAction<ParadoxScriptParameterCondition>(ParadoxScriptParameterCondition::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.conditionalStatementToPropertyForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptParameterCondition, updater: ModPsiUpdater) {
        return ParadoxConditionalStatementManipulationService.convertToPropertyForm(element, context.project)
    }

    override fun isElementApplicable(element: ParadoxScriptParameterCondition, context: ActionContext): Boolean {
        return ParadoxConditionalStatementManipulationService.isBlockForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptParameterCondition
    }
}
