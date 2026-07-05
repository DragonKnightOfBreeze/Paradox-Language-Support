package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 事件脚本文件中（位于事件声明中的）的不正确的事件ID的代码检查。
 */
class IncorrectEventIdInspection : EventInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val elements = file.properties(inline = true)
        for (element in elements) checkEventIdForEventDeclaration(element, holder)
        return holder.resultsArray
    }

    private fun checkEventIdForEventDeclaration(element: ParadoxScriptProperty, holder: ProblemsHolder) {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return
        if (definitionInfo.type != ParadoxDefinitionTypes.event) return
        val nameField = definitionInfo.typeConfig.nameField
        val eventId = definitionInfo.name
        if (ParadoxEventManager.isValidEventId(eventId)) return
        val nameElement = selectScope { element.nameElement(nameField) } ?: return
        val description = ChronicleBundle.message("inspection.script.incorrectEventId.desc", eventId)
        holder.registerProblem(nameElement, description)
    }
}
