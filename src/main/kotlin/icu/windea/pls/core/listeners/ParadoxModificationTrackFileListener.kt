package icu.windea.pls.core.listeners

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*

/**
 * 监听文件以跟踪更改。
 */
class ParadoxModificationTrackFileListener : AsyncFileListener {
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
                onChange(files)
            }
            
            private fun onChange(files: MutableSet<VirtualFile>) {
                //这才是正确的做法，如此简单！
                val provider = ParadoxModificationTrackerProvider.getInstance()
                for(file in files) {
                    if(file.fileType == ParadoxScriptFileType) {
                        val fileInfo = file.fileInfo ?: continue
                        val filePath = fileInfo.pathToEntry.path
                        val fileExtension = fileInfo.pathToEntry.fileExtension.lowercase() //ignore case
                        provider.ScriptFileTracker.incModificationCount()
                        for(tracker in provider.ScriptFileTrackers.values) {
                            val keys = tracker.keys
                            for(key in keys) {
                                if(key.path.matchesPath(filePath) && (key.extensions.isEmpty() || key.extensions.contains(fileExtension))) {
                                    tracker.incModificationCount()
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}