package icu.windea.pls.script.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了参数。
 */
class UnsupportedParameterInspection: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxParameter || element is ParadoxArgument) {
                    val resolved = element.reference?.resolve()
                    if(resolved == null) {
                        holder.registerProblem(element, PlsBundle.message("inspection.script.bug.unsupportedParameter.description"))
                    }
                }
            }
        }
    }
}