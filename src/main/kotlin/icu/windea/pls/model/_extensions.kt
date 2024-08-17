package icu.windea.pls.model

import com.intellij.codeInsight.highlighting.*

fun ReadWriteAccessDetector.Access.optimizeValue() = fromAccess(this)
fun ParadoxGameType.optimizeValue() = fromGameType(this)
fun ParadoxLocalisationCategory.optimizeValue() = fromLocalisationCategory(this)

inline fun <reified T> Byte.deoptimizeValue() = deoptimizeValue(T::class.java)
@Suppress("UNCHECKED_CAST") fun <T> Byte.deoptimizeValue(type: Class<T>): T {
    return when(type) {
        ReadWriteAccessDetector.Access::class.java -> toAccess(this)
        ParadoxGameType::class.java -> toGameType(this)
        ParadoxLocalisationCategory::class.java -> toLocalisationCategory(this)
        else -> throw UnsupportedOperationException()
    } as T
}

private fun fromAccess(value: ReadWriteAccessDetector.Access): Byte {
    return value.ordinal.toByte()
}

private fun toAccess(value: Byte): ReadWriteAccessDetector.Access {
    return when {
        value == 0.toByte() -> ReadWriteAccessDetector.Access.Read
        value == 1.toByte() -> ReadWriteAccessDetector.Access.Write
        else -> ReadWriteAccessDetector.Access.ReadWrite
    }
}

private fun fromGameType(value: ParadoxGameType): Byte {
    return value.ordinal.toByte()
}

private fun toGameType(value: Byte): ParadoxGameType {
    return ParadoxGameType.entries[value.toInt()]
}

private fun fromLocalisationCategory(value: ParadoxLocalisationCategory): Byte {
    return value.ordinal.toByte()
} 

private fun toLocalisationCategory(value: Byte): ParadoxLocalisationCategory {
    return ParadoxLocalisationCategory.resolve(value)
}
