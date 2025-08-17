package icu.windea.pls.model

import com.intellij.codeInsight.highlighting.*

fun CwtType.optimizeValue() = fromCwtType(this)
fun CwtSeparatorType.optimizeValue() = fromCwtSeparatorType(this)
fun ParadoxGameType.optimizeValue() = fromGameType(this)
fun ParadoxLocalisationType.optimizeValue() = fromLocalisationType(this)
fun ReadWriteAccessDetector.Access.optimizeValue() = fromAccess(this)

inline fun <reified T> Byte.deoptimizeValue() = deoptimizeValue(T::class.java)
@Suppress("UNCHECKED_CAST")
fun <T> Byte.deoptimizeValue(type: Class<T>): T {
    return when (type) {
        CwtType::class.java -> toCwtType(this)
        CwtSeparatorType::class.java -> toCwtSeparatorType(this)
        ParadoxGameType::class.java -> toGameType(this)
        ParadoxLocalisationType::class.java -> toLocalisationType(this)
        ReadWriteAccessDetector.Access::class.java -> toAccess(this)
        else -> throw UnsupportedOperationException()
    } as T
}

private fun fromCwtType(value: CwtType) = value.ordinal.toByte()
private fun toCwtType(value: Byte) = CwtType.entries[value.toInt()]

private fun fromCwtSeparatorType(value: CwtSeparatorType) = value.ordinal.toByte()
private fun toCwtSeparatorType(value: Byte) = CwtSeparatorType.entries[value.toInt()]

private fun fromGameType(value: ParadoxGameType) = value.ordinal.toByte()
private fun toGameType(value: Byte) = ParadoxGameType.entries[value.toInt()]

private fun fromLocalisationType(value: ParadoxLocalisationType) = value.ordinal.toByte()
private fun toLocalisationType(value: Byte) = ParadoxLocalisationType.resolve(value)

private fun fromAccess(value: ReadWriteAccessDetector.Access) = value.ordinal.toByte()
private fun toAccess(value: Byte) = when {
    value == 0.toByte() -> ReadWriteAccessDetector.Access.Read
    value == 1.toByte() -> ReadWriteAccessDetector.Access.Write
    else -> ReadWriteAccessDetector.Access.ReadWrite
}
