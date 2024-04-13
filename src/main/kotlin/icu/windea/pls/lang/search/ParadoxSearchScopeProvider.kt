package icu.windea.pls.lang.search

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*

/**
 * 在查找使用以及其他一些地方提供自定义的额外的查询作用域。
 */
class ParadoxSearchScopeProvider : SearchScopeProvider {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.provider.name")
    }
    
    override fun getSearchScopes(project: Project, dataContext: DataContext): List<SearchScope> {
        val file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE) ?: return emptyList()
        val contextFile = file.findTopHostFileOrThis()
        val rootFile = selectRootFile(contextFile) ?: return emptyList()
        val rootInfo = rootFile.rootInfo ?: return emptyList()
        val isInProject = ProjectFileIndex.getInstance(project).isInContent(contextFile)
        when {
            rootInfo is ParadoxGameRootInfo -> {
                val settings = getProfilesSettings().gameSettings.get(rootFile.path)
                if(settings == null) return emptyList()
                val gameDirectory = rootFile
                val modDependencyDirectories = ParadoxSearchScope.getDependencyDirectories(settings)
                val result = mutableListOf<SearchScope>()
                result.add(ParadoxGameSearchScope(project, contextFile, rootFile))
                if(isInProject) {
                    result.add(ParadoxGameWithDependenciesSearchScope(project, contextFile, gameDirectory, modDependencyDirectories,))
                }
                return result
            }
            rootInfo is ParadoxModRootInfo -> {
                val settings = getProfilesSettings().modSettings.get(rootFile.path)
                if(settings == null) return emptyList()
                val modDirectory = rootFile
                val gameDirectory = settings.finalGameDirectory?.toVirtualFile(false)
                val modDependencyDirectories = ParadoxSearchScope.getDependencyDirectories(settings, modDirectory)
                val result = mutableListOf<SearchScope>()
                result.add(ParadoxModSearchScope(project, contextFile, modDirectory))
                if(isInProject) {
                    result.add(ParadoxGameSearchScope(project, contextFile, gameDirectory))
                    result.add(ParadoxModAndGameSearchScope(project, contextFile, modDirectory, gameDirectory))
                    result.add(ParadoxModWithDependenciesSearchScope(project, contextFile, modDirectory, gameDirectory, modDependencyDirectories))
                }
                return result
            }
        }
        return emptyList()
    }
}