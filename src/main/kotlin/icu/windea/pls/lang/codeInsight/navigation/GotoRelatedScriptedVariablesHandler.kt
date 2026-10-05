package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType

class GotoRelatedScriptedVariablesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedScriptedVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = ParadoxPsiFileService.findLocalisation(file, offset) ?: return null
        if (!ParadoxPsiMatchService.isLocalisation(element, ParadoxLocalisationType.Normal)) return null
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedScriptedVariables.search.l", element.name)) {
            // need read actions here if necessary
            readAction {
                val resolved = ParadoxLocalisationManager.getRelatedScriptedVariables(element)
                targets.addAll(resolved)
            }
        }
        if (targets.isNotEmpty()) targets.removeIf { it == element }
        return GotoData(element, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val localisationName = sourceElement.castOrNull<ParadoxLocalisationProperty>()?.name ?: return ""
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.chooseTitle.l", localisationName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedScriptedVariables.findUsagesTitle.l", name.escapeXml().or.anonymous())
        }
        if (name == null) return ""
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.findUsagesTitle.l", name.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.notFoundMessage")
    }
}
