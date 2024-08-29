package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 检查事件命名空间的格式是否合法。
 */
class IncorrectEventNamespaceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if(!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptProperty) visitDefinition(element)
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitDefinition(element: ParadoxScriptProperty) {
                val definitionInfo = element.definitionInfo ?: return
                if(definitionInfo.type != "event_namespace") return
                val nameField = definitionInfo.typeConfig.nameField
                val eventNamespace = definitionInfo.name
                if(ParadoxEventManager.isValidEventNamespace(eventNamespace)) return
                val nameElement = if(nameField == null) element.propertyKey else element.findProperty(nameField)?.propertyValue
                if(nameElement == null) return //忽略
                holder.registerProblem(nameElement, PlsBundle.message("inspection.script.incorrectEventNamespace.desc", eventNamespace))
            }
        }
    }
    
    private fun shouldCheckFile(file: PsiFile): Boolean {
        //仅检查事件脚本文件
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path
        return "txt" == filePath.fileExtension && "events".matchesPath(filePath.path)
    }
}
