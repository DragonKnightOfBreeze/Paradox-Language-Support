package icu.windea.pls.lang.search.scope

import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope

fun GlobalSearchScope.withFilePath(filePath: String, fileExtension: String? = null): GlobalSearchScope {
    if (SearchScope.isEmptyScope(this)) return this
    return ParadoxWithFilePathSearchScope(this, filePath, fileExtension)
}

fun GlobalSearchScope.withFileTypes(vararg fileTypes: FileType): GlobalSearchScope {
    if (SearchScope.isEmptyScope(this)) return this
    return GlobalSearchScope.getScopeRestrictedByFileTypes(this, *fileTypes)
}
