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
			val file = event.file
			if(file.fileType.isBinary) return //unexpected
			if(file.name.equals(PlsConstants.descriptorFileName, true)) {
				//描述符文件内容发生变化时，需要更新rootInfo和modSettings
				//icu.windea.pls.lang.model.ParadoxModRootInfo.getDescriptorInfo
				val descriptorInfo = ParadoxCoreHandler.getDescriptorInfo(file)
				file.putUserData(PlsKeys.descriptorInfoKey, descriptorInfo)
				val modPath = file.fileInfo?.rootInfo?.rootFile?.path
				if(modPath != null) {
					val allModSettings = getAllModSettings()
					val modSettings = allModSettings.settings.get(modPath)
					if(modSettings != null) {
						modSettings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
						modSettings.version = descriptorInfo.version?.takeIfNotEmpty()
						modSettings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
					}
				}
			}
		}
	}
}