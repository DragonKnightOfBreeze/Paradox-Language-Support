package icu.windea.pls.lang.inspections.localisation.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 不正确的[ParadoxDatabaseObjectExpression]的检查。
 */
class IncorrectDatabaseObjectExpressionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxLocalisationExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                if (!element.isDatabaseObjectExpression(strict = true)) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val expression = ParadoxDatabaseObjectExpression.resolve(value, textRange, configGroup) ?: return
                val errors = expression.getAllErrors(element)
                if (errors.isEmpty()) return
                errors.forEach { error -> error.register(element, holder) }
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return ParadoxFileManager.inLocalisationPath(fileInfo.path)
    }
}


