package icu.windea.pls.ep.util.presentation

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cast
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithDefinitionType
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

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

    override fun <T : ParadoxDefinitionPresentation> supports(element: ParadoxDefinitionElement, type: Class<T>, relax: Boolean): Boolean {
        if (relax) return true
        val definitionInfo = element.definitionInfo ?: return false
        if (!PlsAnnotationManager.check(type, definitionInfo.gameType)) return false
        if (!PlsAnnotationManager.check(type, definitionInfo)) return false
        return true
    }

    override fun <T : ParadoxDefinitionPresentation> get(element: ParadoxDefinitionElement, type: Class<T>): T? {
        return getFromCache(element, type)
    }

    private fun <T : ParadoxDefinitionPresentation> getFromCache(element: ParadoxDefinitionElement, type: Class<T>): T? {
        val key = getKey(type)
        return CachedValuesManager.getCachedValue(element, key) {
            val value = getPresentation(element, type)
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, ScriptFile, LocalisationFile)
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun <T : ParadoxDefinitionPresentation> getKey(type: Class<T>): Key<CachedValue<T>> {
        return keyCache.get(type).cast()
    }

    private fun <T : ParadoxDefinitionPresentation> getPresentation(element: ParadoxDefinitionElement, type: Class<T>): T? {
        try {
            val presentation = type.getConstructor(ParadoxDefinitionElement::class.java).newInstance(element)
            return presentation
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn("Cannot create definition presentation (type: $type)", e)
            return null
        }
    }
}
