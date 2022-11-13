package icu.windea.pls.localisation.reference

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxCommandScopeCompletionProvider
 */
class ParadoxLocalisationCommandScopeReference(
	element: ParadoxLocalisationCommandScope,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandScope>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		val name = element.name
		val project = element.project
		//尝试被识别为预定义的localisation_command
		val localisationScope = doResolveLocalisationScope(name, project)
		if(localisationScope != null) return localisationScope
		
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return null
		//尝试识别为value[event_target]或value[global_event_target]
		val selector = valueSetValueSelector().gameType(gameType).preferRootFrom(element)
		val eventTarget = ParadoxValueSetValueSearch.search(name, "event_target", project, selector = selector).findFirst()
		if(eventTarget != null) return ParadoxValueSetValueElement(element, name, "event_target", project, gameType)
		val globalEventTarget = ParadoxValueSetValueSearch.search(name, "global_event_target", project, selector = selector).findFirst()
		if(globalEventTarget != null) return ParadoxValueSetValueElement(element, name, "global_event_target", project, gameType)
		return null
	}
	
	private fun doResolveLocalisationScope(name: String, project: Project): PsiElement? {
		val gameType = element.fileInfo?.rootInfo?.gameType ?: return null
		val configGroup = getCwtConfig(project).get(gameType) ?: return null
		return CwtConfigHandler.resolveLocalisationScope(name, configGroup)
	}
}