package icu.windea.pls.lang

import com.google.common.cache.*
import com.intellij.codeInsight.completion.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*

object ParadoxModifierHandler {
    val modifierConfigKey = Key.create<CwtModifierConfig>("paradox.modifier.config")
    val supportKey = Key.create<ParadoxModifierSupport>("paradox.modifier.support")
    val modifierCacheKey = KeyWithDefaultValue.create<Cache<String, ParadoxModifierElement>>("paradox.modifier.cache") {
        CacheBuilder.newBuilder().buildCache()
    }
    val modifierModificationTrackerKey = Key.create<ModificationTracker>("paradox.modifier.modificationTracker")
    val modifierModificationCountKey = Key.create<Long>("paradox.modifier.modificationCount")
    
    //可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
    //不同的游戏类型存在一些通过不同逻辑生成的修正
    //插件使用的modifiers.cwt中应当去除生成的修正
    
    fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.DEFAULT): Boolean {
        return ParadoxModifierSupport.matchModifier(name, element, configGroup, matchType)
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
        
        val cacheKey = "${name}@${configGroup.gameType}"
        val modifierCache = configGroup.project.getUserData(modifierCacheKey)!!
        val cached = modifierCache.get(cacheKey) //无法解析时不会缓存
        if(cached != null) {
            val modificationTracker = cached.getUserData(modifierModificationTrackerKey)
            if(modificationTracker != null) {
                val modificationCount = cached.getUserData(modifierModificationCountKey) ?: 0
                if(modificationCount == modificationTracker.modificationCount) {
                    val resolved = ParadoxModifierElement(element, name, cached.gameType, cached.project)
                    cached.copyUserDataTo(resolved)
                    return resolved
                }
            }
        }
        
        //进行代码补全时，可能需要使用指定的扩展点解析修正
        val resolved = useSupport?.resolveModifier(name, element, configGroup)
            ?: ParadoxModifierSupport.resolveModifier(name, element, configGroup)
            ?: return null
        
        val ep = resolved.getUserData(supportKey)
        if(ep != null) {
            val modificationTracker = ep.getModificationTracker(resolved)
            if(modificationTracker != null) {
                resolved.putUserData(modifierModificationTrackerKey, modificationTracker)
                resolved.putUserData(modifierModificationCountKey, modificationTracker.modificationCount)
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
    
    //TODO 检查修正的相关本地化和图标到底是如何确定的
    
    fun getModifierNameKeys(name: String): List<String> {
        //mod_$, ALL_UPPER_CASE is ok.
        return buildList {
            val nameKey = "mod_${name}"
            add(nameKey)
            add(nameKey.uppercase())
        }
    }
    
    fun getModifierDescKeys(name: String): List<String> {
        //mod_$_desc, ALL_UPPER_CASE is ok.
        return buildList {
            val descKey = "mod_${name}_desc"
            add(descKey)
            add(descKey.uppercase())
        }
    }
    
    fun getModifierIconPaths(name: String): List<String> {
        //gfx/interface/icons/modifiers/mod_$.dds
        return buildList {
            add("gfx/interface/icons/modifiers/mod_${name}.dds")
        }
    }
    
    fun getModifierLocalizedNames(name: String, project: Project, contextElement: PsiElement?): Set<String> {
        val nameKeys = getModifierNameKeys(name)
        val localizedNames = mutableSetOf<String>()
        nameKeys.forEach { nameKey ->
            val selector = localisationSelector(project, contextElement).preferLocale(preferredParadoxLocale())
            ParadoxLocalisationSearch.search(nameKey, selector).processQueryAsync { localisation ->
                val r = ParadoxLocalisationTextExtractor.extract(localisation).takeIfNotEmpty()
                if(r != null) localizedNames.add(r)
                true
            }
        }
        return localizedNames
    }
    
    //documentation methods
    
    fun getCategoriesText(categories: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
        return buildString {
            var appendSeparator = false
            append("<code>")
            for(category in categories) {
                if(appendSeparator) append(", ") else appendSeparator = true
                appendCwtLink("${gameType.linkToken}modifier_categories/$category", category, contextElement)
            }
            append("</code>")
        }
    }
    
    fun getScopeText(scopeId: String, gameType: ParadoxGameType?, contextElement: PsiElement): String {
        return buildString {
            append("<code>")
            ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
            append("</code>")
        }
    }
    
    fun getScopesText(scopeIds: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
        return buildString {
            var appendSeparator = false
            append("<code>")
            for(scopeId in scopeIds) {
                if(appendSeparator) append(", ") else appendSeparator = true
                ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
            }
            append("</code>")
        }
    }
}