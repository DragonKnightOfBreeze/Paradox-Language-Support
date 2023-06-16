package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*

/**
 * 监听文件更改以更新相关信息缓存。
 */
class ParadoxCoreFileListener : AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier {
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                var reparseOpenedFiles = false
                
                run {
                    for(event in events) {
                        if(event is VFileContentChangeEvent) {
                            val fileName = event.file.name
                            if(fileName.equals(PlsConstants.descriptorFileName, true)) {
                                val rootFile = event.file.fileInfo?.rootInfo?.rootFile
                                clearRootInfo(rootFile)
                            } else if(fileName.equals(PlsConstants.launcherSettingsFileName, true)) {
                                val rootFile = event.file.fileInfo?.rootInfo?.rootFile
                                clearRootInfo(rootFile)
                            }
                        }
                    }
                }
                run {
                    for(event in events) {
                        when(event) {
                            is VFileCreateEvent -> {
                                if(event.childName.equals(PlsConstants.descriptorFileName, true)) {
                                    clearRootInfo(event.parent)
                                    reparseOpenedFiles = true
                                }
                            }
                            is VFileDeleteEvent -> {
                                val file = event.file
                                if(file.name.equals(PlsConstants.descriptorFileName, true)) {
                                    clearRootInfo(file.parent)
                                    reparseOpenedFiles = true
                                }
                            }
                            is VFileCopyEvent -> {
                                if(event.newChildName.equals(PlsConstants.descriptorFileName, true)) {
                                    clearRootInfo(event.newParent)
                                    reparseOpenedFiles = true
                                }
                            }
                            is VFileMoveEvent -> {
                                if(event.file.name.equals(PlsConstants.descriptorFileName, true)) {
                                    clearRootInfo(event.oldParent)
                                    clearRootInfo(event.newParent)
                                    reparseOpenedFiles = true
                                }
                            }
                            is VFilePropertyChangeEvent -> {
                                if(event.propertyName == VirtualFile.PROP_NAME) {
                                    if(event.newValue.toString().equals(PlsConstants.descriptorFileName, true)) {
                                        clearRootInfo(event.file.parent)
                                        reparseOpenedFiles = true
                                    } else if(event.oldValue.toString().equals(PlsConstants.descriptorFileName, true)) {
                                        clearRootInfo(event.file.parent)
                                        reparseOpenedFiles = true
                                    }
                                }
                            }
                        }
                    }
                }
                
                if(reparseOpenedFiles) {
                    runReadAction { ParadoxCoreHandler.reparseOpenedFiles() }
                }
            }
        }
    }
    
    private fun clearRootInfo(rootFile: VirtualFile?) {
        if(rootFile == null) return
        //清空根目录信息缓存
        rootFile.tryPutUserData(PlsKeys.rootInfoStatusKey, null)
    }
}