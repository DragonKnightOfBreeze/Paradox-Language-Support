package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*

@Suppress("DialogTitleCapitalization")
class GotoFilesHandler: GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxFiles"
    }
    
    override fun getSourceAndTargetElements(editor: Editor?, file: PsiFile?): GotoData? {
        TODO("Not yet implemented")
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
