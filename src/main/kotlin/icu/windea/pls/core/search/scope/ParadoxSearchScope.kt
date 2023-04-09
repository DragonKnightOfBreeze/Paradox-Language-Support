package icu.windea.pls.core.search.scope

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

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
            val fileInfo = file.fileInfo
            return fromFile(project, file, fileInfo)
        }
        
        @JvmStatic
        fun fromFile(project: Project, file: VirtualFile?, fileInfo: ParadoxFileInfo?): GlobalSearchScope {
            if(file == null) return allScope(project)
            if(fileInfo == null) return allScope(project) //use all scope here
            //如果文件不在项目中，改为使用allScope
            if(!ProjectFileIndex.getInstance(project).isInContent(file)) return allScope(project) //use all scope here
            val rootInfo = fileInfo.rootInfo
            when(rootInfo) {
                is ParadoxGameRootInfo -> {
                    val gameDirectory = rootInfo.rootFile
                    val settings = getProfilesSettings().gameSettings.get(gameDirectory.path)
                    val dependencyDirectories =  getDependencyDirectories(settings)
                    return ParadoxGameWithDependenciesSearchScope(project, gameDirectory, dependencyDirectories)
                }
                is ParadoxModRootInfo -> {
                    val modDirectory = rootInfo.rootFile
                    val settings = getProfilesSettings().modSettings.get(modDirectory.path)
                    val gameDirectory = settings?.gameDirectory?.toVirtualFile(false)
                    val dependencyDirectories = getDependencyDirectories(settings, modDirectory)
                    return ParadoxModWithDependenciesSearchScope(project, modDirectory, gameDirectory, dependencyDirectories)
                }
            }
        }
        
        @JvmStatic
        fun modScope(project: Project, context: PsiElement): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE
            if(!ProjectFileIndex.getInstance(project).isInContent(file)) return EMPTY_SCOPE //use empty scope here
            val fileInfo = file.fileInfo
            val rootInfo = fileInfo?.rootInfo
            val modDirectory = rootInfo?.castOrNull<ParadoxModRootInfo>()?.rootFile
            return ParadoxModSearchScope(project, modDirectory)
        }
        
        @JvmStatic
        fun gameScope(project: Project, context: PsiElement): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE
            if(!ProjectFileIndex.getInstance(project).isInContent(file)) return EMPTY_SCOPE //use empty scope here
            val fileInfo = file.fileInfo
            val rootInfo = fileInfo?.rootInfo
            val gameDirectory = rootInfo?.castOrNull<ParadoxGameRootInfo>()?.rootFile
            return ParadoxGameSearchScope(project, gameDirectory)
        }
        
        @JvmStatic
        fun modAndGameScope(project: Project, context: PsiElement): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE
            if(!ProjectFileIndex.getInstance(project).isInContent(file)) return EMPTY_SCOPE //use empty scope here
            val fileInfo = file.fileInfo
            val rootInfo = fileInfo?.rootInfo
            when(rootInfo) {
                is ParadoxGameRootInfo -> {
                    val gameDirectory = rootInfo.rootFile
                    return ParadoxModAndGameSearchScope(project, null, gameDirectory)
                }
                is ParadoxModRootInfo -> {
                    val modDirectory = rootInfo.rootFile
                    return ParadoxModAndGameSearchScope(project, modDirectory, null)
                }
                else -> {
                    return ParadoxModAndGameSearchScope(project, null, null)
                }
            }
        }
        
        @JvmStatic
        fun modWithDependenciesScope(project: Project, context: PsiElement): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE
            if(!ProjectFileIndex.getInstance(project).isInContent(file)) return EMPTY_SCOPE //use empty scope here
            val fileInfo = file.fileInfo
            val rootInfo = fileInfo?.rootInfo
            val modDirectory = rootInfo?.castOrNull<ParadoxModRootInfo>()?.rootFile
            if(modDirectory == null) return ParadoxModWithDependenciesSearchScope(project, null, null, emptySet())
            val settings = getProfilesSettings().gameSettings.get(modDirectory.path)
            val gameDirectory = settings?.gameDirectory?.toVirtualFile(false)
            val dependencyDirectories =  getDependencyDirectories(settings)
            return ParadoxModWithDependenciesSearchScope(project, modDirectory, gameDirectory, dependencyDirectories)
        }
        
        @JvmStatic
        fun gameWithDependenciesScope(project: Project, context: PsiElement): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE
            if(!ProjectFileIndex.getInstance(project).isInContent(file)) return EMPTY_SCOPE //use empty scope here
            val fileInfo = file.fileInfo
            val rootInfo = fileInfo?.rootInfo
            val gameDirectory = rootInfo?.castOrNull<ParadoxGameRootInfo>()?.rootFile
            if(gameDirectory == null) return ParadoxGameWithDependenciesSearchScope(project, null, emptySet())
            val settings = getProfilesSettings().modSettings.get(gameDirectory.path)
            val dependencyDirectories = getDependencyDirectories(settings)
            return ParadoxGameWithDependenciesSearchScope(project, gameDirectory, dependencyDirectories)
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



