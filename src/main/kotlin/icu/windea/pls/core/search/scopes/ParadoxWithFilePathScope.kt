package icu.windea.pls.core.search.scopes

import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*

@Suppress("EqualsOrHashCode")
class ParadoxWithFilePathScope(
    val delegate: GlobalSearchScope,
    val filePath: String,
    val fileExtension: String? = null
) : ParadoxGlobalSearchScope(delegate.project) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.filePathAware", delegate.displayName, filePath, fileExtension.orEmpty())
    }
    
    override fun contains(file: VirtualFile): Boolean {
        val path = file.fileInfo?.path?.path ?: return false
        if(!filePath.matchesPath(path)) return false
        if(fileExtension != null && fileExtension != file.extension) return false
        return delegate.contains(file)
    }
    
    override fun calcHashCode(): Int {
        var result = delegate.hashCode()
        result = result * 31 + filePath.hashCode()
        result = result * 31 + fileExtension.hashCode()
        return result
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxWithFilePathScope
            && delegate == other.delegate
            && filePath == other.filePath
            && fileExtension == other.fileExtension
    }
    
    override fun toString(): String {
        return "$delegate - in $filePath with file extension ${fileExtension.orEmpty()}"
    }
}