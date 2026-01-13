package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
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

        file.properties(inline = true).forEach f@{ element ->
            val definitionInfo = element.definitionInfo ?: return@f
            if (definitionInfo.type != ParadoxDefinitionTypes.event) return@f
            val nameField = definitionInfo.typeConfig.nameField
            val eventId = definitionInfo.name
            if (ParadoxEventManager.isValidEventId(eventId)) return@f
            val nameElement = if (nameField == null) element.propertyKey else element.select { propertyOld(nameField) }?.propertyValue
            if (nameElement == null) return@f // 忽略
            holder.registerProblem(nameElement, PlsBundle.message("inspection.script.incorrectEventId.desc", eventId))
        }

        return holder.resultsArray
    }
}
