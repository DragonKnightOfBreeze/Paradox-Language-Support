package icu.windea.pls.core.search.scopes

import com.intellij.openapi.fileTypes.*
import com.intellij.psi.search.*

fun GlobalSearchScope.withFilePath(filePath: String, fileExtension: String? = null): GlobalSearchScope {
    return ParadoxWithFilePathScope(this, filePath, fileExtension)
}

fun GlobalSearchScope.withFileTypes(vararg fileTypes: FileType): GlobalSearchScope {
    return GlobalSearchScope.getScopeRestrictedByFileTypes(this, *fileTypes)
}