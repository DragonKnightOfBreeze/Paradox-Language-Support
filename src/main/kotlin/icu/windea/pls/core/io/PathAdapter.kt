package icu.windea.pls.core.io

import icu.windea.pls.core.*
import java.nio.file.*
import kotlin.reflect.*

class PathAdapter(
    val filePath: Path,
    val action: (Path) -> Unit
) {
    fun get(): Path {
        return synchronized(this) { doGet() }
    }

    private fun doGet(): Path {
        if (filePath.exists()) action(filePath)
        return filePath
    }
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun PathAdapter.getValue(thisRef: Any?, property: KProperty<*>): Path = get()
