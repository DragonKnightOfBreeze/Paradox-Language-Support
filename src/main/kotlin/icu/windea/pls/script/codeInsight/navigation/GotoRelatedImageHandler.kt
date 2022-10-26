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
import icu.windea.pls.script.psi.*
import java.util.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedImageHandler : GotoTargetHandler() {
	override fun getFeatureUsedKey(): String {
		return "navigation.goto.paradoxRelatedImage"
	}
	
	override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
		val project = file.project
		val element = PsiUtilCore.getElementAtOffset(file, editor.caretModel.offset).getSelfOrPrevSiblingNotWhitespace()
		val definition = element.findParentDefinition() ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val imageInfos = definitionInfo.images
		if(imageInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
		val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
		val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
			//need read action here
			runReadAction {
				for((_, locationExpression) in imageInfos) {
					ProgressManager.checkCanceled()
					val (_, files) = locationExpression.resolveAll(definition, definitionInfo, project) ?: continue
					if(files.isNotEmpty()) targets.addAll(files)
				}
			}
		}, PlsBundle.message("script.goto.relatedImage.search", definitionInfo.name), true, project)
		if(!runResult) return null
		return GotoData(definition, targets.toTypedArray(), emptyList())
	}
	
	override fun shouldSortTargets(): Boolean {
		return false
	}
	
	override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
		val definitionName = sourceElement.castOrNull<ParadoxDefinitionProperty>()?.definitionInfo?.name ?: unknownString
		return PlsBundle.message("script.goto.relatedImage.chooseTitle", definitionName.escapeXml())
	}
	
	override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
		val definitionName = sourceElement.castOrNull<ParadoxDefinitionProperty>()?.definitionInfo?.name ?: unknownString
		return PlsBundle.message("script.goto.relatedImage.findUsagesTitle", definitionName)
	}
	
	override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
		return PlsBundle.message("script.goto.relatedImage.notFoundMessage")
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