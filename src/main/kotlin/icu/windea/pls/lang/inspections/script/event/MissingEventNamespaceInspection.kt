package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 检查事件脚本文件是否缺失事件命名空间。
 */
class MissingEventNamespaceInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        //仅检查事件脚本文件
        if(!isEventScriptFile(file)) return null
        
		file as ParadoxScriptFile
        val rootBlock = file.block ?: return null
        val eventNamespace = rootBlock.findChildOfType<ParadoxScriptProperty> { it.name.equals("namespace", true) }
        if(eventNamespace == null) {
            val holder = ProblemsHolder(manager, file, isOnTheFly)
            holder.registerProblem(file, PlsBundle.message("inspection.script.missingEventNamespace.description"))
            return holder.resultsArray
        }
        return null
    }
    
    private fun isEventScriptFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.pathToEntry
        return "txt" == filePath.fileExtension && "events".matchesPath(filePath.path)
    }
}