package icu.windea.pls.lang

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import icu.windea.pls.core.util.tryPutUserData
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.lang.util.PlsAnalyzeManager

/**
 * 用于监听游戏或模组文件的更改，以更新相关缓存。
 */
class ParadoxFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier {
        val filesToClearRootInfo = mutableSetOf<VirtualFile>()
        val filesToClearFileInfo = mutableSetOf<VirtualFile>()
        val filesToClearLocaleConfig = mutableSetOf<VirtualFile>()
        val filesToClearSliceInfos = mutableSetOf<VirtualFile>()
        var reparseOpenedFiles = false
        var refreshFilePaths = false
        var refreshInlineScripts = false

        for (event in events) {
            ProgressManager.checkCanceled()
            when (event) {
                is VFileCreateEvent -> {
                    val fileName = event.childName
                    run {
                        if (shouldRefreshForFilePaths(event.parent)) refreshFilePaths = true
                    }
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
                        filesToClearLocaleConfig.add(file)
                        filesToClearSliceInfos.add(file)
                    }
                    run {
                        if (shouldRefreshForFilePaths(file.parent)) refreshFilePaths = true
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
                        if (shouldRefreshForFilePaths(event.newParent)) refreshFilePaths = true
                    }
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
                        filesToClearLocaleConfig.add(file)
                        filesToClearSliceInfos.add(file)
                    }
                    run {
                        if (shouldRefreshForFilePaths(file)) refreshFilePaths = true
                        if (shouldRefreshForInlineScripts(file)) refreshInlineScripts = true
                    }
                    run {
                        if (ParadoxMetadataManager.metadataFileNames.none { fileName.equals(it, true) }) return@run
                        selectRootFile(event.oldParent)?.let { filesToClearRootInfo.add(it) }
                        selectRootFile(event.newParent)?.let { filesToClearRootInfo.add(it) }
                        reparseOpenedFiles = true
                    }
                }
                is VFilePropertyChangeEvent -> {
                    if (event.propertyName != VirtualFile.PROP_NAME) continue
                    val file = event.file
                    val newFileName = event.newValue.toString()
                    val oldFileName = event.oldValue.toString()
                    run {
                        filesToClearFileInfo.add(file)
                        filesToClearLocaleConfig.add(file)
                        filesToClearSliceInfos.add(file)
                    }
                    run {
                        if (shouldRefreshForFilePaths(file)) refreshFilePaths = true
                        if (shouldRefreshForInlineScripts(file)) refreshInlineScripts = true
                    }
                    run {
                        if (ParadoxMetadataManager.metadataFileNames.none { newFileName.equals(it, true) || oldFileName.equals(it, true) }) return@run
                        selectRootFile(file)?.let { filesToClearRootInfo.add(it) }
                        reparseOpenedFiles = true
                    }
                }
                is VFileContentChangeEvent -> {
                    val file = event.file
                    val fileName = file.name
                    run {
                        filesToClearLocaleConfig.add(file)
                        filesToClearSliceInfos.add(file)
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
                filesToClearRootInfo.forEach { file -> file.tryPutUserData(PlsKeys.rootInfo, null) }
                filesToClearFileInfo.forEach { file -> file.tryPutUserData(PlsKeys.fileInfo, null) }
                filesToClearLocaleConfig.forEach { file -> file.tryPutUserData(PlsKeys.localeConfig, null) }
                filesToClearSliceInfos.forEach { file -> file.tryPutUserData(ParadoxImageManager.Keys.sliceInfos, null) }
            }

            override fun afterVfsChange() {
                if (refreshFilePaths) {
                    refreshForFilePaths()
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

    private fun shouldRefreshForFilePaths(file: VirtualFile): Boolean {
        return file.fileInfo != null
    }

    private fun refreshForFilePaths() {
        ParadoxModificationTrackers.FilePath.incModificationCount()
    }

    private fun shouldRefreshForInlineScripts(file: VirtualFile): Boolean {
        return ParadoxInlineScriptManager.getInlineScriptExpression(file) != null
    }

    private fun refreshForInlineScripts() {
        ParadoxModificationTrackers.ScriptFile.incModificationCount()
        ParadoxModificationTrackers.InlineScripts.incModificationCount()
    }

    private fun reparseOpenedFiles() {
        // 重新解析所有项目的所有已打开的文件
        val files = PlsAnalyzeManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsAnalyzeManager.reparseFiles(files)
    }

    private fun reparseOpenedFilesForInlineScripts() {
        // 重新解析所有项目的所有已打开的内联脚本文件
        val files = PlsAnalyzeManager.findOpenedFiles(onlyParadoxFiles = true, onlyInlineScriptFiles = true)
        PlsAnalyzeManager.reparseFiles(files)
    }
}
