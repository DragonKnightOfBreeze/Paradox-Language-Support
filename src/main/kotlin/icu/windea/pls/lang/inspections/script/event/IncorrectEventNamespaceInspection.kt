package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.matchesPath
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.properties

/**
 * 检查事件脚本文件中的事件命名空间的格式是否合法。
 *
 * 注意：这项代码检查不是强制性的，未通过这项代码检查并不意味着脚本文件中存在错误，以至于导致游戏运行时的异常。
 */
class IncorrectEventNamespaceInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        //仅检查事件脚本文件
        if (file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path
        return "txt" == filePath.fileExtension && "events".matchesPath(filePath.path)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)

        file.properties().options(inline = true).forEach f@{ element ->
            val definitionInfo = element.definitionInfo ?: return@f
            if (definitionInfo.type != "event_namespace") return@f
            val nameField = definitionInfo.typeConfig.nameField
            val eventNamespace = definitionInfo.name
            if (ParadoxEventManager.isValidEventNamespace(eventNamespace)) return@f
            val nameElement = if (nameField == null) element.propertyKey else element.findProperty(nameField)?.propertyValue
            if (nameElement == null) return@f //忽略
            holder.registerProblem(nameElement, PlsBundle.message("inspection.script.incorrectEventNamespace.desc", eventNamespace))
        }

        return holder.resultsArray
    }
}
