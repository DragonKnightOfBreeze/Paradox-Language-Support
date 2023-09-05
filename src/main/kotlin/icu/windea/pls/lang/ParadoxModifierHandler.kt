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
import icu.windea.pls.lang.modifier.impl.*
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
        return resolveModifier(name, element, configGroup)
    }
    
    fun resolveModifier(name: String, element: PsiElement): ParadoxModifierElement? {
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = getConfigGroups(project).get(gameType)
        return resolveModifier(name, element, configGroup)
    }
    
    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierElement? {
        val modifierData = getModifierData(element, name, configGroup, useSupport)
        return modifierData?.toModifierElement(element)
    }
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet) {
        val element = context.contextElement!!
        if(element !is ParadoxScriptStringExpressionElement) return
        
        val modifierNames = mutableSetOf<String>()
        ParadoxModifierSupport.completeModifier(context, result, modifierNames)
    }
    
    fun getModifierData(element: PsiElement, name: String, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierData? {
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
    
    fun getModifierNameKeys(name: String, element: PsiElement, onlyBase: Boolean = false): Set<String> {
        return buildSet {
            ParadoxModifierNameDescProvider.EP_NAME.extensionList.forEachFast { 
                it.addModifierNameKey(name, element, this)
                if(onlyBase) return@buildSet
            }
        }
    }
    
    fun getModifierDescKeys(name: String, element: PsiElement, onlyBase: Boolean = false): Set<String> {
        return buildSet {
            ParadoxModifierNameDescProvider.EP_NAME.extensionList.forEachFast {
                it.addModifierDescKey(name, element, this)
                if(onlyBase) return@buildSet
            }
        }
    }
    
    fun getModifierIconPaths(name: String, element: PsiElement, onlyBase: Boolean = false): Set<String> {
        return buildSet {
            ParadoxModifierIconProvider.EP_NAME.extensionList.forEachFast { 
                it.addModifierIconPath(name, element, this)
                if(onlyBase) return@buildSet
            }
        }
    }
    
    fun getModifierLocalizedNames(name: String, element: PsiElement, project: Project): Set<String> {
        ProgressManager.checkCanceled()
        val keys = getModifierNameKeys(name, element)
        return keys.firstNotNullOfOrNull { key ->
            val selector = localisationSelector(project, element)
                .preferLocale(ParadoxLocaleHandler.getPreferredLocale())
                .withConstraint(ParadoxLocalisationConstraint.Modifier)
            val localizedNames = mutableSetOf<String>()
            ParadoxLocalisationSearch.search(key, selector).processQueryAsync { localisation ->
                ProgressManager.checkCanceled()
                val r = ParadoxLocalisationTextRenderer.render(localisation).takeIfNotEmpty()
                if(r != null) localizedNames.add(r)
                true
            }
            localizedNames.takeIfNotEmpty()
        }.orEmpty()
    }
}

private val PlsKeys.modifierDataCache by createKey("paradox.modifier.data.cache") {
    NestedCache<VirtualFile, _, _, _> { CacheBuilder.newBuilder().buildCache<String, ParadoxModifierData>().trackedBy { it.modificationTracker } }
}
private val Project.modifierDataCache by PlsKeys.modifierDataCache
