package icu.windea.pls.integrations.lints

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.integrations.lints.tools.PlsLintToolProvider
import icu.windea.pls.integrations.lints.tools.PlsTigerLintToolProvider

/**
 * 用于监听 Tiger 检查工具的 `.conf` 配置文件的更改，以便在必要时刷新检查结果缓存。
 */
class PlsTigerConfFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        //仅检查配置文件的文件名是否匹配

        val enabledTools = PlsLintToolProvider.EP_NAME.extensionList.filterIsInstance<PlsTigerLintToolProvider> { it.isEnabled() }
        if (enabledTools.isEmpty()) return null

        val changedConfFileNames = mutableSetOf<String>()

        for (event in events) {
            ProgressManager.checkCanceled()
            when (event) {
                is VFileDeleteEvent -> {
                    event.file.name.takeIf { isConfFile(it) }?.let { changedConfFileNames += it }
                }
                is VFileCopyEvent -> {
                    event.newChildName.takeIf { isConfFile(it) }?.let { changedConfFileNames += it }
                }
                is VFileMoveEvent -> {
                    event.file.name.takeIf { isConfFile(it) }?.let { changedConfFileNames += it }
                }
                is VFilePropertyChangeEvent -> {
                    if (event.propertyName != VirtualFile.PROP_NAME) continue
                    event.file.name.takeIf { isConfFile(it) }?.let { changedConfFileNames += it }
                }
                is VFileContentChangeEvent -> {
                    event.file.name.takeIf { isConfFile(it) }?.let { changedConfFileNames += it }
                }
            }
        }

        if (changedConfFileNames.isEmpty()) return null
        val gameType2ConfFileName = enabledTools.associateBy({ it.forGameType }, { getConfFileName(it) })
        val gameTypeChanged = gameType2ConfFileName.filterValues { it in changedConfFileNames }.keys

        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                gameTypeChanged.forEach { PlsTigerLintManager.modificationTrackers.getValue(it).incModificationCount() }
            }
        }
    }

    private fun isConfFile(fileName: String): Boolean {
        return fileName.endsWith(".conf", true)
    }

    private fun getConfFileName(provider: PlsTigerLintToolProvider): String {
        return provider.exePath?.orNull()?.normalizePath()?.substringAfterLast('/') ?: "${provider.name}.conf"
    }
}
