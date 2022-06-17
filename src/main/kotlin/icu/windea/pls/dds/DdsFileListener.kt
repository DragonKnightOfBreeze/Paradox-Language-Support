package icu.windea.pls.dds

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.util.*
import kotlin.io.path.*

/**
 * 监听DDS文件，如果文件内容发生变化，作为对应的PNG文件缓存。
 */
class DdsFileListener : AsyncFileListener {
	override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
		for(event in events) {
			//当DDS文件内容发生变化或者被删除时，需要重新转化
			when(event) {
				is VFileContentChangeEvent -> doInvalidate(event.file)
				is VFileDeleteEvent -> doInvalidate(event.file)
			}
		}
		return null
	}
	
	//icu.windea.pls.util.ParadoxDdsUrlResolver.doResolveByFile(com.intellij.openapi.vfs.VirtualFile)
	private fun doInvalidate(file: VirtualFile) {
		if(file.fileType != DdsFileType) return
		//如果可以得到相对于游戏或模组根路径的文件路径，则使用绝对根路径+相对路径定位，否则直接使用绝对路径
		val fileInfo = file.fileInfo
		val rootPath = fileInfo?.rootPath
		val ddsRelPath = fileInfo?.path?.path
		val ddsAbsPath = if(rootPath != null && ddsRelPath != null) {
			rootPath.absolutePathString() + "/" + ddsRelPath
		} else {
			file.toNioPath().absolutePathString()
		}
		DdsToPngConverter.invalidate(ddsAbsPath)
	}
}