package icu.windea.pls.lang.util.data

import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.ep.util.data.ParadoxDefinitionData
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.colorValue
import icu.windea.pls.script.psi.floatValue
import icu.windea.pls.script.psi.intValue
import icu.windea.pls.script.psi.stringValue
import icu.windea.pls.script.psi.value
import java.awt.Color
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.typeOf

inline fun <reified T> ParadoxScriptData.getValue(): T? {
    return ParadoxScriptDataValueProvider.getValue(this, typeOf<T>()) as? T?
}

inline fun <reified T> ParadoxScriptData.getValue(defaultValue: T): T {
    return ParadoxScriptDataValueProvider.getValue(this, typeOf<T>()) as? T? ?: defaultValue
}

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
    val data: ParadoxScriptData?,
    val type: KType,
    val defaultValue: T
) {
    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (data == null) return defaultValue
        return ParadoxScriptDataValueProvider.getValue(data, type) as? T? ?: defaultValue
    }
}

class ParadoxScriptAllDataDelegateProvider<T>(
    val allData: List<ParadoxScriptData>,
    val type: KType,
) {
    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        if (allData.isEmpty()) return emptyList()
        return allData.mapNotNull { ParadoxScriptDataValueProvider.getValue(it, type) as? T? }
    }
}

object ParadoxScriptDataValueProvider {
    private val cacheKey = createKey<MutableMap<KType, Any?>>("paradox.script.data.value.cache")

    fun getValue(data: ParadoxScriptData, type: KType): Any? {
        return doGetValueFromCache(data, type)
    }

    private fun doGetValueFromCache(data: ParadoxScriptData, type: KType): Any? {
        val propertyValues = data.getOrPutUserData(cacheKey) { mutableMapOf() }
        return propertyValues.getOrPut(type) { doGetValue(data, type) ?: EMPTY_OBJECT }.takeUnless { it == EMPTY_OBJECT }
    }

    private fun doGetValue(data: ParadoxScriptData, type: KType): Any? {
        val valueElement = data.valueElement ?: return null
        val kClass = type.classifier as? KClass<*> ?: return null
        return when {
            kClass == Any::class -> valueElement.value()
            kClass == Boolean::class -> valueElement.booleanValue()
            kClass == Int::class -> valueElement.intValue()
            kClass == Float::class -> valueElement.floatValue()
            kClass == String::class -> valueElement.stringValue()
            kClass == Color::class -> valueElement.colorValue()
            kClass.isSubclassOf(List::class) -> {
                val elementType = type.arguments.first().type ?: return null
                data.children?.mapNotNullTo(mutableListOf()) { doGetValue(it, elementType) }.orEmpty()
            }
            kClass.isSubclassOf(Collection::class) -> {
                val elementType = type.arguments.first().type ?: return null
                data.children?.mapNotNullTo(mutableSetOf()) { doGetValue(it, elementType) }.orEmpty()
            }
            kClass.isSubclassOf(ParadoxDefinitionData::class) -> {
                kClass.java.getConstructor(ParadoxScriptData::class.java).newInstance(data) as ParadoxDefinitionData
            }
            else -> null
        }
    }
}
