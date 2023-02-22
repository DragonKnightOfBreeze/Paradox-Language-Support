package icu.windea.pls.localisation.references

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.highlighter.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationCommandScopeCompletionProvider
 */
class ParadoxLocalisationCommandScopePsiReference(
	element: ParadoxLocalisationCommandScope,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandScope>(element, rangeInElement), PsiNodeReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): PsiElement? {
		val element = element
		val name = element.name
		val project = element.project
		val gameType = selectGameType(element) ?: return null
		
		//尝试识别为system_link或者localisation_scope
		val configGroup = getCwtConfig(project).get(gameType) ?: return null
		val systemLink = CwtConfigHandler.resolvePredefinedScope(name, configGroup)
		if(systemLink != null) return systemLink
		val localisationScope = CwtConfigHandler.resolvePredefinedLocalisationScope(name, configGroup)
		if(localisationScope != null) return localisationScope
		
		//尝试识别为预定义的value[event_target] （忽略大小写）
		val predefinedEventTarget = configGroup.values.get("event_target")?.valueConfigMap?.get(name)
		if(predefinedEventTarget != null) return predefinedEventTarget.pointer.element
		
		//尝试识别为value[event_target]或value[global_event_target]
		val selector = valueSetValueSelector(project, element).preferSameRoot()
		val eventTarget = ParadoxValueSetValueSearch.search(name, "event_target", selector = selector).findFirst()
		if(eventTarget != null) return ParadoxValueSetValueElement(element, name, "event_target", project, gameType)
		val globalEventTarget = ParadoxValueSetValueSearch.search(name, "global_event_target", selector = selector).findFirst()
		if(globalEventTarget != null) return ParadoxValueSetValueElement(element, name, "global_event_target", project, gameType)
		
		return null
	}
	
	override fun getTextAttributesKey(): TextAttributesKey? {
		val element = element
		val name = element.name
		val project = element.project
		val gameType = selectGameType(element) ?: return null
		
		//尝试被识别为预定义的localisation_command
		val configGroup = getCwtConfig(project).get(gameType) ?: return null
		val systemLink = CwtConfigHandler.resolvePredefinedScope(name, configGroup)
		if(systemLink != null) return ParadoxScriptAttributesKeys.SYSTEM_LINK_KEY
		val localisationScope = CwtConfigHandler.resolvePredefinedLocalisationScope(name, configGroup)
		if(localisationScope != null) return ParadoxScriptAttributesKeys.SCOPE_KEY
		
		//尝试识别为预定义的value[event_target] （忽略大小写）
		val predefinedEventTarget = configGroup.values.get("event_target")?.valueConfigMap?.get(name)
		if(predefinedEventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
		
		//尝试识别为value[event_target]或value[global_event_target]
		val selector = valueSetValueSelector(project, element)
		val eventTarget = ParadoxValueSetValueSearch.search(name, "event_target", selector = selector).findFirst()
		if(eventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
		val globalEventTarget = ParadoxValueSetValueSearch.search(name, "global_event_target", selector = selector).findFirst()
		if(globalEventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
		
		return null
	}
}
