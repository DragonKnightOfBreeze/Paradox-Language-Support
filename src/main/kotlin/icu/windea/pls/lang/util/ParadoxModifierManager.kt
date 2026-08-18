package icu.windea.pls.lang.util

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.cache.createNestedCache
import icu.windea.pls.core.cache.trackedBy
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.registerKeyWithThis
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.index.constraints.ParadoxLocalisationIndexConstraint
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.resolve.ParadoxModifierService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.search.util.withConstraint
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.model.support
import icu.windea.pls.model.toInfo
import icu.windea.pls.model.toPsiElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

@Optimized
object ParadoxModifierManager {
    object Keys : KeyRegistry() {
        val modifierNameKeys by registerKey<Set<String>>(Keys)
        val modifierDescKeys by registerKey<Set<String>>(Keys)
        val modifierIconPaths by registerKey<Set<String>>(Keys)
    }

    private val CwtConfigGroup.modifierInfoCache by registerKeyWithThis(CwtConfigGroup.Keys) {
        // rootFile -> cacheKey -> modifierInfo
        createNestedCache<VirtualFile, _, _> {
            CacheBuilder().build<String, ParadoxModifierInfo>().cancelable().trackedBy { it.modificationTracker }
        }
    }

    // 可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
    // 不同的游戏类型存在一些通过不同逻辑生成的修正
    // 插件使用的modifiers.cwt中应当去除生成的修正

    fun resolveModifier(element: ParadoxScriptStringExpressionElement): ParadoxModifierLightElement? {
        val name = element.value
        val gameType = selectGameType(element) ?: return null
        val project = element.project
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        return resolveModifier(name, element, configGroup)
    }

    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierLightElement? {
        val modifierInfo = getModifierInfo(name, element, configGroup, useSupport)
        return modifierInfo?.toPsiElement(element)
    }

    fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.contextElement !is ParadoxScriptStringExpressionElement) return
        ParadoxModifierService.completeModifier(context, result)
    }

    @Suppress("unused")
    fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean {
        return ParadoxModifierService.processModifier(element, configGroup, processor)
    }

    fun getModifierInfo(name: String, element: PsiElement, configGroup: CwtConfigGroup, useSupport: ParadoxModifierSupport? = null): ParadoxModifierInfo? {
        val rootFile = selectRootFile(element) ?: return null
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = name // 3.0.2 #385 although modifier names are case-insensitive, cache keys here should still use vanilla input
        val modifierInfo = cache.get(cacheKey) {
            // 进行代码补全时，可能需要使用指定的扩展点解析修正
            useSupport?.resolveModifier(name, element, configGroup)?.also { it.support = useSupport }
                ?: ParadoxModifierService.resolveModifier(name, element, configGroup)
                ?: ParadoxModifierInfo.EMPTY
        }
        if (modifierInfo == ParadoxModifierInfo.EMPTY) return null
        return modifierInfo
    }

    fun getModifierInfo(name: String, element: PsiElement): ParadoxModifierInfo? {
        val gameType = selectGameType(element) ?: return null
        val rootFile = selectRootFile(element) ?: return null
        val project = element.project
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = name // 3.0.2 #385 although modifier names are case-insensitive, cache keys here should still use vanilla input
        val modifierInfo = cache.get(cacheKey) {
            ParadoxModifierService.resolveModifier(name, element, configGroup) ?: ParadoxModifierInfo.EMPTY
        }
        if (modifierInfo == ParadoxModifierInfo.EMPTY) return null
        return modifierInfo
    }

    @Suppress("unused")
    fun getModifierInfo(modifierElement: ParadoxModifierLightElement): ParadoxModifierInfo? {
        val gameType = modifierElement.gameType
        val rootFile = selectRootFile(modifierElement) ?: return null
        val project = modifierElement.project
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val cache = configGroup.modifierInfoCache.get(rootFile)
        val cacheKey = modifierElement.name // 3.0.2 #385 although modifier names are case-insensitive, cache keys here should still use vanilla input
        val modifierInfo = cache.get(cacheKey) {
            modifierElement.toInfo()
        }
        return modifierInfo
    }

    fun getModifierNameKeys(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(Keys.modifierNameKeys) {
            val result = ParadoxModifierService.getModifierNameKeys(element, modifierInfo)
            result.optimized()
        }
    }

    fun getModifierDescKeys(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(Keys.modifierDescKeys) {
            val result = ParadoxModifierService.getModifierDescKeys(element, modifierInfo)
            result.optimized()
        }
    }

    fun getModifierIconPaths(name: String, element: PsiElement): Set<String> {
        val modifierInfo = getModifierInfo(name, element) ?: return emptySet()
        return modifierInfo.getOrPutUserData(Keys.modifierIconPaths) {
            val result = ParadoxModifierService.getModifierIconPaths(element, modifierInfo)
            result.optimized()
        }
    }

    fun getModifierPresentableName(name: String, element: PsiElement, project: Project): String? {
        ProgressManager.checkCanceled()
        val keys = getModifierNameKeys(name, element)
        return keys.firstNotNullOfOrNull { key ->
            val selector = ParadoxLocalisationSearch.selector(project, element)
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                .withConstraint(ParadoxLocalisationIndexConstraint.Modifier) // so ignore case
            val nameLocalisation = ParadoxLocalisationSearch.searchNormal(key, selector).find()
            nameLocalisation?.let { ParadoxLocalisationManager.getPresentableText(it) }
        }
    }

    fun getModifierPresentableNames(name: String, element: PsiElement, project: Project): Set<String> {
        ProgressManager.checkCanceled()
        val keys = getModifierNameKeys(name, element)
        return keys.firstNotNullOfOrNull { key ->
            val selector = ParadoxLocalisationSearch.selector(project, element)
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                .withConstraint(ParadoxLocalisationIndexConstraint.Modifier) // so ignore case
            val nameLocalisations = ParadoxLocalisationSearch.searchNormal(key, selector).findAll()
            nameLocalisations.mapNotNullFast { ParadoxLocalisationManager.getPresentableText(it) }.toSet().orNull()
        }.orEmpty()
    }
}
