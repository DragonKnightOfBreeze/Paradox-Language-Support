package icu.windea.pls.integrations.lints

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.filterIsInstanceTo
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.integrations.lints.providers.TigerLintToolProvider

@Optimized
class TigerConfFileChangeCollector {
    private val enabledTools: MutableSet<TigerLintToolProvider> = mutableSetOf()
    private val changedConfFileNames: MutableSet<String> = mutableSetOf()

    fun collectChange(events: List<VFileEvent>) {
        LintToolProvider.EP_NAME.extensionList.filterIsInstanceTo<TigerLintToolProvider, _>(enabledTools) { it.isEnabled() }
        if (enabledTools.isEmpty()) return

        // 仅检查配置文件的文件名是否匹配

        events.forEachFast f@{ event ->
            ProgressManager.checkCanceled()
            when (event) {
                // NOTE 3.0.1 should also watch VFileCreateEvent and VFileContentChangeEvent
                is VFileCreateEvent -> {
                    val fileName = event.childName
                    if (!isConfFile(fileName)) return@f
                    changedConfFileNames += fileName
                }
                is VFileDeleteEvent -> {
                    val fileName = event.file.name
                    if (!isConfFile(fileName)) return@f
                    changedConfFileNames += fileName
                }
                is VFileCopyEvent -> {
                    val fileName = event.newChildName
                    if (!isConfFile(fileName)) return@f
                    changedConfFileNames += fileName
                }
                is VFileMoveEvent -> {
                    val fileName = event.file.name
                    if (!isConfFile(fileName)) return@f
                    changedConfFileNames += fileName
                }
                is VFilePropertyChangeEvent -> {
                    if (event.propertyName != VirtualFile.PROP_NAME) return@f
                    val fileName = event.file.name
                    if (!isConfFile(fileName)) return@f
                    changedConfFileNames += fileName
                }
                is VFileContentChangeEvent -> {
                    val fileName = event.file.name
                    if (!isConfFile(fileName)) return@f
                    changedConfFileNames += fileName
                }
            }
        }
    }

    private fun isConfFile(fileName: String): Boolean {
        return fileName.endsWith(".conf", true)
    }

    private fun getConfFileName(provider: TigerLintToolProvider): String {
        return provider.exePath?.orNull()?.normalizePath()?.substringAfterLast('/') ?: "${provider.name}.conf"
    }

    fun isChanged(): Boolean {
        return enabledTools.isNotEmpty() && changedConfFileNames.isNotEmpty()
    }

    fun afterVfsChange() {
        val gameType2ConfFileName = enabledTools.associateBy({ it.forGameType }, { getConfFileName(it) })
        val gameTypeChanged = gameType2ConfFileName.filterValues { it in changedConfFileNames }.keys
        gameTypeChanged.forEach { TigerLintToolService.getInstance().getModificationTracker(it).incModificationCount() }
    }
}
