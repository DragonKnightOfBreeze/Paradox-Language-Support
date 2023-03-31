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
                        val filePath = fileInfo.path.path
                        provider.ScriptFile.incModificationCount()
                        if("common".matchesPath(filePath) && !"common/inline_scripts".matchesPath(filePath)) {
                            provider.Modifier.incModificationCount()
                        }
                        when {
                            "common/technologies".matchesPath(filePath) -> {
                                provider.Technologies.incModificationCount()
                            }
                            "common/on_actions".matchesPath(filePath) -> {
                                provider.OnActions.incModificationCount()
                            }
                            "events".matchesPath(filePath) -> {
                                provider.Events.incModificationCount()
                            }
                            "common/scripted_variables".matchesPath(filePath) -> {
                                provider.ScriptedVariables.incModificationCount()
                            }
                            "common/inline_scripts".matchesPath(filePath) -> {
                                provider.InlineScripts.incModificationCount()
                            }
                        }
                    }
                }
            }
        }
    }
}