package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.ParadoxSnippetManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType

class GotoRelatedDefinitionsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedDefinitions"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement? = null
        run {
            // 定义引用片段（相关定义）
            if (sourceElement != null) return@run
            val element = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (element !is ParadoxDefinitionSnippetLightElement) return@run
            sourceElement = element
            val name = ParadoxPsiPresentationService.getNameForDefinitionSnippet(element) ?: return@run
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedDefinitions.search.s", name.orAnonymous().escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxSnippetManager.getRelatedDefinitions(element))
                }
            }
        }
        run {
            // 正常本地化（相关定义）
            if (sourceElement != null) return@run
            val element = findElement(file, offset) ?: return@run
            if (!ParadoxPsiMatchService.isLocalisation(element, ParadoxLocalisationType.Normal)) return@run
            sourceElement = element
            val name = ParadoxPsiPresentationService.getNameForLocalisation(element) ?: return@run
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedDefinitions.search.l", name.orAnonymous().escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxLocalisationManager.getRelatedDefinitions(element))
                }
            }
        }
        if (targets.isEmpty() || sourceElement == null) return null // unavailable
        targets.removeIf { it == sourceElement } // remove current target from targets
        return GotoData(sourceElement, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFileService.findLocalisation(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinitionSnippet(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedDefinitions.chooseTitle.s", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedDefinitions.chooseTitle.l", name.orAnonymous().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedDefinitions.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinitionSnippet(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedDefinitions.findUsagesTitle.s", name.orAnonymous().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedDefinitions.findUsagesTitle.l", name.orAnonymous().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedDefinitions.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedDefinitions.notFoundMessage")
    }
}
