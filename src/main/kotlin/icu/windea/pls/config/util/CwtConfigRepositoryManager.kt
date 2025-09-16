package icu.windea.pls.config.util

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportRawProgress
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.application
import com.intellij.util.io.createDirectories
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPath
import icu.windea.pls.lang.listeners.ParadoxConfigDirectoriesListener
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.PlsGitManager
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UnstableApiUsage")
object CwtConfigRepositoryManager {
    fun getDefaultUrl(gameType: ParadoxGameType): String {
        return "https://github.com/DragonKnightOfBreeze/cwtools-${gameType.id}-config"
    }

    fun getDefaultDirectoryName(gameType: ParadoxGameType): String {
        return "cwtools-${gameType.id}-config"
    }

    fun getGameTypeIdFromDefaultDirectoryName(directoryName: String): String? {
        return directoryName.removeSurroundingOrNull("cwtools-", "-config")
    }

    fun validateUrl(builder: ValidationInfoBuilder, gameType: ParadoxGameType, url: String): ValidationInfo? {
        // 规则仓库URL应当包含对应的游戏类型ID

        if (url.isEmpty()) return builder.warning(PlsBundle.message("config.repo.validation.urlNotConfigured"))
        if (!url.contains(gameType.id)) return builder.error(PlsBundle.message("config.repo.validation.urlMustContainGameTypeId", gameType.id))
        return null
    }

    @Synchronized
    fun validateUrlsByGit(urls: List<String>): Boolean {
        // NOTE 这里需要执行git命令的校验，需要并发进行

        val results = runWithModalProgressBlocking(ModalTaskOwner.guess(), PlsBundle.message("config.repo.validation.progress.title")) r@{
            reportRawProgress { reporter ->
                reporter.text(PlsBundle.message("config.repo.validation.progress.text"))
                reporter.details(PlsBundle.message("config.repo.validation.progress.details"))

                val resultFutures = urls.map {
                    async {
                        withContext(Dispatchers.IO) {
                            tryCheckRemote(it)
                        }
                    }
                }
                resultFutures.awaitAll()
            }
        }
        // 如果存在报错，显示错误弹窗并直接返回
        if (results.any { it.isFailure }) {
            val errorMessage = results
                .filter { it.isFailure }
                .mapNotNull { it.exceptionOrNull()?.message?.orNull() }
                .distinct()
                .joinToString("<br>") { it.replace("\n", "<br>") }
            Messages.showErrorDialog(errorMessage, PlsBundle.message("config.repo.validation.result.title"))
            return false
        }

        // 显示成功的消息弹窗
        Messages.showInfoMessage(PlsBundle.message("config.repo.validation.result.0"), PlsBundle.message("config.repo.validation.result.title"))
        return true
    }

    private fun tryCheckRemote(string: String): Result<String> {
        if (string.isEmpty()) return Result.success("")
        return runCatchingCancelable { PlsGitManager.checkRemote(string) }
    }

    fun isValidToSync(): Boolean {
        val settings = PlsFacade.getConfigSettings()
        val valid = settings.enableRemoteConfigGroups
            && settings.remoteConfigDirectory.isNotNullOrEmpty()
            && settings.configRepositoryUrls.values.any { it.isNotNullOrEmpty() }
        return valid
    }

    @Synchronized
    fun syncFromUrls() {
        // NOTE 这里需要先获取一个project，但是应当如何获取呢……
        val openProjects = ProjectManager.getInstance().openProjects
        val project = openProjects.firstOrNull() ?: return

        // NOTE 这里需要先验证是否真的需要刷新
        if (!isValidToSync()) return

        val settings = PlsFacade.getConfigSettings()
        val urlMap = settings.configRepositoryUrls.orNull() ?: return
        val parentDirectory = settings.remoteConfigDirectory?.orNull() ?: return

        // 创建父目录，如果存在报错，发送通知并直接返回
        val r = runCatchingCancelable { parentDirectory.toPath().createDirectories() }
        if (r.isFailure) {
            val warningMessage = PlsBundle.message("config.repo.sync.createDirectoryFailed")
            PlsCoreManager.createNotification(NotificationType.ERROR, PlsBundle.message("config.repo.sync.result.title"), warningMessage).notify(project)
            return
        }

        PlsFacade.getCoroutineScope().launch c@{
            val results = withBackgroundProgress(project, PlsBundle.message("config.repo.sync.progress.title"), cancellable = true) p@{
                reportRawProgress { reporter ->
                    reporter.text(PlsBundle.message("config.repo.sync.progress.text"))
                    reporter.details(PlsBundle.message("config.repo.sync.progress.details"))

                    val resultFutures = mutableListOf<Deferred<Result<String>>>()
                    urlMap.forEach { (_, url) ->
                        resultFutures += async {
                            withContext(Dispatchers.IO) {
                                trySyncFromRemote(url, parentDirectory)
                            }
                        }
                    }
                    resultFutures.awaitAll()
                }
            }

            val action = NotificationAction.createSimple(PlsBundle.message("config.repo.sync.action.openRepoDir")) {
                BrowserUtil.open(parentDirectory)
            }

            // 如果存在报错，发送通知并直接返回
            if (results.any { it.isFailure }) {
                val warningMessage = PlsBundle.message("config.repo.sync.result.2")
                val notification = PlsCoreManager.createNotification(NotificationType.WARNING, PlsBundle.message("config.repo.sync.result.title"), warningMessage)
                    .addAction(action)
                openProjects.forEach { notification.notify(it) }
                return@c
            }

            val updated = results.any { result -> result.getOrNull().let { !PlsGitManager.isUpdateToDate(it) } }

            // 发送成功的通知
            val successMessage = if (updated) PlsBundle.message("config.repo.sync.result.0")
            else PlsBundle.message("config.repo.sync.result.1")
            val notification = PlsCoreManager.createNotification(NotificationType.INFORMATION, PlsBundle.message("config.repo.sync.result.title"), successMessage)
                .addAction(action)
            openProjects.forEach { notification.notify(it) }

            // 如果需要刷新规则分组数据，则通知规则目录发生变更
            if (updated) {
                application.messageBus.syncPublisher(ParadoxConfigDirectoriesListener.TOPIC).onChange()
            }
        }
    }

    private fun trySyncFromRemote(url: String, parentDirectory: String): Result<String> {
        if (url.isEmpty()) return Result.success("")
        return runCatchingCancelable { PlsGitManager.syncFromRemote(url, parentDirectory) }
    }
}
