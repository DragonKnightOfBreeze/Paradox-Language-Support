package icu.windea.pls.images.dds

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.lang.util.image.*

/**
 * 监听DDS文件。
 * 如果文件内容发生变化，或者文件被删除或移动，作废对应的PNG文件缓存。
 */
class DdsFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        val files = getFiles(events)
        if(files.isEmpty()) return null
        return object : AsyncFileListener.ChangeApplier {
            override fun beforeVfsChange() {
                for (file in files) {
                    ParadoxDdsImageResolver.clearCacheFiles(file)
                }
            }
        }
    }

    private fun getFiles(events: List<VFileEvent>): Set<VirtualFile> {
        val files = mutableSetOf<VirtualFile>()
        for (event in events) {
            when (event) {
                is VFileContentChangeEvent -> files += event.file
                is VFileDeleteEvent -> files += event.file
                is VFileMoveEvent -> files += event.file
                is VFilePropertyChangeEvent -> files += event.file
            }
        }
        files.removeIf { it.fileType != DdsFileType }
        return files
    }
}
