package icu.windea.pls.lang.configGroup

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.core.collections.*

class CwtConfigGroupFileListener: AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val filePaths = mutableSetOf<String>()
        events.forEachFast { event ->
            when(event) {
                is VFileCreateEvent -> {
                    filePaths.add(event.parent.path + "/" + event.childName)
                }
                is VFileCopyEvent -> {
                    filePaths.add(event.newParent.path + "/" + event.newChildName)
                }
                is VFileMoveEvent -> {
                    filePaths.add(event.newPath)
                    filePaths.add(event.oldPath)
                }
                else -> {
                    event.file?.let { filePaths.add(it.path) }
                }
            }
        }
        if(filePaths.isEmpty()) return null
        
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
                
                ProjectManager.getInstance().openProjects.forEachFast f1@{ project ->
                    val configGroupService = project.service<CwtConfigGroupService>()
                    val configGroups = configGroupService.getConfigGroups()
                    configGroups.values.forEach f2@{ configGroup ->
                        if(configGroup.changed.get()) return@f2
                        val isChanged = fileProviders.any { it.isChanged(configGroup, filePaths) }
                        if(isChanged) configGroup.changed.set(true)
                    }
                }
            }
        }
    }
}