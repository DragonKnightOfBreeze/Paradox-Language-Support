package icu.windea.pls.lang.search.scope

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.matchesPath
import icu.windea.pls.lang.fileInfo

@Suppress("EqualsOrHashCode")
class ParadoxWithFileExtensionsSearchScope(
    val delegate: GlobalSearchScope,
    val fileExtensions: Collection<String>,
) : ParadoxSearchScope(delegate.project, null) {
    override fun getDisplayName(): String {
        return ChronicleBundle.message("search.scope.name.withFileExtensions", delegate.displayName, fileExtensions.joinToString())
    }

    override fun containsFromTop(topFile: VirtualFile): Boolean {
        if (fileExtensions.isNotEmpty() && topFile.extension !in fileExtensions) return false
        return delegate.contains(topFile)
    }

    override fun calcHashCode(): Int {
        var result = delegate.hashCode()
        result = result * 31 + fileExtensions.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ParadoxWithFileExtensionsSearchScope
            && delegate == other.delegate
            && fileExtensions == other.fileExtensions
    }

    override fun toString(): String {
        return "$delegate - with file extensions ${fileExtensions.joinToString()}"
    }
}

@Suppress("EqualsOrHashCode")
class ParadoxWithFilePathSearchScope(
    val delegate: GlobalSearchScope,
    val filePath: String,
    val fileExtension: String? = null
) : ParadoxSearchScope(delegate.project, null) {
    override fun getDisplayName(): String {
        return ChronicleBundle.message("search.scope.name.withFilePath", delegate.displayName, filePath, fileExtension.orEmpty())
    }

    override fun containsFromTop(topFile: VirtualFile): Boolean {
        if (fileExtension != null && fileExtension != topFile.extension) return false
        val path = topFile.fileInfo?.path?.path ?: return false
        if (!filePath.matchesPath(path)) return false
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
