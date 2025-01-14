@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import java.io.*

inline fun <T, V> DataInput.readOrReadFrom(from: T?, selector: (T) -> V, readAction: () -> V): V {
    if (from == null) return readAction()
    if (readBoolean()) return selector(from)
    return readAction()
}

inline fun <T, V> DataOutput.writeOrWriteFrom(value: T, from: T?, selector: (T) -> V, writeAction: (V) -> Unit) {
    if (from == null) return writeAction(selector(value))
    if (selector(value) == selector(from)) return writeBoolean(true)
    writeBoolean(false)
    writeAction(selector(value))
}

inline fun DataOutput.writeByte(v: Byte) = writeByte(v.toInt())

inline fun DataInput.readIntFast(): Int = DataInputOutputUtil.readINT(this)

inline fun DataOutput.writeIntFast(value: Int) = DataInputOutputUtil.writeINT(this, value)

inline fun DataInput.readUTFFast(): String = IOUtil.readUTF(this)

inline fun DataOutput.writeUTFFast(value: String) = IOUtil.writeUTF(this, value)

inline fun <reified T : StubIndexExtension<*, *>> findStubIndex(): T {
    return StubIndexExtension.EP_NAME.findExtensionOrFail(T::class.java)
}

inline fun <reified T : FileBasedIndexExtension<*, *>> findFileBasedIndex(): T {
    return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtensionOrFail(T::class.java)
}

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
