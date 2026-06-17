package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.manipulation.ParadoxEventManipulationService
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 事件脚本文件中的缺失的事件命名空间声明的代码检查。
 *
 * 说明：
 * - 此代码检查是启发式的，可能存在误报。
 * - 此代码检查未通过时，不一定意味着会引发游戏引擎层面的异常。
 * - 实际上，事件脚本文件中可以不声明或者声明多个事件命名空间，事件ID不需要匹配同文件中的先前声明的事件命名空间。
 */
class MissingEventNamespaceInspection : EventInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        if (!ParadoxEventManipulationService.isMissingEventNamespaceDeclarationInFile(file)) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("inspection.script.missingEventNamespace.desc")
        holder.registerProblem(file, description)
        return holder.resultsArray
    }
}
