package icu.windea.pls.ep.data

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.ep.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.script.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.lang.util.script.*

/**
 * 用于获取定义数据。
 *
 * 这里直接获取的应当是未加工过的必要的数据。
 *
 * 需要解析封装变量，不需要判断是否合法。兼容需要内联的情况。
 *
 * @see ParadoxDefinitionData
 */
@WithGameTypeEP
abstract class ParadoxDefinitionDataProvider<T : ParadoxDefinitionData> {
    val dataType: Class<T> by lazy { javaClass.genericSuperclass.genericType(0)!! }
    val cachedDataKey: Key<CachedValue<T>> by lazy { createKey("stellaris.cached.data.by.${javaClass.name}") }
    
    abstract fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean
    
    fun getData(definition: ParadoxScriptDefinitionElement): T? {
        return CachedValuesManager.getCachedValue(definition, cachedDataKey) {
            val value = doGetData(definition)
            //invalidated on ScriptedVariablesTracker or InlineScriptsTracker
            val project = definition.project
            val tracker1 = ParadoxModificationTrackerProvider.getInstance(project).ScriptedVariablesTracker
            val tracker2 = ParadoxModificationTrackerProvider.getInstance(project).InlineScriptsTracker
            CachedValueProvider.Result.create(value, definition, tracker1, tracker2)
        }
    }
    
    private fun doGetData(definition: ParadoxScriptDefinitionElement): T? {
        val data = when {
            definition is ParadoxScriptFile -> ParadoxScriptDataResolver.resolve(definition, inline = true)
            definition is ParadoxScriptProperty -> ParadoxScriptDataResolver.resolveProperty(definition, inline = true)
            else -> null
        }
        if(data == null) return null
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
                if(!gameType.supportsByAnnotation(ep)) return@f null
                if(ep.dataType != dataType) return@f null
                if(!ep.supports(definition, definitionInfo)) return@f null
                ep.getData(definition) as? T?
            }
        }
    }
}