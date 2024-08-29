package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 检查事件脚本文件是否缺失事件命名空间。
 *
 * 注意：这项代码检查不是强制性的，未通过这项代码检查并不意味着脚本文件中存在错误，以至于导致游戏运行时的异常。
 */
class MissingEventNamespaceInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if(!shouldCheckFile(file)) return null
        
		file as ParadoxScriptFile
        val rootBlock = file.block ?: return null
        val eventNamespace = rootBlock.findChildOfType<ParadoxScriptProperty> { it.name.equals("namespace", true) }
        if(eventNamespace == null) {
            val holder = ProblemsHolder(manager, file, isOnTheFly)
            holder.registerProblem(file, PlsBundle.message("inspection.script.missingEventNamespace.desc"))
            return holder.resultsArray
        }
        return null
    }
    
    private fun shouldCheckFile(file: PsiFile): Boolean {
        //仅检查事件脚本文件
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path
        return "txt" == filePath.fileExtension && "events".matchesPath(filePath.path)
    }
}
