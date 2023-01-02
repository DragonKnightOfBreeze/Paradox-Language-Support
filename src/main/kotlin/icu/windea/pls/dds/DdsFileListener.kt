package icu.windea.pls.dds

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.tool.*
import kotlin.io.path.*

/**
 * 监听DDS文件。
 * 如果文件内容发生变化，或者文件被删除或移动，作废对应的PNG文件缓存。
 */
class DdsFileListener : AsyncFileListener {
	override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
		return object : AsyncFileListener.ChangeApplier {
			override fun afterVfsChange() {
				//当DDS文件内容发生变化或者被删除时，需要重新转化
				for(event in events) {
					when {
						event is VFileContentChangeEvent -> doInvalidateDdsFile(event.file)
						event is VFileDeleteEvent -> doInvalidateDdsFile(event.file)
						event is VFileMoveEvent -> doInvalidateDdsFile(event.file)
						event is VFilePropertyChangeEvent -> doInvalidateDdsFile(event.file)
					}
				}
			}
		}
	}
}