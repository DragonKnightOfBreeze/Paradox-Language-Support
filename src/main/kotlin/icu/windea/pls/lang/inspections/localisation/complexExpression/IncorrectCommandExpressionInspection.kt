package icu.windea.pls.lang.inspections.localisation.complexExpression

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression

/**
 * 不正确的 [ParadoxCommandExpression] 的检查。
 */
class IncorrectCommandExpressionInspection : IncorrectComplexExpressionBase() {
    override fun resolveComplexExpression(element: ParadoxLocalisationExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        if (!element.isCommandExpression()) return null
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        return ParadoxCommandExpression.resolve(value, textRange, configGroup)
    }

    override fun getFixes(element: ParadoxLocalisationExpressionElement, complexExpression: ParadoxComplexExpression): Array<out LocalQuickFix> {
        return arrayOf(EscapeCommandFix(element))
    }

    private class EscapeCommandFix(
        element: PsiElement
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
        override fun getText() = PlsBundle.message("fix.localisation.escapeCommand")

        override fun getFamilyName() = text

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val commandElement = startElement.parent?.castOrNull<ParadoxLocalisationCommand>() ?: return
            val startOffset = commandElement.startOffset
            file.fileDocument.insertString(startOffset, "[")
        }
    }
}
