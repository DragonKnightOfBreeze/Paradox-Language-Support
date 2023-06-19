package icu.windea.pls.script.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.priority.*
import icu.windea.pls.script.psi.*

/**
 * 检查（全局）封装变量的重载是否不正确。（覆盖规则为FIOS）
 */
class IncorrectScriptedVariableOverrideInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val fileInfo = file.fileInfo ?: return PsiElementVisitor.EMPTY_VISITOR
        val isGlobal = "common/scripted_variables".matchesPath(fileInfo.pathToEntry.path)
        if(!isGlobal) return PsiElementVisitor.EMPTY_VISITOR //only for global scripted variables
        val virtualFile = file.virtualFile
        val inProject = virtualFile != null && ProjectFileIndex.getInstance(project).isInContent(virtualFile)
        if(!inProject) return PsiElementVisitor.EMPTY_VISITOR //only for project files
        
        val priority = ParadoxPriorityProvider.getPriority(file)
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptScriptedVariable) {
                    if(element.parent !is ParadoxScriptRootBlock) return
                    visitScriptedVariable(element)
                }
            }
            
            private fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                if(priority == ParadoxPriority.FIOS || priority == ParadoxPriority.LIOS) {
                    val selector = scriptedVariableSelector(project, file)
                    val name = element.name ?: return
                    if(name.isParameterized()) return //ignored
                    val first = ParadoxGlobalScriptedVariableSearch.search(name, selector).findAll().firstOrNull() ?: return
                    val firstRootInfo = first.fileInfo?.rootInfo ?: return
                    val rootInfo = fileInfo.rootInfo
                    if(firstRootInfo.rootFile != rootInfo.rootFile) {
                        //different root file -> incorrect override (for FIOS and LIOS)
                        val locationElement = element.scriptedVariableName
                        holder.registerProblem(locationElement, PlsBundle.message("inspection.script.bug.incorrectScriptedVariableOverride.description", name, priority))
                    }
                }
            }
        }
    }
}

/**
 * 检查（全局）封装变量的重载是否不正确。（覆盖规则为FIOS）
 */
class IncorrectDefinitionOverrideInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val fileInfo = file.fileInfo ?: return PsiElementVisitor.EMPTY_VISITOR
        val virtualFile = file.virtualFile
        val inProject = virtualFile != null && ProjectFileIndex.getInstance(project).isInContent(virtualFile)
        if(!inProject) return PsiElementVisitor.EMPTY_VISITOR //only for project files
        
        val priority = ParadoxPriorityProvider.getPriority(file)
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty && element.definitionInfo != null) {
                    visitDefinition(element)
                }
            }
            
            private fun visitDefinition(element: ParadoxScriptProperty) {
                if(priority == ParadoxPriority.FIOS || priority == ParadoxPriority.LIOS) {
                    val selector = definitionSelector(project, file)
                    val name = element.name
                    if(name.isParameterized()) return //ignored
                    val first = ParadoxDefinitionSearch.search(name, selector).findAll().firstOrNull() ?: return
                    val firstRootInfo = first.fileInfo?.rootInfo ?: return
                    val rootInfo = fileInfo.rootInfo
                    if(firstRootInfo.rootFile != rootInfo.rootFile) {
                        //different root file -> incorrect override (for FIOS and LIOS)
                        val locationElement = element.propertyKey
                        holder.registerProblem(locationElement, PlsBundle.message("inspection.script.bug.incorrectDefinitionOverride.description", name, priority))
                    }
                }
            }
        }
    }
}