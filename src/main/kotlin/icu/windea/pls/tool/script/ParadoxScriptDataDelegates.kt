package icu.windea.pls.tool.script

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import java.awt.*
import kotlin.reflect.*
import kotlin.reflect.full.*

inline operator fun <reified T> ParadoxScriptData.get(path: String): ParadoxScriptDataDelegateProvider<T?> {
    return ParadoxScriptDataDelegateProvider(getData(path), typeOf<T>(), null)
}

inline operator fun <reified T> ParadoxScriptData.get(path: String, defaultValue: T): ParadoxScriptDataDelegateProvider<T> {
    return ParadoxScriptDataDelegateProvider(getData(path), typeOf<T>(), defaultValue)
}

val ParadoxScriptData.Keys.propertyValuesKey by lazy { Key.create<MutableMap<KProperty<*>, Any?>>("paradox.data.property.values") }

class ParadoxScriptDataDelegateProvider<T>(
    val delegate: ParadoxScriptData?,
    val type: KType,
    val defaultValue: T
) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if(delegate == null) return defaultValue
        val map = delegate.getOrPutUserData(ParadoxScriptData.Keys.propertyValuesKey) { mutableMapOf() }
        val value = map.getOrPut(property) { getValueOfType(delegate.value, type) } as? T?
        return value ?: defaultValue
    }
    
    private fun getValueOfType(value: ParadoxScriptValue?, type: KType): Any? {
        if(value == null) return null
        val kClass = type.classifier as? KClass<*> ?: return null
        return when {
            kClass == Any::class -> value.value()
            kClass == Boolean::class -> value.booleanValue()
            kClass == Int::class -> value.intValue()
            kClass == Float::class -> value.floatValue()
            kClass == String::class -> value.stringValue()
            kClass == Color::class -> value.colorValue()
            kClass.isSubclassOf(List::class) -> value.castOrNull<ParadoxScriptBlock>()?.valueList
                ?.mapNotNullTo(mutableListOf()) t@{
                    val elementType = type.arguments.first().type ?: return@t null
                    getValueOfType(it, elementType)
                }
            kClass.isSubclassOf(Collection::class) -> value.castOrNull<ParadoxScriptBlock>()?.valueList
                ?.mapNotNullTo(mutableSetOf()) t@{
                    val elementType = type.arguments.first().type ?: return@t null
                    getValueOfType(it, elementType)
                }
            else -> null
        }
    }
}
