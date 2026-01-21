package icu.windea.pls.lang

import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

/**
 * 用于监听游戏或模组文件的更改，以便在必要时刷新相关缓存，以及重新解析和索引相关文件。
 */
class ParadoxFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        val collector = ParadoxFileChangeCollector()
        collector.collectChange(events)
        if (!collector.isChanged()) return null
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                collector.afterVfsChange()
            }
        }
    }
}
