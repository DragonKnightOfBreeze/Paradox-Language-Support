package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 检查事件脚本文件中的事件ID的格式是否合法。
 *
 * 注意：这项代码检查不是强制性的，未通过这项代码检查并不意味着脚本文件中存在错误，以至于导致游戏运行时的异常。
 */
class IncorrectEventIdInspection : EventInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)

        val elements = file.properties(inline = true)
        for (element in elements) {
            ProgressManager.checkCanceled()
            val definitionInfo = element.definitionInfo ?: continue
            if (definitionInfo.type != ParadoxDefinitionTypes.event) continue
            val nameField = definitionInfo.typeConfig.nameField
            val eventId = definitionInfo.name
            if (ParadoxEventManager.isValidEventId(eventId)) continue
            val nameElement = selectScope { element.nameElement(nameField) } ?: continue
            val description = PlsBundle.message("inspection.script.incorrectEventId.desc", eventId)
            holder.registerProblem(nameElement, description)
        }

        return holder.resultsArray
    }
}
