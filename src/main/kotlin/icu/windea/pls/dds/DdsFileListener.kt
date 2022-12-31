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
						event is VFileContentChangeEvent -> doInvalidate(event.file)
						event is VFileDeleteEvent -> doInvalidate(event.file)
						event is VFileMoveEvent -> doInvalidate(event.file)
						event is VFilePropertyChangeEvent && event.propertyName == "name" -> doInvalidate(event.file)
					}
				}
			}
		}
	}
	
	//icu.windea.pls.tool.ParadoxDdsUrlResolver.doResolveByFile(com.intellij.openapi.vfs.VirtualFile)
	private fun doInvalidate(ddsFile: VirtualFile) {
		if(ddsFile.fileType != DdsFileType) return
		//如果可以得到相对于游戏或模组根路径的文件路径，则使用绝对根路径+相对路径定位，否则直接使用绝对路径
		val fileInfo = ddsFile.fileInfo
		val rootPath = fileInfo?.rootPath
		val ddsRelPath = fileInfo?.path?.path
		val ddsAbsPath = if(rootPath != null && ddsRelPath != null) {
			rootPath.absolutePathString() + "/" + ddsRelPath
		} else {
			ddsFile.toNioPath().absolutePathString()
		}
		DdsToPngConverter.invalidate(ddsAbsPath)
	}
}