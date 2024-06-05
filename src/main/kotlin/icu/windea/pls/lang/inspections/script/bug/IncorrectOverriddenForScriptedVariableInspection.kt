package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.priority.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * （对于脚本文件）检查是否存在不正确的对（全局）封装的重载。
 */
class IncorrectOverriddenForScriptedVariableInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val fileInfo = file.fileInfo ?: return PsiElementVisitor.EMPTY_VISITOR
        val isGlobal = "common/scripted_variables".matchesPath(fileInfo.pathToEntry.path)
        if(!isGlobal) return PsiElementVisitor.EMPTY_VISITOR //only for global scripted variables
        val virtualFile = file.virtualFile
        val inProject = virtualFile != null && ProjectFileIndex.getInstance(project).isInContent(virtualFile)
        if(!inProject) return PsiElementVisitor.EMPTY_VISITOR //only for project files
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptScriptedVariable) {
                    if(element.parent !is ParadoxScriptRootBlock) return
                    visitScriptedVariable(element)
                }
            }
            
            private fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                val priority = ParadoxPriorityProvider.getPriority(element)
                if(priority == ParadoxPriority.ORDERED) return //only for FIOS and LIOS
                val selector = scriptedVariableSelector(project, file)
                val name = element.name ?: return
                if(name.isParameterized()) return ////parameterized -> ignored
                val results = ParadoxGlobalScriptedVariableSearch.search(name, selector).findAll()
                if(results.size < 2) return //no override -> skip
                val firstResult = results.first()
                val firstRootInfo = firstResult.fileInfo?.rootInfo ?: return
                val rootInfo = fileInfo.rootInfo
                if(firstRootInfo.rootFile != rootInfo.rootFile) {
                    //different root file -> incorrect override
                    val locationElement = element.scriptedVariableName
                    val message = PlsBundle.message("inspection.script.incorrectOverriddenForScriptedVariable.description", name, priority)
                    val fix = NavigateToOverriddenScriptedVariablesFix(name, element, results)
                    holder.registerProblem(locationElement, message, fix)
                }
            }
        }
    }
    
    private class NavigateToOverriddenScriptedVariablesFix(key: String, element: PsiElement, scriptedVariables: Collection<PsiElement>) : NavigateToFix(key, element, scriptedVariables) {
        override fun getText() = PlsBundle.message("inspection.script.incorrectOverriddenForScriptedVariable.fix.1")
        
        override fun getPopupTitle(editor: Editor) =
            PlsBundle.message("inspection.script.incorrectOverriddenForScriptedVariable.fix.1.popup.title", key)
        
        override fun getPopupText(editor: Editor, value: PsiElement) =
            PlsBundle.message("inspection.script.incorrectOverriddenForScriptedVariable.fix.1.popup.text", key, editor.document.getLineNumber(value.textOffset))
    }
}

