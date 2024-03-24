package icu.windea.pls.lang.search.scope

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.model.*

sealed class ParadoxSearchScope(
    project: Project?
) : GlobalSearchScope(project) {
    override fun isSearchInModuleContent(aModule: Module): Boolean {
        return true
    }
    
    override fun isSearchInLibraries(): Boolean {
        return true
    }
    
    companion object {
        @JvmStatic
        fun fromElement(element: PsiElement): GlobalSearchScope? {
            val psiFile = element.containingFile?.originalFile ?: return null
            val file = psiFile.virtualFile ?: return null
            val project = psiFile.project
            return fromFile(project, file)
        }
        
        @JvmStatic
        fun fromFile(project: Project, file: VirtualFile?): GlobalSearchScope? {
            if(file == null) return null
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo
            if(rootInfo == null) return null
            if(!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return null
            when(rootInfo) {
                is ParadoxGameRootInfo -> {
                    val gameDirectory = rootInfo.rootFile
                    val settings = getProfilesSettings().gameSettings.get(gameDirectory.path)
                    val dependencyDirectories = getDependencyDirectories(settings)
                    return ParadoxGameWithDependenciesSearchScope(project, contextFile, gameDirectory, dependencyDirectories)
                }
                is ParadoxModRootInfo -> {
                    val modDirectory = rootInfo.rootFile
                    val settings = getProfilesSettings().modSettings.get(modDirectory.path)
                    val gameDirectory = settings?.finalGameDirectory?.toVirtualFile(false)
                    val dependencyDirectories = getDependencyDirectories(settings, modDirectory)
                    return ParadoxModWithDependenciesSearchScope(project, contextFile, modDirectory, gameDirectory, dependencyDirectories)
                }
            }
        }
        
        @JvmStatic
        fun modScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if(!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            val modDirectory = rootInfo.castOrNull<ParadoxModRootInfo>()?.rootFile
            return ParadoxModSearchScope(project, contextFile, modDirectory)
        }
        
        @JvmStatic
        fun gameScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if(!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            val gameDirectory = rootInfo.castOrNull<ParadoxGameRootInfo>()?.rootFile
            return ParadoxGameSearchScope(project, contextFile, gameDirectory)
        }
        
        @JvmStatic
        fun modAndGameScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if(!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            when(rootInfo) {
                is ParadoxGameRootInfo -> {
                    val gameDirectory = rootInfo.rootFile
                    return ParadoxModAndGameSearchScope(project, contextFile, null, gameDirectory)
                }
                is ParadoxModRootInfo -> {
                    val modDirectory = rootInfo.rootFile
                    return ParadoxModAndGameSearchScope(project, contextFile, modDirectory, null)
                }
                else -> {
                    return ParadoxModAndGameSearchScope(project, contextFile, null, null)
                }
            }
        }
        
        @JvmStatic
        fun modWithDependenciesScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if(!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            val modDirectory = rootInfo.castOrNull<ParadoxModRootInfo>()?.rootFile
            if(modDirectory == null) return ParadoxModWithDependenciesSearchScope(project, contextFile, null, null, emptySet())
            val settings = getProfilesSettings().gameSettings.get(modDirectory.path)
            val gameDirectory = settings?.gameDirectory?.toVirtualFile(false)
            val dependencyDirectories = getDependencyDirectories(settings)
            return ParadoxModWithDependenciesSearchScope(project, contextFile, modDirectory, gameDirectory, dependencyDirectories)
        }
        
        @JvmStatic
        fun gameWithDependenciesScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if(!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            val gameDirectory = rootInfo.castOrNull<ParadoxGameRootInfo>()?.rootFile
            if(gameDirectory == null) return ParadoxGameWithDependenciesSearchScope(project, contextFile, null, emptySet())
            val settings = getProfilesSettings().modSettings.get(gameDirectory.path)
            val dependencyDirectories = getDependencyDirectories(settings)
            return ParadoxGameWithDependenciesSearchScope(project, contextFile, gameDirectory, dependencyDirectories)
        }
        
        @JvmStatic
        fun getDependencyDirectories(settings: ParadoxGameOrModSettingsState?, modDirectory: VirtualFile? = null): Set<VirtualFile> {
            if(settings == null) return emptySet()
            val modDependencyDirectories = mutableSetOf<VirtualFile>()
            for(modDependency in settings.modDependencies) {
                //要求模组依赖是启用的，或者是当前模组自身
                if(modDependency.enabled || (modDirectory != null && modDependency.modDirectory == modDirectory.path)) {
                    val modDependencyDirectory = modDependency.modDirectory?.toVirtualFile(false)
                    if(modDependencyDirectory != null) {
                        modDependencyDirectories.add(modDependencyDirectory)
                    }
                }
            }
            return modDependencyDirectories
        }
    }
}



