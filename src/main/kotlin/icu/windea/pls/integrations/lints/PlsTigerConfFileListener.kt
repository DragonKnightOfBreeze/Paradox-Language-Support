package icu.windea.pls.integrations.lints

import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

/**
 * 用于监听 Tiger 检查工具的 `.conf` 配置文件的更改，以便在必要时刷新检查结果缓存。
 */
class PlsTigerConfFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        val collector = PlsTigerConfFileChangeCollector()
        collector.collectChange(events)
        if (!collector.isChanged()) return null
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                collector.afterVfsChange()
            }
        }
    }
}
