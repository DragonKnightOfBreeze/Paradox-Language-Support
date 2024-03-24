package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedCwtConfigsHandler : GotoTargetHandler() {
	override fun getFeatureUsedKey(): String {
		return "navigation.goto.paradoxRelatedCwtConfigs"
	}
    
    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        val relatedConfigs = CwtRelatedConfigProvider.getRelatedConfigs(element)
        val targets = relatedConfigs.mapNotNull { it.pointer.element }
        return GotoData(element, targets.toTypedArray(), emptyList())
    }
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
		return ParadoxPsiManager.findScriptExpression(file, offset)
	}
	
	override fun shouldSortTargets(): Boolean {
		return false
	}
	
	override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
		val expression = sourceElement.castOrNull<ParadoxTypedElement>()?.expression ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedCwtConfigs.chooseTitle", expression.escapeXml())
	}
	
	override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
		val expression = sourceElement.castOrNull<ParadoxTypedElement>()?.expression ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedCwtConfigs.findUsagesTitle", expression)
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
