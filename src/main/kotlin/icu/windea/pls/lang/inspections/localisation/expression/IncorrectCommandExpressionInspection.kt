package icu.windea.pls.lang.inspections.localisation.expression

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 不正确的[ParadoxCommandExpression]的检查。
 */
class IncorrectCommandExpressionInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return ParadoxFileManager.inLocalisationPath(fileInfo.path)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                if (!element.isCommandExpression()) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val expression = ParadoxCommandExpression.resolve(value, textRange, configGroup) ?: return
                val errors = expression.getAllErrors(element)
                if (errors.isEmpty()) return
                val fix = EscapeCommandFix(element)
                errors.forEach { error -> error.register(element, holder, fix) }
            }
        }
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
