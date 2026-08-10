package icu.windea.pls.lang.analysis

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import java.nio.file.Path

@Suppress("unused")
object ParadoxAnalysisInjectionManager : ParadoxAnalysisScope {
    // region Get Methods

    fun inferGameTypeFromFileName(file: VirtualFile): ParadoxGameType? {
        if (!useGameTypeInference()) return null
        val name = file.nameWithoutExtension
        val gameType = name.split('_', '.').firstNotNullOfOrNull { ParadoxGameType.getSpecific(it) }
        return gameType
    }

    fun getInjectedRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        return rootFile.injectedRootInfo
    }

    fun getInjectedFileInfo(file: VirtualFile): ParadoxFileInfo? {
        return file.injectedFileInfo
    }

    fun getInjectedLocaleConfig(file: VirtualFile): CwtLocaleConfig? {
        return file.injectedLocaleConfig
    }

    fun getInjectedRootKeys(file: VirtualFile): List<String> {
        return file.injectedRootKeys.orEmpty()
    }

    fun useDefaultFileExtensions(): Boolean {
        return useDefaultFileExtensions
    }

    fun useGameTypeInference(): Boolean {
        return useGameTypeInference
    }

    fun getMarkedRootInfo(): ParadoxRootInfo? {
        return markedRootInfo
    }

    fun getMarkedFileInfo(): ParadoxFileInfo? {
        return markedFileInfo
    }

    fun getMarkedConfigPath(): String? {
        return markedConfigPath
    }

    fun getMarkedConfigDirectory(): Path? {
        return markedConfigDirectory
    }

    fun useOnlyBuiltInAndInjectedConfigFiles(): Boolean {
        return useOnlyBuiltInAndInjectedConfigFiles
    }

    fun useOnlyInjectedConfigFiles(): Boolean {
        return useOnlyInjectedConfigFiles
    }

    // endregion

    // region Manipulation Methods

    fun createRootInfo(gameType: ParadoxGameType, gameVersion: String? = null): ParadoxRootInfo.Injected {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        val rootDirectory = markedRootDirectory
        return ParadoxRootInfo.Injected(rootDirectory?.toVirtualFile(), gameType, gameVersion)
    }

    fun injectRootInfo(rootFile: VirtualFile, rootInfo: ParadoxRootInfo?): Boolean {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        rootFile.injectedRootInfo = rootInfo
        return true
    }

    fun injectFileInfo(file: VirtualFile, fileInfo: ParadoxFileInfo?): Boolean {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        file.injectedFileInfo = fileInfo
        return true
    }

    fun injectFileInfo(file: VirtualFile, rootInfo: ParadoxRootInfo, path: String, entry: String = "", group: ParadoxFileGroup? = null): Boolean {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        val filePath = ParadoxPath.resolve(path)
        val fileEntry = entry
        val fileGroup = group ?: ParadoxFileGroup.resolvePossible(path.substringAfterLast('/'))
        val fileInfo = ParadoxFileInfo.Default(filePath, fileEntry, fileGroup, rootInfo)
        return injectFileInfo(file, fileInfo)
    }

    fun injectLocaleConfig(file: VirtualFile, localeConfig: CwtLocaleConfig?): Boolean {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        file.injectedLocaleConfig = localeConfig
        return true
    }

    fun injectRootKeys(file: VirtualFile, rootKeys: List<String>): Boolean {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        file.injectedRootKeys = rootKeys.orNull()
        return true
    }

    fun useDefaultFileExtensions(value: Boolean) {
        useDefaultFileExtensions = value
    }

    fun useGameTypeInference(value: Boolean) {
        useGameTypeInference = value
    }

    fun markRootInfo(rootInfo: ParadoxRootInfo) {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        markedRootInfo = rootInfo
    }

    fun clearMarkedRootInfo() {
        markedRootInfo = null
    }

    fun markFileInfo(fileInfo: ParadoxFileInfo) {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        markedFileInfo = fileInfo
    }

    fun markFileInfo(rootInfo: ParadoxRootInfo, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        val filePath = ParadoxPath.resolve(path)
        val fileEntry = entry
        val fileGroup = group ?: ParadoxFileGroup.resolvePossible(path.substringAfterLast('/'))
        val fileInfo = ParadoxFileInfo.Default(filePath, fileEntry, fileGroup, rootInfo)
        markedFileInfo = fileInfo
    }

    fun clearMarkedFileInfo() {
        markedFileInfo = null
    }

    fun markRootDirectory(relPath: String, path: Path) {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        markedRootPath = relPath
        markedRootDirectory = path
    }

    fun clearMarkedRootDirectory() {
        markedRootPath = null
        markedRootDirectory = null
    }

    fun markConfigDirectory(relPath: String, path: Path) {
        ParadoxAnalysisLifecycleService.ensureLoaded()
        markedConfigPath = relPath
        markedConfigDirectory = path
    }

    fun clearMarkedConfigDirectory() {
        markedConfigPath = null
        markedConfigDirectory = null
    }

    fun useOnlyBuiltInAndInjectedConfigFiles(value: Boolean) {
        useOnlyBuiltInAndInjectedConfigFiles = value
    }

    fun useOnlyInjectedConfigFiles(value: Boolean) {
        useOnlyInjectedConfigFiles = value
    }

    // endregion
}
