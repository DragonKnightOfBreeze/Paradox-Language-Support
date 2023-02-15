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
				val descriptorInfo = ParadoxCoreHandler.getDescriptorInfo(file)
				file.putUserData(PlsKeys.descriptorInfoKey, descriptorInfo)
				val modPath = file.fileInfo?.rootInfo?.rootFile?.path
				if(modPath != null) {
					val allModSettings = getAllModSettings()
					val settings = allModSettings.descriptorSettings.get(modPath)
					if(settings != null) {
						settings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
						settings.version = descriptorInfo.version?.takeIfNotEmpty()
						settings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
					}
				}
			}
		}
	}
}