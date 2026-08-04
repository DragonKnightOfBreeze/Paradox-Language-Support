package icu.windea.pls.lang.search.scope

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.matchesPath
import icu.windea.pls.lang.fileInfo

@Suppress("EqualsOrHashCode")
class ParadoxWithFilePathSearchScope(
    val delegate: GlobalSearchScope,
    val filePath: String,
    val fileExtension: String? = null
) : ParadoxSearchScope(delegate.project, null) {
    override fun getDisplayName(): String {
        return ChronicleBundle.message("search.scope.name.withFilePath", delegate.displayName, filePath, fileExtension.orEmpty())
    }

    override fun contains(file: VirtualFile): Boolean {
        if (!delegate.contains(file)) return false // NOTE 3.0.1 should check delegate first
        return super.contains(file)
    }

    override fun containsFromTop(topFile: VirtualFile): Boolean {
        if (fileExtension != null) {
            val extension = topFile.extension
            if (fileExtension != extension) return false
        }
        val path = topFile.fileInfo?.path?.path ?: return false
        return filePath.matchesPath(path)
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
