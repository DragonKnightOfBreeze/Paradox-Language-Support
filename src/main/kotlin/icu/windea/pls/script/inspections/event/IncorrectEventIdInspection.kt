package icu.windea.pls.script.inspections.event

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 检查是否ID的格式是否合法。
 */
class IncorrectEventIdInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        //仅检查事件脚本文件
        if(file !is ParadoxScriptFile) return null
        val fileInfo = file.fileInfo ?: return null
        if(!"events".matchesPath(fileInfo.entryPath.path, acceptSelf = false)) return null
        
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty) visitDefinition(element)
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitDefinition(element: ParadoxScriptProperty) {
                val definitionInfo = element.definitionInfo ?: return
                if(definitionInfo.type != "event") return
                val nameField = definitionInfo.typeConfig.nameField
                val eventId = definitionInfo.name
                if(ParadoxEventHandler.isValidEventId(eventId)) return
                val nameElement = if(nameField == null) element.propertyKey else element.findProperty(nameField)?.propertyValue
                if(nameElement == null) return //忽略
                holder.registerProblem(nameElement, PlsBundle.message("inspection.script.event.incorrectEventId.description", eventId))
            }
        })
        return holder.resultsArray
    }
}
