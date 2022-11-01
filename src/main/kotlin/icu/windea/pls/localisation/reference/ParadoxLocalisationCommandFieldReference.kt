package icu.windea.pls.localisation.reference

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxCommandFieldCompletionProvider
 */
class ParadoxLocalisationCommandFieldReference(
	element: ParadoxLocalisationCommandField,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		throw IncorrectOperationException() //不允许重命名
	}
	
	//TODO
	
	override fun resolve(): PsiElement? {
		val name = element.name
		val project = element.project
		//尝试识别为预定义的localisation_command
		doResolveLocalisationCommand(name, project)?.let { return it }
		
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return null
		//尝试识别为<scripted_loc>
		val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
		findDefinitionByType(name, "scripted_loc", project, selector = selector)?.let { return it }
		//尝试识别为value[variable]
		val selector1 = valueSetValueSelector().gameType(gameType).preferRootFrom(element)
		val eventTarget = ParadoxValueSetValuesSearch.search(name, "variable", project, selector = selector1).findFirst()
		if(eventTarget != null) return ParadoxValueSetValueElement(element, name, "variable", project , gameType)
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		val name = element.name
		val project = element.project
		//尝试识别为预定义的localisation_command
		doResolveLocalisationCommand(name, project)?.let { return arrayOf(PsiElementResolveResult(it)) }
		
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return ResolveResult.EMPTY_ARRAY
		//尝试识别为<scripted_loc>
		val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
		findDefinitionsByType(name, "scripted_loc", project, selector = selector).takeIfNotEmpty()?.let { return it.mapToArray { e -> PsiElementResolveResult(e) }}
		//尝试识别为value[variable]
		val selector1 = valueSetValueSelector().gameType(gameType).preferRootFrom(element)
		val eventTarget = ParadoxValueSetValuesSearch.search(name, "variable", project, selector = selector1).findFirst()
		if(eventTarget != null) return ParadoxValueSetValueElement(element, name, "variable", project , gameType).let { return arrayOf(PsiElementResolveResult(it)) }
		return ResolveResult.EMPTY_ARRAY
	}
	
	private fun doResolveLocalisationCommand(name: String, project: Project): PsiElement? {
		val gameType = element.fileInfo?.rootInfo?.gameType ?: return null
		val configGroup = getCwtConfig(project).get(gameType) ?: return null
		return CwtConfigHandler.resolveLocalisationCommand(name, configGroup)
	}
}
