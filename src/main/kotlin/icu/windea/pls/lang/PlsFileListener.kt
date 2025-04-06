package icu.windea.pls.lang

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*

/**
 * 用于监听文件更改以更新相关缓存。
 */
class PlsFileListener : AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier {
        val filesToClearRootInfo = mutableSetOf<VirtualFile>()
        val filesToClearFileInfo = mutableSetOf<VirtualFile>()
        val filesToClearLocale = mutableSetOf<VirtualFile>()
        var reparseOpenedFiles = false
        var refreshInlineScripts = false

        for (event in events) {
            when (event) {
                is VFileCreateEvent -> {
                    val fileName = event.childName
                    run {
                        if (ParadoxMetadataManager.metadataFileNames.none { fileName.equals(it, true) }) return@run
                        selectRootFile(event.parent)?.let { filesToClearRootInfo.add(it) }
                        reparseOpenedFiles = true
                    }
                }
                is VFileDeleteEvent -> {
                    val file = event.file
                    val fileName = file.name
                    run {
                        filesToClearFileInfo.add(file)
                        filesToClearLocale.add(file)
                    }
                    run {
                        if (ParadoxMetadataManager.metadataFileNames.none { fileName.equals(it, true) }) return@run
                        selectRootFile(file.parent)?.let { filesToClearRootInfo.add(it) }
                        reparseOpenedFiles = true
                    }
                }
                is VFileCopyEvent -> {
                    val fileName = event.newChildName
                    run {
                        if (ParadoxMetadataManager.metadataFileNames.none { fileName.equals(it, true) }) return@run
                        selectRootFile(event.newParent)?.let { filesToClearRootInfo.add(it) }
                        reparseOpenedFiles = true
                    }
                }
                is VFileMoveEvent -> {
                    val file = event.file
                    val fileName = file.name
                    run {
                        filesToClearFileInfo.add(file)
                        filesToClearLocale.add(file)
                    }
                    run {
                        if (ParadoxMetadataManager.metadataFileNames.none { fileName.equals(it, true) }) return@run
                        selectRootFile(event.oldParent)?.let { filesToClearRootInfo.add(it) }
                        selectRootFile(event.newParent)?.let { filesToClearRootInfo.add(it) }
                        reparseOpenedFiles = true
                    }
                    run {
                        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return@run
                        refreshInlineScripts = true
                    }
                }
                is VFilePropertyChangeEvent -> {
                    if (event.propertyName != VirtualFile.PROP_NAME) continue
                    val file = event.file
                    val newFileName = event.newValue.toString()
                    val oldFileName = event.oldValue.toString()
                    run {
                        filesToClearFileInfo.add(file)
                        filesToClearLocale.add(file)
                    }
                    run {
                        if (ParadoxMetadataManager.metadataFileNames.none { newFileName.equals(it, true) || oldFileName.equals(it, true) }) return@run
                        selectRootFile(file)?.let { filesToClearRootInfo.add(it) }
                        reparseOpenedFiles = true
                    }
                    run {
                        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return@run
                        refreshInlineScripts = true
                    }
                }
                is VFileContentChangeEvent -> {
                    val file = event.file
                    val fileName = file.name
                    run {
                        filesToClearLocale.add(file)
                    }
                    run {
                        if (ParadoxMetadataManager.metadataFileNames.none { fileName.equals(it, true) }) return@run
                        selectRootFile(file)?.let { filesToClearRootInfo.add(it) }
                    }
                }
            }
        }

        return object : AsyncFileListener.ChangeApplier {
            override fun beforeVfsChange() {
                if (filesToClearRootInfo.isNotEmpty()) {
                    filesToClearRootInfo.forEach { clearRootInfo(it) }
                }
                if (filesToClearFileInfo.isNotEmpty()) {
                    filesToClearFileInfo.forEach { clearFileInfo(it) }
                }
                if (filesToClearLocale.isNotEmpty()) {
                    filesToClearLocale.forEach { clearLocale(it) }
                }
                if (refreshInlineScripts) {
                    refreshForInlineScripts()
                }
                if (reparseOpenedFiles) {
                    reparseOpenedFiles()
                } else if (refreshInlineScripts) {
                    reparseOpenedFilesForInlineScripts()
                }
            }
        }
    }

    private fun clearRootInfo(rootFile: VirtualFile) {
        rootFile.tryPutUserData(PlsKeys.rootInfo, null)
    }

    private fun clearFileInfo(file: VirtualFile) {
        file.tryPutUserData(PlsKeys.fileInfo, null)
    }

    private fun clearLocale(file: VirtualFile) {
        file.tryPutUserData(PlsKeys.localeConfig, null)
    }

    private fun reparseOpenedFiles() {
        //重新解析所有项目的所有已打开的文件
        val files = PlsManager.findOpenedFiles()
        PlsManager.reparseAndRefreshFiles(files)
    }

    private fun reparseOpenedFilesForInlineScripts() {
        //重新解析所有项目的所有已打开的内联脚本文件
        val files = PlsManager.findOpenedFiles { file, _ -> ParadoxInlineScriptManager.getInlineScriptExpression(file) != null }
        PlsManager.reparseAndRefreshFiles(files)
    }

    private fun refreshForInlineScripts() {
        //重新解析内联脚本文件
        ParadoxModificationTrackers.ScriptFileTracker.incModificationCount()
        ParadoxModificationTrackers.InlineScriptsTracker.incModificationCount()
    }
}
