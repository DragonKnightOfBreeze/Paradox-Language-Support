package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.codeInsight.navigation.activateFileWithPsiElement
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.codeInsight.ParadoxTargetInfo
import java.util.*

class GotoLocalisationsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        val type = element.type ?: return null
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        runWithModalProgressBlocking(project, PlsBundle.message("script.goto.localisations.search", element.name)) {
            // need read actions here if necessary
            readAction {
                val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                val resolved = when (type) {
                    ParadoxLocalisationType.Normal -> ParadoxLocalisationSearch.search(element.name, selector).findAll()
                    ParadoxLocalisationType.Synced -> ParadoxSyncedLocalisationSearch.search(element.name, selector).findAll()
                }
                targets.addAll(resolved)
            }
        }
        if (targets.isNotEmpty()) targets.removeIf { it == element }
        return GotoData(element, targets.distinct().toTypedArray(), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFinder.findLocalisation(file, offset) { BY_NAME }
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxTargetInfo.from(sourceElement)?.name ?: return ""
        return PlsBundle.message("script.goto.localisations.chooseTitle", name.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxTargetInfo.from(sourceElement)?.name ?: return ""
        return PlsBundle.message("script.goto.localisations.findUsagesTitle", name.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.localisations.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
