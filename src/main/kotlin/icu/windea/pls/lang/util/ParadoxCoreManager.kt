package icu.windea.pls.lang.util

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.application
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.config.configGroup.localisationLocalesById
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.trimFast
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.tryPutUserData
import icu.windea.pls.ep.metadata.ParadoxInferredGameTypeProvider
import icu.windea.pls.ep.metadata.ParadoxMetadataProvider
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.listeners.ParadoxRootInfoListener
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.model.toRootInfo
import java.nio.file.Path
import kotlin.io.path.notExists

object ParadoxCoreManager {
    fun getRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        if (!rootFile.isDirectory) return null

        //首先尝试获取注入的rootInfo
        val injectedRootInfo = rootFile.getUserData(PlsKeys.injectedRootInfo)
        if (injectedRootInfo != null) return injectedRootInfo

        val cachedRootInfo = rootFile.getUserData(PlsKeys.rootInfo)
        if (cachedRootInfo != null) return cachedRootInfo.castOrNull()

        synchronized(rootFile) {
            val _cachedRootInfo = rootFile.getUserData(PlsKeys.rootInfo)
            if (_cachedRootInfo != null) return _cachedRootInfo.castOrNull()

            //resolve rootInfo
            try {
                val rootInfo = doGetRootInfo(rootFile)
                rootFile.tryPutUserData(PlsKeys.rootInfo, rootInfo ?: EMPTY_OBJECT)
                if (rootInfo != null && !PlsVfsManager.isLightFile(rootFile)) {
                    application.messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onAdd(rootInfo)
                }
                return rootInfo
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                thisLogger().warn(e)
                rootFile.tryPutUserData(PlsKeys.rootInfo, EMPTY_OBJECT)
                return null
            }
        }
    }

    private fun doGetRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        val metadata = ParadoxMetadataProvider.getMetadata(rootFile) ?: return null
        return metadata.toRootInfo()
    }

    fun getFileInfo(element: PsiElement): ParadoxFileInfo? {
        val file = selectFile(element) ?: return null
        return getFileInfo(file)
    }

    fun getFileInfo(file: VirtualFile): ParadoxFileInfo? {
        //首先尝试获取注入的fileInfo
        val injectedFileInfo = file.getUserData(PlsKeys.injectedFileInfo)
        if (injectedFileInfo != null) return injectedFileInfo

        //no fileInfo for VirtualFileWindow (injected PSI)
        if (PlsVfsManager.isInjectedFile(file)) return null

        val cachedFileInfo = file.getUserData(PlsKeys.fileInfo)
        if (cachedFileInfo != null) return cachedFileInfo.castOrNull()

        synchronized(file) {
            val _cachedFileInfo = file.getUserData(PlsKeys.fileInfo)
            if (_cachedFileInfo != null) return _cachedFileInfo.castOrNull()

            //resolve fileInfo by file path
            try {
                val filePath = file.path
                var currentFilePath = filePath.toPathOrNull() ?: return null
                var currentFile = doGetFile(file, currentFilePath)
                while (true) {
                    val rootInfo = if (currentFile == null) null else getRootInfo(currentFile)
                    if (rootInfo != null) {
                        val fileInfo = doGetFileInfo(file, filePath, rootInfo)
                        file.tryPutUserData(PlsKeys.fileInfo, fileInfo ?: EMPTY_OBJECT)
                        return fileInfo
                    }
                    currentFilePath = currentFilePath.parent ?: break
                    currentFile = doGetFile(currentFile?.parent, currentFilePath)
                }
                file.tryPutUserData(PlsKeys.fileInfo, EMPTY_OBJECT)
                return null
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                thisLogger().warn(e)
                file.tryPutUserData(PlsKeys.fileInfo, EMPTY_OBJECT)
                return null
            }
        }
    }

    private fun doGetFile(file: VirtualFile?, filePath: Path): VirtualFile? {
        //尝试兼容某些file是LightVirtualFile的情况（例如，file位于VCS DIFF视图中）
        if (file is LightVirtualFile) {
            file.originalFile?.let { return it }
            runReadAction { VfsUtil.findFile(filePath, false) }?.let { return it }
            return null
        }
        return file
    }

    private fun doGetFileInfo(file: VirtualFile, filePath: String, rootInfo: ParadoxRootInfo): ParadoxFileInfo? {
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val relPath = ParadoxPath.resolve(filePath.removePrefix(rootInfo.rootFile.path).trimFast('/'))
        val (path, entryName) = resolvePathAndEntryName(relPath, rootInfo)
        val fileType = when {
            path.length == 1 && rootInfo is ParadoxRootInfo.Game -> ParadoxFileType.Other
            file.isDirectory -> ParadoxFileType.Other
            ParadoxFileManager.isIgnoredFile(file.name) -> ParadoxFileType.Other
            else -> ParadoxFileType.resolve(path)
        }
        val fileInfo = ParadoxFileInfo(path, entryName, fileType, rootInfo)
        return fileInfo
    }

    fun getFileInfo(filePath: FilePath): ParadoxFileInfo? {
        try {
            //直接尝试通过filePath获取fileInfo
            var currentFilePath = filePath.path.toPathOrNull() ?: return null
            var currentFile = VfsUtil.findFile(currentFilePath, false)
            while (true) {
                val rootInfo = if (currentFile == null) null else getRootInfo(currentFile)
                if (rootInfo != null) {
                    val newFileInfo = doGetFileInfo(filePath, rootInfo)
                    return newFileInfo
                }
                currentFilePath = currentFilePath.parent ?: break
                currentFile = VfsUtil.findFile(currentFilePath, false)
            }
            return null
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            return null
        }
    }

    private fun doGetFileInfo(filePath: FilePath, rootInfo: ParadoxRootInfo): ParadoxFileInfo? {
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val relPath = ParadoxPath.resolve(filePath.path.removePrefix(rootInfo.rootFile.path).trimFast('/'))
        val (path, entryName) = resolvePathAndEntryName(relPath, rootInfo)
        val fileType = when {
            path.length == 1 && rootInfo is ParadoxRootInfo.Game -> ParadoxFileType.Other
            filePath.isDirectory -> ParadoxFileType.Other
            ParadoxFileManager.isIgnoredFile(filePath.name) -> ParadoxFileType.Other
            else -> ParadoxFileType.resolve(path)
        }
        val fileInfo = ParadoxFileInfo(path, entryName, fileType, rootInfo)
        return fileInfo
    }

    private fun resolvePathAndEntryName(relPath: ParadoxPath, rootInfo: ParadoxRootInfo): Tuple2<ParadoxPath, String> {
        if (rootInfo is ParadoxRootInfo.Mod) return relPath to ""
        relPath.path.removePrefixOrNull("game/")?.let { return ParadoxPath.resolve(it) to "game" }
        rootInfo.gameType.entryNames.forEach { entryName ->
            relPath.path.removePrefixOrNull("$entryName/")?.let { return ParadoxPath.resolve(it) to entryName }
        }
        return relPath to ""
    }

    fun getLocaleConfig(file: VirtualFile, project: Project): CwtLocaleConfig? {
        //使用简单缓存与文件索引以优化性能（避免直接访问PSI）

        //首先尝试获取注入的localeConfig
        val injectedLocaleConfig = file.getUserData(PlsKeys.injectedLocaleConfig)
        if (injectedLocaleConfig != null) return injectedLocaleConfig

        val cachedLocaleConfig = file.getUserData(PlsKeys.localeConfig)
        if (cachedLocaleConfig != null) return cachedLocaleConfig.castOrNull()

        synchronized(file) {
            val _cachedLocaleConfig = file.getUserData(PlsKeys.localeConfig)
            if (_cachedLocaleConfig != null) return _cachedLocaleConfig.castOrNull()

            val indexId = ParadoxIndexKeys.FileLocale
            val localeId = FileBasedIndex.getInstance().getFileData(indexId, file, project).keys.singleOrNull() ?: return null
            val localeConfig = PlsFacade.getConfigGroup(project, null).localisationLocalesById.get(localeId)
            file.tryPutUserData(PlsKeys.localeConfig, localeConfig ?: EMPTY_OBJECT)
            return localeConfig
        }
    }

    fun getInferredGameType(rootFile: VirtualFile): ParadoxGameType? {
        return ParadoxInferredGameTypeProvider.getGameType(rootFile)
    }

    fun getQuickGameDirectory(gameType: ParadoxGameType): String? {
        val path = PlsFacade.getDataProvider().getSteamGamePath(gameType.steamId, gameType.title)
        if (path == null || path.notExists()) return null
        return path.toString()
    }

    fun getGameVersionFromGameDirectory(gameDirectory: String?): String? {
        val gameDirectory0 = gameDirectory?.normalizePath()?.orNull() ?: return null
        val rootFile = gameDirectory0.toVirtualFile(true)?.takeIf { it.exists() } ?: return null
        val rootInfo = rootFile.rootInfo
        if (rootInfo !is ParadoxRootInfo.Game) return null
        return rootInfo.version
    }

    fun validateGameDirectory(builder: ValidationInfoBuilder, gameType: ParadoxGameType, gameDirectory: String?): ValidationInfo? {
        //验证游戏目录是否合法
        //* 路径合法
        //* 路径对应的目录存在
        //* 路径是游戏目录（基于 ParadoxMetadataProvider）
        val gameDirectory0 = gameDirectory?.normalizePath()?.orNull() ?: return null
        val path = gameDirectory0.toPathOrNull()
        if (path == null) return builder.error(PlsBundle.message("gameDirectory.error.1"))
        val rootFile = VfsUtil.findFile(path, true)?.takeIf { it.exists() }
        if (rootFile == null) return builder.error(PlsBundle.message("gameDirectory.error.2"))
        val rootInfo = rootFile.rootInfo
        if (rootInfo !is ParadoxRootInfo.Game) return builder.error(PlsBundle.message("gameDirectory.error.3", gameType.title))
        return null
    }
}
