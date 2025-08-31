package icu.windea.pls.lang.inspections.localisation.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.expression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.expression.getAllErrors
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.model.paths.ParadoxPathMatcher
import icu.windea.pls.model.paths.matches

/**
 * 不正确的[ParadoxDatabaseObjectExpression]的检查。
 */
class IncorrectDatabaseObjectExpressionInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return fileInfo.path.matches(ParadoxPathMatcher.InLocalisationPath)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
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
}


