package icu.windea.pls.tool.script

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import java.awt.*
import kotlin.reflect.*
import kotlin.reflect.full.*

class ParadoxScriptDataDelegateProvider<T>(
    val delegate: ParadoxScriptData?,
    val defaultValue: T
)

operator fun <T> ParadoxScriptData.get(path: String): ParadoxScriptDataDelegateProvider<T?> {
    return ParadoxScriptDataDelegateProvider(this.getData(path), null)
}

operator fun <T> ParadoxScriptData.get(path: String, defaultValue: T): ParadoxScriptDataDelegateProvider<T> {
    return ParadoxScriptDataDelegateProvider(this.getData(path), defaultValue)
}

val ParadoxScriptData.Keys.propertyValuesKey by lazy { Key.create<MutableMap<KProperty<*>, Any?>>("paradox.data.property.values") }

inline operator fun <reified T> ParadoxScriptDataDelegateProvider<T>.getValue(thisRef: Any, property: KProperty<*>): T {
    if(delegate == null) return defaultValue
    val map = delegate.getOrPutUserData(ParadoxScriptData.Keys.propertyValuesKey) { mutableMapOf() }
    val value = map.getOrPut(property) { InternalExtensionsHolder.getValueOfType(delegate.value, typeOf<T>()) } as? T?
    return value ?: defaultValue
}

@Suppress("UnusedReceiverParameter")
fun InternalExtensionsHolder.getValueOfType(value: ParadoxScriptValue?, type: KType): Any? {
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