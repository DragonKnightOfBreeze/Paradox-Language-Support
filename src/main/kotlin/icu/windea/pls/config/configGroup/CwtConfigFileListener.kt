package icu.windea.pls.config.configGroup

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import icu.windea.pls.ep.configGroup.BuiltInCwtConfigGroupFileProvider
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于监听规则文件的更改，以便在必要时通知规则分组发生更改。
 */
class CwtConfigFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        val contextFiles = mutableSetOf<VirtualFile>()

        for (event in events) {
            ProgressManager.checkCanceled()
            when (event) {
                is VFileDeleteEvent -> {
                    val fileName = event.file.name
                    if (!isCwtFile(fileName)) continue
                    event.file.parent?.let { contextFiles += it }
                }
                is VFileCopyEvent -> {
                    val fileName = event.newChildName
                    if (!isCwtFile(fileName)) continue
                    event.newParent.let { contextFiles += it }
                }
                is VFileMoveEvent -> {
                    val fileName = event.file.name
                    if (!isCwtFile(fileName)) continue
                    event.oldParent?.let { contextFiles += it }
                    event.newParent.let { contextFiles += it }
                }
                is VFilePropertyChangeEvent -> {
                    if (event.propertyName != VirtualFile.PROP_NAME) continue
                    val fileName = event.file.name
                    if (!isCwtFile(fileName)) continue
                    event.file.parent?.let { contextFiles += it }
                }
            }
        }

        if (contextFiles.isEmpty()) return null

        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                //TODO 2.0.0-dev+ 如果更改过多，这里可能相对比较耗时

                val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
                ProjectManager.getInstance().openProjects.forEach f1@{ project ->
                    val configGroupService = project.service<CwtConfigGroupService>()
                    val configGroups = mutableSetOf<CwtConfigGroup>()
                    fileProviders.forEach f2@{ fileProvider ->
                        if (fileProvider is BuiltInCwtConfigGroupFileProvider) return@f2
                        val rootDirectories = contextFiles.mapNotNullTo(mutableSetOf()) { fileProvider.getRootDirectory(project) }
                        rootDirectories.forEach f3@{ rootDirectory ->
                            val configGroup = fileProvider.getContainingConfigGroup(rootDirectory, project) ?: return@f3
                            if (configGroup.gameType == null) {
                                ParadoxGameType.entries.forEach { gameType ->
                                    configGroups += configGroupService.getConfigGroup(gameType)
                                }
                            } else {
                                configGroups += configGroup
                            }
                        }
                    }
                    val configGroupsToChange = configGroups.filter { !it.changed.get() }
                    if (configGroupsToChange.isEmpty()) return@f1
                    configGroupsToChange.forEach { configGroup -> configGroup.changed.set(true) }
                    configGroupService.updateRefreshFloatingToolbar()
                }
            }
        }
    }

    private fun isCwtFile(fileName: String): Boolean {
        return fileName.endsWith(".cwt", true)
    }
}
