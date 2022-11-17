package icu.windea.pls.localisation.references

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.cwt.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationCommandScopeCompletionProvider
 */
class ParadoxLocalisationCommandScopePsiReference(
	element: ParadoxLocalisationCommandScope,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandScope>(element, rangeInElement), SmartPsiReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的valueSetValue
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			resolved is PsiNamedElement -> resolved.setName(newElementName)
			resolved is ParadoxScriptExpressionElement -> resolved.value = newElementName
			else -> throw IncorrectOperationException() //不允许重命名
		}
		//重命名当前元素
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): PsiElement? {
		val name = element.name
		val project = element.project
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return null
		//尝试被识别为预定义的localisation_command
		val configGroup = getCwtConfig(project).get(gameType) ?: return null
		val systemScope = CwtConfigHandler.resolveSystemScope(name, configGroup)
		if(systemScope != null) return systemScope
		val localisationScope = CwtConfigHandler.resolveLocalisationScope(name, configGroup)
		if(localisationScope != null) return localisationScope
		
		//尝试识别为value[event_target]或value[global_event_target]
		val selector = valueSetValueSelector().gameType(gameType).preferRootFrom(element, exact)
		val eventTarget = ParadoxValueSetValueSearch.search(name, "event_target", project, selector = selector).findFirst()
		if(eventTarget != null) return ParadoxValueSetValueElement(element, name, "event_target", project, gameType)
		val globalEventTarget = ParadoxValueSetValueSearch.search(name, "global_event_target", project, selector = selector).findFirst()
		if(globalEventTarget != null) return ParadoxValueSetValueElement(element, name, "global_event_target", project, gameType)
		
		return null
	}
	
	override fun resolveTextAttributesKey(): TextAttributesKey? {
		val name = element.name
		val project = element.project
		val gameType = ParadoxSelectorUtils.selectGameType(element) ?: return null
		
		//尝试被识别为预定义的localisation_command
		val configGroup = getCwtConfig(project).get(gameType) ?: return null
		val systemScope = CwtConfigHandler.resolveSystemScope(name, configGroup)
		if(systemScope != null) return ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY
		val localisationScope = CwtConfigHandler.resolveLocalisationScope(name, configGroup)
		if(localisationScope != null) return ParadoxScriptAttributesKeys.SCOPE_KEY
		
		//尝试识别为value[event_target]或value[global_event_target]
		val selector = valueSetValueSelector().gameType(gameType)
		val eventTarget = ParadoxValueSetValueSearch.search(name, "event_target", project, selector = selector).findFirst()
		if(eventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
		val globalEventTarget = ParadoxValueSetValueSearch.search(name, "global_event_target", project, selector = selector).findFirst()
		if(globalEventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
		
		return null
	}
}
