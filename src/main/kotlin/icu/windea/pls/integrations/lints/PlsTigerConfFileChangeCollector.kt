package icu.windea.pls.integrations.lints

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.filterIsInstanceTo
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.integrations.lints.tools.PlsLintToolProvider
import icu.windea.pls.integrations.lints.tools.PlsTigerLintToolProvider

@Optimized
class PlsTigerConfFileChangeCollector {
    private val enabledTools: MutableSet<PlsTigerLintToolProvider> = FastSet()
    private val changedConfFileNames: MutableSet<String> = FastSet()

    fun collectChange(events: List<VFileEvent>) {
        PlsLintToolProvider.EP_NAME.extensionList.filterIsInstanceTo<PlsTigerLintToolProvider, _>(enabledTools) { it.isEnabled() }
        if (enabledTools.isEmpty()) return

        // 仅检查配置文件的文件名是否匹配

        events.forEachFast f@{ event ->
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
                    if (event.propertyName != VirtualFile.PROP_NAME) return@f
                    event.file.name.takeIf { isConfFile(it) }?.let { changedConfFileNames += it }
                }
                is VFileContentChangeEvent -> {
                    event.file.name.takeIf { isConfFile(it) }?.let { changedConfFileNames += it }
                }
            }
        }
    }

    private fun isConfFile(fileName: String): Boolean {
        return fileName.endsWith(".conf", true)
    }

    private fun getConfFileName(provider: PlsTigerLintToolProvider): String {
        return provider.exePath?.orNull()?.normalizePath()?.substringAfterLast('/') ?: "${provider.name}.conf"
    }

    fun isChanged(): Boolean {
        return enabledTools.isNotEmpty() && changedConfFileNames.isNotEmpty()
    }

    fun afterVfsChange() {
        val gameType2ConfFileName = enabledTools.associateBy({ it.forGameType }, { getConfFileName(it) })
        val gameTypeChanged = gameType2ConfFileName.filterValues { it in changedConfFileNames }.keys
        gameTypeChanged.forEach { PlsTigerLintManager.modificationTrackers.getValue(it).incModificationCount() }
    }
}
