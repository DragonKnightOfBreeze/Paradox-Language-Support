package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class NonTriggeredEventInspection: LocalInspectionTool() {
    //see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/88
    
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if(!shouldCheckFile(file)) return null
        
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        
        file as ParadoxScriptFile
        file.processProperty(inline = true) p@{ element ->
            val definitionInfo = element.definitionInfo ?: return@p true
            if(definitionInfo.type != "event") return@p true
            if("triggered" !in definitionInfo.typeConfig.subtypes.keys) return@p true //no "triggered" subtype declared, skip
            if("triggered" in definitionInfo.subtypes) return@p true
            holder.registerProblem(element, PlsBundle.message("inspection.script.nonTriggeredEvent.desc"))
            true
        }
        
        return holder.resultsArray
    }
    
    private fun shouldCheckFile(file: PsiFile): Boolean {
        //仅检查事件脚本文件
        if(file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path
        return "txt" == filePath.fileExtension && "events".matchesPath(filePath.path)
    }
}
