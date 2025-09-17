package icu.windea.pls.lang.search.scope

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findTopHostFileOrThis
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxRootInfo

open class ParadoxSearchScope(
    project: Project?,
    val contextFile: VirtualFile?,
) : GlobalSearchScope(project) {
    override fun isSearchInModuleContent(aModule: Module): Boolean {
        return true
    }

    override fun isSearchInLibraries(): Boolean {
        return true
    }

    override fun contains(file: VirtualFile): Boolean {
        val topFile = file.findTopHostFileOrThis()
        if (!ParadoxFileManager.canReference(contextFile, topFile)) return false //判断上下文文件能否引用另一个文件中的内容
        return containsFromTop(topFile)
    }

    protected open fun containsFromTop(topFile: VirtualFile): Boolean {
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
            if (file == null) return null
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo
            if (rootInfo == null) return null
            if (!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return null
            when (rootInfo) {
                is ParadoxRootInfo.Game -> {
                    val gameDirectory = rootInfo.rootFile
                    val settings = PlsFacade.getProfilesSettings().gameSettings.get(gameDirectory.path)
                    val dependencyDirectories = getDependencyDirectories(settings)
                    return ParadoxGameWithDependenciesSearchScope(project, contextFile, gameDirectory, dependencyDirectories)
                }
                is ParadoxRootInfo.Mod -> {
                    val modDirectory = rootInfo.rootFile
                    val settings = PlsFacade.getProfilesSettings().modSettings.get(modDirectory.path)
                    val gameDirectory = settings?.finalGameDirectory?.toVirtualFile(false)
                    val dependencyDirectories = getDependencyDirectories(settings, modDirectory)
                    return ParadoxModWithDependenciesSearchScope(project, contextFile, modDirectory, gameDirectory, dependencyDirectories)
                }
                else -> return null
            }
        }

        @JvmStatic
        fun allScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return allScope(project) //use all scope here
            return allScope(project).intersectWith(ParadoxSearchScope(project, file))
        }

        @JvmStatic
        fun fileScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            return GlobalSearchScope.fileScope(project, file) //限定在当前文件作用域
        }

        @JvmStatic
        fun rootFileScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            val modDirectory = rootInfo.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile
            return ParadoxModSearchScope(project, contextFile, modDirectory)
        }

        @JvmStatic
        fun modScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if (!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            val modDirectory = rootInfo.castOrNull<ParadoxRootInfo.Mod>()?.rootFile
            return ParadoxModSearchScope(project, contextFile, modDirectory)
        }

        @JvmStatic
        fun gameScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if (!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            val gameDirectory = rootInfo.castOrNull<ParadoxRootInfo.Game>()?.rootFile
            return ParadoxGameSearchScope(project, contextFile, gameDirectory)
        }

        @JvmStatic
        fun modAndGameScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if (!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            when (rootInfo) {
                is ParadoxRootInfo.Game -> {
                    val gameDirectory = rootInfo.rootFile
                    return ParadoxModAndGameSearchScope(project, contextFile, null, gameDirectory)
                }
                is ParadoxRootInfo.Mod -> {
                    val modDirectory = rootInfo.rootFile
                    return ParadoxModAndGameSearchScope(project, contextFile, modDirectory, null)
                }
                else -> return EMPTY_SCOPE
            }
        }

        @JvmStatic
        fun modWithDependenciesScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if (!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            val modDirectory = rootInfo.castOrNull<ParadoxRootInfo.Mod>()?.rootFile
            if (modDirectory == null) return ParadoxModWithDependenciesSearchScope(project, contextFile, null, null, emptySet())
            val settings = PlsFacade.getProfilesSettings().gameSettings.get(modDirectory.path)
            val gameDirectory = settings?.gameDirectory?.toVirtualFile(false)
            val dependencyDirectories = getDependencyDirectories(settings)
            return ParadoxModWithDependenciesSearchScope(project, contextFile, modDirectory, gameDirectory, dependencyDirectories)
        }

        @JvmStatic
        fun gameWithDependenciesScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return EMPTY_SCOPE //use empty scope here
            val contextFile = file.findTopHostFileOrThis()
            val rootInfo = selectRootFile(contextFile)?.fileInfo?.rootInfo ?: return EMPTY_SCOPE //use empty scope here
            if (!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return EMPTY_SCOPE //use empty scope here
            val gameDirectory = rootInfo.castOrNull<ParadoxRootInfo.Game>()?.rootFile
            if (gameDirectory == null) return ParadoxGameWithDependenciesSearchScope(project, contextFile, null, emptySet())
            val settings = PlsFacade.getProfilesSettings().modSettings.get(gameDirectory.path)
            val dependencyDirectories = getDependencyDirectories(settings)
            return ParadoxGameWithDependenciesSearchScope(project, contextFile, gameDirectory, dependencyDirectories)
        }

        @JvmStatic
        fun getDependencyDirectories(settings: ParadoxGameOrModSettingsState?, modDirectory: VirtualFile? = null): Set<VirtualFile> {
            if (settings == null) return emptySet()
            val modDependencyDirectories = mutableSetOf<VirtualFile>()
            for (modDependency in settings.modDependencies) {
                //要求模组依赖是启用的，或者是当前模组自身
                if (modDependency.enabled || (modDirectory != null && modDependency.modDirectory == modDirectory.path)) {
                    val modDependencyDirectory = modDependency.modDirectory?.toVirtualFile(false)
                    if (modDependencyDirectory != null) {
                        modDependencyDirectories.add(modDependencyDirectory)
                    }
                }
            }
            return modDependencyDirectories
        }
    }
}



