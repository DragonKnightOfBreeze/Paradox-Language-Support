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

/** 优先从给定对象[from]读取（存在时），否则执行[readAction]。 */
inline fun <T, V> DataInput.readOrReadFrom(from: T?, selector: (T) -> V, readAction: () -> V): V {
    if (from == null) return readAction()
    if (readBoolean()) return selector(from)
    return readAction()
}

/** 若[value]与[from]在[selector]结果上相等，仅写入布尔位，否则写入布尔位及真实数据。 */
inline fun <T, V> DataOutput.writeOrWriteFrom(value: T, from: T?, selector: (T) -> V, writeAction: (V) -> Unit) {
    if (from == null) return writeAction(selector(value))
    if (selector(value) == selector(from)) return writeBoolean(true)
    writeBoolean(false)
    writeAction(selector(value))
}

/** 写入单字节（便捷重载）。 */
inline fun DataOutput.writeByte(v: Byte) = writeByte(v.toInt())

/** 使用 IDEA 的高效实现读取整型。 */
inline fun DataInput.readIntFast(): Int = DataInputOutputUtil.readINT(this)

/** 使用 IDEA 的高效实现写入整型。 */
inline fun DataOutput.writeIntFast(value: Int) = DataInputOutputUtil.writeINT(this, value)

/** 使用 IDEA 的高效实现读取 UTF 字符串。 */
inline fun DataInput.readUTFFast(): String = IOUtil.readUTF(this)

/** 使用 IDEA 的高效实现写入 UTF 字符串。 */
inline fun DataOutput.writeUTFFast(value: String) = IOUtil.writeUTF(this, value)

/** 查找并返回指定类型的 StubIndex 扩展。 */
inline fun <reified T : StubIndexExtension<*, *>> findStubIndex(): T {
    return StubIndexExtension.EP_NAME.findExtensionOrFail(T::class.java)
}

/** 查找并返回指定类型的 FileBasedIndex 扩展。 */
inline fun <reified T : FileBasedIndexExtension<*, *>> findFileBasedIndex(): T {
    return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtensionOrFail(T::class.java)
}

/** 遍历索引键为[key]的所有元素并处理，哑模式下直接返回 true。 */
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

/** 遍历所有键并按谓词筛选，再处理对应元素，哑模式下直接返回 true。 */
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
 * 遍历所有键，找出第一个满足[predicate]的元素交由[processor]处理；
 * 若未找到则使用[getDefaultValue]；哑模式下直接返回 true。
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
