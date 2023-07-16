package icu.windea.pls.lang

import com.google.common.cache.*
import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*

object ParadoxModifierHandler {
    object Keys {
        val modifierCache = KeyWithDefaultValue.create<Cache<String, ParadoxModifierElement>>("paradox.modifier.cache") { CacheBuilder.newBuilder().buildCache() }
        val modifierModificationTracker = Key.create<ModificationTracker>("paradox.modifier.modificationTracker")
        val modifierModificationCount = Key.create<Long>("paradox.modifier.modificationCount")
    }
    
    //可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
    //不同的游戏类型存在一些通过不同逻辑生成的修正
    //插件使用的modifiers.cwt中应当去除生成的修正
    
    fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        return ParadoxModifierSupport.matchModifier(name, element, configGroup)
    }
    
    fun resolveModifier(element: ParadoxScriptStringExpressionElement, useSupport: ParadoxModifierSupport? = null): ParadoxModifierElement? {
        val name = element.value
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = getCwtConfig(project).get(gameType)
        return resolveModifier(name, element, configGroup, useSupport)
    }
    
    fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierElement? {
        //如果可以缓存，需要缓存解析结果
        
        val gameType = configGroup.gameType ?: return null
        val cacheKey = "${name}@${gameType}"
        val modifierCache = selectRootFile(element)?.getUserData(Keys.modifierCache) ?: return null
        val cached = modifierCache.get(cacheKey) //无法解析时不会缓存
        if(cached != null) {
            val modificationTracker = cached.getUserData(Keys.modifierModificationTracker)
            if(modificationTracker != null) {
                val modificationCount = cached.getUserData(Keys.modifierModificationCount) ?: 0
                if(modificationCount == modificationTracker.modificationCount) {
                    val resolved = ParadoxModifierElement(element, name, gameType, configGroup.project)
                    cached.copyUserDataTo(resolved)
                    return resolved
                }
            }
        }
        
        //进行代码补全时，可能需要使用指定的扩展点解析修正
        val resolved = useSupport?.resolveModifier(name, element, configGroup)
            ?: ParadoxModifierSupport.resolveModifier(name, element, configGroup)
            ?: return null
        
        val ep = resolved.getUserData(ParadoxModifierSupport.Keys.support)
        if(ep != null) {
            val modificationTracker = ep.getModificationTracker(resolved)
            if(modificationTracker != null) {
                resolved.putUserData(Keys.modifierModificationTracker, modificationTracker)
                resolved.putUserData(Keys.modifierModificationCount, modificationTracker.modificationCount)
                modifierCache.put(cacheKey, resolved)
            }
        }
        return resolved
    }
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        val element = contextElement
        if(element !is ParadoxScriptStringExpressionElement) return
        val modifierNames = mutableSetOf<String>()
        ParadoxModifierSupport.completeModifier(context, result, modifierNames)
    }
    
    fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
        return ParadoxModifierSupport.getModifierCategories(element)
    }
    
    //related localisations & images methods
    
    fun getModifierNameKey(name: String): String {
        //mod_$, ALL_UPPER_CASE is ok.
        return "mod_${name}"
    }
    
    fun getModifierDescKey(name: String): String {
        //mod_$_desc, ALL_UPPER_CASE is ok.
        return "mod_${name}_desc"
    }
    
    fun getModifierIconPath(name: String): String {
        //gfx/interface/icons/modifiers/mod_$.dds
        return "gfx/interface/icons/modifiers/mod_${name}.dds"
    }
    
    fun getModifierLocalizedNames(name: String, project: Project, contextElement: PsiElement?): Set<String> {
        ProgressManager.checkCanceled()
        val nameKey = getModifierNameKey(name)
        val localizedNames = mutableSetOf<String>()
        val selector = localisationSelector(project, contextElement)
            .preferLocale(preferredParadoxLocale())
            .withConstraint(ParadoxLocalisationConstraint.Modifier)
        ParadoxLocalisationSearch.search(nameKey, selector).processQueryAsync { localisation ->
            ProgressManager.checkCanceled()
            val r = ParadoxLocalisationTextRenderer.render(localisation).takeIfNotEmpty()
            if(r != null) localizedNames.add(r)
            true
        }
        return localizedNames
    }
}