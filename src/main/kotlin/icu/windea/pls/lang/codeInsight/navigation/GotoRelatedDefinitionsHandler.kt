package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.codeInsight.navigation.activateFileWithPsiElement
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.escapeXml
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import java.util.*

class GotoRelatedDefinitionsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedDefinitions"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        val localisation = element
        if (localisation.type != ParadoxLocalisationType.Normal) return null
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
            //need read action here
            runReadAction {
                val resolved = ParadoxLocalisationManager.getRelatedDefinitions(localisation)
                targets.addAll(resolved)
            }
        }, PlsBundle.message("script.goto.relatedDefinitions.search", localisation.name), true, project)
        if (!runResult) return null
        if (targets.isNotEmpty()) targets.removeIf { it == localisation }
        return GotoData(localisation, targets.distinct().toTypedArray(), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFinder.findLocalisation(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val localisationName = sourceElement.castOrNull<ParadoxLocalisationProperty>()?.name ?: return ""
        return PlsBundle.message("script.goto.relatedDefinitions.chooseTitle", localisationName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val localisationName = sourceElement.castOrNull<ParadoxLocalisationProperty>()?.name ?: return ""
        return PlsBundle.message("script.goto.relatedDefinitions.findUsagesTitle", localisationName.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.relatedDefinitions.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
