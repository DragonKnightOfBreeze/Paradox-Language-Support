package icu.windea.pls.config.configGroup

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.toolbar.floating.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.model.*

private val LOGGER = logger<CwtConfigFileListener>()

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
                    val configGroups = mutableSetOf<CwtConfigGroup>()
                    fileProviders.forEach f2@{ fileProvider ->
                        if (fileProvider is BuiltInCwtConfigGroupFileProvider) return@f2
                        val rootDirectories = contextFiles.mapNotNullTo(mutableSetOf()) { fileProvider.getRootDirectory(project) }
                        rootDirectories.forEach f3@{ rootDirectory ->
                            val configGroup = fileProvider.getContainingConfigGroup(rootDirectory, project) ?: return@f3
                            if (configGroup.gameType == null) {
                                ParadoxGameType.entries.forEach { gameType ->
                                    configGroups += PlsFacade.getConfigGroup(project, gameType)
                                }
                            } else {
                                configGroups += configGroup
                            }
                        }
                    }
                    val configGroupsToChange = configGroups.filter { !it.changed.get() }
                    if (configGroupsToChange.isEmpty()) return@f1
                    configGroupsToChange.forEach { configGroup -> configGroup.changed.set(true) }
                    FloatingToolbarProvider.EP_NAME.findExtensionOrFail(ConfigGroupRefreshFloatingProvider::class.java).updateToolbarComponents(project)
                }
            }
        }
    }

    private fun isCwtFile(fileName: String): Boolean {
        return fileName.endsWith(".cwt", true)
    }
}
