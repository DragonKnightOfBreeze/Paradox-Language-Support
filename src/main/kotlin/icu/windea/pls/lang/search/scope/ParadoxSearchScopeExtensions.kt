package icu.windea.pls.lang.search.scope

import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.lang.search.util.ParadoxSearchSelector

fun GlobalSearchScope.withFileExtensions(fileExtensions: Collection<String>): GlobalSearchScope {
    if (SearchScope.isEmptyScope(this)) return this
    if (fileExtensions.isEmpty()) return this
    return ParadoxWithFileExtensionsSearchScope(this, fileExtensions)
}

fun GlobalSearchScope.withFilePath(filePath: String, fileExtension: String? = null): GlobalSearchScope {
    if (SearchScope.isEmptyScope(this)) return this
    return ParadoxWithFilePathSearchScope(this, filePath, fileExtension)
}

fun GlobalSearchScope.withFileTypes(vararg fileTypes: FileType): GlobalSearchScope {
    if (SearchScope.isEmptyScope(this)) return this
    return GlobalSearchScope.getScopeRestrictedByFileTypes(this, *fileTypes)
}

fun GlobalSearchScope.withConfig(configName: String?, configType: CwtConfigType, selector: ParadoxSearchSelector<*>): GlobalSearchScope {
    if (SearchScope.isEmptyScope(this)) return this
    if (configName.isNullOrEmpty()) return this // skip if config name is empty (not specified)
    val gameType = selector.gameType ?: return this // skip if game type is null (in case)
    val project = selector.project
    val configGroup = CwtConfigGroupService.getInstance(project).getConfigGroup(gameType)
    return ParadoxWithConfigSearchScope(this, configName, configType, configGroup)
}
