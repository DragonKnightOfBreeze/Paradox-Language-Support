package icu.windea.pls.lang.data

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

/**
 * 用于获取定义数据。
 *
 * 这里直接获取的应当是未加工过的必要的数据。
 *
 * 需要解析封装变量，不需要判断是否合法。兼容需要内联的情况。
 */
interface ParadoxDefinitionDataProvider<T> {
    val definitionType: String
    val dataType: Class<T>
    val gameType: ParadoxGameType? get() = null
    val cachedDataKey: Key<CachedValue<T>>
    
    fun getData(definition: ParadoxScriptDefinitionElement): T? {
        return CachedValuesManager.getCachedValue(definition, cachedDataKey) {
            val value = doGetData(definition)
            CachedValueProvider.Result.create(value, definition)
        }
    }
    
    fun doGetData(definition: ParadoxScriptDefinitionElement): T? {
        val data = when {
            definition is ParadoxScriptFile -> ParadoxScriptDataResolver.resolve(definition, inline = true)
            definition is ParadoxScriptProperty -> ParadoxScriptDataResolver.resolveProperty(definition, inline = true)
            else -> null
        }
        if(data == null) return null
        return doGetData(data)
    }
    
    fun doGetData(data: ParadoxScriptData): T?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxDefinitionDataProvider<*>>("icu.windea.pls.definitionDataProvider")
        
        fun getInstance(definitionType: String, gameType: ParadoxGameType? = null): ParadoxDefinitionDataProvider<*>? {
            return EP_NAME.extensionList.find {
                it.definitionType == definitionType && (it.gameType == null || it.gameType == gameType)
            }
        }
        
        fun <T> getInstance(dataType: Class<T>): ParadoxDefinitionDataProvider<T>? {
            return EP_NAME.extensionList.find {
                it.dataType == dataType
            }?.castOrNull()
        }
    }
}

/**
 * 获取定义的指定类型的数据。
 */
inline fun <reified T> ParadoxScriptDefinitionElement.getData(): T? {
    return ParadoxDefinitionDataProvider.getInstance(T::class.java)?.getData(this)
}