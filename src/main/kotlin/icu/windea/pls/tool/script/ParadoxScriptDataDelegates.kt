package icu.windea.pls.tool.script

import icu.windea.pls.core.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.script.psi.*
import java.awt.*
import kotlin.reflect.*
import kotlin.reflect.full.*

inline fun <reified T> ParadoxScriptData.get(path: String): ParadoxScriptDataDelegateProvider<T?> {
    return ParadoxScriptDataDelegateProvider(getData(path), typeOf<T>(), null)
}

inline fun <reified T> ParadoxScriptData.get(path: String, defaultValue: T): ParadoxScriptDataDelegateProvider<T> {
    return ParadoxScriptDataDelegateProvider(getData(path), typeOf<T>(), defaultValue)
}

inline fun <reified T> ParadoxScriptData.getAll(path: String): ParadoxScriptAllDataDelegateProvider<T> {
    return ParadoxScriptAllDataDelegateProvider(getAllData(path), typeOf<T>())
}

class ParadoxScriptDataDelegateProvider<T>(
    val delegate: ParadoxScriptData?,
    val type: KType,
    val defaultValue: T
) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if(delegate == null) return defaultValue
        return getValueOfType(delegate, type) as? T? ?: defaultValue
    }
}

class ParadoxScriptAllDataDelegateProvider<T>(
    val delegate: List<ParadoxScriptData>,
    val type: KType,
) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        if(delegate.isEmpty()) return emptyList()
        return delegate.mapNotNull { getValueOfType(it, type) as? T? }
    }
}

private val ParadoxScriptData.Keys.propertyValuesKey by createKey<MutableMap<KType, Any?>>("paradox.data.property.values")

private fun getValueOfTypeFromCache(data: ParadoxScriptData, type: KType) : Any? {
    val propertyValues = data.getUserData(ParadoxScriptData.Keys.propertyValuesKey)!!
    return propertyValues.getOrPut(type) { getValueOfType(data, type) ?: EMPTY_OBJECT }.takeUnless { it == EMPTY_OBJECT }
}

private fun getValueOfType(data: ParadoxScriptData, type: KType): Any? {
    val value = data.value ?: return null
    val kClass = type.classifier as? KClass<*> ?: return null
    return when {
        kClass == Any::class -> value.value()
        kClass == Boolean::class -> value.booleanValue()
        kClass == Int::class -> value.intValue()
        kClass == Float::class -> value.floatValue()
        kClass == String::class -> value.stringValue()
        kClass == Color::class -> value.colorValue()
        kClass.isSubclassOf(List::class) -> {
            val elementType = type.arguments.first().type ?: return null
            data.children?.mapNotNullTo(mutableListOf()) { getValueOfType(it, elementType) }.orEmpty()
        }
        kClass.isSubclassOf(Collection::class) -> {
            val elementType = type.arguments.first().type ?: return null
            data.children?.mapNotNullTo(mutableSetOf()) { getValueOfType(it, elementType) }.orEmpty()
        }
        kClass.isSubclassOf(ParadoxDefinitionData::class) -> {
            getDefinitionData(data, kClass.java)
        }
        else -> null
    }
}

private fun getDefinitionData(data: ParadoxScriptData, dataType: Class<out Any>): ParadoxDefinitionData {
    return dataType.getConstructor(ParadoxScriptData::class.java).newInstance(data) as ParadoxDefinitionData
}