package icu.windea.pls.script.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了内联脚本。
 */
class UnsupportedInlineScriptUsageInspection: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val extension = holder.file.name.substringAfterLast('.').lowercase()
        if(extension == "asset") {
            return object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    ProgressManager.checkCanceled()
                    if(element is ParadoxScriptProperty) {
                        if(ParadoxInlineScriptHandler.getInfo(element) != null) {
                            holder.registerProblem(element, PlsBundle.message("inspection.script.bug.unsupportedInlineScriptUsage.description.1"))
                        }
                    }
                }
            }
        }
        return PsiElementVisitor.EMPTY_VISITOR
    }
}

