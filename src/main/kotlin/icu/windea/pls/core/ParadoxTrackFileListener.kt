package icu.windea.pls.core

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.*

/**
 * 监听文件以跟踪更改。
 */
class ParadoxTrackFileListener : AsyncFileListener {
	override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier {
		return object : AsyncFileListener.ChangeApplier {
			override fun afterVfsChange() {
				val files = mutableSetOf<VirtualFile>()
				//当DDS文件内容发生变化或者被删除时，需要重新转化
				for(event in events) {
					when {
						event is VFileContentChangeEvent -> files.add(event.file)
						event is VFileCreateEvent -> files.add(event.parent)
						event is VFileDeleteEvent -> files.add(event.file)
						event is VFileMoveEvent -> files.add(event.file)
					}
				}
				track(files)
			}
			
			private fun track(files: MutableSet<VirtualFile>) {
				//这才是正确的做法，如此简单！
				trackNoInlineScript(files)
			}
			
			private fun trackNoInlineScript(files: MutableSet<VirtualFile>) {
				for(file in files) {
					if(file.fileType != ParadoxScriptFileType) continue
					if(file.isDirectory) {
						val path = file.fileInfo?.path?.path ?: continue
						if(!ParadoxInlineScriptHandler.inlineScriptDirPath.matchesPath(path)) continue
						ParadoxModificationTrackerProvider.getInstance().Modifier.incModificationCount()
						ParadoxModificationTrackerProvider.getInstance().InlineScript.incModificationCount()
						break
					} else {
						if(ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null) continue
						ParadoxModificationTrackerProvider.getInstance().Modifier.incModificationCount()
						ParadoxModificationTrackerProvider.getInstance().InlineScript.incModificationCount()
						break
					}
				}
			}
		}
	}
}