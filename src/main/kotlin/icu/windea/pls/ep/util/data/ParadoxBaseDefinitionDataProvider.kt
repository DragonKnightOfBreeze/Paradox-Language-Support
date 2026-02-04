package icu.windea.pls.ep.util.data

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
import icu.windea.pls.lang.util.data.ParadoxScriptData
import icu.windea.pls.lang.util.data.ParadoxScriptDataResolver
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 支持符合以下条件的定义的数据：
 * - 其类型继承自 [ParadoxDefinitionDataBase]。
 * - 其类型注有适当的注解（[WithGameType]、[WithDefinitionType]）。
 */
class ParadoxBaseDefinitionDataProvider : ParadoxDefinitionDataProvider {
    private val keyCache = CacheBuilder().build<Class<out ParadoxDefinitionData>, Key<CachedValue<ParadoxDefinitionData>>> {
        val shortKey = it.name.removePrefix("icu.windea.pls.ep.data.")
        createKey("cached.paradox.definition.data:$shortKey")
    }

    override fun <T : ParadoxDefinitionData> supports(element: ParadoxScriptDefinitionElement, type: Class<T>, relax: Boolean): Boolean {
        if (relax) return true
        val definitionInfo = element.definitionInfo ?: return false
        if (!PlsAnnotationManager.check(type, definitionInfo.gameType)) return false
        if (!PlsAnnotationManager.check(type, definitionInfo)) return false
        return true
    }

    override fun <T : ParadoxDefinitionData> get(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
        return doGetDataFromCache(element, type)
    }

    private fun <T : ParadoxDefinitionData> doGetDataFromCache(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
        val key = doGetDataKey(type)
        return CachedValuesManager.getCachedValue(element, key) {
            val value = doGetData(element, type)
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, ScriptedVariables, InlineScripts)
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun <T : ParadoxDefinitionData> doGetDataKey(type: Class<T>): Key<CachedValue<T>> {
        return keyCache.get(type).cast()
    }

    private fun <T : ParadoxDefinitionData> doGetData(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
        try {
            val scriptData = ParadoxScriptDataResolver.INLINE.resolve(element) ?: return null
            val data = type.getConstructor(ParadoxScriptData::class.java).newInstance(scriptData)
            return data
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn("Cannot create definition data (type: $type)", e)
            return null
        }
    }
}
