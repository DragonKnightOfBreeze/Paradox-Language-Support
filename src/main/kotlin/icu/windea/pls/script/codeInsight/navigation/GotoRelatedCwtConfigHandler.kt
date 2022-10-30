package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.script.psi.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedCwtConfigHandler : GotoTargetHandler() {
	override fun getFeatureUsedKey(): String {
		return "navigation.goto.paradoxRelatedCwtConfig"
	}
	
	override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
		val element = PsiUtilCore.getElementAtOffset(file, editor.caretModel.offset).getSelfOrPrevSiblingNotWhitespace()
		val location = element.parentOfTypes(ParadoxScriptPropertyKey::class, ParadoxScriptValue::class) ?: return null
		//获取所有匹配的CWT规则，不存在匹配的CWT规则时，不选用默认的（仅匹配property的key但不匹配property的value的）CWT规则
		//包括内联的和未被内联的（即alias或single_alias，显示时使用特殊的别名图标）规则
		val configType = if(location is ParadoxScriptPropertyKey) CwtPropertyConfig::class.java else CwtValueConfig::class.java
		val allowDefinitionSelf = location !is ParadoxScriptPropertyKey
		val configs = ParadoxCwtConfigHandler.resolveConfigs(location, configType, allowDefinitionSelf, false)
		val targets = buildSet {
			for(config in configs) {
				config.resolvedOrNull()?.pointer?.element?.let { add(it) }
				config.pointer.element?.let { add(it) }
			}
		}
		return GotoData(location, targets.toTypedArray(), emptyList())
	}
	
	override fun shouldSortTargets(): Boolean {
		return false
	}
	
	override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
		val expression = sourceElement.castOrNull<ParadoxScriptTypedElement>()?.configExpression ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedCwtConfig.chooseTitle", expression.escapeXml())
	}
	
	override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
		val expression = sourceElement.castOrNull<ParadoxScriptTypedElement>()?.configExpression ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedCwtConfig.findUsagesTitle", expression)
	}
	
	override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
		return PlsBundle.message("script.goto.relatedCwtConfig.notFoundMessage")
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