package icu.windea.pls.core.listeners

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*

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
				//描述符文件内容发生变化时，这里直接清空缓存即可
				//icu.windea.pls.lang.model.ParadoxModRootInfo.getDescriptorInfo
				file.putUserData(PlsKeys.descriptorInfoKey, null)
			}
		}
	}
}