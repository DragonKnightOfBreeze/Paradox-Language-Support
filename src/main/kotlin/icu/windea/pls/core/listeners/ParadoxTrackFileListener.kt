package icu.windea.pls.core.listeners

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.core.*
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
				var trackScriptFile = true
				var trackInlineScript = true
				var trackModifier = true
				var count = 0
				val provider = ParadoxModificationTrackerProvider.getInstance()
				for(file in files) {
					if(file.fileType == ParadoxScriptFileType) {
						if(trackScriptFile) {
							trackScriptFile = false
							count++
							provider.ScriptFile.incModificationCount()
						}
						val isInlineScriptFile = ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null
						if(isInlineScriptFile) {
							if(trackInlineScript) {
								trackInlineScript = false
								count++
								provider.InlineScript.incModificationCount()
							}
						} else {
							if(trackModifier) {
								trackModifier = false
								count++
								provider.Modifier.incModificationCount()
							}
						}
					}
					if(count == 3) break
				}
			}
		}
	}
}