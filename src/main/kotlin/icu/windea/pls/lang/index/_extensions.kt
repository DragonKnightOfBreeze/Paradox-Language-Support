package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import com.intellij.util.indexing.*
import java.io.*

@Suppress("NOTHING_TO_INLINE")
inline fun DataOutput.writeByte(v: Byte) = writeByte(v.toInt())

inline fun <reified T : StubIndexExtension<*, *>> findStubIndex(): T {
    return StubIndexExtension.EP_NAME.findExtensionOrFail(T::class.java)
}

inline fun <reified T : FileBasedIndexExtension<*, *>> findIndex(): T {
    return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtensionOrFail(T::class.java)
}
