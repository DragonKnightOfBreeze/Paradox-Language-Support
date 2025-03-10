package icu.windea.pls.ep.data

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.data.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@WithGameTypeEP
abstract class ParadoxDefinitionDataProvider<T : ParadoxDefinitionData> {
    val dataType: Class<T> by lazy { javaClass.genericSuperclass.genericType(0)!! }
    val cachedDataKey: Key<CachedValue<T>> by lazy { createKey("cached.data.by.${javaClass.name}") }

    abstract fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getData(definition: ParadoxScriptDefinitionElement): T? {
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
        return dataType.getConstructor(ParadoxScriptData::class.java).newInstance(data)
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionDataProvider<*>>("icu.windea.pls.definitionDataProvider")

        @Suppress("UNCHECKED_CAST")
        fun <T : ParadoxDefinitionData> getData(dataType: Class<T>, definition: ParadoxScriptDefinitionElement): T? {
            val definitionInfo = definition.definitionInfo ?: return null
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                if (ep.dataType != dataType) return@f null
                if (!ep.supports(definition, definitionInfo)) return@f null
                ep.getData(definition) as? T?
            }
        }
    }
}
