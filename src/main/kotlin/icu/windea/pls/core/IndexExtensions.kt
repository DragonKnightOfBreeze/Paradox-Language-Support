@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.io.DataInputOutputUtil
import com.intellij.util.io.IOUtil
import java.io.DataInput
import java.io.DataOutput

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

/** 查找注册的 StubIndex 扩展。*/
inline fun <reified T : StubIndexExtension<*, *>> findStubIndex(): T {
    return StubIndexExtension.EP_NAME.findExtensionOrFail(T::class.java)
}

/** 查找注册的 FileBasedIndex 扩展。*/
inline fun <reified T : FileBasedIndexExtension<*, *>> findFileBasedIndex(): T {
    return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtensionOrFail(T::class.java)
}

/**
 * 遍历给定键 [key] 下的所有元素并用 [processor] 处理。
 *
 * Dumb 模式下直接返回 `true`。
 */
inline fun <K : Any, reified T : PsiElement> StubIndexKey<K, T>.processAllElements(
    key: K,
    project: Project,
    scope: GlobalSearchScope,
    crossinline processor: (T) -> Boolean
): Boolean {
    if (DumbService.isDumb(project)) return true

    return StubIndex.getInstance().processElements(this, key, project, scope, T::class.java) { element ->
        ProgressManager.checkCanceled()
        processor(element)
    }
}

/**
 * 遍历所有键，并对命中的键（满足 [keyPredicate]）下的元素调用 [processor]。
 *
 * 提供按键粒度的过滤与处理能力。
 */
inline fun <K : Any, reified T : PsiElement> StubIndexKey<K, T>.processAllElementsByKeys(
    project: Project,
    scope: GlobalSearchScope,
    crossinline keyPredicate: (key: K) -> Boolean = { true },
    crossinline processor: (key: K, element: T) -> Boolean
): Boolean {
    if (DumbService.isDumb(project)) return true

    return StubIndex.getInstance().processAllKeys(this, p@{ key ->
        ProgressManager.checkCanceled()
        if (keyPredicate(key)) {
            StubIndex.getInstance().processElements(this, key, project, scope, T::class.java) { element ->
                ProgressManager.checkCanceled()
                processor(key, element)
            }
        }
        true
    }, scope)
}

/**
 * 遍历所有键，找到第一个满足 [predicate] 的元素，并调用 [processor]；若未找到，则使用 [getDefaultValue] 的返回值（可为 null）。
 *
 * 可在遍历每个键前调用 [resetDefaultValue] 重置外部缓存。
 */
inline fun <K : Any, reified T : PsiElement> StubIndexKey<K, T>.processFirstElementByKeys(
    project: Project,
    scope: GlobalSearchScope,
    crossinline keyPredicate: (key: K) -> Boolean = { true },
    crossinline predicate: (element: T) -> Boolean = { true },
    crossinline getDefaultValue: () -> T? = { null },
    crossinline resetDefaultValue: () -> Unit = {},
    crossinline processor: (element: T) -> Boolean
): Boolean {
    if (DumbService.isDumb(project)) return true

    var value: T?
    return StubIndex.getInstance().processAllKeys(this, p@{ key ->
        ProgressManager.checkCanceled()
        if (keyPredicate(key)) {
            value = null
            resetDefaultValue()
            StubIndex.getInstance().processElements(this, key, project, scope, T::class.java) { element ->
                ProgressManager.checkCanceled()
                if (predicate(element)) {
                    value = element
                    return@processElements false
                }
                true
            }
            val finalValue = value ?: getDefaultValue()
            if (finalValue != null) {
                val result = processor(finalValue)
                if (!result) return@p false
            }
        }
        true
    }, scope)
}
