package icu.windea.pls.ep.data

import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.util.data.ParadoxScriptData
import icu.windea.pls.lang.util.data.ParadoxScriptDataResolver
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

abstract class ParadoxDefinitionDataProviderBase<T : ParadoxDefinitionData>() : ParadoxDefinitionDataProvider<T> {
    private val cachedDataKey: Key<CachedValue<T>> by lazy { createKey("cached.data.by.${javaClass.name}") }

    override fun getData(definition: ParadoxScriptDefinitionElement): T? {
        return doGetDataFromCache(definition)
    }

    private fun ParadoxDefinitionDataProviderBase<T>.doGetDataFromCache(definition: ParadoxScriptDefinitionElement): T? {
        return CachedValuesManager.getCachedValue(definition, cachedDataKey) {
            val value = doGetData(definition)
            value.withDependencyItems(
                definition,
                ParadoxModificationTrackers.ScriptedVariablesTracker,
                ParadoxModificationTrackers.InlineScriptsTracker,
            )
        }
    }

    private fun doGetData(definition: ParadoxScriptDefinitionElement): T? {
        val data = when {
            definition is ParadoxScriptFile -> ParadoxScriptDataResolver.resolve(definition, inline = true)
            definition is ParadoxScriptProperty -> ParadoxScriptDataResolver.resolveProperty(definition, inline = true)
            else -> null
        }
        if (data == null) return null
        return doGetData(data)
    }

    private fun doGetData(data: ParadoxScriptData): T {
        return type.getConstructor(ParadoxScriptData::class.java).newInstance(data)
    }
}
