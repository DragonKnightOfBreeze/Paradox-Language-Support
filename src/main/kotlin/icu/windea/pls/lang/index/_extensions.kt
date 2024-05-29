package icu.windea.pls.lang.index

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.stubs.*
import com.intellij.util.indexing.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxGameType.*

fun ReadWriteAccessDetector.Access.toByte() = this.ordinal

fun Byte.toReadWriteAccess() = when {
    this == 0.toByte() -> ReadWriteAccessDetector.Access.Read
    this == 1.toByte() -> ReadWriteAccessDetector.Access.Write
    else -> ReadWriteAccessDetector.Access.ReadWrite
}

fun ParadoxGameType.toByte() = this.ordinal

fun Byte.toGameType() = entries[this.toInt()]

fun ParadoxLocalisationCategory.toByte() = this.ordinal

fun Byte.toLocalisationCategory() = ParadoxLocalisationCategory.resolve(this)


inline fun <reified T : StubIndexExtension<*, *>> findStubIndex(): T {
    return StubIndexExtension.EP_NAME.findExtensionOrFail(T::class.java)
}

inline fun <reified T : FileBasedIndexExtension<*, *>> findIndex(): T {
    return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtensionOrFail(T::class.java)
}
