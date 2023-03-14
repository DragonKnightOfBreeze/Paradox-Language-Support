package icu.windea.pls.script.inspections.event

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 检查事件命名空间的格式是否合法。
 */
class IncorrectEventNamespaceInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        //仅检查事件脚本文件
        if(file !is ParadoxScriptFile) return null
        val fileInfo = file.fileInfo ?: return null
        if(!"events".matchesPath(fileInfo.entryPath.path, acceptSelf = false)) return null
        
        val rootBlock = file.block ?: return null
        val properties = rootBlock.propertyList
        if(properties.isEmpty()) return null //空文件，不进行检查
        val namespaceProperties = properties.filter { it.name.equals("namespace", true) }
        if(namespaceProperties.isEmpty()) return null //没有事件命名空间，不进行检查
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        for(namespaceProperty in namespaceProperties) {
            ProgressManager.checkCanceled()
            val namespacePropertyValue = namespaceProperty.propertyValue?.castOrNull<ParadoxScriptString>() ?: continue //事件ID不是字符串，另行检查
            val namespace = namespacePropertyValue.stringValue
            if(!ParadoxEventHandler.isValidEventNamespace(namespace)) {
                holder.registerProblem(namespacePropertyValue, PlsBundle.message("inspection.script.event.incorrectEventNamespace.description", namespace))
            }
        }
        return holder.resultsArray
    }
}
