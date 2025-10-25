package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.priority.ParadoxPriorityProvider
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingScriptedVariablesFix
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.model.ParadoxPriority
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 检测不正确的对（全局）封装变量的重载的代码检查。
 *
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 */
class IncorrectOverrideForScriptedVariableInspection : OverrideRelatedInspectionBase() {
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
                val priority = ParadoxPriorityProvider.getPriority(element)
                if (priority == ParadoxPriority.ORDERED) return // skip for ORDERED

                val name = element.name
                if (name.isNullOrEmpty()) return // anonymous -> skipped
                if (name.isParameterized()) return // parameterized -> ignored
                val selector = selector(project, file).scriptedVariable()
                val results = ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll()
                if (results.size < 2) return // no override -> skip
                val firstResult = results.first()
                val firstRootInfo = firstResult.fileInfo?.rootInfo
                if (firstRootInfo !is ParadoxRootInfo.MetadataBased) return
                val rootInfo = fileInfo.rootInfo
                if (rootInfo !is ParadoxRootInfo.MetadataBased) return
                if (firstRootInfo.rootFile == rootInfo.rootFile) return

                // different root file -> incorrect override
                val locationElement = element.scriptedVariableName
                val message = PlsBundle.message("inspection.incorrectOverrideForScriptedVariable.desc", name, priority)
                val fix = NavigateToOverridingScriptedVariablesFix(name, element, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }
}
