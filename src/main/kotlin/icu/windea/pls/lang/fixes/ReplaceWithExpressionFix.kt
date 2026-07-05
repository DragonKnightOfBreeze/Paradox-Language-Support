package icu.windea.pls.lang.fixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.psi.ParadoxExpressionElement

class ReplaceWithExpressionFix(
    private val replacement: String,
) : PsiUpdateModCommandQuickFix(), PriorityAction, DumbAware {
    override fun getName() = ChronicleBundle.message("fix.replaceWithExpression.name", replacement)

    override fun getFamilyName() = ChronicleBundle.message("fix.replaceWithExpression.familyName")

    override fun getPriority() = PriorityAction.Priority.TOP // 最高优先级，如果可用

    override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
        if (element !is ParadoxExpressionElement) return
        element.setValue(replacement)
    }
}
