@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

inline fun Any.getFieldValue(fieldName: String): Any? {
    val field = this::class.java.getDeclaredField(fieldName)
    field.trySetAccessible()
    return field.get(this)
}

@JvmName("getFieldValueOf")
inline fun <reified T : Any> Any.getFieldValue(fieldName: String): Any? {
    val field = T::class.java.getDeclaredField(fieldName)
    field.trySetAccessible()
    return field.get(this)
}

inline fun Any.setFieldValue(fieldName: String, value: Any?) {
    val field = this::class.java.getDeclaredField(fieldName)
    field.trySetAccessible()
    field.set(this, value)
}

@JvmName("setFieldValueOf")
inline fun <reified T : Any> Any.setFieldValue(fieldName: String, value: Any?) {
    val field = T::class.java.getDeclaredField(fieldName)
    field.trySetAccessible()
    field.set(this, value)
}
