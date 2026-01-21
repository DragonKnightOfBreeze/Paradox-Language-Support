package icu.windea.pls.config.configGroup

import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

/**
 * 用于监听规则文件的更改，以便在必要时通知规则分组发生更改。
 */
class CwtConfigFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        val collector = CwtConfigFileChangeCollector()
        collector.collectChange(events)
        if (!collector.isChanged()) return null
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                collector.afterVfsChange()
            }
        }
    }
}
