package icu.windea.pls.lang.search.scope

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.matchesPath
import icu.windea.pls.lang.fileInfo

@Suppress("EqualsOrHashCode")
class ParadoxWithFilePathSearchScope(
    val delegate: GlobalSearchScope,
    val filePath: String,
    val fileExtension: String? = null
) : ParadoxSearchScope(delegate.project, null) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.filePathAware", delegate.displayName, filePath, fileExtension.orEmpty())
    }

    override fun containsFromTop(topFile: VirtualFile): Boolean {
        val path = topFile.fileInfo?.path?.path ?: return false
        if (!filePath.matchesPath(path)) return false
        if (fileExtension != null && fileExtension != topFile.extension) return false
        return delegate.contains(topFile)
    }

    override fun calcHashCode(): Int {
        var result = delegate.hashCode()
        result = result * 31 + filePath.hashCode()
        result = result * 31 + fileExtension.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ParadoxWithFilePathSearchScope
            && delegate == other.delegate
            && filePath == other.filePath
            && fileExtension == other.fileExtension
    }

    override fun toString(): String {
        return "$delegate - in $filePath with file extension ${fileExtension.orEmpty()}"
    }
}
