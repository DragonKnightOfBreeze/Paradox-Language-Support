package icu.windea.pls.script.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.priority.*
import icu.windea.pls.script.psi.*

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
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty) {
                    val definitionInfo = element.definitionInfo
                    if(definitionInfo != null) visitDefinition(element, definitionInfo)
                }
            }
            
            private fun visitDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
                val priority = ParadoxPriorityProvider.getPriority(element)
                if(priority == ParadoxPriority.ORDERED) return
                val selector = definitionSelector(project, file)
                val name = definitionInfo.name
                val type = definitionInfo.type
                if(name.isParameterized()) return //ignored
                val results = ParadoxDefinitionSearch.search(name, type, selector).findAll()
                if(results.size < 2) return //no override, skip
                val firstResult = results.first()
                val firstRootInfo = firstResult.fileInfo?.rootInfo ?: return
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
