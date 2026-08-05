package icu.windea.pls.core.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector

typealias ReadWriteAccess = ReadWriteAccessDetector.Access

// region Inline Methods

@Suppress("NOTHING_TO_INLINE", "unused")
inline fun ReadWriteAccess.optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

// endregion

object ReadWriteAccessC {
    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun deoptimized(value: Byte): ReadWriteAccess = ReadWriteAccess.entries[value.toInt()] // 3.0.1 radical optimization

    // endregion
}
