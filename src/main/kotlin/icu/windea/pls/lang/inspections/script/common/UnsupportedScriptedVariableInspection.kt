package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.script.psi.ParadoxScriptNormalConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 检查是否在不支持的上下文中声明了封装变量。
 *
 * 规则如下：
 * - 不支持在条件化块中声明封装变量。
 */
class UnsupportedScriptedVariableInspection : LocalInspectionTool(), DumbAware {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                ProgressManager.checkCanceled()
                checkInConditionalBlock(element, holder)
            }
        }
    }

    private fun checkInConditionalBlock(element: ParadoxScriptScriptedVariable, holder: ProblemsHolder) {
        val parents = element.parents(withSelf = false).takeWhile { it !is ParadoxScriptRootBlock }
        val inConditionalBlock = parents.any { it is ParadoxScriptNormalConditionalBlock }
        if (!inConditionalBlock) return
        holder.registerProblem(element, ChronicleInspectionBundle.message("script.unsupportedScriptedVariable.desc.1"))
    }
}
