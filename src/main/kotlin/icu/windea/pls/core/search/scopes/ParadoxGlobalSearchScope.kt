package icu.windea.pls.core.search.scopes

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

sealed class ParadoxGlobalSearchScope(
    project: Project
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
            if(fileInfo == null) return fileScope(project, file)
            //如果文件不在项目中，改为使用allScope
            if(!ProjectFileIndex.getInstance(project).isInContent(file)) return allScope(project)
            val rootInfo = fileInfo.rootInfo
            val rootFile = rootInfo.rootFile
            val path = rootFile.path
            when(rootInfo) {
                is ParadoxGameRootInfo -> {
                    val settings = getProfilesSettings().gameSettings.get(path)
                    if(settings == null) return fileScope(project, file)
                    val gameDirectory = rootFile
                    val modDependencyDirectories = mutableSetOf<VirtualFile>()
                    for(modDependency in settings.modDependencies) {
                        if(modDependency.enabled) {
                            val modDependencyDirectory = modDependency.modDirectory?.toVirtualFile(false)
                            if(modDependencyDirectory != null) {
                                modDependencyDirectories.add(modDependencyDirectory)
                            }
                        }
                    }
                    return ParadoxGameWithDependenciesScope(project, gameDirectory, modDependencyDirectories)
                }
                is ParadoxModRootInfo -> {
                    val settings = getProfilesSettings().modSettings.get(path)
                    if(settings == null) return fileScope(project, file)
                    val modDirectory = rootFile
                    val gameDirectory = settings.gameDirectory?.toVirtualFile(false)
                    val modDependencyDirectories = mutableSetOf<VirtualFile>()
                    for(modDependency in settings.modDependencies) {
                        if(modDependency.enabled) {
                            val modDependencyDirectory = modDependency.modDirectory?.toVirtualFile(false)
                            if(modDependencyDirectory != null) {
                                modDependencyDirectories.add(modDependencyDirectory)
                            }
                        }
                    }
                    return ParadoxModWithDependenciesScope(project, modDirectory, gameDirectory, modDependencyDirectories)
                }
            }
        }
    }
}



