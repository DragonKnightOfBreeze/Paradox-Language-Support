package icu.windea.pls.config.configGroup

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.config.configGroup.CwtBuiltInConfigGroupFileProvider
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.model.ParadoxGameType

@Optimized
class CwtConfigFileChangeCollector {
    private val contextFiles: MutableSet<VirtualFile> = FastSet()
    private val contextDirectories: MutableSet<VirtualFile> by lazy { contextFiles.mapNotNullTo(FastSet()) { it.parent } }

    fun collectChange(events: List<VFileEvent>) {
        events.forEachFast f@{ event ->
            ProgressManager.checkCanceled()
            when (event) {
                is VFileDeleteEvent -> {
                    val fileName = event.file.name
                    if (!isCwtFile(fileName)) return@f
                    event.file.parent?.let { contextFiles += it }
                }
                is VFileCopyEvent -> {
                    val fileName = event.newChildName
                    if (!isCwtFile(fileName)) return@f
                    event.newParent.let { contextFiles += it }
                }
                is VFileMoveEvent -> {
                    val fileName = event.file.name
                    if (!isCwtFile(fileName)) return@f
                    event.oldParent?.let { contextFiles += it }
                    event.newParent.let { contextFiles += it }
                }
                is VFilePropertyChangeEvent -> {
                    if (event.propertyName != VirtualFile.PROP_NAME) return@f
                    val fileName = event.file.name
                    if (!isCwtFile(fileName)) return@f
                    event.file.parent?.let { contextFiles += it }
                }
            }
        }
    }

    private fun isCwtFile(fileName: String): Boolean {
        return fileName.endsWith(".cwt", true)
    }

    fun isChanged(): Boolean {
        return contextFiles.isNotEmpty() && contextDirectories.isNotEmpty()
    }

    fun afterVfsChange() {
        // TODO 2.0.0-dev+ 如果更改过多，这里可能相对比较耗时

        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val openProjects = ProjectManager.getInstance().openProjects
        openProjects.forEach f1@{ project ->
            val configGroupService = CwtConfigGroupService.getInstance(project)
            val configGroups = mutableSetOf<CwtConfigGroup>()
            fileProviders.forEachFast f2@{ fileProvider ->
                if (fileProvider is CwtBuiltInConfigGroupFileProvider) return@f2
                contextDirectories.forEach f3@{ contextDirectory ->
                    val configGroup = fileProvider.getContainingConfigGroup(contextDirectory, project) ?: return@f3
                    configGroups += configGroup
                    if (configGroup.gameType == ParadoxGameType.Core) {
                        ParadoxGameType.getAll().forEachFast { gameType -> configGroups += configGroupService.getConfigGroup(gameType) }
                    }
                }
            }
            val configGroupsToChange = configGroups.filter { !it.changed }
            if (configGroupsToChange.isEmpty()) return@f1
            configGroupsToChange.forEachFast { configGroup -> configGroup.changed = true }
            configGroupService.updateRefreshFloatingToolbar()
        }
    }
}
