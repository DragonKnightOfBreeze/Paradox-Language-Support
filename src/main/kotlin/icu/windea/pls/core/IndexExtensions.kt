@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.io.DataInputOutputUtil
import com.intellij.util.io.IOUtil
import java.io.DataInput
import java.io.DataOutput

fun IndexInputFilter(predicate: (VirtualFile) -> Boolean): FileBasedIndex.InputFilter {
    return FileBasedIndex.InputFilter(predicate)
}

// fun IndexInputFilter(vararg fileTypes: FileType): FileBasedIndex.InputFilter {
//     if (fileTypes.isEmpty()) return FileBasedIndex.InputFilter { true }
//     return DefaultFileTypeSpecificInputFilter(*fileTypes)
// }
//
// fun IndexInputFilter(vararg fileTypes: FileType, predicate: (VirtualFile) -> Boolean): FileBasedIndex.InputFilter {
//     if (fileTypes.isEmpty()) return FileBasedIndex.InputFilter(predicate)
//     return object : DefaultFileTypeSpecificInputFilter(*fileTypes) {
//         override fun acceptInput(file: VirtualFile): Boolean {
//             return predicate(file)
//         }
//     }
// }

/**
 * 从 [from] 复用已有值或从输入流读取。
 *
 * 当 [from] 为空时执行 [readAction]；否则先读一个布尔标记，若为 `true` 则复用 [from] 的字段 [selector]，否则执行 [readAction]。
 */
inline fun <T, V> DataInput.readOrReadFrom(from: T?, selector: (T) -> V, readAction: () -> V): V {
    if (from == null) return readAction()
    if (readBoolean()) return selector(from)
    return readAction()
}

/**
 * 将 [value] 写入输出流，必要时与 [from] 进行增量对比以节省空间。
 *
 * 当 [from] 不为空且 [selector(value)] 与 [selector(from)] 相等时，写入布尔 `true`；否则写入 `false` 并执行 [writeAction]。
 */
inline fun <T, V> DataOutput.writeOrWriteFrom(value: T, from: T?, selector: (T) -> V, writeAction: (V) -> Unit) {
    if (from == null) return writeAction(selector(value))
    if (selector(value) == selector(from)) return writeBoolean(true)
    writeBoolean(false)
    writeAction(selector(value))
}

/** 写入单字节（避免显式 toInt 调用）。*/
inline fun DataOutput.writeByte(v: Byte) = writeByte(v.toInt())

/** 使用 IDEA 提供的紧凑编码读写 `Int`。*/
inline fun DataInput.readIntFast(): Int = DataInputOutputUtil.readINT(this)

inline fun DataOutput.writeIntFast(value: Int) = DataInputOutputUtil.writeINT(this, value)

/** 使用 IDEA 提供的 UTF 编解码读写 `String`。*/
inline fun DataInput.readUTFFast(): String = IOUtil.readUTF(this)

inline fun DataOutput.writeUTFFast(value: String) = IOUtil.writeUTF(this, value)

// /** 查找注册的 StubIndex 扩展。*/
// inline fun <reified T : StubIndexExtension<*, *>> findStubIndex(): T {
//     return StubIndexExtension.EP_NAME.findExtensionOrFail(T::class.java)
// }

/** 查找注册的 FileBasedIndex 扩展。*/
inline fun <reified T : FileBasedIndexExtension<*, *>> findFileBasedIndex(): T {
    return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtensionOrFail(T::class.java)
}
