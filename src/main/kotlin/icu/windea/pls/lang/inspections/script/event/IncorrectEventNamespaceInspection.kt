package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 事件脚本文件中的不正确的事件命名空间声明的代码检查。
 */
class IncorrectEventNamespaceInspection : EventInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val elements = file.properties(inline = true)
        for (element in elements) checkEventNamespace(element, holder)
        return holder.resultsArray
    }

    private fun checkEventNamespace(element: ParadoxScriptProperty, holder: ProblemsHolder) {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return
        if (definitionInfo.type != ParadoxDefinitionTypes.eventNamespace) return
        val nameField = definitionInfo.typeConfig.nameField
        val eventNamespace = definitionInfo.name
        if (ParadoxEventManager.isValidEventNamespace(eventNamespace)) return
        val nameElement = selectScope { element.nameElement(nameField) } ?: return
        val description = PlsBundle.message("inspection.script.incorrectEventNamespace.desc", eventNamespace)
        holder.registerProblem(nameElement, description)
    }
}
