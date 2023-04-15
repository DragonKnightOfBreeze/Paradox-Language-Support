@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.tool.script

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import java.awt.*
import kotlin.reflect.*
import kotlin.reflect.full.*

operator fun <T> ParadoxScriptData.get(path: String): ParadoxScriptDataWrapper<T?> {
    return ParadoxScriptDataWrapper(this.getData(path), null)
}

operator fun <T> ParadoxScriptData.get(path: String, defaultValue: T): ParadoxScriptDataWrapper<T> {
    return ParadoxScriptDataWrapper(this.getData(path), defaultValue)
}

val ParadoxScriptData.Keys.propertyValuesKey by lazy { Key.create<MutableMap<KProperty<*>, Any?>>("paradox.data.property.values") }

inline operator fun <reified T> ParadoxScriptDataWrapper<T>.getValue(thisRef: Any, property: KProperty<*>): T {
    if(delegate == null) return defaultValue
    val map = delegate.getOrPutUserData(ParadoxScriptData.Keys.propertyValuesKey) { mutableMapOf() }
    val value = map.getOrPut(property) { getValueOfType(delegate.value, typeOf<T>()) } as? T?
    return value ?: defaultValue
}

@PublishedApi
internal fun getValueOfType(value: ParadoxScriptValue?, type: KType): Any? {
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
            ?.mapNotNullTo(SmartList()) t@{
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