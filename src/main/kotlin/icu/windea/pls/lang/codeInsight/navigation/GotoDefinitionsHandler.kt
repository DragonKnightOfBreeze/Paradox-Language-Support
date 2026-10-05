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
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.select.selectScope

class GotoDefinitionsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefinitions"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement?
        val element = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return null
        if (!element.isDefinitionTypeKeyOrName()) return null
        val definition = selectScope { element.parentDefinition() } ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        if (definitionInfo.name.isEmpty()) return null // 排除匿名定义
        sourceElement = definition
        val name = ParadoxPsiPresentationService.getNameForDefinition(definition) ?: return null
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.definitions.search", name.orAnonymous().escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxDefinitionSearch.selector(project, definition).contextSensitive()
                val resolved = ParadoxDefinitionSearch.searchElement(definitionInfo.name, definitionInfo.type, selector).findAll()
                targets.addAll(resolved)
            }
        }
        if (targets.isEmpty()) return null // unavailable
        targets.removeIf { it == sourceElement } // remove current target from targets
        return GotoData(sourceElement, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.definitions.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.definitions.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.definitions.notFoundMessage")
    }
}
