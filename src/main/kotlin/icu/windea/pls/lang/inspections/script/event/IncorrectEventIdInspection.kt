package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

/**
 * 检查事件脚本文件中的事件ID的格式是否合法。
 *
 * 注意：这项代码检查不是强制性的，未通过这项代码检查并不意味着脚本文件中存在错误，以至于导致游戏运行时的异常。
 */
class IncorrectEventIdInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (!shouldCheckFile(file)) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)

        file as ParadoxScriptFile
        file.processProperty(inline = true) p@{ element ->
            val definitionInfo = element.definitionInfo ?: return@p true
            if (definitionInfo.type != ParadoxDefinitionTypes.Event) return@p true
            val nameField = definitionInfo.typeConfig.nameField
            val eventId = definitionInfo.name
            if (ParadoxEventManager.isValidEventId(eventId)) return@p true
            val nameElement = if (nameField == null) element.propertyKey else element.findProperty(nameField)?.propertyValue
            if (nameElement == null) return@p true //忽略
            holder.registerProblem(nameElement, PlsBundle.message("inspection.script.incorrectEventId.desc", eventId))
            true
        }

        return holder.resultsArray
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        //仅检查事件脚本文件
        if (file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path
        return "txt" == filePath.fileExtension && "events".matchesPath(filePath.path)
    }
}
