package icu.windea.pls.core.search.scopes

import com.intellij.psi.search.*

fun GlobalSearchScope.withFilePath(filePath: String, fileExtension: String? = null): ParadoxGlobalSearchScope {
    return ParadoxWithFilePathScope(this, filePath, fileExtension)
}