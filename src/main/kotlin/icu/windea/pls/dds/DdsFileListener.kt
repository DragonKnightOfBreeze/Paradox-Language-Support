package icu.windea.pls.dds

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.util.*
import kotlin.io.path.*

class DdsFileListener: AsyncFileListener {
	override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
		for(event in events) {
			if(event is VFileContentChangeEvent){
				//当DDS文件内容变更时，需要重新转化
				onContentChanged(event.file)
			}
		}
		return null
	}
	
	//icu.windea.pls.util.ParadoxDdsUrlResolver.doResolveByFile(com.intellij.openapi.vfs.VirtualFile)
	private fun onContentChanged(file:VirtualFile) {
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