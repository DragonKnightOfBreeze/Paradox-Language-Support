package icu.windea.pls.lang

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*

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

        //处理描述符文件的变动
        run {
            events.forEach { event ->
                when (event) {
                    is VFileCreateEvent -> {
                        if (event.childName.equals(PlsConstants.modDescriptorFileName, true)) {
                            event.parent.let { filesToClearRootInfo.add(it) }
                            reparseOpenedFiles = true
                        }
                    }
                    is VFileDeleteEvent -> {
                        event.file.let { filesToClearFileInfo.add(it) }
                        if (event.file.name.equals(PlsConstants.modDescriptorFileName, true)) {
                            event.file.parent.let { filesToClearRootInfo.add(it) }
                            reparseOpenedFiles = true
                        }
                    }
                    is VFileCopyEvent -> {
                        if (event.newChildName.equals(PlsConstants.modDescriptorFileName, true)) {
                            event.newParent.let { filesToClearRootInfo.add(it) }
                            reparseOpenedFiles = true
                        }
                    }
                    is VFileMoveEvent -> {
                        event.file.let { filesToClearFileInfo.add(it) }
                        if (event.file.name.equals(PlsConstants.modDescriptorFileName, true)) {
                            event.oldParent?.let { filesToClearRootInfo.add(it) }
                            event.newParent.let { filesToClearRootInfo.add(it) }
                            reparseOpenedFiles = true
                        }
                    }
                    is VFilePropertyChangeEvent -> {
                        if (event.propertyName == VirtualFile.PROP_NAME) {
                            event.file.let { filesToClearFileInfo.add(it) }
                            if (event.newValue.toString().equals(PlsConstants.modDescriptorFileName, true)) {
                                event.file.parent?.let { filesToClearRootInfo.add(it) }
                                reparseOpenedFiles = true
                            } else if (event.oldValue.toString().equals(PlsConstants.modDescriptorFileName, true)) {
                                event.file.parent?.let { filesToClearRootInfo.add(it) }
                                reparseOpenedFiles = true
                            }
                        }
                    }
                    is VFileContentChangeEvent -> {
                        val fileName = event.file.name
                        if (ParadoxMetadataManager.metadataFileNames.any { fileName.equals(it, true) }) {
                            selectRootFile(event.file)?.let { filesToClearRootInfo.add(it) }
                        }
                    }
                }
            }
        }

        //处理内联脚本文件的变动
        run {
            events.forEach { event ->
                when (event) {
                    is VFileMoveEvent -> {
                        if (ParadoxInlineScriptManager.getInlineScriptExpression(event.file) != null) {
                            refreshInlineScripts = true
                            return@run
                        }
                    }
                    is VFilePropertyChangeEvent -> {
                        if (event.propertyName == VirtualFile.PROP_NAME) {
                            if (ParadoxInlineScriptManager.getInlineScriptExpression(event.file) != null) {
                                refreshInlineScripts = true
                                return@run
                            }
                        }
                    }
                }
            }
        }

        //处理本地化文件的语言区域的变动
        run {
            events.forEach { event ->
                when (event) {
                    is VFileContentChangeEvent -> {
                        if (event.file.fileType == ParadoxLocalisationFileType) {
                            event.file.let { filesToClearLocale.add(it) }
                        }
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
                    refreshInlineScripts()
                }
                if (reparseOpenedFiles) {
                    reparseOpenedFiles()
                }
            }
        }
    }

    private fun clearRootInfo(rootFile: VirtualFile?) {
        if (rootFile == null) return
        rootFile.tryPutUserData(PlsKeys.rootInfo, null)
    }

    private fun clearFileInfo(file: VirtualFile?) {
        if (file == null) return
        file.tryPutUserData(PlsKeys.fileInfo, null)
    }

    private fun clearLocale(file: VirtualFile?) {
        if (file == null) return
        file.tryPutUserData(PlsKeys.localeConfig, null)
    }

    private fun reparseOpenedFiles() {
        //重新解析所有项目的所有已打开的文件
        val openedFiles = PlsManager.findOpenedFiles()
        PlsManager.reparseAndRefreshFiles(openedFiles)
    }

    private fun refreshInlineScripts() {
        ParadoxModificationTrackers.ScriptFileTracker.incModificationCount()
        ParadoxModificationTrackers.InlineScriptsTracker.incModificationCount()
        //重新解析内联脚本文件
        val files = PlsManager.findOpenedFiles { file, _ -> ParadoxInlineScriptManager.getInlineScriptExpression(file) != null }
        PlsManager.reparseAndRefreshFiles(files)
    }
}
