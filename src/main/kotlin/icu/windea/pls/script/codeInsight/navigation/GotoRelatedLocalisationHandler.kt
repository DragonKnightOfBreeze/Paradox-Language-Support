package icu.windea.pls.script.codeInsight.navigation

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
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*
import java.util.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedLocalisationHandler : GotoTargetHandler() {
	override fun getFeatureUsedKey(): String {
		return "navigation.goto.paradoxRelatedLocalisation"
	}
	
	override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
		val project = file.project
		val offset = editor.caretModel.offset
		val element = findElement(file, offset) ?: return null
		val definition = element.findParentDefinition() ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val localisationInfos = definitionInfo.localisation
		if(localisationInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
		val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
		val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
			//need read action here
			runReadAction {
				for((_, locationExpression) in localisationInfos) {
					ProgressManager.checkCanceled()
					val selector = localisationSelector().gameTypeFrom(definition).preferRootFrom(definition).preferLocale(preferredParadoxLocale())
					val (_, localisations) = locationExpression.resolveAll(definitionInfo.name, definition, project, selector = selector) ?: continue
					if(localisations.isNotEmpty()) targets.addAll(localisations)
				}
			}
		}, PlsBundle.message("script.goto.relatedLocalisation.search", definitionInfo.name), true, project)
		if(!runResult) return null
		return GotoData(definition, targets.toTypedArray(), emptyList())
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
		//direct parent
		return file.findElementAtCaret(offset) {
			it.parent as? ParadoxScriptExpressionElement
		}?.takeIf { it.isExpressionElement() }
	}
	
	override fun shouldSortTargets(): Boolean {
		return false
	}
	
	override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
		val definitionName = sourceElement.castOrNull<ParadoxDefinitionProperty>()?.definitionInfo?.name ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedLocalisation.chooseTitle", definitionName.escapeXml())
	}
	
	override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
		val definitionName = sourceElement.castOrNull<ParadoxDefinitionProperty>()?.definitionInfo?.name ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedLocalisation.findUsagesTitle", definitionName)
	}
	
	override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
		return PlsBundle.message("script.goto.relatedLocalisation.notFoundMessage")
	}
	
	override fun useEditorFont(): Boolean {
		return false
	}
	
	override fun navigateToElement(descriptor: Navigatable) {
		if(descriptor is PsiElement) {
			NavigationUtil.activateFileWithPsiElement(descriptor, true)
		} else {
			descriptor.navigate(true)
		}
	}
}
