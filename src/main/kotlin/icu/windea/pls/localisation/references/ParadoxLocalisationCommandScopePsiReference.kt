package icu.windea.pls.localisation.references

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.highlighter.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationCommandScopeCompletionProvider
 */
class ParadoxLocalisationCommandScopePsiReference(
    element: ParadoxLocalisationCommandScope,
    rangeInElement: TextRange,
    val prefix: String?
) : PsiReferenceBase<ParadoxLocalisationCommandScope>(element, rangeInElement) {
    val project by lazy { element.project }
    
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(rangeInElement.replace(element.text, newElementName))
    }
    
    //缓存解析结果以优化性能
    
    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationCommandScopePsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationCommandScopePsiReference, incompleteCode: Boolean): PsiElement? {
            return ref.doResolve()
        }
    }
    
    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }
    
    private fun doResolve(): PsiElement? {
        val element = element
        val name = rangeInElement.substring(element.text)
        val gameType = selectGameType(element) ?: return null
        val configGroup = getConfigGroup(project, gameType)
        
        if(prefix == null) {
            //尝试识别为system_link或者localisation_scope
            val systemLink = CwtConfigHandler.resolvePredefinedScope(name, configGroup)
            if(systemLink != null) return systemLink
            val localisationScope = CwtConfigHandler.resolvePredefinedLocalisationScope(name, configGroup)
            if(localisationScope != null) return localisationScope
        }
        
        if(prefix == null || prefix == ParadoxDynamicValueHandler.EVENT_TARGET_PREFIX) {
            //尝试识别为预定义的value[event_target] （忽略大小写）
            val predefinedEventTarget = configGroup.dynamicValueTypes.get("event_target")?.valueConfigMap?.get(name)
            if(predefinedEventTarget != null) return predefinedEventTarget.pointer.element
            
            //尝试识别为value[event_target]或value[global_event_target]
            return ParadoxDynamicValueElement(element, name, ParadoxDynamicValueHandler.EVENT_TARGETS, Access.Read, gameType, project)
        }
        
        return null
    }
    
    fun getAttributesKey(): TextAttributesKey? {
        val element = element
        val name = rangeInElement.substring(element.text)
        val gameType = selectGameType(element) ?: return null
        val configGroup = getConfigGroup(project, gameType)
        
        if(prefix == null) {
            //尝试识别为system_link或者localisation_scope
            val systemLink = CwtConfigHandler.resolvePredefinedScope(name, configGroup)
            if(systemLink != null) return ParadoxScriptAttributesKeys.SYSTEM_LINK_KEY
            val localisationScope = CwtConfigHandler.resolvePredefinedLocalisationScope(name, configGroup)
            if(localisationScope != null) return ParadoxScriptAttributesKeys.SCOPE_KEY
        }
        
        
        if(prefix == null || prefix == ParadoxDynamicValueHandler.EVENT_TARGET_PREFIX) {
            //尝试识别为预定义的value[event_target] （忽略大小写）
            val predefinedEventTarget = configGroup.dynamicValueTypes.get("event_target")?.valueConfigMap?.get(name)
            if(predefinedEventTarget != null) return ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
            
            //尝试识别为value[event_target]或value[global_event_target]
            return ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
        }
        
        return null
    }
}
