package icu.windea.pls.dds

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.lang.util.image.*
import kotlin.io.path.*

/**
 * 监听DDS文件。
 * 如果文件内容发生变化，或者文件被删除或移动，作废对应的PNG文件缓存。
 */
class DdsFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier {
        return object : AsyncFileListener.ChangeApplier {
            override fun beforeVfsChange() {
                val files = mutableSetOf<VirtualFile>()
                for (event in events) {
                    when {
                        event is VFileContentChangeEvent -> files.add(event.file)
                        event is VFileDeleteEvent -> files.add(event.file)
                        event is VFileMoveEvent -> files.add(event.file)
                        event is VFilePropertyChangeEvent -> files.add(event.file)
                    }
                }
                for (file in files) {
                    if (file.fileType != DdsFileType) continue
                    val ddsAbsPath = file.toNioPath().absolutePathString()
                    ParadoxDdsImageResolver.invalidateUrl(ddsAbsPath)
                }
            }
        }
    }
}
