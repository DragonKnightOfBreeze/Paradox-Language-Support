package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupFileSource
import icu.windea.pls.config.settings.PlsConfigSettings
import icu.windea.pls.config.util.CwtConfigRepositoryManager
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.analysis.ParadoxAnalysisDataService
import icu.windea.pls.lang.tools.PlsGitService
import icu.windea.pls.model.ParadoxGameType

abstract class CwtConfigGroupFileProviderBase : CwtConfigGroupFileProvider {
    override fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup? {
        val rootDirectory = getRootDirectory(project) ?: return null
        return getContainingConfigGroupFromRootDirectory(file, project, rootDirectory)
    }

    protected fun getContainingConfigGroupFromRootDirectory(file: VirtualFile, project: Project, rootDirectory: VirtualFile): CwtConfigGroup? {
        val relativePath = VfsUtil.getRelativePath(file, rootDirectory) ?: return null
        val directoryName = relativePath.substringBefore('/')
        val gameTypeId = getGameTypeIdFromDirectoryName(project, directoryName) ?: return null
        val gameType = ParadoxGameType.get(gameTypeId, withCore = true) ?: ParadoxGameType.Core
        return PlsFacade.getConfigGroup(project, gameType)
    }

    override fun processFiles(configGroup: CwtConfigGroup, rootDirectory: VirtualFile, consumer: (String, VirtualFile) -> Boolean): Boolean {
        processFilesInRootDirectory(configGroup, rootDirectory, consumer)
        return true
    }

    protected fun processFilesInRootDirectory(configGroup: CwtConfigGroup, rootDirectory: VirtualFile, consumer: (String, VirtualFile) -> Boolean) {
        if (!isEnabled && source != CwtConfigGroupFileSource.BuiltIn) return
        if (!rootDirectory.isDirectory) return
        val gameType = configGroup.gameType
        val project = configGroup.project
        run {
            val coreDirectoryName = getDirectoryName(project, ParadoxGameType.Core) ?: return@run
            val coreDirectory = rootDirectory.findChild(coreDirectoryName) ?: return@run
            processFilesInConfigDirectory(coreDirectory, consumer)
        }
        run {
            if (!isEnabled || gameType == ParadoxGameType.Core) return@run
            val directoryName = getDirectoryName(project, gameType) ?: return@run
            val directory = rootDirectory.findChild(directoryName) ?: return@run
            processFilesInConfigDirectory(directory, consumer)
        }
    }

    protected fun processFilesInConfigDirectory(configDirectory: VirtualFile, consumer: (String, VirtualFile) -> Boolean) {
        if (!configDirectory.isDirectory) return
        VfsUtil.visitChildrenRecursively(configDirectory, object : VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.extension?.lowercase() == "cwt") {
                    val filePath = VfsUtil.getRelativePath(file, configDirectory) ?: return true
                    consumer(filePath, file)
                }
                return true
            }
        })
    }

    override fun getHintMessage(): String? {
        val messageIndex = getMessageIndex()
        if (messageIndex < 0) return null
        return PlsBundle.message("configGroup.hint", messageIndex)
    }

    override fun getNotificationMessage(configGroup: CwtConfigGroup): String? {
        val messageIndex = getMessageIndex()
        if (messageIndex < 0) return null
        val gameType = configGroup.gameType
        val isBuiltIn = source == CwtConfigGroupFileSource.BuiltIn
        val isShared = gameType == ParadoxGameType.Core
        val title = when {
            isShared -> PlsBundle.message("configGroup.title.shared", messageIndex)
            else -> PlsBundle.message("configGroup.title", messageIndex)
        }
        val notification = PlsBundle.message("configGroup.notification", title, gameType.title)
        val message = when {
            isEnabled || (isBuiltIn && isShared) -> PlsBundle.message("configGroup.notification.enabled", notification)
            else -> PlsBundle.message("configGroup.notification.disabled", notification)
        }
        return message
    }

    protected open fun getMessageIndex(): Int = -1
}

/**
 * 提供插件的内置规则分组。
 *
 * 位置：`config/{gameType}`
 *
 * - 位于插件压缩包中的插件 jar 包中。
 * - `{gameType}` 为游戏类型 ID，对于共享的规则分组则为 `core`。
 *
 * 注意：共享的内置规则分组总是会被启用。
 */
class CwtBuiltInConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    private val rootDirectory by lazy { doGetRootDirectory() }

    override val source get() = CwtConfigGroupFileSource.BuiltIn

    override val isEnabled get() = PlsConfigSettings.getInstance().state.enableBuiltInConfigGroups

    override fun getRootDirectory(project: Project): VirtualFile? {
        return rootDirectory
    }

    private fun doGetRootDirectory(): VirtualFile? {
        val rootPath = "/config"
        val rootUrl = rootPath.toClasspathUrl()
        val file = VfsUtil.findFileByURL(rootUrl)
        return file?.takeIf { it.isDirectory }
    }

    override fun getMessageIndex() = 0
}

/**
 * 提供远程规则分组。来自远程规则仓库。
 *
 * 位置：`{remoteConfigDirectory}/{directoryName}`
 *
 * - `{remoteConfigDirectory}` 可以配置。
 * - `{directoryName}` 为仓库目录的名字，对于共享的规则分组则为 `core`。
 *
 * 更改配置后，PLS 会自动从配置的远程仓库中克隆和拉取这些规则分组。
 * 在自动或手动同步后，才允许刷新规则分组数据。
 *
 * @see CwtConfigRepositoryManager
 */
class CwtRemoteConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override val source get() = CwtConfigGroupFileSource.Remote

    override val isEnabled get() = PlsConfigSettings.getInstance().state.enableRemoteConfigGroups

    override fun getRootDirectory(project: Project): VirtualFile? {
        return doGetRootDirectory()
    }

    private fun doGetRootDirectory(): VirtualFile? {
        val directory = PlsConfigSettings.getInstance().state.remoteConfigDirectory
        val absoluteDirectory = directory?.normalizePath()?.orNull() ?: return null
        val path = absoluteDirectory.toPathOrNull() ?: return null
        val file = path.toVirtualFile(refreshIfNeed = true)
        return file?.takeIf { it.isDirectory }
    }

    override fun getDirectoryName(project: Project, gameType: ParadoxGameType): String? {
        // should be `cwtools-{gameType}-config` or `core`
        if (gameType == ParadoxGameType.Core) return "core"
        val fromConfig = PlsConfigSettings.getInstance().state.configRepositoryUrls[gameType.id]?.orNull()
            ?.let { PlsGitService.getInstance().getRepositoryPathFromUrl(it) }
        return fromConfig
    }

    override fun getGameTypeIdFromDirectoryName(project: Project, directoryName: String): String? {
        if (directoryName == "core") return directoryName
        val fromDefault = CwtConfigRepositoryManager.getGameTypeIdFromDefaultDirectoryName(directoryName)
        if (fromDefault != null) return fromDefault
        val fromConfig = PlsConfigSettings.getInstance().state.configRepositoryUrls.entries
            .find { PlsGitService.getInstance().getRepositoryPathFromUrl(it.value) == directoryName }
            ?.key
        return fromConfig
    }

    override fun getMessageIndex() = 1
}

/**
 * 提供全局的本地规则分组。
 *
 * 位置：`{localConfigDirectory}/{gameType}`
 *
 * - `{localConfigDirectory}` 可以配置。
 * - `{gameType}` 为游戏类型 ID，对于共享的规则分组则为 `core`。
 */
class CwtLocalConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override val source get() = CwtConfigGroupFileSource.Local

    override val isEnabled get() = PlsConfigSettings.getInstance().state.enableLocalConfigGroups

    override fun getRootDirectory(project: Project): VirtualFile? {
        return doGetRootDirectory()
    }

    private fun doGetRootDirectory(): VirtualFile? {
        val directory = PlsConfigSettings.getInstance().state.localConfigDirectory
        val absoluteDirectory = directory?.normalizePath()?.orNull() ?: return null
        val path = absoluteDirectory.toPathOrNull() ?: return null
        val file = path.toVirtualFile(refreshIfNeed = true)
        return file?.takeIf { it.isDirectory }
    }

    override fun getMessageIndex() = 2
}

/**
 * 提供项目的本地规则分组。
 *
 * 位置：`{projectLocalConfigDirectoryName}/{gameType}`
 *
 * - `{projectLocalConfigDirectoryName}` 位于项目根目录中，且可以配置。
 * - `{gameType}` 为游戏类型 ID，对于共享的规则分组则为 `core`。
 */
class CwtProjectConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override val source get() = CwtConfigGroupFileSource.Local

    override val isEnabled get() = PlsConfigSettings.getInstance().state.enableProjectLocalConfigGroups

    override fun getRootDirectory(project: Project): VirtualFile? {
        return doGetRootDirectory(project)
    }

    private fun doGetRootDirectory(project: Project): VirtualFile? {
        val projectRootDirectory = project.guessProjectDir() ?: return null
        val rootPath = PlsConfigSettings.getInstance().state.projectLocalConfigDirectoryName?.orNull() ?: ".config"
        val file = VfsUtil.findRelativeFile(projectRootDirectory, rootPath)
        return file?.takeIf { it.isDirectory }
    }

    override fun getMessageIndex() = 3
}

/**
 * 提供注入的规则分组。可用于集成测试。
 *
 * 位置：`{injectedConfigDirectory}/{gameType}`
 *
 * - `{injectedConfigDirectory}` 需要在加载规则数据前，预先手动指定。
 * - `{gameType}` 为游戏类型 ID，对于共享的规则分组则为 `core`。
 *
 * @see ParadoxAnalysisDataService.markedConfigDirectory
 */
class CwtInjectedConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    private val dataService get() = ParadoxAnalysisDataService.getInstance()

    override val source get() = CwtConfigGroupFileSource.BuiltIn

    override val isEnabled get() = with(dataService) { markedConfigDirectory != null }

    override fun getRootDirectory(project: Project): VirtualFile? {
        return doGetRootDirectory()
    }

    private fun doGetRootDirectory(): VirtualFile? {
        val path = with(dataService) { markedConfigDirectory } ?: return null
        val file = path.toVirtualFile(refreshIfNeed = true)
        return file?.takeIf { it.isDirectory }
    }

    override fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup? {
        val fs = file.fileSystem
        if (fs.protocol == "temp") {
            // NOTE 2.1.3 一些地方（如规则符号的索引的集成测试）会用到
            if (!file.path.startsWith("/src/")) return null
            val relPath = with(dataService) { markedConfigPath } ?: return null
            val tempRootDirectory = fs.findFileByPath("/src/${relPath}") ?: return null
            return getContainingConfigGroupFromRootDirectory(file, project, tempRootDirectory)
        }
        return super.getContainingConfigGroup(file, project)
    }
}
