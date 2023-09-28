package icu.windea.pls.core.index

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.stubs.*
import com.intellij.util.indexing.*
import icu.windea.pls.model.*

fun ReadWriteAccessDetector.Access.toByte() = this.ordinal

fun Byte.toReadWriteAccess() = when {
    this == 0.toByte() -> ReadWriteAccessDetector.Access.Read
    this == 1.toByte() -> ReadWriteAccessDetector.Access.Write
    else -> ReadWriteAccessDetector.Access.ReadWrite
}

fun ParadoxGameType.toByte() = this.ordinal

fun Byte.toGameType() = ParadoxGameType.values[this.toInt()]

fun ParadoxLocalisationCategory.toByte() = this.ordinal

fun Byte.toLocalisationCategory() = ParadoxLocalisationCategory.values[this.toInt()]


inline fun <reified T: StubIndexExtension<*,*>> findStubIndex(): T{
    return StubIndexExtension.EP_NAME.findExtensionOrFail(T::class.java)
}

inline fun <reified T : FileBasedIndexExtension<*, *>> findIndex(): T {
    return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtensionOrFail(T::class.java)
}

val ParadoxScriptedVariableNameIndexKey by lazy { findStubIndex<ParadoxScriptedVariableNameIndex>().getKey() }
val ParadoxDefinitionNameIndexKey by lazy { findStubIndex<ParadoxDefinitionNameIndex>().getKey() }
val ParadoxDefinitionTypeIndexKey by lazy { findStubIndex<ParadoxDefinitionTypeIndex>().getKey() }
val ParadoxLocalisationNameIndexKey by lazy { findStubIndex<ParadoxLocalisationNameIndex>().getKey() }
val ParadoxLocalisationNameIndexModifierKey by lazy { findStubIndex<ParadoxLocalisationNameIndex.ModifierIndex>().getKey() }
val ParadoxSyncedLocalisationNameIndexKey by lazy { findStubIndex<ParadoxSyncedLocalisationNameIndex>().getKey() }

val ParadoxFilePathIndexName by lazy { findIndex<ParadoxFilePathIndex>().name }
val ParadoxFileLocaleIndexName by lazy { findIndex<ParadoxFileLocaleIndex>().name }
val ParadoxExpressionIndexName by lazy { findIndex<ParadoxExpressionIndex>().name }