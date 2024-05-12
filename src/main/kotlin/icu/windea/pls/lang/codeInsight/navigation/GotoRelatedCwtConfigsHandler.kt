package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.config.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

class GotoRelatedCwtConfigsHandler : GotoTargetHandler() {
	override fun getFeatureUsedKey(): String {
		return "navigation.goto.paradoxRelatedCwtConfigs"
	}
    
    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
		//possible for any element in script or localisation files (but related CWT configs may not exist)
		
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) {
			it.takeIf { e -> e !is PsiWhiteSpace && e !is PsiComment }
		} ?: return null
        val relatedConfigs = CwtRelatedConfigProvider.getRelatedConfigs(file, offset)
        val targets = relatedConfigs.mapNotNull { it.pointer.element }
        return GotoData(element, targets.distinct().toTypedArray(), emptyList())
    }
	
	override fun shouldSortTargets(): Boolean {
		return false
	}
	
	override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
		return PlsBundle.message("script.goto.relatedCwtConfigs.chooseTitle")
	}
	
	override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
		return PlsBundle.message("script.goto.relatedCwtConfigs.findUsagesTitle")
	}
	
	override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
		return PlsBundle.message("script.goto.relatedCwtConfigs.notFoundMessage")
	}
	
	override fun navigateToElement(descriptor: Navigatable) {
		if(descriptor is PsiElement) {
			activateFileWithPsiElement(descriptor, true)
		} else {
			descriptor.navigate(true)
		}
	}
}
