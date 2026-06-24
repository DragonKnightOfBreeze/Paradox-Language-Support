package icu.windea.pls.lang.intentions.localisation

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.script.psi.ParadoxScriptElementFactory

/**
 * 将封装变量引用替换为其解析结果。
 */
@Suppress("UnstableApiUsage")
class ReplaceScriptedVariableReferenceWithResolvedValueIntention : PsiUpdateModCommandAction<ParadoxScriptedVariableReference>(ParadoxScriptedVariableReference::class.java) {
    override fun getFamilyName() = PlsBundle.message("intention.replaceScriptedVariableReferenceWithResolvedValue")

    override fun invoke(context: ActionContext, element: ParadoxScriptedVariableReference, updater: ModPsiUpdater) {
        val result = getResult(element) ?: return
        val newElement = ParadoxScriptElementFactory.createValueFromText(context.project, result)
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptedVariableReference, context: ActionContext): Boolean {
        return getResult(element) != null
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptedVariableReference
    }

    private fun getResult(element: ParadoxScriptedVariableReference): String? {
        val resolved = element.resolved() ?: return null
        return resolved.value
    }
}
