package icu.windea.pls.config.util

import com.intellij.ide.*
import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.progress.*
import com.intellij.ui.layout.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*
import kotlin.Result

@Suppress("UnstableApiUsage")
object PlsConfigRepositoryManager {
    fun getDefaultUrl(gameType: ParadoxGameType): String {
        return "https://github.com/DragonKnightOfBreeze/cwtools-${gameType.id}-config"
    }

    fun getDefaultDirectoryName(gameType: ParadoxGameType): String {
        return "cwtools-${gameType.id}-config"
    }

    fun getGameTypeIdFromDefaultDirectoryName(directoryName: String): String? {
        if (directoryName == "core") return directoryName
        return directoryName.removeSurroundingOrNull("cwtools-", "-config")?.takeIf { ParadoxGameType.canResolve(it) }
    }

    fun validateUrl(builder: ValidationInfoBuilder, gameType: ParadoxGameType, url: String): ValidationInfo? {
        //规则仓库URL应当包含对应的游戏类型ID

        if (url.isEmpty()) return builder.warning("The repository url is not configured")
        if (!url.contains(gameType.id)) return builder.error("The repository url must contain game type id '${gameType.id}'")
        return null
    }

    @Synchronized
    fun validateUrlsByGit(urls: List<String>): Boolean {
        //NOTE 这里的需要执行git命令的校验，需要并发进行

        val results = runWithModalProgressBlocking(ModalTaskOwner.guess(), "Validation Config Repository Urls") r@{
            reportRawProgress { reporter ->
                reporter.text("Validate config repository urls...")
                reporter.details("Execute 'git ls-remote' commands asynchronously...")

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
        //如果存在报错，显示错误弹窗并直接返回
        if (results.any { it.isFailure }) {
            val errorMessage = results
                .filter { it.isFailure }
                .mapNotNull { it.exceptionOrNull()?.message?.orNull() }
                .distinct()
                .joinToString("<br>") { it.replace("\n", "<br>") }
            Messages.showErrorDialog(errorMessage, "Validation Result")
            return false
        }

        //显示成功的消息弹窗
        Messages.showInfoMessage("Success. All config repository urls are connectable.", "Validation Result")
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
        //NOTE 这里需要先获取一个project，但是应当如何获取呢……
        val openProjects = ProjectManager.getInstance().openProjects
        val project = openProjects.firstOrNull() ?: return

        //NOTE 这里需要先验证是否真的需要刷新
        val valid = isValidToSync()
        if (!valid) return

        val settings = PlsFacade.getConfigSettings()
        val urlMap = settings.configRepositoryUrls.orNull() ?: return
        val parentDirectory = settings.remoteConfigDirectory?.orNull() ?: return

        //创建父目录，如果存在报错，发送通知并直接返回
        val r = runCatchingCancelable { parentDirectory.toPath().createDirectories() }
        if (r.isFailure) {
            val warningMessage = "Failed. Cannot create remote config directory."
            createNotification("Synchronize Result", warningMessage, NotificationType.ERROR).notify(project)
            return
        }

        PlsFacade.getCoroutineScope().launch c@{
            val results = withBackgroundProgress(project, "Synchronize from Config Repository Urls", cancellable = true) p@{
                reportRawProgress { reporter ->
                    reporter.text("Synchronize from config repository urls...")
                    reporter.details("Execute 'git clone' or 'git pull' commands asynchronously...")

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

            val action = NotificationAction.createSimple("Open remote config directory") {
                BrowserUtil.open(parentDirectory)
            }

            //如果存在报错，发送通知并直接返回
            if (results.any { it.isFailure }) {
                val warningMessage = "Failed. Please check it manually."
                val notification = createNotification("Synchronize Result", warningMessage, NotificationType.WARNING)
                    .addAction(action)
                openProjects.forEach { notification.notify(it) }
                return@c
            }

            val updated = results.any { result -> result.getOrNull().let { !PlsGitManager.isUpdateToDate(it) } }

            //发送成功的通知
            val successMessage = if (updated) "Success. All config repositories are updated if necessary."
            else "Success. All config repositories are up to date."
            val notification = createNotification("Synchronize Result", successMessage, NotificationType.INFORMATION)
                .addAction(action)
            openProjects.forEach { notification.notify(it) }

            //如果需要刷新规则分组数据，则通知规则目录发生变更
            if (updated) {
                val messageBus = ApplicationManager.getApplication().messageBus
                messageBus.syncPublisher(ParadoxConfigDirectoriesListener.TOPIC).onChange()
            }

            //TODO 1.4.2 适用刷新规则分组数据后，之后的功能全部正常
        }
    }

    private fun trySyncFromRemote(url: String, parentDirectory: String): Result<String> {
        if (url.isEmpty()) return Result.success("")
        return runCatchingCancelable { PlsGitManager.syncFromRemote(url, parentDirectory) }
    }
}
