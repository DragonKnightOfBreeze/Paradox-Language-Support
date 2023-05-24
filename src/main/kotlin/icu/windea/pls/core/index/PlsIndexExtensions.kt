package icu.windea.pls.core.index

import com.intellij.codeInsight.highlighting.*
import icu.windea.pls.lang.model.*

fun ReadWriteAccessDetector.Access.toByte() = this.ordinal

fun Byte.toReadWriteAccess() = when {
    this == 0.toByte() -> ReadWriteAccessDetector.Access.Read
    this == 1.toByte() -> ReadWriteAccessDetector.Access.Write
    else -> ReadWriteAccessDetector.Access.ReadWrite
}

fun ParadoxGameType.toByte() = this.ordinal

fun Byte.toGameType() = ParadoxGameType.values[this.toInt()]