package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.isInlineScriptUsage
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * （对于脚本文件）检查是否在不支持的地方使用了内联脚本。
 */
class UnsupportedInlineScriptUsageInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val extension = holder.file.name.substringAfterLast('.').lowercase()
        if (extension == "asset") {
            return object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is ParadoxScriptProperty) {
                        if (element.name.isInlineScriptUsage()) {
                            holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedInlineScriptUsage.desc.1"))
                        }
                    }
                }
            }
        }
        return PsiElementVisitor.EMPTY_VISITOR
    }
}

