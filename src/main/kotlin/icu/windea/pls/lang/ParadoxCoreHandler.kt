package icu.windea.pls.lang

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.codeInsight.hints.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileTypes.ex.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*
import java.lang.invoke.*

object ParadoxCoreHandler {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    fun shouldIndexFile(virtualFile: VirtualFile): Boolean {
        try {
            //仅索引有根目录的文件
            val fileInfo = virtualFile.fileInfo ?: return false
            val path = fileInfo.path.path
            //不索引内联脚本文件
            if("common/inline_scripts".matchesPath(path)) {
                return false
            }
            return true
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            return false
        }
    }
    
    @JvmStatic
    fun getFileInfo(virtualFile: VirtualFile): ParadoxFileInfo? {
        return virtualFile.getCopyableUserData(PlsKeys.fileInfoKey)
    }
    
    @JvmStatic
    fun getFileInfo(file: PsiFile): ParadoxFileInfo? {
        return file.originalFile.virtualFile?.let { getFileInfo(it) }
    }
    
    @JvmStatic
    fun getFileInfo(element: PsiElement): ParadoxFileInfo? {
        return element.containingFile?.let { getFileInfo(it) }
    }
    
    @JvmStatic
    fun resolveRootInfo(rootFile: VirtualFile, canBeNotAvailable: Boolean = true): ParadoxRootInfo? {
        if(!rootFile.isDirectory) return null
        val rootInfo = rootFile.getCopyableUserData(PlsKeys.rootInfoKey)
        if(rootInfo != null && (canBeNotAvailable || rootInfo.isAvailable)) {
            onAddRootInfo(rootInfo)
            return rootInfo
        }
        if(rootInfo != null) {
            onRemoveRootInfo(rootInfo)
        }
        val resolvedRootInfo = try {
            doResolveRootInfo(rootFile)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            logger.warn(e)
            null
        }
        runCatching {
            rootFile.putCopyableUserData(PlsKeys.rootInfoKey, resolvedRootInfo)
        }
        if(resolvedRootInfo != null) {
            onAddRootInfo(resolvedRootInfo)
        }
        return resolvedRootInfo
    }
    
    private fun onAddRootInfo(rootInfo: ParadoxRootInfo) {
        ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onAdd(rootInfo)
    }
    
    private fun onRemoveRootInfo(rootInfo: ParadoxRootInfo) {
        ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onRemove(rootInfo)
    }
    
    private fun doResolveRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        if(rootFile is StubVirtualFile || !rootFile.isValid) return null
        if(!rootFile.isDirectory) return null
        
        // 尝试从此目录向下查找descriptor.mod
        val descriptorFile = rootFile.findChild(PlsConstants.descriptorFileName)
        if(descriptorFile != null) {
            return ParadoxModRootInfo(rootFile, descriptorFile, ParadoxRootType.Mod)
        }
        
        // 尝试从此目录向下递归查找launcher-settings.json，如果找到，再根据"dlcPath"的值获取游戏文件的根目录
        // 注意游戏文件可能位于此目录的game子目录中，而非直接位于此目录中
        val launcherSettingsFile = getLauncherSettingsFile(rootFile)
        if(launcherSettingsFile != null) {
            val launcherSettingsInfo = getLauncherSettingsInfo(launcherSettingsFile)
            if(launcherSettingsInfo == null) return null
            return ParadoxGameRootInfo(rootFile, launcherSettingsFile, ParadoxRootType.Game, launcherSettingsInfo)
        }
        
        return null
    }
    
    private fun getLauncherSettingsFile(root: VirtualFile): VirtualFile? {
        root.findChild(PlsConstants.launcherSettingsFileName)
            ?.takeIf { !it.isDirectory }
            ?.let { return it }
        root.findChild("launcher")
            ?.takeIf { it.isDirectory }
            ?.findChild(PlsConstants.launcherSettingsFileName)
            ?.takeIf { !it.isDirectory }
            ?.let { return it }
        return null
        
        //不能这样做 - 太慢了！
        //var result: VirtualFile? = null
        //VfsUtilCore.visitChildrenRecursively(root, object : VirtualFileVisitor<Void?>() {
        //    override fun visitFileEx(file: VirtualFile): Result {
        //        if(file.isDirectory) {
        //            if(file.name.startsWith('.')) return SKIP_CHILDREN //skip .git, .idea, .vscode, etc.
        //            return CONTINUE
        //        }
        //        if(file.name == PlsConstants.launcherSettingsFileName) {
        //            result = file
        //            return skipTo(root)
        //        }
        //        return CONTINUE
        //    }
        //})
        //return result
    }
    
    fun getLauncherSettingsInfo(file: VirtualFile): ParadoxLauncherSettingsInfo? {
        //launcher-settings.json
        return file.getOrPutUserData(PlsKeys.launcherSettingsInfoKey) {
            try {
                return doGetLauncherSettingsInfo(file)
            } catch(e: Exception) {
                if(e is ProcessCanceledException) throw e
                logger.warn(e)
                return null
            }
        }
    }
    
    private fun doGetLauncherSettingsInfo(file: VirtualFile): ParadoxLauncherSettingsInfo? {
        return jsonMapper.readValue(file.inputStream)
    }
    
    fun getDescriptorInfo(file: VirtualFile): ParadoxDescriptorInfo {
        //see: descriptor.cwt
        return file.getOrPutUserData(PlsKeys.descriptorInfoKey) {
            return runReadAction { doGetDescriptorInfo(file) }
        }
    }
    
    private fun doGetDescriptorInfo(file: VirtualFile): ParadoxDescriptorInfo {
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
        return ParadoxDescriptorInfo(name, version, picture, tags, supportedVersion, remoteFileId, path)
    }
    
    @JvmStatic
    fun resolveFileInfo(file: VirtualFile): ParadoxFileInfo? {
        if(file is StubVirtualFile || !file.isValid) return null
        val name = file.name
        var currentFile: VirtualFile? = file.parent
        while(currentFile != null) {
            val rootInfo = resolveRootInfo(currentFile, false)
            if(rootInfo != null) {
                //filePath.relative(rootPath)
                val filePath = file.path.removePrefix(rootInfo.rootFile.path).trimStart('/')
                val path = ParadoxPath.resolve(filePath)
                val entryPath = resolveEntryPath(path, rootInfo)
                val fileType = ParadoxFileType.resolve(file, rootInfo.gameType, entryPath)
                val cachedFileInfo = file.getCopyableUserData(PlsKeys.fileInfoKey)
                if(cachedFileInfo != null && cachedFileInfo.path == path && cachedFileInfo.entryPath == entryPath
                    && cachedFileInfo.fileType == fileType && cachedFileInfo.rootInfo == rootInfo) {
                    return cachedFileInfo
                }
                val fileInfo = ParadoxFileInfo(name, path, entryPath, fileType, rootInfo)
                runCatching { file.putCopyableUserData(PlsKeys.fileInfoKey, fileInfo) }
                return fileInfo
            }
            currentFile = currentFile.parent
        }
        runCatching { file.putCopyableUserData(PlsKeys.fileInfoKey, null) }
        return null
    }
    
    private fun resolveEntryPath(path: ParadoxPath, rootInfo: ParadoxRootInfo): ParadoxPath {
        val filePath = path.path
        rootInfo.gameEntry?.let { entry ->
            if(entry == filePath) return EmptyParadoxPath
            filePath.removePrefixOrNull("$entry/")?.let { return ParadoxPath.resolve(it) }
        }
        rootInfo.gameType.entries.forEach { entry ->
            if(entry == filePath) return EmptyParadoxPath
            filePath.removePrefixOrNull("$entry/")?.let { return ParadoxPath.resolve(it) }
        }
        return path
    }
    
    
    @JvmStatic
    fun reparseFilesInRoot(rootFilePaths: List<String>) {
        //重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
        try {
            FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Root of paradox files $rootFilePaths changed.") { }
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignore
        } finally {
            //要求重新索引
            runWriteAction {
                for(rootFilePath in rootFilePaths) {
                    val path = rootFilePath.toPathOrNull() ?: continue
                    val rootFile = VfsUtil.findFile(path, false) ?: continue
                    FileBasedIndex.getInstance().requestReindex(rootFile)
                }
            }
        }
    }
    
    @JvmStatic
    fun reparseFilesByFileNames(fileNames: Set<String>) {
        //重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
        val files = mutableListOf<VirtualFile>()
        try {
            val project = getTheOnlyOpenOrDefaultProject()
            FilenameIndex.processFilesByNames(fileNames, true, GlobalSearchScope.allScope(project), null) { file ->
                files.add(file)
                true
            }
            FileContentUtil.reparseFiles(project, files, true)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            //ignore
        } finally {
            runWriteAction {
                //要求重新索引
                for(file in files) {
                    FileBasedIndex.getInstance().requestReindex(file)
                }
            }
        }
    }
    
    @Suppress("UnstableApiUsage")
    fun refreshInlayHints(predicate: (VirtualFile, Project) -> Boolean = { _, _ -> true }) {
        //当某些配置变更后，需要刷新内嵌提示
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
            logger.warn(e.message)
        }
    }
}