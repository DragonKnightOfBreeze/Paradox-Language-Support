package icu.windea.pls.core.listeners

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

/**
 * 监听文件更改已更新根目录信息、文件信息和模组描述符信息。
 */
class ParadoxCoreFileListener : AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier {
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                run {
                    for(event in events) {
                        if(event is VFileContentChangeEvent) {
                            if(event.file.name.equals(PlsConstants.descriptorFileName, true)) {
                                updateDescriptorInfoAndSyncSettings(event.file)
                            }
                        }
                    }
                }
                run {
                    for(event in events) {
                        if(event is VFileCreateEvent) {
                            if(event.childName.equals(PlsConstants.descriptorFileName, true)) {
                                clearRootInfo(event.parent)
                            }
                        } else if(event is VFileDeleteEvent) {
                            val file = event.file
                            if(file.name.equals(PlsConstants.descriptorFileName, true)) {
                                clearRootInfo(event.file)
                            }
                        } else if(event is VFileCopyEvent) {
                            if(event.newChildName.equals(PlsConstants.descriptorFileName, true)) {
                                clearRootInfo(event.newParent)
                            }
                        } else if(event is VFileMoveEvent) {
                            if(event.file.name.equals(PlsConstants.descriptorFileName, true)) {
                                clearRootInfo(event.oldParent)
                                clearRootInfo(event.newParent)
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun clearRootInfo(rootFile: VirtualFile) {
        //清空根目录信息缓存
        rootFile.putUserData(PlsKeys.rootInfoStatusKey, null)
    }
    
    private fun updateDescriptorInfoAndSyncSettings(file: VirtualFile) {
        //更新模组描述符文件时，同步更新缓存的模组描述符信息和模组描述符配置
        //TODO 1.0.6+ 如果用户一直在更改descriptor.mod，这个事件可能会被触发很多次
        if(file.fileType.isBinary) return //unexpected
        file.putUserData(PlsKeys.descriptorInfoKey, null)
        val descriptorInfo = ParadoxCoreHandler.getDescriptorInfo(file) ?: return
        file.putUserData(PlsKeys.descriptorInfoKey, descriptorInfo)
        val fileInfo = file.fileInfo ?: return
        val modRootInfo = fileInfo.rootInfo.castOrNull<ParadoxModRootInfo>()
        if(modRootInfo != null) {
            modRootInfo.descriptorInfo = descriptorInfo
            val modDirectory = modRootInfo.rootFile.path
            val settings = getProfilesSettings()
            val modDescriptorSettings = settings.modDescriptorSettings.get(modDirectory)
            if(modDescriptorSettings != null) {
                modDescriptorSettings.fromDescriptorInfo(descriptorInfo)
                settings.updateSettings()
            }
        }
    }
}