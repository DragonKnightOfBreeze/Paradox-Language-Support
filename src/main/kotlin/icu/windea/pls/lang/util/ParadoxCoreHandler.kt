package icu.windea.pls.lang.util

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vcs.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.testFramework.*
import com.intellij.util.*
import com.intellij.util.concurrency.annotations.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.util.script.*
import icu.windea.pls.localisation.*
import icu.windea.pls.model.*
import icu.windea.pls.model.path.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.nio.file.*
import kotlin.io.path.*

object ParadoxCoreHandler {
    fun onAddRootInfo(rootFile: VirtualFile, rootInfo: ParadoxRootInfo) {
        if(ParadoxFileManager.isLightFile(rootFile)) return
        ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onAdd(rootInfo)
    }
    
    fun onRemoveRootInfo(rootFile: VirtualFile, rootInfo: ParadoxRootInfo) {
        if(ParadoxFileManager.isLightFile(rootFile)) return
        ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onRemove(rootInfo)
    }
    
    fun getRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        if(!rootFile.isDirectory) return null
        //这里不需要判断文件是否仍然合法（未被删除）
        
        //首先尝试获取注入的rootInfo
        val injectedRootInfo = rootFile.getUserData(PlsKeys.injectedRootInfo)
        if(injectedRootInfo != null) return injectedRootInfo
        
        val cachedRootInfoOrEmpty = rootFile.getUserData(PlsKeys.rootInfo)
        val cachedRootInfo = cachedRootInfoOrEmpty.castOrNull<ParadoxRootInfo>()
        if(cachedRootInfoOrEmpty != null) return cachedRootInfo
        
        try {
            val rootInfo = doGetRootInfo(rootFile)
            if(rootInfo != null) {
                rootFile.tryPutUserData(PlsKeys.rootInfo, rootInfo)
                onAddRootInfo(rootFile, rootInfo)
            } else {
                rootFile.tryPutUserData(PlsKeys.rootInfo, EMPTY_OBJECT)
                if(cachedRootInfo != null) onRemoveRootInfo(rootFile, cachedRootInfo)
            }
            rootFile.tryPutUserData(PlsKeys.rootInfo, rootInfo)
            return rootInfo
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            rootFile.tryPutUserData(PlsKeys.rootInfo, EMPTY_OBJECT)
            return null
        }
    }
    
    private fun doGetRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        // 尝试从此目录向下查找descriptor.mod
        val descriptorFile = disableLogger { getDescriptorFile(rootFile) }
        if(descriptorFile != null) {
            val descriptorInfo = getDescriptorInfo(descriptorFile) ?: return null
            return ParadoxModRootInfo(rootFile, descriptorFile, descriptorInfo)
        }
        
        // 尝试从此目录向下递归查找launcher-settings.json，如果找到，再根据"dlcPath"的值获取游戏文件的根目录
        // 注意游戏文件可能位于此目录的game子目录中，而非直接位于此目录中
        val launcherSettingsFile = disableLogger { getLauncherSettingsFile(rootFile) }
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
    
    fun getInferredGameType(rootInfo: ParadoxModRootInfo): ParadoxGameType? {
        val parentDir = rootInfo.rootFile.parent
        runCatchingCancelable {
            //如果模组目录直接位于游戏创意工坊目录下，直接推断为对应的游戏类型
            val steamWorkshopDir = parentDir ?: return@runCatchingCancelable null
            val steamId = steamWorkshopDir.name
            ParadoxGameType.resolveBySteamId(steamId)?.takeIf { getSteamWorkshopPath(steamId) == steamWorkshopDir.toNioPath().absolutePathString() }
        }.getOrNull()?.let { return it }
        runCatchingCancelable {
            //如果模组目录直接位于游戏数据目录下的mod子目录下，直接推断为对应的游戏类型
            val modDir = parentDir.takeIf { it.name == "mod" } ?: return@runCatchingCancelable null
            val gameDataDir = modDir.parent ?: return@runCatchingCancelable null
            val gameName = gameDataDir.name
            ParadoxGameType.resolveByTitle(gameName)?.takeIf { getGameDataPath(gameName) == gameDataDir.toNioPath().absolutePathString() }
        }.getOrNull()?.let { return it }
        return null
    }
    
    fun getFileInfo(element: PsiElement): ParadoxFileInfo? {
        val file = selectFile(element) ?: return null
        return getFileInfo(file)
    }
    
    fun getFileInfo(file: VirtualFile): ParadoxFileInfo? {
        //这里不需要判断文件是否仍然合法（未被删除）
        
        //首先尝试获取注入的fileInfo
        val injectedFileInfo = file.getUserData(PlsKeys.injectedFileInfo)
        if(injectedFileInfo != null) return injectedFileInfo
        
        val cachedFileInfoOrEmpty = file.getUserData(PlsKeys.fileInfo)
        val cachedFileInfo = cachedFileInfoOrEmpty.castOrNull<ParadoxFileInfo>()
        if(cachedFileInfoOrEmpty != null) return cachedFileInfo
        
        try {
            val fileName = file.name
            val filePath = file.path
            var currentFilePath = filePath.toPathOrNull() ?: return null
            var currentFile = doGetFile(file, currentFilePath)
            while(true) {
                val rootInfo = if(currentFile == null) null else getRootInfo(currentFile)
                if(rootInfo != null) {
                    val fileInfo = doGetFileInfo(file, filePath, fileName, rootInfo)
                    file.tryPutUserData(PlsKeys.fileInfo, fileInfo)
                    return fileInfo
                }
                currentFilePath = currentFilePath.parent ?: break
                currentFile = doGetFile(currentFile?.parent, currentFilePath)
            }
            file.tryPutUserData(PlsKeys.fileInfo, EMPTY_OBJECT)
            return null
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            file.tryPutUserData(PlsKeys.fileInfo, EMPTY_OBJECT)
            return null
        }
    }
    
    private fun doGetFile(file: VirtualFile?, filePath: Path): VirtualFile? {
        //尝试兼容某些file是LightVirtualFile的情况（例如，file位于VCS DIFF视图中）
        if(file is LightVirtualFile) {
            file.originalFile?.let { return it }
            //since 223-EAP, call 'VfsUtil.findFile(Path, boolean)' may cause:
            //java.lang.Throwable: Slow operations are prohibited on EDT. See SlowOperations.assertSlowOperationsAreAllowed javadoc.
            disableLogger { VfsUtil.findFile(filePath, false) }?.let { return it }
            return null
        }
        return file
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
        try {
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
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
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
    
    fun getLocaleConfig(file: VirtualFile, project: Project): CwtLocalisationLocaleConfig? {
        //这里不需要判断文件是否仍然合法（未被删除）
        //使用简单缓存 + 文件索引以优化性能（避免直接访问PSI）
        
        //首先尝试获取注入的localeConfig
        val injectedLocaleConfig = file.getUserData(PlsKeys.injectedLocaleConfig)
        if(injectedLocaleConfig != null) return injectedLocaleConfig
        
        val cachedLocaleConfig = file.getUserData(PlsKeys.localeConfig)
        if(cachedLocaleConfig != null) return cachedLocaleConfig.castOrNull()
        
        val indexKey = ParadoxFileLocaleIndexInstance.name
        val localeId = FileBasedIndex.getInstance().getFileData(indexKey, file, project).keys.singleOrNull() ?: return null
        val localeConfig = getConfigGroup(project, null).localisationLocalesById.get(localeId)
        file.tryPutUserData(PlsKeys.localeConfig, localeConfig ?: EMPTY_OBJECT)
        return localeConfig
    }
    
    @RequiresWriteLock
    fun reparseFilesByRootFilePaths(rootFilePaths: Set<String>): MutableSet<VirtualFile> {
        //重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
        val files = mutableSetOf<VirtualFile>()
        rootFilePaths.forEach f@{ rootFilePath ->
            val rootFile = VfsUtil.findFile(rootFilePath.toPathOrNull() ?: return@f, true) ?: return@f
            VfsUtil.visitChildrenRecursively(rootFile, object : VirtualFileVisitor<Void>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    if(file.isFile) files.add(file)
                    return true
                }
            })
        }
        FileContentUtil.reparseFiles(getDefaultProject(), files, false)
        return files
    }
    
    @RequiresWriteLock
    fun reparseFilesByFileNames(fileNames: Set<String>): Set<VirtualFile> {
        //重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
        val files = mutableSetOf<VirtualFile>()
        val project = getTheOnlyOpenOrDefaultProject()
        FilenameIndex.processFilesByNames(fileNames, false, GlobalSearchScope.allScope(project), null) { file ->
            if(file.isFile) files.add(file)
            true
        }
        FileContentUtil.reparseFiles(getDefaultProject(), files, false)
        return files
    }
    
    @RequiresWriteLock
    fun reparseOpenedFiles() {
        //重新解析所有项目的所有已打开的文件
        FileContentUtil.reparseOpenedFiles()
    }
    
    fun requestReindex(files: Collection<VirtualFile>) {
        //请求重新索引
        val fileBasedIndex = FileBasedIndex.getInstance()
        files.forEach { file ->
            fileBasedIndex.requestReindex(file)
        }
    }
    
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
                            val psiFile = file.toPsiFile(project) ?: continue
                            DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
                            
                            //removed in IU-233
                            //InlayHintsPassFactory.clearModificationStamp(fileEditor.editor)
                        }
                    }
                }
            }
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            thisLogger().warn(e.message)
        }
    }
    
    fun refreshInlayHints() {
        //刷新脚本文件和本地化文件的内嵌提示
        refreshInlayHints { file, _ ->
            val fileType = file.fileType
            fileType == ParadoxScriptFileType || fileType == ParadoxLocalisationFileType
        }
    }
    
    fun refreshInlineScriptInlayHints() {
        //重新解析内联脚本文件
        ProjectManager.getInstance().openProjects.forEach { project ->
            ParadoxModificationTrackerProvider.getInstance(project).ScriptFileTracker.incModificationCount()
            ParadoxModificationTrackerProvider.getInstance(project).InlineScriptsTracker.incModificationCount()
        }
        //刷新内联脚本文件的内嵌提示
        refreshInlayHints { file, _ ->
            ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null
        }
    }
}