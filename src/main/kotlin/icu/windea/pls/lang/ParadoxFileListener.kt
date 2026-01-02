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
import icu.windea.pls.lang.analysis.ParadoxAnalysisDataService
import icu.windea.pls.lang.analysis.ParadoxMetadataService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.PlsDaemonManager

/**
 * 用于监听游戏或模组文件的更改，以更新相关缓存。
 */
class ParadoxFileListener : AsyncFileListener {
    private val dataService get() = ParadoxAnalysisDataService.getInstance()

    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier {
        var reparseOpenedFiles = false
        var refreshFilePaths = false
        var refreshInlineScripts = false

        for (event in events) {
            ProgressManager.checkCanceled()
            when (event) {
                is VFileCreateEvent -> {
                    val fileName = event.childName

                    if (shouldRestartAnalysis(fileName)) {
                        clearRootInfo(event.parent)
                        reparseOpenedFiles = true
                    }
                    if (shouldRefreshForFilePaths(event.parent)) {
                        refreshFilePaths = true
                    }
                }
                is VFileDeleteEvent -> {
                    val file = event.file
                    val fileName = file.name

                    clearFileInfo(file)
                    clearLocaleConfig(file)
                    clearSliceInfos(file)

                    if (shouldRestartAnalysis(fileName)) {
                        clearRootInfo(file.parent)
                        reparseOpenedFiles = true
                    }
                    if (shouldRefreshForFilePaths(file.parent)) {
                        refreshFilePaths = true
                    }
                }
                is VFileCopyEvent -> {
                    val fileName = event.newChildName

                    if (shouldRestartAnalysis(fileName)) {
                        clearRootInfo(event.newParent)
                        reparseOpenedFiles = true
                    }
                    if (shouldRefreshForFilePaths(event.newParent)) {
                        refreshFilePaths = true
                    }
                }
                is VFileMoveEvent -> {
                    val file = event.file
                    val fileName = file.name

                    clearFileInfo(file)
                    clearLocaleConfig(file)
                    clearSliceInfos(file)

                    if (shouldRestartAnalysis(fileName)) {
                        clearRootInfo(event.oldParent)
                        clearRootInfo(event.newParent)
                        reparseOpenedFiles = true
                    }
                    if (shouldRefreshForFilePaths(file)) {
                        refreshFilePaths = true
                    }
                    if (shouldRefreshForInlineScripts(file)) {
                        refreshInlineScripts = true
                    }
                }
                is VFilePropertyChangeEvent -> {
                    if (event.propertyName != VirtualFile.PROP_NAME) continue
                    val file = event.file
                    val newFileName = event.newValue.toString()
                    val oldFileName = event.oldValue.toString()

                    clearFileInfo(file)
                    clearLocaleConfig(file)
                    clearSliceInfos(file)

                    if (shouldRestartAnalysis(newFileName) || shouldRestartAnalysis(oldFileName)) {
                        clearRootInfo(file)
                        reparseOpenedFiles = true
                    }
                    if (shouldRefreshForFilePaths(file)) {
                        refreshFilePaths = true
                    }
                    if (shouldRefreshForInlineScripts(file)) {
                        refreshInlineScripts = true
                    }
                }
                is VFileContentChangeEvent -> {
                    val file = event.file
                    val fileName = file.name

                    clearLocaleConfig(file)
                    clearSliceInfos(file)

                    if (shouldRestartAnalysis(fileName)) {
                        clearRootInfo(file)
                        reparseOpenedFiles = true
                    }
                }
            }
        }

        return object : AsyncFileListener.ChangeApplier {
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

    private fun clearRootInfo(file: VirtualFile) {
        val rootFile = selectRootFile(file) ?: return
        with(dataService) { rootFile.cachedRootInfo = null }
    }

    private fun clearFileInfo(file: VirtualFile) {
        with(dataService) { file.cachedFileInfo = null }
    }

    private fun clearLocaleConfig(file: VirtualFile) {
        with(dataService) { file.cachedLocaleConfig = null }
    }

    private fun clearSliceInfos(file: VirtualFile) {
        with(dataService) { file.sliceInfos = null }
    }

    private fun shouldRestartAnalysis(fileName: String): Boolean {
        return ParadoxMetadataService.metadataFileNames.any { fileName.equals(it, true) }
    }

    private fun shouldRefreshForFilePaths(file: VirtualFile?): Boolean {
        return file?.fileInfo != null
    }

    private fun shouldRefreshForInlineScripts(file: VirtualFile): Boolean {
        return ParadoxInlineScriptManager.getInlineScriptExpression(file) != null
    }

    private fun reparseOpenedFiles() {
        // 重新解析所有项目的所有已打开的文件
        val files = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsDaemonManager.reparseFiles(files)
    }

    private fun reparseOpenedFilesForInlineScripts() {
        // 重新解析所有项目的所有已打开的内联脚本文件
        val files = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true, onlyInlineScriptFiles = true)
        PlsDaemonManager.reparseFiles(files)
    }

    private fun refreshForFilePaths() {
        ParadoxModificationTrackers.FilePath.incModificationCount()
    }

    private fun refreshForInlineScripts() {
        ParadoxModificationTrackers.ScriptFile.incModificationCount()
        ParadoxModificationTrackers.InlineScripts.incModificationCount()
    }
}
