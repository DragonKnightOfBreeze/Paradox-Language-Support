package icu.windea.pls.core.inspections

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import org.intellij.grammar.*

//com.intellij.lang.properties.codeInspection.PropertiesInspectionSuppressor
//org.intellij.grammar.inspection.BnfInspectionSuppressor

/**
 * 基于特定位置的特定注释过滤代码检查。
 */
class ParadoxLocalisationInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val propertyElement = element.parentOfType<ParadoxLocalisationProperty>()
        if(propertyElement != null && isSuppressedInComment(propertyElement, toolId)) return true
        val file = (propertyElement ?: element).containingFile
        if(file != null && isSuppressedInComment(file, toolId)) return true
        return false
    }
    
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        if(element == null) return SuppressQuickFix.EMPTY_ARRAY
        return buildList {
            val fileName = element.containingFile?.name
            if(fileName != null) add(SupressForFileFix(toolId, fileName))
            add(SupressForPropertyFix(toolId))
        }.toTypedArray()
    }
    
    private class SupressForFileFix(
        private val toolId: String,
        private val fileName: String
    ) : SuppressByCommentFix(toolId, ParadoxLocalisationFile::class.java) {
        override fun getText(): String {
            return PlsBundle.message("localisation.supress.for.file", fileName)
        }
        
        override fun getCommentsFor(container: PsiElement): List<PsiElement>? {
            return getCommentsForSuppression(container).toList()
        }
        
        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            if(container is PsiFile) {
                val text = SuppressionUtilCore.SUPPRESS_INSPECTIONS_TAG_NAME + " " + myID
                val comment = SuppressionUtil.createComment(project, text, BnfLanguage.INSTANCE)
                container.addAfter(comment, null)
            }
        }
    }
    
    private class SupressForPropertyFix(
        private val toolId: String
    ) : SuppressByCommentFix(toolId, ParadoxLocalisationProperty::class.java) {
        override fun getText(): String {
            return PlsBundle.message("localisation.supress.for.property")
        }
        
        override fun getCommentsFor(container: PsiElement): List<PsiElement>? {
            return getCommentsForSuppression(container).toList()
        }
        
        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            super.createSuppression(project, element, container)
        }
    }
}