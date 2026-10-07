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
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.ParadoxSnippetManager
import icu.windea.pls.model.ParadoxLocalisationType

class GotoRelatedDefinitionsHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedDefinitions"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        run {
            // 定义引用片段（相关定义）
            val sourceElement = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (sourceElement !is ParadoxDefinitionSnippetLightElement) return@run
            val name = ParadoxPsiPresentationService.getNameForDefinitionSnippet(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedDefinitions.search.s", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxSnippetManager.getRelatedDefinitions(sourceElement))
                }
            }
            return getGotoData(sourceElement, targets)
        }
        run {
            // 普通本地化（相关定义）
            val sourceElement = ParadoxPsiFileService.findLocalisation(file, offset) ?: return@run
            if (!ParadoxPsiMatchService.isLocalisation(sourceElement, ParadoxLocalisationType.Normal)) return@run
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedDefinitions.search.l", name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxLocalisationManager.getRelatedDefinitions(sourceElement))
                }
            }
            return getGotoData(sourceElement, targets)
        }
        return null
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinitionSnippet(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedDefinitions.chooseTitle.s", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedDefinitions.chooseTitle.l", name.orUnresolved().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedDefinitions.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForDefinitionSnippet(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedDefinitions.findUsagesTitle.s", name.orUnresolved().escapeXml())
        }
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedDefinitions.findUsagesTitle.l", name.orUnresolved().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedDefinitions.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedDefinitions.notFoundMessage")
    }
}
