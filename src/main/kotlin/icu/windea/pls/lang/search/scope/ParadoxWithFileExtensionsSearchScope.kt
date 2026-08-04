package icu.windea.pls.lang.search.scope

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.ChronicleBundle

@Suppress("EqualsOrHashCode")
class ParadoxWithFileExtensionsSearchScope(
    val delegate: GlobalSearchScope,
    val fileExtensions: Collection<String>,
) : ParadoxSearchScope(delegate.project, null) {
    override fun getDisplayName(): String {
        return ChronicleBundle.message("search.scope.name.withFileExtensions", delegate.displayName, fileExtensions.joinToString())
    }

    override fun contains(file: VirtualFile): Boolean {
        if (!delegate.contains(file)) return false // NOTE 3.0.1 should check delegate first
        return super.contains(file)
    }

    override fun containsFromTop(topFile: VirtualFile): Boolean {
        return fileExtensions.isEmpty() || topFile.extension in fileExtensions
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
        return "$delegate - with file extensions of ${fileExtensions.joinToString()}"
    }
}
