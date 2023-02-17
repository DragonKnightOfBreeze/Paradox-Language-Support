package icu.windea.pls.core.listeners

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*

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
				val descriptorInfo = ParadoxCoreHandler.getDescriptorInfo(file)
				file.putUserData(PlsKeys.descriptorInfoKey, descriptorInfo)
				val modDirectory = file.fileInfo?.rootInfo?.rootFile?.path
				if(modDirectory != null) {
					val settings = getProfilesSettings()
					val modDescriptorSettings = settings.modDescriptorSettings.get(modDirectory)
					if(modDescriptorSettings != null) {
						modDescriptorSettings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
						modDescriptorSettings.version = descriptorInfo.version?.takeIfNotEmpty()
						modDescriptorSettings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
						settings.updateSettings()
					}
				}
			}
		}
	}
}