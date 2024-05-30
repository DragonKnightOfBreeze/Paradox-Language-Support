package icu.windea.pls.lang.util

import com.google.common.cache.*
import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.script.psi.*

object ParadoxModifierHandler {
    //可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
    //不同的游戏类型存在一些通过不同逻辑生成的修正
    //插件使用的modifiers.cwt中应当去除生成的修正
    
    fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        return ParadoxModifierSupport.matchModifier(name, element, configGroup)
    }
    
    fun resolveModifier(element: ParadoxScriptStringExpressionElement): ParadoxModifierElement? {
        val name = element.value
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = getConfigGroup(project, gameType)
        return resolveModifier(name, element, configGroup)
    }
    
    fun resolveModifier(name: String, element: PsiElement): ParadoxModifierElement? {
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = getConfigGroup(project, gameType)
        return resolveModifier(name, element, configGroup)
    }
    
    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierElement? {
        val modifierInfo = getModifierInfo(name, element, configGroup, useSupport)
        return modifierInfo?.toPsiElement(element)
    }
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet) {
        val element = context.contextElement!!
        if(element !is ParadoxScriptStringExpressionElement) return
        
        val modifierNames = mutableSetOf<String>()
        ParadoxModifierSupport.completeModifier(context, result, modifierNames)
    }
    
    fun getModifierInfo(name: String, element: PsiElement, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierInfo? {
        val rootFile = selectRootFile(element) ?: return null
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = name
        val modifierInfo = cache.get(cacheKey) {
            //进行代码补全时，可能需要使用指定的扩展点解析修正
            useSupport?.resolveModifier(name, element, configGroup)?.also { it.support = useSupport }
                ?: ParadoxModifierSupport.resolveModifier(name, element, configGroup)
                ?: ParadoxModifierInfo.EMPTY
        }
        if(modifierInfo == ParadoxModifierInfo.EMPTY) return null
        return modifierInfo
    }
    
    fun getModifierInfo(name: String, element: PsiElement): ParadoxModifierInfo? {
        val gameType = selectGameType(element) ?: return null
        val rootFile = selectRootFile(element) ?: return null
        val project = element.project
        val configGroup = getConfigGroup(project, gameType)
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = name
        val modifierInfo = cache.get(cacheKey) {
            ParadoxModifierSupport.resolveModifier(name, element, configGroup) ?: ParadoxModifierInfo.EMPTY
        }
        if(modifierInfo == ParadoxModifierInfo.EMPTY) return null
        return modifierInfo
    }
    
    fun getModifierInfo(modifierElement: ParadoxModifierElement): ParadoxModifierInfo? {
        val gameType = modifierElement.gameType
        val rootFile = selectRootFile(modifierElement) ?: return null
        val project = modifierElement.project
        val configGroup = getConfigGroup(project, gameType)
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = modifierElement.name
        val modifierInfo = cache.get(cacheKey) {
            modifierElement.toInfo()
        }
        return modifierInfo
    }
    
    fun getModifierNameKeys(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(PlsKeys.modifierNameKeys) {
            ParadoxModifierNameDescProvider.getModifierNameKeys(element, modifierInfo)
        }
    }
    
    fun getModifierDescKeys(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(PlsKeys.modifierDescKeys) {
            ParadoxModifierNameDescProvider.getModifierDescKeys(element, modifierInfo)
        }
    }
    
    fun getModifierIconPaths(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(PlsKeys.modifierIconPaths) {
            ParadoxModifierIconProvider.getModifierIconPaths(element, modifierInfo)
        }
    }
    
    fun getModifierLocalizedNames(name: String, element: PsiElement, project: Project): Set<String> {
        ProgressManager.checkCanceled()
        val keys = getModifierNameKeys(name, element)
        return keys.firstNotNullOfOrNull { key ->
            val selector = localisationSelector(project, element)
                .preferLocale(ParadoxLocaleHandler.getPreferredLocaleConfig())
                .withConstraint(ParadoxLocalisationConstraint.Modifier)
            val localizedNames = mutableSetOf<String>()
            ParadoxLocalisationSearch.search(key, selector).processQueryAsync { localisation ->
                ProgressManager.checkCanceled()
                val r = ParadoxLocalisationTextRenderer.render(localisation).orNull()
                if(r != null) localizedNames.add(r)
                true
            }
            localizedNames.orNull()
        }.orEmpty()
    }
}

//rootFile -> cacheKey -> modifierInfo
//depends on config group
private val CwtConfigGroup.modifierInfoCache by createKeyDelegate(CwtConfigContext.Keys) {
    createNestedCache<VirtualFile, _, _, _> {
        CacheBuilder.newBuilder().buildCache<String, ParadoxModifierInfo>().trackedBy { it.modificationTracker }
    }
}

private val PlsKeys.modifierNameKeys by createKey<Set<String>>("paradox.modifierNameKeys")
private val PlsKeys.modifierDescKeys by createKey<Set<String>>("paradox.modifierDescKeys")
private val PlsKeys.modifierIconPaths by createKey<Set<String>>("paradox.modifierIconPaths")