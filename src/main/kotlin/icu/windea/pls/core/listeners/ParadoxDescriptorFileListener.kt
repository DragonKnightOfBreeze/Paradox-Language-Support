package icu.windea.pls.core.listeners

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

/**
 * 监听模组描述符文件的变化。
 */
class ParadoxDescriptorFileListener : AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier {
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                for(event in events) {
                    onChange(event)
                }
            }
        }
    }
    
    private fun onChange(event: VFileEvent) {
        if(event is VFileContentChangeEvent) {
            //TODo 如果用户一直在更改descriptor.mod，这个事件可能会触发很多次
            val file = event.file
            if(file.fileType.isBinary) return //unexpected
            if(file.name.equals(PlsConstants.descriptorFileName, true)) {
                val descriptorInfo = ParadoxCoreHandler.getDescriptorInfo(file) ?: return
                file.putUserData(PlsKeys.descriptorInfoKey, descriptorInfo)
                val fileInfo = file.fileInfo ?: return
                val modRootInfo = fileInfo.rootInfo.castOrNull<ParadoxModRootInfo>()
                if(modRootInfo != null) {
                    modRootInfo.descriptorInfo = descriptorInfo
                }
                val modDirectory = fileInfo.rootInfo.rootFile.path
                val settings = getProfilesSettings()
                val modDescriptorSettings = settings.modDescriptorSettings.get(modDirectory)
                if(modDescriptorSettings != null) {
                    modDescriptorSettings.fromDescriptorInfo(descriptorInfo)
                    settings.updateSettings()
                }
            }
        }
    }
}