package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxLocalisationType

class GotoRelatedScriptedVariablesHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedScriptedVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        run {
            if (file !is ParadoxLocalisationFile) return@run
            val sourceElement = ParadoxPsiFileService.findLocalisation(file, offset) ?: return@run
            if (!ParadoxPsiMatchService.isLocalisation(sourceElement, ParadoxLocalisationType.Normal)) return@run
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedScriptedVariables.search.l", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    val resolved = ParadoxLocalisationManager.getRelatedScriptedVariables(sourceElement)
                    targets.addAll(resolved)
                }
            }
            return getGotoData(sourceElement, targets)
        }
        return null
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedScriptedVariables.chooseTitle.l", name.orUnresolved().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedScriptedVariables.findUsagesTitle.l", name.orUnresolved().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.notFoundMessage")
    }
}
