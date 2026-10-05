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
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
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
        // 定义引用片段（相关定义）
        run {
            val resolved = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (resolved !is ParadoxDefinitionSnippetLightElement) return@run
            val targets = mutableListOf<PsiElement>()
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedDefinitions.search", resolved.name.escapeXml())) {
                // need read actions here if necessary
                readAction {
                    targets.addAll(ParadoxSnippetManager.getRelatedDefinitions(resolved))
                }
            }
            return GotoData(resolved.parent, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
        }
        // 正常本地化（相关定义）
        val element = findElement(file, offset) ?: return null
        if (!ParadoxPsiMatchService.isLocalisation(element, ParadoxLocalisationType.Normal)) return null
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedDefinitions.search", element.name)) {
            // need read actions here if necessary
            readAction {
                val resolved = ParadoxLocalisationManager.getRelatedDefinitions(element)
                targets.addAll(resolved)
            }
        }
        if (targets.isNotEmpty()) targets.removeIf { it == element }
        return GotoData(element, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFileService.findLocalisation(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val sourceName = sourceElement.castOrNull<ParadoxLocalisationProperty>()?.name ?: sourceElement.text.unquote()
        return ChronicleBundle.message("script.goto.relatedDefinitions.chooseTitle", sourceName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val sourceName = sourceElement.castOrNull<ParadoxLocalisationProperty>()?.name ?: sourceElement.text.unquote()
        return ChronicleBundle.message("script.goto.relatedDefinitions.findUsagesTitle", sourceName.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedDefinitions.notFoundMessage")
    }
}
