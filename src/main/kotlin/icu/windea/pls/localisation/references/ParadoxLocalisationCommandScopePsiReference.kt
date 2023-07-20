package icu.windea.pls.localisation.references

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
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
    val project by lazy { element.project }
    
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(rangeInElement.replace(element.name, newElementName))
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
        val configGroup = getCwtConfig(project).get(gameType)
        
        if(prefix == null) {
            //尝试识别为system_link或者localisation_scope
            val systemLink = ParadoxConfigHandler.resolvePredefinedScope(name, configGroup)
            if(systemLink != null) return systemLink
            val localisationScope = ParadoxConfigHandler.resolvePredefinedLocalisationScope(name, configGroup)
            if(localisationScope != null) return localisationScope
        }
        
        if(prefix == null || prefix == ParadoxValueSetValueHandler.EVENT_TARGET_PREFIX) {
            //尝试识别为预定义的value[event_target] （忽略大小写）
            val predefinedEventTarget = configGroup.values.get("event_target")?.valueConfigMap?.get(name)
            if(predefinedEventTarget != null) return predefinedEventTarget.pointer.element
            
            //尝试识别为value[event_target]或value[global_event_target]
            return ParadoxValueSetValueElement(element, name, ParadoxValueSetValueHandler.EVENT_TARGETS, Access.Read, gameType, project)
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
        
        
        if(prefix == null || prefix == ParadoxValueSetValueHandler.EVENT_TARGET_PREFIX) {
            //尝试识别为预定义的value[event_target] （忽略大小写）
            val predefinedEventTarget = configGroup.values.get("event_target")?.valueConfigMap?.get(name)
            if(predefinedEventTarget != null) return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
            
            //尝试识别为value[event_target]或value[global_event_target]
            return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
        }
        
        return null
    }
}
