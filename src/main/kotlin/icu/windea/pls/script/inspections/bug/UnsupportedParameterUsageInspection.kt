package icu.windea.pls.script.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了参数。
 */
class UnsupportedParameterUsageInspection: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxParameter || element is ParadoxConditionParameter) {
                    val resolved = element.reference?.resolve()
                    if(resolved == null) {
                        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedParameterUsage.description.1"))
                    }
                }
            }
        }
    }
}

