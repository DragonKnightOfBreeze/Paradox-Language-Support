package icu.windea.pls.core.inspections

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 基于特定位置的特定注释过滤代码检查。
 */
class ParadoxScriptInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        var current = element
        while(current !is PsiFile) {
            current = current.parent ?: return false
            if(current is ParadoxScriptProperty || (current is ParadoxScriptValue && current.isBlockValue())) {
                if(isSuppressedInComment(current, toolId)) return true
            }
        }
        if(isSuppressedInComment(current, toolId)) return true
        return false
    }
    
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        if(element == null) return SuppressQuickFix.EMPTY_ARRAY
        return buildList {
            val fileName = element.containingFile?.name
            if(fileName != null) {
                add(SuppressForFileFix(SuppressionUtil.ALL, fileName))
                add(SuppressForFileFix(toolId, fileName))
            }
            var current: PsiElement? = element
            var level = 1
            while(current != null) {
                current = current.findParentDefinition() as? ParadoxScriptProperty
                if(current != null) {
                    val definitionName = current.definitionInfo?.name
                    if(definitionName != null) {
                        add(SuppressForDefinitionFix(toolId, definitionName, level))
                        level++
                    }
                    current = current.parent
                }
            }
            add(SuppressForExpressionFix(toolId))
        }.toTypedArray()
    }
    
    private class SuppressForFileFix(
        private val toolId: String,
        private val fileName: String
    ) : SuppressByCommentFix(toolId, ParadoxScriptFile::class.java) {
        override fun getText(): String {
            if(toolId == SuppressionUtil.ALL) {
                return PlsBundle.message("script.supress.for.file.all", fileName)
            } else {
                return PlsBundle.message("script.supress.for.file", fileName)
            }
        }
        
        override fun getCommentsFor(container: PsiElement): MutableList<out PsiElement>? {
            return getCommentsForSuppression(container).toMutableList()
        }
        
        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            if(container is PsiFile) {
                val text = SuppressionUtilCore.SUPPRESS_INSPECTIONS_TAG_NAME + " " + myID
                val comment = SuppressionUtil.createComment(project, text, ParadoxScriptLanguage)
                container.addAfter(comment, null)
            }
        }
    }
    
    private class SuppressForDefinitionFix(
        private val toolId: String,
        private val definitionName: String,
        private val depth: Int,
    ) : SuppressByCommentFix(toolId, ParadoxScriptProperty::class.java) {
        //definiton here should be a property, not file
        
        override fun getText(): String {
            return PlsBundle.message("script.supress.for.definition", definitionName)
        }
        
        override fun getContainer(context: PsiElement?): PsiElement? {
            if(context == null) return null
            var current: PsiElement = context
            for(i in 1..depth) {
                current = current.findParentDefinition() as? ParadoxScriptProperty ?: return null
                current = current.parent
            }
            return current
        }
        
        override fun getCommentsFor(container: PsiElement): List<PsiElement>? {
            return getCommentsForSuppression(container).toList()
        }
        
        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            super.createSuppression(project, element, container)
        }
    }
    
    private class SuppressForExpressionFix(
        private val toolId: String
    ) : SuppressByCommentFix(toolId, ParadoxScriptMemberElement::class.java) {
        //here just call scriptMemberElement (property / value) "expression"
        
        override fun getText(): String {
            return PlsBundle.message("script.supress.for.expression")
        }
        
        override fun getContainer(context: PsiElement?): PsiElement? {
            if(context == null) return null
            return context.parents(true)
                .find { it is ParadoxScriptProperty || (it is ParadoxScriptValue && it.isBlockValue()) }
        }
        
        override fun getCommentsFor(container: PsiElement): List<PsiElement>? {
            return getCommentsForSuppression(container).toList()
        }
        
        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            super.createSuppression(project, element, container)
        }
    }
}

