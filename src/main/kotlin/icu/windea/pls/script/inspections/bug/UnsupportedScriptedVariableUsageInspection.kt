package icu.windea.pls.script.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了封装变量。
 */
class UnsupportedScriptedVariableUsageInspection: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val extension = file.name.substringAfterLast('.').lowercase()
        if(extension == "asset") {
            return object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    ProgressManager.checkCanceled()
                    if(element is ParadoxScriptedVariableReference) {
                        holder.registerProblem(element, PlsBundle.message("inspection.script.bug.unsupportedScriptedVariableUsage.description.1"))
                    }
                }
            }
        }
        return PsiElementVisitor.EMPTY_VISITOR
    }
}