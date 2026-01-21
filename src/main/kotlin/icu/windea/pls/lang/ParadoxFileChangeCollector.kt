package icu.windea.pls.lang

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
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.lang.analysis.ParadoxAnalysisDataService
import icu.windea.pls.lang.analysis.ParadoxMetadataService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.PlsDaemonManager

@Optimized
class ParadoxFileChangeCollector {
    private val dataService get() = ParadoxAnalysisDataService.getInstance()

    private val rootInfoContextFiles: MutableSet<VirtualFile> = FastSet()
    private val rootFilesToClearRootInfo: MutableSet<VirtualFile> = FastSet()

    private val filesToClearFileInfo: MutableSet<VirtualFile> = FastSet()
    private val filesToClearLocaleConfig: MutableSet<VirtualFile> = FastSet()
    private val filesToClearSliceInfos: MutableSet<VirtualFile> = FastSet()

    private var reparseOpenedFiles: Boolean = false
    private var refreshFilePaths: Boolean = false
    private var refreshInlineScripts: Boolean = false

    fun collectChange(events: List<VFileEvent>) {
        events.forEach f@{ event ->
            ProgressManager.checkCanceled()
            when (event) {
                is VFileCreateEvent -> {
                    val fileName = event.childName

                    if (shouldRestartAnalysis(fileName)) {
                        rootInfoContextFiles += event.parent
                        reparseOpenedFiles = true
                    }
                    if (shouldRefreshForFilePaths(event.parent)) {
                        refreshFilePaths = true
                    }
                }
                is VFileDeleteEvent -> {
                    val file = event.file
                    val fileName = file.name

                    filesToClearFileInfo += file
                    filesToClearLocaleConfig += file
                    filesToClearSliceInfos += file

                    if (shouldRestartAnalysis(fileName)) {
                        selectRootFile(file.parent)?.let { rootFilesToClearRootInfo += it }
                        reparseOpenedFiles = true
                    }
                    if (shouldRefreshForFilePaths(file.parent)) {
                        refreshFilePaths = true
                    }
                }
                is VFileCopyEvent -> {
                    val fileName = event.newChildName

                    if (shouldRestartAnalysis(fileName)) {
                        rootInfoContextFiles += event.newParent
                        reparseOpenedFiles = true
                    }
                    if (shouldRefreshForFilePaths(event.newParent)) {
                        refreshFilePaths = true
                    }
                }
                is VFileMoveEvent -> {
                    val file = event.file
                    val fileName = file.name

                    filesToClearFileInfo += file
                    filesToClearLocaleConfig += file
                    filesToClearSliceInfos += file

                    if (shouldRestartAnalysis(fileName)) {
                        selectRootFile(event.oldParent)?.let { rootFilesToClearRootInfo += it }
                        rootInfoContextFiles += event.newParent
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
                    if (event.propertyName != VirtualFile.PROP_NAME) return@f
                    val file = event.file
                    val newFileName = event.newValue.toString()
                    val oldFileName = event.oldValue.toString()

                    filesToClearFileInfo += file
                    filesToClearLocaleConfig += file
                    filesToClearSliceInfos += file

                    if (shouldRestartAnalysis(newFileName) || shouldRestartAnalysis(oldFileName)) {
                        selectRootFile(file)?.let { rootFilesToClearRootInfo += it }
                        rootInfoContextFiles += file
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

                    filesToClearLocaleConfig += file
                    filesToClearSliceInfos += file

                    if (shouldRestartAnalysis(fileName)) {
                        rootInfoContextFiles += file
                        reparseOpenedFiles = true
                    }
                }
            }
        }
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

    fun isChanged(): Boolean {
        return rootInfoContextFiles.isNotEmpty()
            || rootFilesToClearRootInfo.isNotEmpty()
            || filesToClearFileInfo.isNotEmpty()
            || filesToClearLocaleConfig.isNotEmpty()
            || filesToClearSliceInfos.isNotEmpty()
            || reparseOpenedFiles
            || refreshFilePaths
            || refreshInlineScripts
    }

    fun afterVfsChange() {
        // NOTE 2.1.2 分析数据缓存需要在 VFS 更改之后再清空，否则可能会被再次加载

        // 清空分析数据缓存
        if (rootInfoContextFiles.isNotEmpty()) {
            rootInfoContextFiles.forEach { contextFile ->
                selectRootFile(contextFile)?.let { rootFile -> rootFilesToClearRootInfo += rootFile }
            }
        }
        if (rootFilesToClearRootInfo.isNotEmpty()) {
            rootFilesToClearRootInfo.forEach { rootFile ->
                with(dataService) { rootFile.cachedRootInfo = null }
            }
        }
        if (filesToClearFileInfo.isNotEmpty()) {
            filesToClearFileInfo.forEach { file ->
                with(dataService) { file.cachedFileInfo = null }
            }
        }
        if (filesToClearLocaleConfig.isNotEmpty()) {
            filesToClearLocaleConfig.forEach { file ->
                with(dataService) { file.cachedLocaleConfig = null }
            }
        }
        if (filesToClearSliceInfos.isNotEmpty()) {
            filesToClearSliceInfos.forEach { file ->
                with(dataService) { file.sliceInfos = null }
            }
        }

        // 通知更改 & 重新解析和索引相关文件
        if (refreshFilePaths) {
            ParadoxModificationTrackers.FilePath.incModificationCount()
        }
        if (refreshInlineScripts) {
            ParadoxModificationTrackers.ScriptFile.incModificationCount()
            ParadoxModificationTrackers.InlineScripts.incModificationCount()
        }
        if (reparseOpenedFiles) {
            // 重新解析所有项目的所有已打开的文件
            val files = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true)
            PlsDaemonManager.reparseFiles(files)
        } else if (refreshInlineScripts) {
            // 重新解析所有项目的所有已打开的内联脚本文件
            val files = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true, onlyInlineScriptFiles = true)
            PlsDaemonManager.reparseFiles(files)
        }
    }
}
