@file:Suppress("unused")

package icu.windea.pls.core

import com.intellij.openapi.components.*
import kotlin.reflect.full.*

inline fun <reified T: Any> T.getFieldValue(fieldName: String): Any? {
    val field = T::class.java.getDeclaredField(fieldName)
    field.trySetAccessible()
    return field.get(this)
}

inline fun <reified T: Any> T.setFieldValue(fieldName: String, value: Any?) {
    val field = T::class.java.getDeclaredField(fieldName)
    field.trySetAccessible()
    field.set(this, value)
}


@Suppress("UNCHECKED_CAST") 
val BaseState.propertyNames get() = (getFieldValue("properties") as MutableList<StoredProperty<Any>>).map { it.name }