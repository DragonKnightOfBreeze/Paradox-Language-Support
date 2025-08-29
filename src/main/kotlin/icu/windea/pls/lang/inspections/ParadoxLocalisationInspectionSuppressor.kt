package icu.windea.pls.lang.inspections

import com.intellij.codeInsight.daemon.impl.actions.SuppressByCommentFix
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constants.PlsStringConstants

//com.intellij.lang.properties.codeInspection.PropertiesInspectionSuppressor
//org.intellij.grammar.inspection.BnfInspectionSuppressor

/**
 * 基于特定条件，禁用适用于本地化文件的代码检查。
 */
class ParadoxLocalisationInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val propertyElement = element.parentOfType<ParadoxLocalisationProperty>()
        if (propertyElement != null && ParadoxInspectionManager.isSuppressedInComment(propertyElement, toolId)) return true
        val file = (propertyElement ?: element).containingFile
        if (file != null && ParadoxInspectionManager.isSuppressedInComment(file, toolId)) return true
        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        if (element == null) return SuppressQuickFix.EMPTY_ARRAY
        return buildList {
            run {
                val file = element.containingFile ?: return@run
                val fileName = file.name
                add(SuppressForFileFix(SuppressionUtil.ALL, fileName))
                add(SuppressForFileFix(toolId, fileName))
            }
            add(SuppressForPropertyFix(toolId))
        }.toTypedArray()
    }

    private class SuppressForFileFix(
        private val toolId: String,
        private val fileName: String
    ) : SuppressByCommentFix(toolId, ParadoxLocalisationFile::class.java) {
        override fun getText(): String {
            return when (toolId) {
                SuppressionUtil.ALL -> PlsBundle.message("suppress.for.file.all", fileName)
                else -> PlsBundle.message("suppress.for.file", fileName)
            }
        }

        override fun getCommentsFor(container: PsiElement): List<PsiElement> {
            return ParadoxInspectionManager.getCommentsForSuppression(container).toList()
        }

        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            if (container !is PsiFile) return
            val text = PlsStringConstants.suppressInspectionsTagName + " " + myID
            val comment = SuppressionUtil.createComment(project, text, ParadoxLocalisationLanguage)
            container.addAfter(comment, null)
        }
    }

    private class SuppressForPropertyFix(
        toolId: String
    ) : SuppressByCommentFix(toolId, ParadoxLocalisationProperty::class.java) {
        override fun getText(): String {
            return PlsBundle.message("suppress.for.property")
        }

        override fun getCommentsFor(container: PsiElement): List<PsiElement> {
            return ParadoxInspectionManager.getCommentsForSuppression(container).toList()
        }

        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            super.createSuppression(project, element, container)
        }
    }
}
