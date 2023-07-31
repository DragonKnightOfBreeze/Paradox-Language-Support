package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*

/**
 * 监听文件更改以更新相关信息缓存。
 */
class ParadoxCoreFileListener : AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier {
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                var reparseOpenedFiles = false
                
                for(event in events) {
                    when(event) {
                        is VFileCreateEvent -> {
                            if(event.childName.equals(PlsConstants.descriptorFileName, true)) {
                                doClearRootInfo(event.parent)
                                reparseOpenedFiles = true
                            }
                        }
                        is VFileDeleteEvent -> {
                            doClearFileInfo(event.file)
                            if(event.file.name.equals(PlsConstants.descriptorFileName, true)) {
                                doClearRootInfo(event.file.parent)
                                reparseOpenedFiles = true
                            }
                        }
                        is VFileCopyEvent -> {
                            if(event.newChildName.equals(PlsConstants.descriptorFileName, true)) {
                                doClearRootInfo(event.newParent)
                                reparseOpenedFiles = true
                            }
                        }
                        is VFileMoveEvent -> {
                            doClearFileInfo(event.file)
                            if(event.file.name.equals(PlsConstants.descriptorFileName, true)) {
                                doClearRootInfo(event.oldParent)
                                doClearRootInfo(event.newParent)
                                reparseOpenedFiles = true
                            }
                        }
                        is VFilePropertyChangeEvent -> {
                            if(event.propertyName == VirtualFile.PROP_NAME) {
                                doClearFileInfo(event.file)
                                if(event.newValue.toString().equals(PlsConstants.descriptorFileName, true)) {
                                    doClearRootInfo(event.file.parent)
                                    reparseOpenedFiles = true
                                } else if(event.oldValue.toString().equals(PlsConstants.descriptorFileName, true)) {
                                    doClearRootInfo(event.file.parent)
                                    reparseOpenedFiles = true
                                }
                            }
                        }
                        is VFileContentChangeEvent -> {
                            val fileName = event.file.name
                            if(fileName.equals(PlsConstants.descriptorFileName, true)) {
                                val rootFile = selectRootFile(event.file)
                                doClearRootInfo(rootFile)
                            } else if(fileName.equals(PlsConstants.launcherSettingsFileName, true)) {
                                val rootFile = selectRootFile(event.file)
                                doClearRootInfo(rootFile)
                            }
                        }
                    }
                }
                
                //处理内联脚本文件的变动
                run {
                    for(event in events) {
                        when(event) {
                            is VFileMoveEvent -> {
                                if(ParadoxInlineScriptHandler.getInlineScriptExpression(event.file) != null) {
                                    doRefreshInlineScripts()
                                    return@run
                                }
                            }
                            is VFilePropertyChangeEvent -> {
                                if(event.propertyName == VirtualFile.PROP_NAME) {
                                    if(ParadoxInlineScriptHandler.getInlineScriptExpression(event.file) != null) {
                                        doRefreshInlineScripts()
                                        return@run
                                    }
                                }
                            }
                        }
                    }
                }
                
                if(reparseOpenedFiles) {
                    runReadAction { ParadoxCoreHandler.reparseOpenedFiles() }
                }
            }
        }
    }
    
    private fun doClearRootInfo(rootFile: VirtualFile?) {
        if(rootFile == null) return
        //清空根目录信息缓存
        rootFile.tryPutUserData(PlsKeys.rootInfoStatus, null)
    }
    
    private fun doClearFileInfo(file: VirtualFile?) {
        if(file == null) return
        file.tryPutUserData(PlsKeys.fileInfoStatus, null)
    }
    
    private fun doRefreshInlineScripts() {
        //要求重新解析内联脚本文件
        ProjectManager.getInstance().openProjects.forEach { project ->
            ParadoxPsiModificationTracker.getInstance(project).ScriptFileTracker.incModificationCount()
            ParadoxPsiModificationTracker.getInstance(project).InlineScriptsTracker.incModificationCount()
        }
        //刷新内联脚本文件的内嵌提示
        ParadoxCoreHandler.refreshInlayHints { file, _ ->
            ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null
        }
    }
}