package icu.windea.pls.lang.util

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
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.util.StatefulValue
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.analyze.ParadoxAnalyzeService
import icu.windea.pls.lang.listeners.ParadoxRootInfoListener
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path
import kotlin.io.path.notExists

object ParadoxAnalyzeManager {
    private val logger = thisLogger()

    fun getRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        if (!rootFile.isDirectory) return null

        // try to get injected root info first
        doGetInjectedRootInfo(rootFile)?.let { return it }

        // get root info from cache (load if necessary)
        return doGetCachedRootInfo(rootFile)
    }

    private fun doGetInjectedRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        return rootFile.getUserData(PlsKeys.injectedRootInfo)
    }

    private fun doGetCachedRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        val cachedRootInfo = rootFile.getOrPutUserData(PlsKeys.cachedRootInfo) { StatefulValue() }
        if (cachedRootInfo.isInitialized) return cachedRootInfo.value
        synchronized(cachedRootInfo) {
            if (cachedRootInfo.isInitialized) return cachedRootInfo.value
            try {
                val rootInfo = ParadoxAnalyzeService.resolveRootInfo(rootFile)
                cachedRootInfo.value = rootInfo
                if (rootInfo != null && !PlsFileManager.isLightFile(rootFile)) {
                    application.messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onAdd(rootInfo)
                }
                return rootInfo
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                logger.warn(e)
                cachedRootInfo.value = null
                return null
            }
        }
    }

    fun getFileInfo(element: PsiElement): ParadoxFileInfo? {
        val file = selectFile(element) ?: return null
        return getFileInfo(file)
    }

    fun getFileInfo(file: VirtualFile): ParadoxFileInfo? {
        // no fileInfo for `VirtualFileWindow` (injected PSI)
        if (PlsFileManager.isInjectedFile(file)) return null

        // try to get injected file info first
        doGetInjectedFileInfo(file)?.let { return it }

        // get file info from cache (load if necessary)
        return doGetCachedFileInfo(file)
    }

    fun getFileInfo(filePath: FilePath): ParadoxFileInfo? {
        return doGetFileInfo(filePath)
    }

    private fun doGetInjectedFileInfo(file: VirtualFile): ParadoxFileInfo? {
        return file.getUserData(PlsKeys.injectedFileInfo)
    }

    private fun doGetCachedFileInfo(file: VirtualFile): ParadoxFileInfo? {
        val cachedFileInfo = file.getOrPutUserData(PlsKeys.cachedFileInfo) { StatefulValue() }
        if (cachedFileInfo.isInitialized) return cachedFileInfo.value.takeIf { doValidateCachedFileInfo(it) }
        synchronized(cachedFileInfo) {
            if (cachedFileInfo.isInitialized) return cachedFileInfo.value.takeIf { doValidateCachedFileInfo(it) }
            try {
                val filePath = file.path
                var currentFilePath = filePath.toPathOrNull() ?: return null
                var currentFile = doGetFile(file, currentFilePath)
                while (true) {
                    val rootInfo = if (currentFile == null) null else getRootInfo(currentFile)
                    if (rootInfo != null) {
                        val fileInfo = ParadoxAnalyzeService.resolveFileInfo(file, rootInfo)
                        cachedFileInfo.value = fileInfo
                        return fileInfo
                    }
                    currentFilePath = currentFilePath.parent ?: break
                    currentFile = doGetFile(currentFile?.parent, currentFilePath)
                }
                cachedFileInfo.value = null
                return null
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                logger.warn(e)
                cachedFileInfo.value = null
                return null
            }
        }
    }

    private fun doValidateCachedFileInfo(fileInfo: ParadoxFileInfo?): Boolean {
        if (fileInfo != null && fileInfo.rootInfo is ParadoxRootInfo.MetadataBased) {
            // consistency check
            val expectedRootInfo = doGetCachedRootInfo(fileInfo.rootInfo.rootFile)
            if (expectedRootInfo != fileInfo.rootInfo) {
                return false
            }
        }
        return true
    }

    private fun doGetFileInfo(filePath: FilePath): ParadoxFileInfo? {
        try {
            // 直接尝试通过filePath获取fileInfo
            var currentFilePath = filePath.path.toPathOrNull() ?: return null
            var currentFile = VfsUtil.findFile(currentFilePath, false)
            while (true) {
                val rootInfo = if (currentFile == null) null else getRootInfo(currentFile)
                if (rootInfo != null) {
                    val newFileInfo = ParadoxAnalyzeService.resolveFileInfo(filePath, rootInfo)
                    return newFileInfo
                }
                currentFilePath = currentFilePath.parent ?: break
                currentFile = VfsUtil.findFile(currentFilePath, false)
            }
            return null
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.warn(e)
            return null
        }
    }

    private fun doGetFile(file: VirtualFile?, filePath: Path): VirtualFile? {
        // 尝试兼容某些file是LightVirtualFile的情况（例如，file位于VCS DIFF视图中）
        try {
            if (file is LightVirtualFile) {
                file.originalFile?.let { return it }
                VfsUtil.findFile(filePath, false)?.let { return it }
                return null
            }
            return file
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.warn(e)
            return null
        }
    }

    fun getLocaleConfig(file: VirtualFile, project: Project): CwtLocaleConfig? {
        // 使用简单缓存与文件索引以优化性能（避免直接访问 PSI）

        // try to get injected locale config first
        doGetInjectedLocaleConfig(file)?.let { return it }

        // get locale config from cache (load if necessary)
        return doGetCachedLocaleConfig(file, project)
    }

    private fun doGetInjectedLocaleConfig(file: VirtualFile): CwtLocaleConfig? {
        val localeConfig = file.getUserData(PlsKeys.injectedLocaleConfig)
        return localeConfig
    }

    private fun doGetCachedLocaleConfig(file: VirtualFile, project: Project): CwtLocaleConfig? {
        val cachedLocaleConfig = file.getOrPutUserData(PlsKeys.cachedLocaleConfig) { StatefulValue() }
        if (cachedLocaleConfig.isInitialized) return cachedLocaleConfig.value
        synchronized(cachedLocaleConfig) {
            if (cachedLocaleConfig.isInitialized) return cachedLocaleConfig.value
            val localeConfig = ParadoxAnalyzeService.resolveLocaleConfig(file, project)
            cachedLocaleConfig.value = localeConfig
            return localeConfig
        }
    }

    fun getQuickGameDirectory(gameType: ParadoxGameType): String? {
        val path = PlsPathService.getSteamGamePath(gameType.steamId, gameType.title)
        if (path == null || path.notExists()) return null
        return path.toString()
    }

    fun validateGameDirectory(builder: ValidationInfoBuilder, gameType: ParadoxGameType, gameDirectory: String?): ValidationInfo? {
        // 验证游戏目录是否合法
        // - 路径合法
        // - 路径对应的目录存在
        // - 路径是游戏目录（基于 ParadoxMetadataProvider）
        val gameDirectory0 = gameDirectory?.normalizePath()?.orNull() ?: return null
        val path = gameDirectory0.toPathOrNull()
        if (path == null) return builder.error(PlsBundle.message("gameDirectory.error.1"))
        val rootFile = VfsUtil.findFile(path, true)?.takeIf { it.exists() }
        if (rootFile == null) return builder.error(PlsBundle.message("gameDirectory.error.2"))
        val rootInfo = rootFile.rootInfo
        if (rootInfo !is ParadoxRootInfo.Game) return builder.error(PlsBundle.message("gameDirectory.error.3", gameType.title))
        return null
    }

    fun getGameVersionFromGameDirectory(gameDirectory: String?): String? {
        val gameDirectory0 = gameDirectory?.normalizePath()?.orNull() ?: return null
        val rootFile = gameDirectory0.toVirtualFile(true)?.takeIf { it.exists() } ?: return null
        val rootInfo = rootFile.rootInfo
        if (rootInfo !is ParadoxRootInfo.Game) return null
        return rootInfo.version
    }

    /**
     * 比较游戏版本。
     *
     * - 使用由点号分割的整数组成的游戏版本号，如 `3.14`。
     * - 允许通配符，如 "3.14.*"。
     * - 允许后缀，如 `3.99.1 beta`。
     */
    fun compareGameVersion(version1: String, version2: String): Int {
        val s1 = version1.splitByBlank(limit = 2)
        val s2 = version2.splitByBlank(limit = 2)
        val r = compareGameVersionNumbers(s1.first(), s2.first())
        if (r != 0) return r
        return compareGameVersionSuffix(s1.getOrNull(1), s2.getOrNull(1))
    }

    private fun compareGameVersionNumbers(numbers1: String, numbers2: String): Int {
        val l1 = numbers1.split('.')
        val l2 = numbers2.split('.')
        val maxSize = Integer.max(l1.size, l2.size)
        for (i in 0 until maxSize) {
            val r = compareGameVersionNumber(l1.getOrNull(i), l2.getOrNull(i))
            if (r != 0) return r
        }
        return 0
    }

    private fun compareGameVersionNumber(number1: String?, number2: String?): Int {
        val s1 = number1.orEmpty()
        val s2 = number2.orEmpty()
        if (s1 == "*" || s2 == "*") return 0
        if (s1 == s2) return 0
        val n1 = s1.toIntOrNull()
        val n2 = s2.toIntOrNull()
        if (n1 == null || n2 == null) return s1.compareTo(s2)
        return n1.compareTo(n2)
    }

    private fun compareGameVersionSuffix(suffix1: String?, suffix2: String?): Int {
        val s1 = suffix1.orEmpty()
        val s2 = suffix2.orEmpty()
        return when {
            s1.isEmpty() && s2.isEmpty() -> 0
            s1.isEmpty() -> 1
            s2.isEmpty() -> -1
            else -> s1.compareTo(s2)
        }
    }
}
