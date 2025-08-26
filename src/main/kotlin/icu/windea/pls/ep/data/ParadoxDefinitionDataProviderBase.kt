package icu.windea.pls.ep.data

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.data.*
import icu.windea.pls.script.psi.*

abstract class ParadoxDefinitionDataProviderBase<T : ParadoxDefinitionData>() : ParadoxDefinitionDataProvider<T> {
    private val cachedDataKey: Key<CachedValue<T>> by lazy { createKey("cached.data.by.${javaClass.name}") }

    override fun getData(definition: ParadoxScriptDefinitionElement): T? {
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
