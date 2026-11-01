package icu.windea.pls.lang

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.util.application
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.base.analysis.ChronicleAnalysisManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.lang.analysis.ParadoxAnalysisDataManager
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.analysis.ParadoxAnalysisScope
import icu.windea.pls.lang.listeners.ParadoxRootInfoListener
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.constants.ChronicleConstants

@Optimized
class ParadoxFileChangeCollector : ParadoxAnalysisScope {
    private val filesToGetRootInfo: MutableSet<VirtualFile> = hashSetOf()
    private val filesToClearRootInfo: MutableSet<VirtualFile> = hashSetOf()
    private val filesToClearFileInfo: MutableSet<VirtualFile> = hashSetOf()
    private val filesToClearLocaleConfig: MutableSet<VirtualFile> = hashSetOf()

    private var reparseAllOpenFiles: Boolean = false
    private var refreshFilePaths: Boolean = false
    private var refreshInlineScripts: Boolean = false

    fun collectChange(events: List<VFileEvent>) {
        events.forEach f@{ event ->
            ProgressManager.checkCanceled()
            when (event) {
                is VFileCreateEvent -> {
                    val fileName = event.childName
                    val parent = event.parent

                    if (shouldRestartAnalysis(fileName)) {
                        filesToGetRootInfo += parent
                    }
                    if (shouldRefreshForFilePaths(parent)) {
                        refreshFilePaths = true
                    }
                }
                is VFileDeleteEvent -> {
                    val file = event.file
                    val fileName = file.name

                    filesToClearFileInfo += file
                    filesToClearLocaleConfig += file

                    if (shouldRestartAnalysis(fileName)) {
                        file.parent?.let { filesToGetRootInfo += it }
                    }
                    if (shouldRefreshForFilePaths(file.parent)) {
                        refreshFilePaths = true
                    }
                }
                is VFileCopyEvent -> {
                    val fileName = event.newChildName

                    if (shouldRestartAnalysis(fileName)) {
                        event.newParent.let { filesToGetRootInfo += it }
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

                    if (shouldRestartAnalysis(fileName)) {
                        event.newParent.let { filesToGetRootInfo += it }
                        event.oldParent?.let { filesToGetRootInfo += it }
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
                    if (newFileName == oldFileName) return@f // fast return & prevent possible SOF (see `FileContentUtilCore.reparseFiles`)

                    filesToClearFileInfo += file
                    filesToClearLocaleConfig += file

                    if (shouldRestartAnalysis(newFileName) || shouldRestartAnalysis(oldFileName)) {
                        file.parent?.let { filesToGetRootInfo += it }
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

                    if (shouldRestartAnalysis(fileName)) {
                        file.parent?.let { filesToGetRootInfo += it }
                    }
                }
            }
        }
    }

    private fun shouldRestartAnalysis(fileName: String): Boolean {
        return ChronicleConstants.metadataFileNames.any { fileName.equals(it, true) }
    }

    private fun shouldRefreshForFilePaths(file: VirtualFile?): Boolean {
        return file?.fileInfo != null
    }

    private fun shouldRefreshForInlineScripts(file: VirtualFile): Boolean {
        return ParadoxInlineScriptManager.getInlineScriptExpression(file) != null
    }

    fun isChanged(): Boolean {
        return filesToGetRootInfo.isNotEmpty()
            || filesToClearFileInfo.isNotEmpty()
            || filesToClearRootInfo.isNotEmpty()
            || filesToClearLocaleConfig.isNotEmpty()
            || reparseAllOpenFiles
            || refreshFilePaths
            || refreshInlineScripts
    }

    fun afterVfsChange() {
        // 清空分析数据缓存
        // NOTE 2.1.2 分析数据缓存需要在 VFS 更改之后再清空，否则可能会被再次加载
        if (filesToGetRootInfo.isNotEmpty()) {
            filesToGetRootInfo.forEach { file ->
                // NOTE 3.0.5 需要同时检查已缓存和未缓存的数据（仅当 rootFile/metadata 发生变化时才会重置）
                val oldRootInfo = ParadoxAnalysisManager.getFileInfo(file, cached = true)?.rootInfo
                val newRootInfo = ParadoxAnalysisManager.getFileInfo(file, cached = false)?.rootInfo
                val changed = !ParadoxAnalysisManager.isEquivalent(oldRootInfo, newRootInfo)
                if (changed) {
                    application.messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onChange(oldRootInfo, newRootInfo)
                    oldRootInfo?.rootFile?.let { filesToClearRootInfo.add(it) }
                    newRootInfo?.rootFile?.let { filesToClearRootInfo.add(it) }
                }
            }
        }
        if (filesToClearRootInfo.isNotEmpty()) {
            filesToClearRootInfo.forEach { file ->
                ParadoxAnalysisDataManager.clearData(file, ParadoxAnalysisDataManager.Keys.cachedRootInfo)
                // NOTE 3.0.5 这是有必要的，但可能有一定的时间成本（例如，旧的 rootInfo 可能已被缓存为 null，此时其中的 fileInfo 需要全部重置）
                VfsUtil.visitChildrenRecursively(file, object : VirtualFileVisitor<Void>() {
                    override fun visitFile(child: VirtualFile): Boolean {
                        ParadoxAnalysisDataManager.clearData(child, ParadoxAnalysisDataManager.Keys.cachedFileInfo)
                        return true
                    }
                })
            }
            reparseAllOpenFiles = true
        }
        if (filesToClearFileInfo.isNotEmpty()) {
            filesToClearFileInfo.forEach { file ->
                ParadoxAnalysisDataManager.clearData(file, ParadoxAnalysisDataManager.Keys.cachedFileInfo)
            }
        }
        if (filesToClearLocaleConfig.isNotEmpty()) {
            filesToClearLocaleConfig.forEach { file ->
                ParadoxAnalysisDataManager.clearData(file, ParadoxAnalysisDataManager.Keys.cachedLocaleConfig)
            }
        }

        // 通知更改 & 重新解析和索引相关文件
        if (refreshFilePaths) {
            ChronicleModificationTrackers.FilePath.incModificationCount()
        }
        if (refreshInlineScripts) {
            ChronicleModificationTrackers.ScriptFile.incModificationCount()
            ChronicleModificationTrackers.InlineScripts.incModificationCount()
        }
        if (reparseAllOpenFiles) {
            // 重新解析所有项目的所有已打开的文件
            val files = ChronicleAnalysisManager.findAllOpenFiles()
            ChronicleAnalysisManager.reparseFiles(files)
        } else if (refreshInlineScripts) {
            // 重新解析所有项目的所有已打开的内联脚本文件
            val files = ChronicleAnalysisManager.findAllOpenFiles().filter { ParadoxInlineScriptManager.isInlineScriptFile(it) }
            ChronicleAnalysisManager.reparseFiles(files)
        }
    }
}
