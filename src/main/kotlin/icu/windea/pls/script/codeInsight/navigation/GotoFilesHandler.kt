package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import java.util.*

@Suppress("DialogTitleCapitalization")
class GotoFilesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxFiles"
    }
    
    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path.path
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
            //need read action here
            runReadAction {
                val selector = fileSelector(project, file).contextSensitive()
                val resolved = ParadoxFilePathSearch.search(path, null, selector, ignoreLocale = true).findAll()
                resolved.forEach { targets.add(it.toPsiFile(project)) }
            }
        }, PlsBundle.message("script.goto.files.search", file.name), true, project)
        if(!runResult) return null
        return GotoData(file, targets.toTypedArray(), emptyList())
    }
    
    override fun shouldSortTargets(): Boolean {
        return false
    }
    
    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val fileName = sourceElement.castOrNull<PsiFile>()?.name ?: return ""
        return PlsBundle.message("script.goto.files.chooseTitle", fileName.escapeXml())
    }
    
    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val fileName = sourceElement.castOrNull<PsiFile>()?.name ?: return ""
        return PlsBundle.message("script.goto.files.findUsagesTitle", fileName.escapeXml())
    }
    
    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.files.notFoundMessage")
    }
    
    override fun navigateToElement(descriptor: Navigatable) {
        if(descriptor is PsiElement) {
            NavigationUtil.activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
