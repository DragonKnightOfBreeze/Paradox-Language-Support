package icu.windea.pls.lang.analysis

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.LightVirtualFileBase
import com.intellij.util.application
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.util.StatefulValue
import icu.windea.pls.lang.listeners.ParadoxRootInfoListener
import icu.windea.pls.lang.psi.mock.CwtConfigMockPsiElement
import icu.windea.pls.lang.psi.mock.ParadoxMockPsiElement
import icu.windea.pls.lang.psi.stubs.ParadoxLocaleAwareStub
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.index.CwtConfigIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import java.nio.file.Path

object ParadoxAnalysisManager {
    private val logger = thisLogger()
    private val dataService get() = ParadoxAnalysisDataService.getInstance()

    // region Get Methods

    fun getRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        if (!rootFile.isDirectory) return null

        // skip for `StubVirtualFile` (unsupported)
        if (PlsFileManager.isStubFile(rootFile)) return null

        // try to get injected root info first
        doGetInjectedRootInfo(rootFile)?.let { return it }

        // get root info from cache (load if necessary)
        return doGetCachedRootInfo(rootFile)
    }

    private fun doGetInjectedRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        return with(dataService) { markedRootInfo ?: rootFile.injectedRootInfo }
    }

    private fun doGetCachedRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        val cachedRootInfo = with(dataService) { rootFile.cachedRootInfo ?: StatefulValue<ParadoxRootInfo>().also { rootFile.cachedRootInfo = it } }
        if (cachedRootInfo.isInitialized) return cachedRootInfo.value
        synchronized(cachedRootInfo) {
            if (cachedRootInfo.isInitialized) return cachedRootInfo.value
            runCatchingCancelable {
                val rootInfo = ParadoxAnalysisService.resolveRootInfo(rootFile)
                cachedRootInfo.value = rootInfo
                if (rootInfo != null && !PlsFileManager.isLightFile(rootFile)) {
                    application.messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onAdd(rootInfo)
                }
                return rootInfo
            }.onFailure { e -> logger.warn(e) }
            cachedRootInfo.value = null
            return null
        }
    }

    fun getFileInfo(element: PsiElement): ParadoxFileInfo? {
        val file = selectFile(element) ?: return null
        return getFileInfo(file)
    }

    fun getFileInfo(file: VirtualFile): ParadoxFileInfo? {
        // no file info for `VirtualFileWindow` (injected PSI)
        if (PlsFileManager.isInjectedFile(file)) return null

        // skip for `StubVirtualFile` (unsupported)
        if (PlsFileManager.isStubFile(file)) return null

        // try to get injected file info first
        doGetInjectedFileInfo(file)?.let { return it }

        // get file info from cache (load if necessary)
        return doGetCachedFileInfo(file)
    }

    fun getFileInfo(filePath: FilePath): ParadoxFileInfo? {
        return doGetFileInfo(filePath)
    }

    private fun doGetInjectedFileInfo(file: VirtualFile): ParadoxFileInfo? {
        return with(dataService) { markedFileInfo ?: file.injectedFileInfo }
    }

    private fun doGetCachedFileInfo(file: VirtualFile): ParadoxFileInfo? {
        val cachedFileInfo = with(dataService) { file.cachedFileInfo ?: StatefulValue<ParadoxFileInfo>().also { file.cachedFileInfo = it } }
        if (cachedFileInfo.isInitialized) return cachedFileInfo.value.takeIf { doValidateCachedFileInfo(it) }
        synchronized(cachedFileInfo) {
            if (cachedFileInfo.isInitialized) return cachedFileInfo.value.takeIf { doValidateCachedFileInfo(it) }
            runCatchingCancelable {
                val filePath = file.path
                var currentFilePath = filePath.toPathOrNull() ?: return null
                var currentFile = doGetFile(file, currentFilePath)
                while (true) {
                    val rootInfo = if (currentFile == null) null else getRootInfo(currentFile)
                    if (rootInfo != null) {
                        val fileInfo = ParadoxAnalysisService.resolveFileInfo(file, rootInfo)
                        cachedFileInfo.value = fileInfo
                        return fileInfo
                    }
                    currentFilePath = currentFilePath.parent ?: break
                    currentFile = doGetFile(currentFile?.parent, currentFilePath)
                }
            }.onFailure { e -> logger.warn(e) }
            cachedFileInfo.value = null
            return null
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
            var currentFile = currentFilePath.toVirtualFile()
            while (true) {
                val rootInfo = if (currentFile == null) null else getRootInfo(currentFile)
                if (rootInfo != null) {
                    val newFileInfo = ParadoxAnalysisService.resolveFileInfo(filePath, rootInfo)
                    return newFileInfo
                }
                currentFilePath = currentFilePath.parent ?: break
                currentFile = currentFilePath.toVirtualFile()
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
                filePath.toVirtualFile()?.let { return it }
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

        // skip for `StubVirtualFile` (unsupported)
        if (PlsFileManager.isStubFile(file)) return null

        // try to get injected locale config first
        doGetInjectedLocaleConfig(file)?.let { return it }

        // get locale config from cache (load if necessary)
        return doGetCachedLocaleConfig(file, project)
    }

    private fun doGetInjectedLocaleConfig(file: VirtualFile): CwtLocaleConfig? {
        return with(dataService) { file.injectedLocaleConfig }
    }

    private fun doGetCachedLocaleConfig(file: VirtualFile, project: Project): CwtLocaleConfig? {
        val cachedLocaleConfig = with(dataService) { file.cachedLocaleConfig ?: StatefulValue<CwtLocaleConfig>().also { file.cachedLocaleConfig = it } }
        if (cachedLocaleConfig.isInitialized) return cachedLocaleConfig.value
        synchronized(cachedLocaleConfig) {
            if (cachedLocaleConfig.isInitialized) return cachedLocaleConfig.value
            runCatchingCancelable {
                val localeConfig = ParadoxAnalysisService.resolveLocaleConfig(file, project)
                cachedLocaleConfig.value = localeConfig
                return localeConfig
            }.onFailure { e -> logger.warn(e) }
            cachedLocaleConfig.value = null
            return null
        }
    }

    fun getSliceInfos(file: VirtualFile): MutableSet<String> {
        return with(dataService) { file.sliceInfos ?: mutableSetOf<String>().also { file.sliceInfos = it } }
    }

    // endregion

    // region Manipulation Methods

    fun inferGameTypeFromFileName(file: VirtualFile): ParadoxGameType? {
        if (!ParadoxAnalysisInjector.useGameTypeInference()) return null
        val name = file.nameWithoutExtension
        val gameType = name.split('_', '.').firstNotNullOfOrNull { ParadoxGameType.get(it) }
        return gameType
    }

    // endregion

    // region Select Methods

    tailrec fun selectRootFile(from: Any?): VirtualFile? {
        if (from == null) return null
        return when {
            from is VirtualFileWindow -> selectRootFile(from.delegate) // for injected PSI
            from is LightVirtualFileBase && from.originalFile != null -> selectRootFile(from.originalFile)
            from is VirtualFile -> getFileInfo(from)?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile
            else -> selectRootFile(selectFile(from))
        }
    }

    tailrec fun selectFile(from: Any?): VirtualFile? {
        if (from == null) return null
        return when {
            from is ParadoxIndexInfo -> selectFile(from.virtualFile)
            from is VirtualFileWindow -> from.castOrNull() // for injected PSI (result is from, not from.delegate)
            from is LightVirtualFileBase && from.originalFile != null -> selectFile(from.originalFile)
            from is VirtualFile -> from
            from is PsiDirectory -> selectFile(from.virtualFile)
            from is PsiFile -> selectFile(from.originalFile.virtualFile)
            from is PsiElement -> {
                val nextFrom = runReadActionSmartly { from.containingFile }
                selectFile(nextFrom)
            }
            else -> null
        }
    }

    tailrec fun selectGameType(from: Any?): ParadoxGameType? {
        if (from == null) return null
        if (from is ParadoxGameType) return from
        if (from is VirtualFile) inferGameTypeFromFileName(from)?.let { return it }
        return when {
            from is ParadoxIndexInfo -> from.gameType
            from is CwtConfigIndexInfo -> from.gameType
            from is VirtualFileWindow -> selectGameType(from.delegate) // for injected PSI
            from is LightVirtualFileBase && from.originalFile != null -> selectGameType(from.originalFile)
            from is VirtualFile -> getFileInfo(from)?.rootInfo?.gameType
            from is PsiDirectory -> selectGameType(selectFile(from))
            from is PsiFile -> selectGameType(selectFile(from))
            from is CwtConfigMockPsiElement -> from.gameType
            from is ParadoxMockPsiElement -> from.gameType
            from is ParadoxStub<*> -> from.gameType
            from is StubBasedPsiElementBase<*> -> {
                val nextFrom = runReadActionSmartly { from.greenStub?.castOrNull<ParadoxStub<*>>() ?: from.containingFile }
                selectGameType(nextFrom)
            }
            from is PsiElement -> {
                val nextFrom = runReadActionSmartly { from.parent }
                selectGameType(nextFrom)
            }
            else -> null
        }
    }

    tailrec fun selectLocale(from: Any?): CwtLocaleConfig? {
        if (from == null) return null
        if (from is CwtLocaleConfig) return from
        if (from is VirtualFile) ParadoxAnalysisInjector.getInjectedLocaleConfig(from)?.let { return it }
        return when {
            from is PsiDirectory -> ParadoxLocaleManager.getPreferredLocaleConfig()
            from is PsiFile -> getLocaleConfig(from.virtualFile ?: return null, from.project)
            from is ParadoxLocaleAwareStub<*> -> {
                val element = from.containingFileStub?.psi ?: return null
                val id = from.locale ?: return null
                ParadoxLocaleManager.getLocaleConfigById(element, id)
            }
            from is ParadoxLocalisationLocale -> {
                val id = runReadActionSmartly { from.name }
                ParadoxLocaleManager.getLocaleConfigById(from, id)
            }
            from is StubBasedPsiElementBase<*> -> {
                val nextFrom = runReadActionSmartly { from.greenStub?.castOrNull<ParadoxLocaleAwareStub<*>>() ?: from.parent }
                selectLocale(nextFrom)
            }
            from is PsiElement && from.language is ParadoxLocalisationLanguage -> {
                val nextFrom = runReadActionSmartly { from.parent }
                selectLocale(nextFrom)
            }
            else -> ParadoxLocaleManager.getPreferredLocaleConfig()
        }
    }

    // endregion
}
