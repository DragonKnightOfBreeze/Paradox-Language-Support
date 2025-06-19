package icu.windea.pls.integrations.lints

import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.integrations.lints.tools.*
import icu.windea.pls.model.*

/**
 * 用于监听Tiger检查工具的`.conf`配置文件的更改，以便在必要时刷新检查结果缓存。
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
                    event.file.name.takeIf { isConfFile(it) }?.let { changedConfFileNames += it }
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
        val gameTypes = mutableSetOf<ParadoxGameType>()
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
