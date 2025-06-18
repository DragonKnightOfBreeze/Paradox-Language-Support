package icu.windea.pls.integrations.lints

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*

/**
 * 用于监听文件更改，以对于Tiger检查工具，
 * 在必要时异步刷新整个根目录下的检查结果，并通知该文件对应的检查结果需要刷新。
 */
class PlsTigerLintFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        return null //TODO 2.0.0-dev
    }
}
