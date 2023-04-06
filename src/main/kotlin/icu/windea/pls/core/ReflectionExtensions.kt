@file:Suppress("unused")

package icu.windea.pls.core

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