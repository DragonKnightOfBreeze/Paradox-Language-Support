package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*
import org.jetbrains.kotlin.idea.util.application.*
import java.util.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedLocalisationHandler : GotoTargetHandler() {
	override fun getFeatureUsedKey(): String {
		return "navigation.goto.paradoxRelatedLocalisation"
	}
	
	override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
		val project = file.project
		val element = PsiUtilCore.getElementAtOffset(file, editor.caretModel.offset)
		val definition = element.findParentDefinition() ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val localisationInfos = definitionInfo.localisation
		if(localisationInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
		val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
		val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
			runReadAction {
				for((_, locationExpression) in localisationInfos) {
					val selector = localisationSelector().gameTypeFrom(definition).preferRootFrom(definition).preferLocale(preferredParadoxLocale())
					val (_, localisations) = locationExpression.resolveAll(definitionInfo.name, definition, project, selector = selector) ?: continue
					if(localisations.isNotEmpty()) targets.addAll(localisations)
				}
			}
		}, PlsBundle.message("script.goto.relatedLocalisation.search", definitionInfo.name), true, project)
		if(!runResult) return null
		return GotoData(definition, targets.toTypedArray(), emptyList())
	}
	
	override fun shouldSortTargets(): Boolean {
		return false
	}
	
	override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
		val definitionName = sourceElement.castOrNull<ParadoxDefinitionProperty>()?.definitionInfo?.name ?: anonymousString
		return PlsBundle.message("script.goto.relatedLocalisation.chooseTitle", definitionName.escapeXml())
	}
	
	override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
		val definitionName = sourceElement.castOrNull<ParadoxDefinitionProperty>()?.definitionInfo?.name ?: anonymousString
		return PlsBundle.message("script.goto.relatedLocalisation.findUsagesTitle", definitionName.escapeXml())
	}
	
	override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
		return PlsBundle.message("script.goto.relatedLocalisation.notFoundMessage")
	}
	
	override fun useEditorFont(): Boolean {
		return false
	}
	
	override fun navigateToElement(descriptor: Navigatable) {
		if(descriptor  is PsiElement){
			NavigationUtil.activateFileWithPsiElement(descriptor, true)
		} else {
			descriptor.navigate(true)
		}
	}
}