@file:Suppress("unused", "UnstableApiUsage")

package icu.windea.pls.config.configGroup

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiFile
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.containers.CollectionFactory
import icu.windea.pls.base.io.ChronicleGitService
import icu.windea.pls.core.orNull
import icu.windea.pls.core.substringInLast
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.model.ParadoxGameType
import java.util.concurrent.ConcurrentMap
import kotlin.io.path.name

/**
 * 规则文件的服务，主要用于获取所属的规则分组，以及对应的文件路径等信息。
 */
@Service(Service.Level.APP)
class CwtConfigFileService : Disposable {
    private val repoRoot2GameType: ConcurrentMap<VirtualFile, ParadoxGameType> = CollectionFactory.createConcurrentWeakIdentityMap()

    @RequiresReadLock(generateAssertion = false)
    fun getFilePath(file: PsiFile): String? {
        val vFile = file.virtualFile ?: return null
        return getFilePath(vFile, file.project)
    }

    @RequiresReadLock(generateAssertion = false)
    fun getFilePath(file: VirtualFile, project: Project): String? {
        if (file.fileType !== CwtFileType) return null
        val configGroup = getContainingConfigGroup(file, project) ?: return null
        val project = configGroup.project
        val gameType = configGroup.gameType
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType) ?: return@f
            val directory = rootDirectory.findChild(directoryName) ?: return@f
            val relativePath = VfsUtil.getRelativePath(file, directory) ?: return@f
            return relativePath
        }
        return null
    }

    @RequiresReadLock(generateAssertion = false)
    fun isInternalFile(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        return isInternalFile(vFile, file.project)
    }

    @RequiresReadLock(generateAssertion = false)
    fun isInternalFile(file: VirtualFile, project: Project): Boolean {
        if (VirtualFileService.isLightFile(file)) return false
        val filePath = getFilePath(file, project)
        if (filePath == null) {
            // 兼容插件或者规则仓库中的 CWT 文件（此时将其视为规则文件）
            val gameType = getGameTypeFromRepoFile(file)
            if (gameType == null) return false
            return file.parent.toNioPathOrNull()?.any { it.name == "internal" } ?: false
        }
        return isInternalFile(filePath)
    }

    fun isInternalFile(filePath: String): Boolean {
        return filePath.startsWith("internal/")
    }

    @RequiresReadLock(generateAssertion = false)
    fun isRootDirectory(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        return isRootDirectory(vFile, file.project)
    }

    @RequiresReadLock(generateAssertion = false)
    fun isRootDirectory(file: VirtualFile, project: Project): Boolean {
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        return fileProviders.any { it.getRootDirectory(project) == file }
    }

    /**
     * 得到指定文件与项目的所属的规则分组。指定的文件应当是某个规则分组目录，或者其子目录或文件，例如某个规则文件。
     */
    @RequiresReadLock(generateAssertion = false)
    fun getContainingConfigGroup(file: PsiFile): CwtConfigGroup? {
        val vFile = file.virtualFile ?: return null
        return getContainingConfigGroup(vFile, file.project)
    }

    /**
     * 得到指定文件与项目的所属的规则分组。指定的文件应当是某个规则分组目录，或者其子目录或文件，例如某个规则文件。
     */
    @RequiresReadLock(generateAssertion = false)
    fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup? {
        getContainingConfigGroupFromFileProviders(file, project)?.let { return it }

        // 兼容插件或者规则仓库中的 CWT 文件（此时将其视为规则文件）
        getContainingConfigGroupForRepo(file, project)?.let { return it }

        return null
    }

    @RequiresReadLock(generateAssertion = false)
    fun getContainingConfigGroupFromFileProviders(file: VirtualFile, project: Project): CwtConfigGroup? {
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val configGroup = fileProviders.firstNotNullOfOrNull { fileProvider ->
            fileProvider.getContainingConfigGroup(file, project)
        }
        return configGroup
    }

    @RequiresReadLock(generateAssertion = false)
    fun getContainingConfigGroupForRepo(file: VirtualFile, project: Project): CwtConfigGroup? {
        val gameType = getGameTypeFromRepoFile(file)
        if (gameType == null) return null
        return CwtConfigGroupService.getInstance(project).getConfigGroup(gameType)
    }

    @RequiresReadLock(generateAssertion = false)
    fun getGameTypeFromRepoFile(file: VirtualFile): ParadoxGameType? {
        val parents = generateSequence(file.parent) { it.parent }
        val root = parents.find { it.findChild(".git") != null } ?: return null
        return getGameTypeFromRepo(file, root)
    }

    private fun getGameTypeFromRepo(file: VirtualFile, root: VirtualFile): ParadoxGameType? {
        // 使用缓存以优化性能
        return repoRoot2GameType.computeIfAbsent(root) {
            ProgressManager.checkCanceled()
            resolveGameTypeFromRepoFile(file, root)
        }
    }

    private fun resolveGameTypeFromRepoFile(file: VirtualFile, root: VirtualFile): ParadoxGameType? {
        // 直接读取 `.git/config`，避免为此启动 git 进程
        val remoteUrls = ChronicleGitService.getInstance().getRemoteUrls(root)
        val gameTypeId = remoteUrls.firstNotNullOfOrNull t@{ remoteUrl ->
            when {
                remoteUrl.contains("Paradox-Language-Support") || remoteUrl.contains("Paradox-Chronicle") -> {
                    val coreRootPath = root.path.trimEnd('/') + "/cwt/core/"
                    if (file.path.startsWith(coreRootPath)) "core" else null
                }
                else -> remoteUrl.substringInLast("cwtools-", "-config", "").orNull()
            }
        }
        if (gameTypeId.isNullOrEmpty()) return null
        return ParadoxGameType.get(gameTypeId)
    }

    override fun dispose() {
        // 避免内存泄露
        repoRoot2GameType.clear()
    }

    companion object {
        private val logger = logger<CwtConfigFileService>()

        @JvmStatic
        fun getInstance(): CwtConfigFileService = service()
    }
}
