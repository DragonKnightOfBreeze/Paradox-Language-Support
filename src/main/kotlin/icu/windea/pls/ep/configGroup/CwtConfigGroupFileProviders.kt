package icu.windea.pls.ep.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

fun CwtConfigGroupFileProvider.isBuiltIn(): Boolean {
    return this is BuiltInCwtConfigGroupFileProvider
}

abstract class CwtConfigGroupFileProviderBase : CwtConfigGroupFileProvider {
    override fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup? {
        val rootDirectory = getRootDirectory(project) ?: return null
        val relativePath = VfsUtil.getRelativePath(file, rootDirectory) ?: return null
        val gameTypeId = relativePath.substringBefore('/')
        if (gameTypeId == "core") {
            return PlsFacade.getConfigGroup(project, null)
        } else {
            val gameType = ParadoxGameType.resolve(gameTypeId) ?: return null
            return PlsFacade.getConfigGroup(project, gameType)
        }
    }

    override fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean {
        val gameTypeId = configGroup.gameType.id
        val rootDirectory = getRootDirectory(configGroup.project) ?: return true
        if (gameTypeId != "core") rootDirectory.findChild("core")?.let { doProcessFiles(it, consumer) }
        rootDirectory.findChild(gameTypeId)?.let { doProcessFiles(it, consumer) }
        return true
    }

    private fun doProcessFiles(configDirectory: VirtualFile, consumer: (String, VirtualFile) -> Boolean) {
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
}

/**
 * 用于提供插件内置的规则分组。
 *
 * 对应的路径：`config/{gameType}`
 *
 * * 位于插件压缩包中的插件jar包中。
 * * `{gameType}`为游戏类型ID，对于公用规则分组则为`core`。
 *
 * 注意：即使不启用插件内置的规则分组，`config/core`目录下公用的规则文件仍然启用。 TODO 1.4.2
 */
class BuiltInCwtConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    private val rootDirectory by lazy { doGetRootDirectory() }

    override fun getRootDirectory(project: Project): VirtualFile? {
        return rootDirectory
    }

    private fun doGetRootDirectory(): VirtualFile? {
        if (!PlsFacade.getConfigSettings().enableBuiltInConfigGroups) return null
        val rootPath = "/config"
        val rootUrl = rootPath.toClasspathUrl(PlsConstants.locationClass)
        val file = VfsUtil.findFileByURL(rootUrl)
        return file?.takeIf { it.isDirectory }
    }

    override fun getHintMessage() = PlsBundle.message("configGroup.hint.0")

    override fun getNotificationMessage() = PlsBundle.message("configGroup.notification.0")
}

/**
 * 用于提供来自远程仓库的规则分组。
 *
 * 对应的路径：`{localConfigDirectory}/{configRepositoryDirectorName}`
 *
 * * `{localConfigDirectory}`可以配置。
 * * `{configRepositoryDirectorName}`为本地仓库目录的名字，，对于公用规则分组则为`core`。
 *
 * 更改配置后，PLS会自动从配置的远程仓库中克隆和拉取这些规则分组。
 * 在自动或手动同步后，才允许刷新规则分组数据。
 *
 * @see PlsConfigRepositoryManager
 */
class RemoteCwtConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    private val rootDirectory by lazy { doGetRootDirectory() }

    override fun getRootDirectory(project: Project): VirtualFile? {
        return rootDirectory
    }

    override fun getDirectoryName(project: Project, gameType: ParadoxGameType?): String {
        // should be `cwtools-{gameType}-config` or `core`
        if (gameType == null) return "core"
        return PlsFacade.getConfigSettings().configRepositoryDirectorNames[gameType.id]?.orNull()
            ?: PlsConfigRepositoryManager.getDefaultConfigRepositoryDirectoryName(gameType)
    }

    private fun doGetRootDirectory(): VirtualFile? {
        if (!PlsFacade.getConfigSettings().enableRemoteConfigGroups) return null
        val directory = PlsFacade.getConfigSettings().remoteConfigDirectory
        val absoluteDirectory = directory?.normalizePath()?.orNull() ?: return null
        val path = absoluteDirectory.toPathOrNull() ?: return null
        val file = VfsUtil.findFile(path, true)
        return file?.takeIf { it.isDirectory }
    }

    override fun getHintMessage() = PlsBundle.message("configGroup.hint.1")

    override fun getNotificationMessage() = PlsBundle.message("configGroup.notification.1")
}

/**
 * 用于提供全局的本地规则分组。
 *
 * 对应的路径：`{localConfigDirectory}/{gameType}`
 * * `{localConfigDirectory}`可以配置。
 * * `{gameType}`为游戏类型ID，对于公用规则分组则为`core`。
 */
class LocalCwtConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override fun getRootDirectory(project: Project): VirtualFile? {
        if (!PlsFacade.getConfigSettings().enableLocalConfigGroups) return null
        val directory = PlsFacade.getConfigSettings().localConfigDirectory
        val absoluteDirectory = directory?.normalizePath()?.orNull() ?: return null
        val path = absoluteDirectory.toPathOrNull() ?: return null
        val file = VfsUtil.findFile(path, true)
        return file?.takeIf { it.isDirectory }
    }

    override fun getHintMessage() = PlsBundle.message("configGroup.hint.2")

    override fun getNotificationMessage() = PlsBundle.message("configGroup.notification.2")
}

/**
 * 用于提供项目的本地规则分组。
 *
 * 对应的路径：`{projectLocalConfigDirectoryName}/{gameType}`
 *
 * * `{projectLocalConfigDirectoryName}`位于项目根目录中，且可以配置。
 * * `{gameType}`为游戏类型ID，对于公用规则分组则为`core`。
 */
class ProjectCwtConfigGroupFileProvider : CwtConfigGroupFileProviderBase() {
    override fun getRootDirectory(project: Project): VirtualFile? {
        if (!PlsFacade.getConfigSettings().enableProjectLocalConfigGroups) return null
        val projectRootDirectory = project.guessProjectDir() ?: return null
        val rootPath = PlsFacade.getConfigSettings().projectLocalConfigDirectoryName?.orNull() ?: ".config"
        val file = VfsUtil.findRelativeFile(projectRootDirectory, rootPath)
        return file?.takeIf { it.isDirectory }
    }

    override fun getHintMessage() = PlsBundle.message("configGroup.hint.3")

    override fun getNotificationMessage() = PlsBundle.message("configGroup.notification.3")
}

