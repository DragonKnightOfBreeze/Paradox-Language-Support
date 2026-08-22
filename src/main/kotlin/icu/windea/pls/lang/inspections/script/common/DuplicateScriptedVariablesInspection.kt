package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.findChildren
import icu.windea.pls.lang.fixes.navigation.NavigateToDuplicatesFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 检查当前脚本文件中是否存在（位于同一上下文中的）重复的封装变量声明。
 *
 * 提供快速修复：
 * - 导航到重复项
 */
class DuplicateScriptedVariablesInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitBlock(element: ParadoxScriptBlock) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }

            override fun visitRootBlock(element: ParadoxScriptRootBlock) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }

            override fun visitConditionalBlock(element: ParadoxScriptConditionalBlock) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(containerElement: ParadoxScriptMemberContainer, holder: ProblemsHolder) {
        val elementGroup = containerElement.findChildren<ParadoxScriptScriptedVariable>().groupBy { it.name.orEmpty() }
        if (elementGroup.isEmpty()) return
        for ((name, elements) in elementGroup) {
            ProgressManager.checkCanceled()
            if (name.isEmpty()) continue
            if (elements.size <= 1) continue
            elements.forEachFast { element ->
                val location = element.scriptedVariableName
                val description = ChronicleInspectionBundle.message("script.duplicateScriptedVariables.desc", name)
                val fix = NavigateToDuplicatesFix(name, element, elements)
                holder.registerProblem(location, description, fix)
            }
        }
    }
}
