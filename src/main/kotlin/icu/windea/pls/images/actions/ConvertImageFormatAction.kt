package icu.windea.pls.images.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.refactoring.SkipOverwriteChoice
import com.intellij.refactoring.util.CommonRefactoringUtil
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.base.notification.ChronicleNotificationGroups
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.images.ImageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlin.io.path.deleteIfExists

// com.intellij.refactoring.copy.CopyFilesOrDirectoriesHandler

/**
 * 将选中的图片转化为指定的图片格式，并保存到指定的路径。可以批量转化。
 *
 * 转化过程被拆分为读、写、UI 和 IO 四个独立阶段：
 * 1. 在 EDT 中与用户交互，得到转化选项并检查目标目录是否可写。
 * 2. 在 `readAction` 中收集转化任务，并预先询问同名目标文件的覆盖策略。
 * 3. 在后台进度中，无锁地调用 [ImageService] 把图片转码到临时文件；仅最终替换/落位和刷新 VFS 时才进入写操作。
 * 4. 汇报失败项或未捕获的非取消异常。
 */
abstract class ConvertImageFormatAction(
    val targetFormatName: String,
) : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    protected open fun isAvailableForFile(file: VirtualFile): Boolean {
        return ImageService.getInstance().isImageFileType(file.fileType)
    }

    protected open fun getNewFileName(fileName: String): String {
        return fileName.substringBeforeLast('.') + "." + targetFormatName.lowercase()
    }

    protected open fun findFiles(e: AnActionEvent): Sequence<VirtualFile> {
        return VirtualFileService.findFiles(e).filter { isAvailableForFile(it) }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val enabled = project != null && findFiles(e).any()
        e.presentation.isEnabledAndVisible = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val files = findFiles(e).mapNotNull { it.toPsiFile(project) }.toList()
        if (files.isEmpty()) return
        convertAsync(files, project)
    }

    /**
     * 最近一次由 [actionPerformed] 发起的转化任务。仅用于测试等待任务结束。
     */
    internal var lastConversionJob: Job? = null
        private set

    /**
     * 在项目级别协程作用域中异步执行转化，便于测试等待任务结束。
     */
    internal fun convertAsync(files: List<PsiFile>, project: Project): Job {
        val job = ChronicleFacade.getCoroutineScope(project).launch {
            convertSuspend(files, project)
        }
        lastConversionJob = job
        return job
    }

    /**
     * 转化选项。当 [newFileName] 为 `null` 时，表示批量化转化，每个文件按 [getNewFileName] 规则生成目标文件名。
     */
    protected data class ConversionOptions(
        val newFileName: String?,
        val targetDirectory: PsiDirectory,
    )

    /**
     * 单个图片的转化任务。所有路径和格式在收集阶段（`readAction` 中）确定，后续阶段不再访问 PSI 结构。
     */
    private data class ConversionTask(
        val sourceFile: PsiFile,
        val sourcePath: Path,
        val sourceFormat: String,
        val targetFileName: String,
        val targetPath: Path,
        val existingTarget: PsiFile?,
    )

    /**
     * 转化结果。[added] 是成功落位的文件，[failed] 是转码或落位失败的文件。
     */
    internal data class ConversionResult(
        val added: List<PsiFile>,
        val failed: List<PsiFile>,
    )

    /**
     * 与用户交互，得到转化选项。默认在 EDT 中弹出 [ConvertImageFormatDialog]。
     */
    protected open fun promptConversionOptions(files: List<PsiFile>, project: Project, defaultNewFileName: String?): ConversionOptions? {
        val dialog = ConvertImageFormatDialog(files, project, defaultNewFileName, targetFormatName)
        if (!dialog.showAndGet()) return null
        val newFileName = if (files.size == 1) dialog.newFileName else null
        val targetDirectory = dialog.targetDirectory ?: return null
        return ConversionOptions(newFileName, targetDirectory)
    }

    /**
     * 询问同名目标文件的覆盖策略。默认在 EDT 中调用 [SkipOverwriteChoice.askUser]。
     *
     * 备注：当用户直接关闭对话框时，SDK 会返回 [SkipOverwriteChoice.SKIP]，只跳过当前冲突项。
     */
    protected open suspend fun promptOverwriteChoice(
        targetDirectory: PsiDirectory,
        fileName: String,
        title: String,
        isBatch: Boolean,
    ): SkipOverwriteChoice {
        return onEdt { SkipOverwriteChoice.askUser(targetDirectory, fileName, title, isBatch) }
    }

    /**
     * 汇报失败项。默认发送 Warning 通知。
     */
    protected open suspend fun notifyFailed(project: Project, files: List<PsiFile>, failed: List<PsiFile>) {
        val content = when {
            failed.size == 1 -> ChronicleBundle.message("convertImageFormat.error.0", failed.first().virtualFile.presentableUrl, targetFormatName)
            failed.size == files.size -> ChronicleBundle.message("convertImageFormat.error.1", targetFormatName)
            else -> ChronicleBundle.message("convertImageFormat.error.2", targetFormatName)
        }
        onEdt {
            val notification = ChronicleNotificationGroups.global().createNotification(content, NotificationType.WARNING)
            notification.notify(project)
        }
    }

    /**
     * 展示未捕获异常的错误对话框。默认在 EDT 中调用 [Messages.showErrorDialog]。
     */
    protected open suspend fun showErrorDialog(project: Project, message: String?) {
        onEdt { Messages.showErrorDialog(project, message, ChronicleBundle.message("error.title")) }
    }

    /**
     * 以协程方式执行完整的转化流程。
     */
    internal suspend fun convertSuspend(files: List<PsiFile>, project: Project): ConversionResult {
        if (files.isEmpty()) return ConversionResult(emptyList(), emptyList())

        // Stage 1: EDT 交互
        val defaultNewFileName = if (files.size == 1) getNewFileName(files.first().name) else null
        val options = onEdt {
            val promptOptions = promptConversionOptions(files, project, defaultNewFileName) ?: return@onEdt null
            if (!CommonRefactoringUtil.checkReadOnlyStatus(project, setOf(promptOptions.targetDirectory), true)) return@onEdt null
            promptOptions
        } ?: return ConversionResult(emptyList(), emptyList())
        val targetDirectory = options.targetDirectory

        val title = ChronicleBundle.message("convertImageFormat.command.name", targetFormatName)

        try {
            // Stage 2: readAction 解析 + 冲突预询问
            val tasks = collectConversionTasks(files, options, targetDirectory)
            val tasksToRun = resolveOverwriteChoices(tasks, targetDirectory, title)

            // Stage 3: 后台进度转码 + 短暂写操作落位
            val added = mutableListOf<PsiFile>()
            val failed = mutableListOf<PsiFile>()
            if (tasksToRun.isNotEmpty()) {
                runConversionTasks(tasksToRun, project, added, failed)
            }

            // Stage 4: 结果汇报
            if (failed.isNotEmpty()) notifyFailed(project, files, failed)
            return ConversionResult(added.toList(), failed.toList())
        } catch (e: Exception) {
            checkCancellation(e)
            showErrorDialog(project, e.message)
            return ConversionResult(emptyList(), emptyList())
        }
    }

    private suspend fun collectConversionTasks(
        files: List<PsiFile>,
        options: ConversionOptions,
        targetDirectory: PsiDirectory,
    ): List<ConversionTask> {
        val targetDirectoryPath = targetDirectory.virtualFile.toNioPath()
        return readAction {
            files.mapNotNull { file ->
                val sourceVirtualFile = file.virtualFile ?: return@mapNotNull null
                val sourceFormat = sourceVirtualFile.extension?.orNull() ?: return@mapNotNull null
                val targetFileName = options.newFileName ?: getNewFileName(file.name)
                val targetPath = targetDirectoryPath.resolve(targetFileName)
                val existingTarget = targetDirectory.findFile(targetFileName)
                ConversionTask(
                    sourceFile = file,
                    sourcePath = sourceVirtualFile.toNioPath(),
                    sourceFormat = sourceFormat.lowercase(),
                    targetFileName = targetFileName,
                    targetPath = targetPath,
                    existingTarget = existingTarget,
                )
            }
        }
    }

    /**
     * 按顺序询问所有冲突项。`SKIP_ALL` 只跳过剩余的冲突项，无冲突项仍然会被处理。
     */
    private suspend fun resolveOverwriteChoices(
        tasks: List<ConversionTask>,
        targetDirectory: PsiDirectory,
        title: String,
    ): List<ConversionTask> {
        val isBatch = tasks.size > 1
        val result = mutableListOf<ConversionTask>()
        var defaultChoice: SkipOverwriteChoice? = null
        for (task in tasks) {
            if (task.existingTarget == null) {
                result.add(task)
                continue
            }
            val choice = defaultChoice ?: promptOverwriteChoice(targetDirectory, task.targetFileName, title, isBatch)
            when (choice) {
                SkipOverwriteChoice.OVERWRITE -> result.add(task)
                SkipOverwriteChoice.OVERWRITE_ALL -> {
                    defaultChoice = choice
                    result.add(task)
                }

                SkipOverwriteChoice.SKIP -> Unit
                SkipOverwriteChoice.SKIP_ALL -> defaultChoice = choice
            }
        }
        return result
    }

    private suspend fun runConversionTasks(
        tasks: List<ConversionTask>,
        project: Project,
        added: MutableList<PsiFile>,
        failed: MutableList<PsiFile>,
    ) {
        withBackgroundProgress(project, ChronicleBundle.message("convertImageFormat.progress.title"), cancellable = true) {
            reportProgressScope(tasks.size) { reporter ->
                for (task in tasks) {
                    reporter.itemStep(task.sourceFile.name) {
                        convertTask(task, project, added, failed)
                    }
                }
            }
        }
    }

    private suspend fun convertTask(
        task: ConversionTask,
        project: Project,
        added: MutableList<PsiFile>,
        failed: MutableList<PsiFile>,
    ) {
        val targetFormat = targetFormatName.lowercase()
        // NOTE 临时文件必须保留目标扩展名，以确保 texconv、magick 等外部工具正确识别输出格式
        val tempPath = ChronicleDataPathService.getInstance().imagesTempPath
            .resolve("${UUID.randomUUID()}.$targetFormat")
        try {
            // 无锁、非 EDT 地执行转码
            withContext(Dispatchers.IO) {
                ImageService.getInstance().convertImageFormat(task.sourcePath, tempPath, task.sourceFormat, targetFormat)
            }
            // 仅在最终替换/落位和刷新 VFS 时进入写操作
            val convertedFile = edtWriteAction {
                Files.move(tempPath, task.targetPath, StandardCopyOption.REPLACE_EXISTING)
                task.targetPath.toVirtualFile(refreshIfNeed = true)?.toPsiFile(project)
            }
            if (convertedFile != null) {
                added.add(convertedFile)
            } else {
                failed.add(task.sourceFile)
            }
        } catch (e: Exception) {
            checkCancellation(e)
            failed.add(task.sourceFile)
        } finally {
            runCatching { tempPath.deleteIfExists() }
        }
    }

    /**
     * 在 EDT 中执行交互表。已经处于 EDT 时直接执行，避免不必要的重新调度。
     */
    private suspend fun <T> onEdt(action: () -> T): T {
        val application = ApplicationManager.getApplication()
        return if (application.isDispatchThread) action() else withContext(Dispatchers.EDT) { action() }
    }

    /**
     * 转化单个图片到指定目录。保留该方法主要用于单元测试和内部复用。
     */
    internal suspend fun convertImageFormat(file: PsiFile, targetDirectory: PsiDirectory, targetFileName: String): PsiFile? {
        val sourceVirtualFile = file.virtualFile ?: return null
        val sourceFormat = sourceVirtualFile.extension?.orNull() ?: return null
        val targetFormat = targetFormatName.lowercase()
        val sourcePath = sourceVirtualFile.toNioPath()
        val targetPath = targetDirectory.virtualFile.toNioPath().resolve(targetFileName)
        // NOTE 临时文件必须保留目标扩展名，以确保 texconv、magick 等外部工具正确识别输出格式
        val tempPath = ChronicleDataPathService.getInstance().imagesTempPath
            .resolve("${UUID.randomUUID()}.$targetFormat")
        try {
            withContext(Dispatchers.IO) {
                ImageService.getInstance().convertImageFormat(sourcePath, tempPath, sourceFormat.lowercase(), targetFormat)
            }
            return edtWriteAction {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
                targetPath.toVirtualFile(refreshIfNeed = true)?.toPsiFile(file.project)
            }
        } finally {
            runCatching { tempPath.deleteIfExists() }
        }
    }
}
