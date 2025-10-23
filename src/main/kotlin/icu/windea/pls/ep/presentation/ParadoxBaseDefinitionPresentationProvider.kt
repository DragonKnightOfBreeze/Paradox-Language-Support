package icu.windea.pls.ep.presentation

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.cast
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithDefinitionType
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 支持符合以下条件的定义的数据：
 * - 其类型继承自 [ParadoxDefinitionPresentationBase]。
 * - 其类型注有适当的注解（[WithGameType]、[WithDefinitionType]）。
 */
class ParadoxBaseDefinitionPresentationProvider : ParadoxDefinitionPresentationProvider {
    private val keyCache = CacheBuilder().build<Class<out ParadoxDefinitionPresentation>, Key<CachedValue<ParadoxDefinitionPresentation>>> {
        val shortKey = it.name.removePrefix("icu.windea.pls.ep.presentation.")
        createKey("cached.paradox.definition.presentation:$shortKey")
    }

    override fun <T : ParadoxDefinitionPresentation> supports(element: ParadoxScriptDefinitionElement, type: Class<T>): Boolean {
        val definitionInfo = element.definitionInfo ?: return false
        if (!PlsAnnotationManager.check(type, definitionInfo.gameType)) return false
        if (!PlsAnnotationManager.check(type, definitionInfo)) return false
        return true
    }

    override fun <T : ParadoxDefinitionPresentation> get(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
        return doGetPresentationFromCache(element, type)
    }

    private fun <T : ParadoxDefinitionPresentation> doGetPresentationFromCache(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
        val key = doGetPresentationKey(type)
        return CachedValuesManager.getCachedValue(element, key) {
            val value = doGetPresentation(element, type)
            value.withDependencyItems(
                element,
                ParadoxModificationTrackers.FileTracker,
            )
        }
    }

    private fun <T : ParadoxDefinitionPresentation> doGetPresentationKey(type: Class<T>): Key<CachedValue<T>> {
        return keyCache.get(type).cast()
    }

    private fun <T : ParadoxDefinitionPresentation> doGetPresentation(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
        try {
            val presentation = type.getConstructor(ParadoxScriptDefinitionElement::class.java).newInstance(element)
            return presentation
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn("Cannot create definition presentation (type: $type)", e)
            return null
        }
    }
}
