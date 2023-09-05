package icu.windea.pls.lang

import com.google.common.cache.*
import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*

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
        val configGroup = getConfigGroups(project).get(gameType)
        return resolveModifier(element, name, configGroup)
    }
    
    fun resolveModifier(element: ParadoxScriptStringExpressionElement, name: String, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierElement? {
        val modifierData = getModifierData(element, name, configGroup, useSupport)
        return modifierData?.toModifierElement(element)
    }
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet) {
        val element = context.contextElement!!
        if(element !is ParadoxScriptStringExpressionElement) return
        
        val modifierNames = mutableSetOf<String>()
        ParadoxModifierSupport.completeModifier(context, result, modifierNames)
    }
    
    fun getModifierData(element: ParadoxScriptStringExpressionElement, name: String, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierData? {
        val rootFile = selectRootFile(element) ?: return null
        val project = configGroup.project
        val cache = project.modifierDataCache.get(rootFile)
        val cacheKey = name
        val modifierData = cache.getOrPut(cacheKey) {
            //进行代码补全时，可能需要使用指定的扩展点解析修正
            useSupport?.resolveModifier(name, element, configGroup)
                ?: ParadoxModifierSupport.resolveModifier(name, element, configGroup)
                ?: ParadoxModifierData.EMPTY
        }
        if(modifierData == ParadoxModifierData.EMPTY) return null
        return modifierData
    }
    
    fun getResolvedModifierData(element: PsiElement, name: String): ParadoxModifierData? {
        val rootFile = selectRootFile(element) ?: return null
        val project = element.project
        val cache = project.modifierDataCache.get(rootFile)
        val cacheKey = name
        val modifierData = cache.getIfPresent(cacheKey)
        if(modifierData == ParadoxModifierData.EMPTY) return null
        return modifierData
    }
    
    fun getModifierNameKeys(name: String): MutableSet<String> {
        val keys = mutableSetOf<String>()
        ParadoxModifierNameDescProvider.EP_NAME.extensionList.forEachFast { it.addModifierNameKey(, keys) }
        return keys
    }
    
    fun getModifierNameKey(name: String): String {
        //mod_$, ALL_UPPER_CASE is ok.
        return "mod_${name}"
    }
    
    fun getModifierDescKeys(name: String): MutableSet<String> {
        val keys = mutableSetOf<String>()
        ParadoxModifierNameDescProvider.EP_NAME.extensionList.forEachFast { it.addModifierDescKey(, keys) }
        return keys
    }
    
    fun getModifierDescKey(name: String): String {
        //mod_$_desc, ALL_UPPER_CASE is ok.
        return "mod_${name}_desc"
    }
    
    fun getModifierIconPaths(name: String): MutableSet<String> {
        val paths = mutableSetOf<String>()
        ParadoxModifierIconProvider.EP_NAME.extensionList.forEachFast { it.addModifierIconPath(, paths) }
        return paths
    }
    
    fun getModifierIconName(name: String): String {
        //mod_$.dds
        return "mod_${name}.dds"
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
            .preferLocale(ParadoxLocaleHandler.getPreferredLocale())
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

private val PlsKeys.modifierDataCache by createKey("paradox.modifier.data.cache") {
    NestedCache<VirtualFile, _, _, _> { CacheBuilder.newBuilder().buildCache<String, ParadoxModifierData>().trackedBy { it.modificationTracker } }
}
private val Project.modifierDataCache by PlsKeys.modifierDataCache
