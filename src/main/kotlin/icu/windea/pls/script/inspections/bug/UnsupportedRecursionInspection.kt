package icu.windea.pls.script.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * （对于脚本文件）检查是否存在不支持的递归。
 * * 对于每个scripted_variable，检测其值中是否存在递归的scripted_variable引用。
 * * 对于每个scripted_trigger，检测其值中是否存在递归的scripted_trigger调用。
 * * 对于每个scripted_effect，检测其值中是否存在递归的scripted_effect调用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool() {
    //目前仅做检查即可，不需要显示递归的装订线图标
    //在定义声明级别进行此项检查
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if(!isFileToInspect(holder.file)) return PsiElementVisitor.EMPTY_VISITOR
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when(element) {
                    is ParadoxScriptScriptedVariable -> visitScriptedVariable(element)
                    is ParadoxScriptProperty -> visitProperty(element)
                }
            }
            
            private fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                val name = element.name
                if(name.isNullOrEmpty()) return
                
                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveScriptedVariable(element, recursions)
                if(recursions.isEmpty()) return
                val message = PlsBundle.message("inspection.script.unsupportedRecursion.description.1")
                val location = element.scriptedVariableName
                holder.registerProblem(location, message, NavigateToRecursionFix(name, element, recursions))
            }
            
            @Suppress("KotlinConstantConditions")
            private fun visitProperty(element: ParadoxScriptProperty) {
                val definitionInfo = element.definitionInfo ?: return
                val name = definitionInfo.name
                if(name.isEmpty()) return
                val type = definitionInfo.type
                if(type != "scripted_trigger" && type != "scripted_effect") return
                
                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveDefinition(element, recursions) { _, re -> ParadoxPsiManager.isInvocationReference(element, re) }
                if(recursions.isEmpty()) return
                val message = when {
                    definitionInfo.type == "scripted_trigger" -> PlsBundle.message("inspection.script.unsupportedRecursion.description.2.1")
                    definitionInfo.type == "scripted_effect" -> PlsBundle.message("inspection.script.unsupportedRecursion.description.2.2")
                    else -> return
                }
                val location = element.propertyKey
                holder.registerProblem(location, message, NavigateToRecursionFix(name, element, recursions))
            }
        }
    }
    
    private fun isFileToInspect(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.pathToEntry
        return "txt" == filePath.fileExtension && ("common/scripted_triggers".matchesPath(filePath.path) || "common/scripted_effects".matchesPath(filePath.path))
    }
    
    private class NavigateToRecursionFix(key: String, target: PsiElement, recursions: Collection<PsiElement>) : NavigateToFix(key, target, recursions) {
        override fun getText() = PlsBundle.message("inspection.script.unsupportedRecursion.quickFix.1")
        
        override fun getPopupTitle(editor: Editor) =
            PlsBundle.message("inspection.script.unsupportedRecursion.quickFix.1.popup.title", key)
        
        override fun getPopupText(editor: Editor, value: PsiElement) =
            PlsBundle.message("inspection.script.unsupportedRecursion.quickFix.1.popup.text", key, editor.document.getLineNumber(value.textOffset))
    }
}