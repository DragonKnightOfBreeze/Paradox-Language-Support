package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import java.util.*

@Suppress("DialogTitleCapitalization")
class GotoLocalisationsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        val localisation = element
        val localisationInfo = localisation.localisationInfo ?: return null
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
            //need read action here
            runReadAction {
                val selector = selector(project, localisation).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                val resolved = ParadoxLocalisationSearch.search(localisationInfo.name, selector).findAll()
                targets.addAll(resolved)
            }
        }, PlsBundle.message("script.goto.localisations.search", localisationInfo.name), true, project)
        if (!runResult) return null
        if (targets.isNotEmpty()) targets.removeIf { it == localisation }
        return GotoData(localisation, targets.distinct().toTypedArray(), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiManager.findLocalisation(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val localisationName = sourceElement.castOrNull<ParadoxLocalisationProperty>()?.name ?: return ""
        return PlsBundle.message("script.goto.localisations.chooseTitle", localisationName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val localisationName = sourceElement.castOrNull<ParadoxLocalisationProperty>()?.name ?: return ""
        return PlsBundle.message("script.goto.localisations.findUsagesTitle", localisationName.escapeXml())
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
