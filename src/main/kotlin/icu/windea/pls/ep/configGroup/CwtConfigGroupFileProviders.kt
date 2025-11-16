package icu.windea.pls.ep.configGroup

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigRepositoryManager
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.lang.tools.PlsGitService
import icu.windea.pls.model.ParadoxGameType

abstract class CwtConfigGroupFileProviderBase : CwtConfigGroupFileProvider {
    override fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup? {
        val rootDirectory = getRootDirectory(project) ?: return null
        val relativePath = VfsUtil.getRelativePath(file, rootDirectory) ?: return null
        val directoryName = relativePath.substringBefore('/')
        val gameTypeId = getGameTypeIdFromDirectoryName(project, directoryName) ?: return null
        val gameType = ParadoxGameType.get(gameTypeId, withCore = true) ?: ParadoxGameType.Core
        return PlsFacade.getConfigGroup(project, gameType)
    }

    override fun processFiles(configGroup: CwtConfigGroup, rootDirectory: VirtualFile, consumer: (String, VirtualFile) -> Boolean): Boolean {
        // 已启用，或者是内置且共享的规则分组
        if (!isEnabled && type != CwtConfigGroupFileProvider.Type.BuiltIn) return true
        doProcessInRootDirectory(configGroup, rootDirectory, consumer)
        return true
    }

    private fun doProcessInRootDirectory(configGroup: CwtConfigGroup, rootDirectory: VirtualFile, consumer: (String, VirtualFile) -> Boolean) {
        if (!rootDirectory.isDirectory) return
        val configDirectories = mutableSetOf<VirtualFile>()
        val gameType = configGroup.gameType
        val project = configGroup.project
        val coreDirectoryName = getDirectoryName(project, ParadoxGameType.Core)
        rootDirectory.findChild(coreDirectoryName)?.let { configDirectories.add(it) }
        if (isEnabled && gameType != ParadoxGameType.Core) {
            val directoryName = getDirectoryName(project, gameType)
            rootDirectory.findChild(directoryName)?.let { configDirectories.add(it) }
        }
        configDirectories.forEach { configDirectory -> doProcessInConfigDirectory(configDirectory, consumer) }
    }

    private fun doProcessInConfigDirectory(configDirectory: VirtualFile, consumer: (String, VirtualFile) -> Boolean) {
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

    protected open fun getMessageIndex(): Int = -1

    override fun getHintMessage(): String? {
        val messageIndex = getMessageIndex()
        if (messageIndex < 0) return null
        return PlsBundle.message("configGroup.hint", messageIndex)
    }

    override fun getNotificationMessage(configGroup: CwtConfigGroup): String? {
        val messageIndex = getMessageIndex()
        if (messageIndex < 0) return null
        val gameType = configGroup.gameType
        val isBuiltIn = type == CwtConfigGroupFileProvider.Type.BuiltIn
        val isShared = gameType != ParadoxGameType.Core
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
}

/**
 * 用于提供插件内置的规则分组。
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

    override val type get() = CwtConfigGroupFileProvider.Type.BuiltIn

    override val isEnabled get() = PlsFacade.getConfigSettings().state.enableBuiltInConfigGroups

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
 * 用于提供来自远程仓库的规则分组。
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
    override val type get() = CwtConfigGroupFileProvider.Type.Remote

    override val isEnabled get() = PlsFacade.getConfigSettings().state.enableRemoteConfigGroups

    override fun getRootDirectory(project: Project): VirtualFile? {
        return doGetRootDirectory()
    }

    private fun doGetRootDirectory(): VirtualFile? {
        val directory = PlsFacade.getConfigSettings().state.remoteConfigDirectory
        val absoluteDirectory = directory?.normalizePath()?.orNull() ?: return null
        val path = absoluteDirectory.toPathOrNull() ?: return null
        val file = VfsUtil.findFile(path, true)
        return file?.takeIf { it.isDirectory }
    }

    override fun getDirectoryName(project: Project, gameType: ParadoxGameType): String {
        // should be `cwtools-{gameType}-config` or `core`
        if (gameType == ParadoxGameType.Core) return "core"
        val fromConfig = PlsFacade.getConfigSettings().state.configRepositoryUrls[gameType.id]?.orNull()
            ?.let { PlsGitService.getRepositoryPathFromUrl(it) }
        if (fromConfig != null) return fromConfig
        val fromDefault = CwtConfigRepositoryManager.getDefaultDirectoryName(gameType)
        return fromDefault
    }

    override fun getGameTypeIdFromDirectoryName(project: Project, directoryName: String): String? {
        if (directoryName == "core") return directoryName
        val fromDefault = CwtConfigRepositoryManager.getGameTypeIdFromDefaultDirectoryName(directoryName)
        if (fromDefault != null) return fromDefault
        val fromConfig = PlsFacade.getConfigSettings().state.configRepositoryUrls.entries
            .find { PlsGitService.getRepositoryPathFromUrl(it.value) == directoryName }
            ?.key
        return fromConfig
    }

    override fun getMessageIndex() = 1
}

/**
 * 用于提供全局的本地规则分组。
 *
 * 位置：`{localConfigDirectory}/{gameType}`
 *
 * - `{localConfigDirectory}` 可以配置。
 * - `{gameType}` 为游戏类型 ID，对于共享的规则分组则为 `core`。
 */
class CwtLocalConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override val type get() = CwtConfigGroupFileProvider.Type.Local

    override val isEnabled get() = PlsFacade.getConfigSettings().state.enableLocalConfigGroups

    override fun getRootDirectory(project: Project): VirtualFile? {
        return doGetRootDirectory()
    }

    private fun doGetRootDirectory(): VirtualFile? {
        val directory = PlsFacade.getConfigSettings().state.localConfigDirectory
        val absoluteDirectory = directory?.normalizePath()?.orNull() ?: return null
        val path = absoluteDirectory.toPathOrNull() ?: return null
        val file = VfsUtil.findFile(path, true)
        return file?.takeIf { it.isDirectory }
    }

    override fun getMessageIndex() = 2
}

/**
 * 用于提供项目的本地规则分组。
 *
 * 位置：`{projectLocalConfigDirectoryName}/{gameType}`
 *
 * - `{projectLocalConfigDirectoryName}` 位于项目根目录中，且可以配置。
 * - `{gameType}` 为游戏类型 ID，对于共享的规则分组则为 `core`。
 */
class CwtProjectConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override val type get() = CwtConfigGroupFileProvider.Type.Local

    override val isEnabled get() = PlsFacade.getConfigSettings().state.enableProjectLocalConfigGroups

    override fun getRootDirectory(project: Project): VirtualFile? {
        return doGetRootDirectory(project)
    }

    private fun doGetRootDirectory(project: Project): VirtualFile? {
        val projectRootDirectory = project.guessProjectDir() ?: return null
        val rootPath = PlsFacade.getConfigSettings().state.projectLocalConfigDirectoryName?.orNull() ?: ".config"
        val file = VfsUtil.findRelativeFile(projectRootDirectory, rootPath)
        return file?.takeIf { it.isDirectory }
    }

    override fun getMessageIndex() = 3
}

/**
 * 可在单元测试中使用的规则分组。
 *
 * 位置：`config/{gameType}`
 *
 * - 位于特定的测试数据目录中，一般是 `src/test/testData`。
 * - `{gameType}` 为游戏类型 ID，对于共享的规则分组则为 `core`。
 */
class CwtTestConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    private val rootDirectory by lazy { doGetRootDirectory() }

    override val type get() = CwtConfigGroupFileProvider.Type.BuiltIn

    override val isEnabled get() = PlsFacade.isUnitTestMode()

    override fun getRootDirectory(project: Project): VirtualFile? {
        return rootDirectory
    }

    private fun doGetRootDirectory(): VirtualFile? {
        val path = "src/test/testData/config".toPathOrNull() ?: return null
        val file = VfsUtil.findFile(path, true)
        return file?.takeIf { it.isDirectory }
    }
}
