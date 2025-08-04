package icu.windea.pls.lang.inspections

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.*

//com.intellij.lang.properties.codeInspection.PropertiesInspectionSuppressor
//org.intellij.grammar.inspection.BnfInspectionSuppressor

/**
 * 基于特定条件，禁用适用于CSV文件的代码检查。
 */
class ParadoxCsvInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val file = element.containingFile
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
}
