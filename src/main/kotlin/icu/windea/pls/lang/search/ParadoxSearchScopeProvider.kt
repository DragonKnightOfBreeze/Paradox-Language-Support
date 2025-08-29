package icu.windea.pls.lang.search

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.SearchScopeProvider
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.findTopHostFileOrThis
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.search.scope.ParadoxGameSearchScope
import icu.windea.pls.lang.search.scope.ParadoxGameWithDependenciesSearchScope
import icu.windea.pls.lang.search.scope.ParadoxModAndGameSearchScope
import icu.windea.pls.lang.search.scope.ParadoxModSearchScope
import icu.windea.pls.lang.search.scope.ParadoxModWithDependenciesSearchScope
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.settings.finalGameDirectory
import icu.windea.pls.model.ParadoxRootInfo

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
            rootInfo is ParadoxRootInfo.Game -> {
                val settings = PlsFacade.getProfilesSettings().gameSettings.get(rootFile.path)
                if (settings == null) return emptyList()
                val gameDirectory = rootFile
                val modDependencyDirectories = ParadoxSearchScope.getDependencyDirectories(settings)
                val result = mutableListOf<SearchScope>()
                result.add(ParadoxGameSearchScope(project, contextFile, rootFile))
                if (isInProject) {
                    result.add(ParadoxGameWithDependenciesSearchScope(project, contextFile, gameDirectory, modDependencyDirectories))
                }
                return result
            }
            rootInfo is ParadoxRootInfo.Mod -> {
                val settings = PlsFacade.getProfilesSettings().modSettings.get(rootFile.path)
                if (settings == null) return emptyList()
                val modDirectory = rootFile
                val gameDirectory = settings.finalGameDirectory?.toVirtualFile(false)
                val modDependencyDirectories = ParadoxSearchScope.getDependencyDirectories(settings, modDirectory)
                val result = mutableListOf<SearchScope>()
                result.add(ParadoxModSearchScope(project, contextFile, modDirectory))
                if (isInProject) {
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
