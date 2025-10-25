package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingScriptedVariablesFix
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 检测对（全局）封装变量的重载的代码检查。
 *
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 */
class OverrideForScriptedVariableInspection : OverrideRelatedInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val fileInfo = file.fileInfo
        if (fileInfo == null) return PsiElementVisitor.EMPTY_VISITOR
        if (!ParadoxScriptedVariableManager.isGlobalFilePath(fileInfo.path)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptScriptedVariable) visitScriptedVariable(element)
            }

            private fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                val name = element.name
                if (name.isNullOrEmpty()) return // anonymous -> skipped
                if (name.isParameterized()) return // parameterized -> ignored
                val selector = selector(project, file).scriptedVariable()
                val results = ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll()
                if (results.size < 2) return // no override -> skip

                val locationElement = element.scriptedVariableName
                val message = PlsBundle.message("inspection.overrideForScriptedVariable.desc", name)
                val fix = NavigateToOverridingScriptedVariablesFix(name, element, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }
}
