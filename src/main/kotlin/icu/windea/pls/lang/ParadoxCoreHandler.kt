package icu.windea.pls.lang

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.codeInsight.hints.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileTypes.ex.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vcs.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.concurrency.annotations.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.script.*

object ParadoxCoreHandler {
    fun onAddRootInfo(rootFile: VirtualFile, rootInfo: ParadoxRootInfo) {
        if(ParadoxFileManager.isLightFile(rootFile)) return
        ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onAdd(rootInfo)
    }
    
    fun onRemoveRootInfo(rootFile: VirtualFile, rootInfo: ParadoxRootInfo) {
        if(ParadoxFileManager.isLightFile(rootFile)) return
        ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onRemove(rootInfo)
    }
    
    fun getRootInfo(rootFile: VirtualFile, refresh: Boolean = true): ParadoxRootInfo? {
        if(!rootFile.isDirectory) return null
        if(!runCatching { rootFile.isValid }.getOrDefault(true)) return null //注意这里可能会抛出异常
        
        //首先尝试获取注入的rootInfo
        val injectedRootInfo = rootFile.getUserData(PlsKeys.injectedRootInfoKey)
        if(injectedRootInfo != null) return injectedRootInfo
        
        val rootInfo = rootFile.getUserData(PlsKeys.rootInfoKey)
        if(!refresh) return rootInfo
        val rootInfoStatus = rootFile.getUserData(PlsKeys.rootInfoStatusKey)
        if(rootInfoStatus != null) return rootInfo
        
        try {
            val newRootInfo = doGetRootInfo(rootFile)
            if(newRootInfo != null) {
                rootFile.tryPutUserData(PlsKeys.rootInfoStatusKey, true)
                rootFile.tryPutUserData(PlsKeys.rootInfoKey, newRootInfo)
                onAddRootInfo(rootFile, newRootInfo)
            } else {
                rootFile.putUserData(PlsKeys.rootInfoStatusKey, false)
                if(rootInfo != null) onRemoveRootInfo(rootFile, rootInfo)
            }
            rootFile.tryPutUserData(PlsKeys.rootInfoKey, newRootInfo)
            return newRootInfo
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            rootFile.tryPutUserData(PlsKeys.rootInfoStatusKey, null)
            return null
        }
    }
    
    private fun doGetRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        // 尝试从此目录向下查找descriptor.mod
        val descriptorFile = getDescriptorFile(rootFile)
        if(descriptorFile != null) {
            val descriptorInfo = getDescriptorInfo(descriptorFile) ?: return null
            return ParadoxModRootInfo(rootFile, descriptorFile, descriptorInfo)
        }
        
        // 尝试从此目录向下递归查找launcher-settings.json，如果找到，再根据"dlcPath"的值获取游戏文件的根目录
        // 注意游戏文件可能位于此目录的game子目录中，而非直接位于此目录中
        val launcherSettingsFile = getLauncherSettingsFile(rootFile)
        if(launcherSettingsFile != null) {
            val launcherSettingsInfo = getLauncherSettingsInfo(launcherSettingsFile) ?: return null
            return ParadoxGameRootInfo(rootFile, launcherSettingsFile, launcherSettingsInfo)
        }
        
        return null
    }
    
    private fun getLauncherSettingsFile(root: VirtualFile): VirtualFile? {
        //launcher-settings.json
        root.findChild(PlsConstants.launcherSettingsFileName)
            ?.takeIf { !it.isDirectory }
            ?.let { return it }
        root.findChild("launcher")
            ?.takeIf { it.isDirectory }
            ?.findChild(PlsConstants.launcherSettingsFileName)
            ?.takeIf { !it.isDirectory }
            ?.let { return it }
        return null
    }
    
    private fun getDescriptorFile(rootFile: VirtualFile): VirtualFile? {
        return rootFile.findChild(PlsConstants.descriptorFileName)
    }
    
    fun getLauncherSettingsInfo(file: VirtualFile): ParadoxLauncherSettingsInfo? {
        //launcher-settings.json
        try {
            return doGetLauncherSettingsInfo(file)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }
    
    private fun doGetLauncherSettingsInfo(file: VirtualFile): ParadoxLauncherSettingsInfo {
        return jsonMapper.readValue(file.inputStream)
    }
    
    fun getDescriptorInfo(file: VirtualFile): ParadoxModDescriptorInfo? {
        //descriptor.mod
        try {
            return runReadAction { doGetDescriptorInfo(file) }
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }
    
    private fun doGetDescriptorInfo(file: VirtualFile): ParadoxModDescriptorInfo {
        //val psiFile = file.toPsiFile<ParadoxScriptFile>(getDefaultProject()) ?: return null //会导致StackOverflowError
        val psiFile = ParadoxScriptElementFactory.createDummyFile(getDefaultProject(), file.inputStream.reader().readText())
        val data = ParadoxScriptDataResolver.resolve(psiFile)
        val name = data?.getData("name")?.value?.stringValue() ?: file.parent?.name ?: "" //如果没有name属性，则使用根目录名
        val version = data?.getData("version")?.value?.stringValue()
        val picture = data?.getData("picture")?.value?.stringValue()
        val tags = data?.getAllData("tags")?.mapNotNull { it.value?.stringValue() }?.toSet()
        val supportedVersion = data?.getData("supported_version")?.value?.stringValue()
        val remoteFileId = data?.getData("remote_file_id")?.value?.stringValue()
        val path = data?.getData("path")?.value?.stringValue()
        return ParadoxModDescriptorInfo(name, version, picture, tags, supportedVersion, remoteFileId, path)
    }
    
    fun getFileInfo(element: PsiElement, refresh: Boolean = true): ParadoxFileInfo? {
        val file = selectFile(element) ?: return null
        return getFileInfo(file, refresh)
    }
    
    fun getFileInfo(file: VirtualFile, refresh: Boolean = true): ParadoxFileInfo? {
        if(!runCatching { file.isValid }.getOrDefault(true)) return null //注意这里可能会抛出异常
        
        //首先尝试获取注入的fileInfo
        val injectedFileInfo = file.getUserData(PlsKeys.injectedFileInfoKey)
        if(injectedFileInfo != null) return injectedFileInfo
        
        val fileInfo = file.getUserData(PlsKeys.fileInfoKey)
        if(!refresh) return fileInfo
        if(fileInfo != null) {
            val rootFile = fileInfo.rootInfo.rootFile
            val rootInfo = getRootInfo(rootFile)
            if(rootInfo === fileInfo.rootInfo) return fileInfo
        }
        
        //这里不能直接获取file.parent，需要基于filePath尝试获取parent，因为file可能是内存文件
        val isLightFile = ParadoxFileManager.isLightFile(file)
        val fileName = file.name
        val filePath = file.path
        var currentFilePath = filePath.toPathOrNull() ?: return null
        var currentFile = if(isLightFile) VfsUtil.findFile(currentFilePath, false) else file
        while(true) {
            val rootInfo = if(currentFile == null) null else getRootInfo(currentFile)
            if(rootInfo != null) {
                val newFileInfo = doGetFileInfo(file, filePath, fileName, rootInfo)
                file.tryPutUserData(PlsKeys.fileInfoKey, newFileInfo)
                return newFileInfo
            }
            currentFilePath = currentFilePath.parent ?: break
            currentFile = currentFile?.parent ?: if(isLightFile) VfsUtil.findFile(currentFilePath, false) else break
        }
        file.tryPutUserData(PlsKeys.fileInfoKey, null)
        return null
    }
    
    private fun doGetFileInfo(file: VirtualFile, filePath: String, fileName: String, rootInfo: ParadoxRootInfo): ParadoxFileInfo {
        val path = ParadoxPath.resolve(filePath.removePrefix(rootInfo.rootFile.path).trimStart('/'))
        val entry = resolveEntry(path, rootInfo)
        val pathToEntry = if(entry == null) path else ParadoxPath.resolve(path.path.removePrefix("$entry/"))
        val fileType = ParadoxFileType.resolve(file, pathToEntry, rootInfo)
        val fileInfo = ParadoxFileInfo(fileName, path, entry, pathToEntry, fileType, rootInfo)
        return fileInfo
    }
    
    fun getFileInfo(filePath: FilePath): ParadoxFileInfo? {
        //直接尝试通过filePath获取fileInfo
        val fileName = filePath.name
        var currentFilePath = filePath.path.toPathOrNull() ?: return null
        var currentFile = VfsUtil.findFile(currentFilePath, false)
        while(true) {
            val rootInfo = if(currentFile == null) null else getRootInfo(currentFile)
            if(rootInfo != null) {
                val newFileInfo = doGetFileInfo(filePath, fileName, rootInfo)
                return newFileInfo
            }
            currentFilePath = currentFilePath.parent ?: break
            currentFile = VfsUtil.findFile(currentFilePath, false)
        }
        return null
    }
    
    private fun doGetFileInfo(filePath: FilePath, fileName: String, rootInfo: ParadoxRootInfo): ParadoxFileInfo {
        val path = ParadoxPath.resolve(filePath.path.removePrefix(rootInfo.rootFile.path).trimStart('/'))
        val entry = resolveEntry(path, rootInfo)
        val pathToEntry = if(entry == null) path else ParadoxPath.resolve(path.path.removePrefix("$entry/"))
        val fileType = ParadoxFileType.resolve(filePath, pathToEntry, rootInfo)
        val fileInfo = ParadoxFileInfo(fileName, path, entry, pathToEntry, fileType, rootInfo)
        return fileInfo
    }
    
    private fun resolveEntry(path: ParadoxPath, rootInfo: ParadoxRootInfo): String? {
        if(rootInfo is ParadoxModRootInfo) return null
        val filePath = path.path
        rootInfo.gameEntry?.let { entry ->
            if(entry == filePath) return null
            if(filePath.contains("$entry/")) return entry
        }
        rootInfo.gameType.entries.forEach { entry ->
            if(entry == filePath) return null
            if(filePath.contains("$entry/")) return entry
        }
        return null
    }
    
    
    @RequiresWriteLock
    fun reparseFilesByRootFilePaths(rootFilePaths: Set<String>) {
        //重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
        try {
            FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Root of paradox files $rootFilePaths changed.") { }
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e.message)
        } finally {
            //要求重新索引
            for(rootFilePath in rootFilePaths) {
                val path = rootFilePath.toPathOrNull() ?: continue
                val rootFile = VfsUtil.findFile(path, false) ?: continue
                FileBasedIndex.getInstance().requestReindex(rootFile)
            }
        }
    }
    
    @RequiresWriteLock
    fun reparseFilesByFileNames(fileNames: Set<String>) {
        //重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
        val files = mutableSetOf<VirtualFile>()
        try {
            val project = getTheOnlyOpenOrDefaultProject()
            FilenameIndex.processFilesByNames(fileNames, false, GlobalSearchScope.allScope(project), null) { file ->
                files.add(file)
                true
            }
            FileContentUtil.reparseFiles(project, files, true)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e.message)
        } finally {
            //要求重新索引
            for(file in files) {
                FileBasedIndex.getInstance().requestReindex(file)
            }
        }
    }
    
    @RequiresWriteLock
    fun reparseOpenedFiles() {
        //重新解析所有项目的所有已打开的文件
        FileContentUtil.reparseOpenedFiles()
    }
    
    @Suppress("UnstableApiUsage")
    fun refreshInlayHints(predicate: (VirtualFile, Project) -> Boolean = { _, _ -> true }) {
        //刷新符合条件的所有项目的所有已打开的文件的内嵌提示
        //com.intellij.codeInsight.hints.VcsCodeAuthorInlayHintsProviderKt.refreshCodeAuthorInlayHints
        try {
            val openProjects = ProjectManager.getInstance().openProjects
            if(openProjects.isEmpty()) return
            for(project in openProjects) {
                val allEditors = FileEditorManager.getInstance(project).allEditors
                if(allEditors.isEmpty()) continue
                for(fileEditor in allEditors) {
                    if(fileEditor is TextEditor) {
                        val file = fileEditor.file
                        if(predicate(file, project)) {
                            val editor = fileEditor.editor
                            InlayHintsPassFactory.clearModificationStamp(editor)
                        }
                    }
                }
            }
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e.message)
        }
    }
}