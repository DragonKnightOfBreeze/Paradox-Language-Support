package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 将条件化语句转为块形式。
 *
 * ```paradox_script
 * # before
 * PARAM = $PARAM|no$
 *
 * # after
 * [[PARAM] PARAM = $PARAM$ ]
 * ```
 *
 * @see ParadoxConditionalStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ConditionalStatementToBlockFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.conditionalStatementToBlockForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        return ParadoxConditionalStatementManipulationService.convertToBlockForm(element, context.project)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        return ParadoxConditionalStatementManipulationService.canConvertToBlockForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
