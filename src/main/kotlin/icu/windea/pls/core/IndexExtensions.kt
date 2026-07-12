@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import com.intellij.psi.stubs.StubIndexExtension
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.io.DataInputOutputUtil
import com.intellij.util.io.IOUtil
import icu.windea.pls.core.collections.forEachFast
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.io.DataInput
import java.io.DataOutput

/** 查找注册的 StubIndex 扩展。 */
fun <T : StubIndexExtension<*, *>> findStubIndex(type: Class<T>): T {
    return StubIndexExtension.EP_NAME.findExtensionOrFail(type)
}

/** 查找注册的 FileBasedIndex 扩展。 */
fun <T : FileBasedIndexExtension<*, *>> findFileBasedIndex(type: Class<T>): T {
    return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtensionOrFail(type)
}

// fun IndexInputFilter(predicate: (VirtualFile) -> Boolean): FileBasedIndex.InputFilter {
//     return FileBasedIndex.InputFilter(predicate)
// }
//
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

inline fun DataOutput.writeByte(v: Byte) = writeByte(v.toInt())

inline fun DataInput.readIntFast(): Int = DataInputOutputUtil.readINT(this)

inline fun DataOutput.writeIntFast(value: Int) = DataInputOutputUtil.writeINT(this, value)

inline fun DataInput.readUTFFast(): String = IOUtil.readUTF(this)

inline fun DataOutput.writeUTFFast(value: String) = IOUtil.writeUTF(this, value)

/**
 * 将 [value] 写入输出流，必要时与 [from] 进行增量对比以节省空间。
 *
 * 当 [from] 不为空且 `selector(value)` 与 `selector(from)` 相等时，写入 `true`；否则写入 `false` 并执行 [writeAction]。
 */
inline fun <T, V> DataOutput.writeOrWriteFrom(value: T, from: T?, selector: (T) -> V, writeAction: (V) -> Unit) {
    if (from == null) return writeAction(selector(value))
    if (selector(value) == selector(from)) return writeBoolean(true)
    writeBoolean(false)
    writeAction(selector(value))
}

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
 * 对 [list] 执行转换操作 [transform]，写入结果集合的大小与各个元素，然后转换为以索引为值的映射。
 */
inline fun <T> DataOutput.writeIndexedStringList(list: List<T>, transform: (T) -> String?): Object2IntMap<String> {
    val keys = ObjectOpenHashSet<String>()
    list.forEachFast { info ->
        val key = transform(info)
        if (key != null) keys.add(key)
    }
    val size = keys.size
    writeIntFast(size)
    if (size != 0) keys.forEach { writeUTFFast(it) }
    val result = Object2IntOpenHashMap<String>()
    result.defaultReturnValue(-1)
    if (size != 0) keys.forEachIndexed { index, key -> result.put(key, index) }
    return result
}

/**
 * 读取集合的大小与各个元素，然后转换为以索引为键的映射。
 */
inline fun DataInput.readWithIndexStringList(): Int2ObjectMap<String> {
    val size = readIntFast()
    val result = Int2ObjectOpenHashMap<String>()
    repeat(size) { index ->
        val key = readUTFFast()
        result.put(index, key)
    }
    return result
}
