package icu.windea.pls.core.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.localisation.psi.*
import java.util.*

@Suppress("DialogTitleCapitalization")
class GotoLocalisationsHandler: GotoTargetHandler() {
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
                val selector = localisationSelector(project, localisation).contextSensitive().preferLocale(preferredParadoxLocale())
                val resolved = ParadoxLocalisationSearch.search(localisationInfo.name, selector).findAll()
                targets.addAll(resolved)
            }
        }, PlsBundle.message("script.goto.localisations.search", localisationInfo.name), true, project)
        if(!runResult) return null
        return GotoData(localisation, targets.toTypedArray(), emptyList())
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return file.findElementAt(offset) {
            it.parentOfType<ParadoxLocalisationProperty>()
        }
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
        if(descriptor is PsiElement) {
            NavigationUtil.activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}