package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.findElementAt
import icu.windea.pls.lang.resolve.ParadoxConfigService

class GotoRelatedConfigsHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedConfigs"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        // possible for any element in script or localisation files (but related CWT configs may not exist)

        val offset = editor.caretModel.offset
        val sourceElement = file.findElementAt(offset) {
            it.takeIf { e -> e !is PsiWhiteSpace && e !is PsiComment }
        } ?: return null
        val relatedConfigs = ParadoxConfigService.getRelatedConfigs(file, offset)
        if (relatedConfigs.isEmpty()) return null // unavailable
        val targets = relatedConfigs.mapNotNullTo(mutableListOf()) { it.pointer.element }
        if (targets.isEmpty()) return null // unavailable
        return getGotoData(sourceElement, targets, removeSource = false)
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        return ChronicleBundle.message("script.goto.relatedConfigs.chooseTitle")
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        return ChronicleBundle.message("script.goto.relatedConfigs.findUsagesTitle")
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedConfigs.notFoundMessage")
    }
}
