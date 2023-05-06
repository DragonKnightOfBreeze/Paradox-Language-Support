package icu.windea.pls.localisation.references

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.highlighter.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationCommandScopeCompletionProvider
 */
class ParadoxLocalisationCommandScopePsiReference(
	element: ParadoxLocalisationCommandScope,
	rangeInElement: TextRange,
	val prefix: String?
) : PsiReferenceBase<ParadoxLocalisationCommandScope>(element, rangeInElement), AttributesKeyAware {
	companion object {
		const val EVENT_TARGET_PREFIX = "event_target:"
	}
	
	val project by lazy { element.project }
	
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
		return element.setName(rangeInElement.replace(element.name, newElementName))
	}
	
	//缓存解析结果以优化性能
	
	override fun resolve(): PsiElement? {
		if(PlsThreadLocals.defaultResolveToValueSetValue.get()) {
			return doResolve()
		}
		return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
	}
	
	private fun doResolve(): PsiElement? {
		val element = element
		val name = rangeInElement.substring(element.text)
		val gameType = selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).get(gameType)
		
		if(prefix == null) {
			//尝试识别为system_link或者localisation_scope
			val systemLink = ParadoxConfigHandler.resolvePredefinedScope(name, configGroup)
			if(systemLink != null) return systemLink
			val localisationScope = ParadoxConfigHandler.resolvePredefinedLocalisationScope(name, configGroup)
			if(localisationScope != null) return localisationScope
		}
		
		if(prefix == null || prefix == EVENT_TARGET_PREFIX) {
			//尝试识别为预定义的value[event_target] （忽略大小写）
			val predefinedEventTarget = configGroup.values.get("event_target")?.valueConfigMap?.get(name)
			if(predefinedEventTarget != null) return predefinedEventTarget.pointer.element
			
			//尝试识别为value[event_target]或value[global_event_target]（需要预先在脚本文件中使用到）
			if(PlsThreadLocals.defaultResolveToValueSetValue.get()) {
				return ParadoxValueSetValueElement(element, name, "event_target", Access.Read, gameType, project)
			}
			val selector = valueSetValueSelector(project, element).contextSensitive()
			val eventTarget = ParadoxValueSetValueSearch.search(name, "event_target", selector).findFirst()
			if(eventTarget != null) return ParadoxValueSetValueElement(element, name, "event_target", Access.Read, gameType, project)
			val globalEventTarget = ParadoxValueSetValueSearch.search(name, "global_event_target", selector).findFirst()
			if(globalEventTarget != null) return ParadoxValueSetValueElement(element, name, "global_event_target", Access.Read, gameType, project)
		}
		
		return null
	}
	
	override fun getAttributesKey(): TextAttributesKey? {
		val element = element
		val name = rangeInElement.substring(element.text)
		val gameType = selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).get(gameType)
		
		if(prefix == null) {
			//尝试识别为system_link或者localisation_scope
			val systemLink = ParadoxConfigHandler.resolvePredefinedScope(name, configGroup)
			if(systemLink != null) return ParadoxScriptAttributesKeys.SYSTEM_LINK_KEY
			val localisationScope = ParadoxConfigHandler.resolvePredefinedLocalisationScope(name, configGroup)
			if(localisationScope != null) return ParadoxScriptAttributesKeys.SCOPE_KEY
		}
		
		
		if(prefix == null || prefix == EVENT_TARGET_PREFIX) {
			//尝试识别为预定义的value[event_target] （忽略大小写）
			val predefinedEventTarget = configGroup.values.get("event_target")?.valueConfigMap?.get(name)
			if(predefinedEventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
			
			//尝试识别为value[event_target]或value[global_event_target]
			if(PlsThreadLocals.defaultResolveToValueSetValue.get()) {
				return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
			}
			val selector = valueSetValueSelector(project, element)
			val eventTarget = ParadoxValueSetValueSearch.search(name, "event_target", selector).findFirst()
			if(eventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
			val globalEventTarget = ParadoxValueSetValueSearch.search(name, "global_event_target", selector).findFirst()
			if(globalEventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
		}
		
		return null
	}
	
	private object Resolver: ResolveCache.AbstractResolver<ParadoxLocalisationCommandScopePsiReference, PsiElement> {
		override fun resolve(ref: ParadoxLocalisationCommandScopePsiReference, incompleteCode: Boolean): PsiElement? {
			return ref.doResolve()
		}
	}
}
