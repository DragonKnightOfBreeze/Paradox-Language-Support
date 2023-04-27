package icu.windea.pls.core.search

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.lang.model.*

/**
 * 在查找使用以及其他一些地方提供自定义的额外的查询作用域。
 */
class ParadoxSearchScopeProvider : SearchScopeProvider {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.provider.name")
    }
    
    override fun getSearchScopes(project: Project, dataContext: DataContext): List<SearchScope> {
        val file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
        val fileInfo = file?.fileInfo
        if(fileInfo == null) return emptyList()
        val isInProject = ProjectFileIndex.getInstance(project).isInContent(file)
        val rootInfo = fileInfo.rootInfo
        val rootFile = rootInfo.rootFile
        when {
            rootInfo is ParadoxGameRootInfo -> {
                val settings = getProfilesSettings().gameSettings.get(rootFile.path)
                if(settings == null) return emptyList()
                val gameDirectory = rootFile
                val modDependencyDirectories = ParadoxSearchScope.getDependencyDirectories(settings)
                val result = mutableListOf<SearchScope>()
                result.add(ParadoxGameSearchScope(project, file, rootFile))
                if(isInProject) {
                    result.add(ParadoxGameWithDependenciesSearchScope(project, file, gameDirectory, modDependencyDirectories,))
                }
                return result
            }
            rootInfo is ParadoxModRootInfo -> {
                val settings = getProfilesSettings().modSettings.get(rootFile.path)
                if(settings == null) return emptyList()
                val modDirectory = rootFile
                val gameDirectory = settings.gameDirectory?.toVirtualFile(false)
                val modDependencyDirectories = ParadoxSearchScope.getDependencyDirectories(settings, modDirectory)
                val result = mutableListOf<SearchScope>()
                result.add(ParadoxModSearchScope(project, file, modDirectory))
                if(isInProject) {
                    result.add(ParadoxGameSearchScope(project, file, gameDirectory))
                    result.add(ParadoxModAndGameSearchScope(project, file, modDirectory, gameDirectory))
                    result.add(ParadoxModWithDependenciesSearchScope(project, file, modDirectory, gameDirectory, modDependencyDirectories))
                }
                return result
            }
        }
        return emptyList()
    }
}