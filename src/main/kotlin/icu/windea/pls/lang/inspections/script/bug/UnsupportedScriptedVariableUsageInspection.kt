package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.script.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了封装变量。
 */
class UnsupportedScriptedVariableUsageInspection: LocalInspectionTool() {
    //NOTE: vanilla files indicates that this inspection may be unnecessary!
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val extension = holder.file.name.substringAfterLast('.').lowercase()
        if(extension == "asset") {
            return object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    ProgressManager.checkCanceled()
                    if(element is ParadoxScriptedVariableReference) {
                        val resolved = element.reference?.resolve() ?: return
                        if(resolved.parent !is ParadoxScriptRootBlock) return //快速判断
                        val fileInfo = resolved.fileInfo ?: return
                        if(!"common/scripted_variables".matchesPath(fileInfo.pathToEntry.path)) return
                        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedScriptedVariableUsage.description.1"))
                    }
                }
            }
        }
        return PsiElementVisitor.EMPTY_VISITOR
    }
}

