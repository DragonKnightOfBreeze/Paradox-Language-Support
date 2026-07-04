package icu.windea.pls.lang.inspections.suppress

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.lang.inspections.ChronicleSuppressionUtil
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.constants.ChronicleConstants

/**
 * 基于特定条件，禁用适用于CSV文件的代码检查。
 */
class ParadoxCsvInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val file = element.containingFile
        if (file != null && ChronicleSuppressionUtil.isSuppressedInComment(file, toolId)) return true
        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        if (element == null) return SuppressQuickFix.EMPTY_ARRAY
        val file = element.containingFile ?: return SuppressQuickFix.EMPTY_ARRAY
        return buildList {
            run {
                val fileName = file.name
                this += SuppressForFileFix(SuppressionUtil.ALL, fileName)
                this += SuppressForFileFix(toolId, fileName)
            }
        }.toArray(SuppressQuickFix.EMPTY_ARRAY)
    }

    private class SuppressForFileFix(
        private val toolId: String,
        private val fileName: String
    ) : ParadoxSuppressByCommentFix(toolId, ParadoxLocalisationFile::class.java) {
        override fun getText(): String {
            return when (toolId) {
                SuppressionUtil.ALL -> ChronicleBundle.message("suppress.for.file.all", fileName)
                else -> ChronicleBundle.message("suppress.for.file", fileName)
            }
        }

        override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
            if (container !is PsiFile) return
            val text = ChronicleConstants.suppressInspectionsTagName + " " + myID
            val comment = SuppressionUtil.createComment(project, text, ParadoxLocalisationLanguage)
            container.addAfter(comment, null)
        }
    }
}
