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
import icu.windea.pls.core.util.io.clean
import icu.windea.pls.core.util.io.deleteQuietly
import icu.windea.pls.core.util.io.moveToAtomically
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.images.ImageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.util.UUID

// com.intellij.refactoring.copy.CopyFilesOrDirectoriesHandler

/**
 * 将选中的图片转换为指定的图片格式，并保存到指定的路径。可以批量转换。
 *
 * 目标格式由 [ConvertImageFormatDialog] 中的下拉框指定，支持 [supportedTargetFormatNames] 中列出的常见图片格式。
 * 当目标路径与源路径相同时，该任务会被跳过，以避免无意义的自覆盖。
 *
 * 转换过程被拆分为读、写、UI 和 IO 四个独立阶段：
 * 1. 在 EDT 中与用户交互，得到转换选项并检查目标目录是否可写。
 * 2. 在 `readAction` 中收集转换任务，并预先询问同名目标文件的覆盖策略。
 * 3. 在后台进度中，无锁地调用 [ImageService] 把图片转码到临时文件；仅最终落位和刷新 VFS 时才进入写操作。
 * 4. 汇报失败项或未捕获的非取消异常。
 */
open class ConvertImageFormatAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    protected open fun isAvailableForFile(file: VirtualFile): Boolean {
        return ImageService.getInstance().isImageFileType(file.fileType)
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
     * 最近一次由 [actionPerformed] 发起的转换任务。仅用于测试等待任务结束。
     */
    internal var lastConversionJob: Job? = null
        private set

    /**
     * 在项目级别协程作用域中异步执行转换，便于测试等待任务结束。
     */
    internal fun convertAsync(files: List<PsiFile>, project: Project): Job {
        val job = ChronicleFacade.getCoroutineScope(project).launch {
            convertSuspend(files, project)
        }
        lastConversionJob = job
        return job
    }

    /**
     * 转换选项。当 [newFileName] 为 `null` 时，表示批量化转换，每个文件按 [getNewFileName] 规则生成目标文件名。
     * [targetFormatName] 是用户在当前转换中指定的目标图片格式。
     */
    protected data class ConversionOptions(
        val newFileName: String?,
        val targetDirectory: PsiDirectory,
        val targetFormatName: String,
    )

    /**
     * 单个图片的转换任务。所有路径和格式在收集阶段（`readAction` 中）确定，后续阶段不再访问 PSI 结构。
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
     * 转换结果。[added] 是成功落位的文件，[failed] 是转码或落位失败的文件。
     */
    internal data class ConversionResult(
        val added: List<PsiFile>,
        val failed: List<PsiFile>,
    )

    /**
     * 与用户交互，得到转换选项。默认在 EDT 中弹出 [ConvertImageFormatDialog]。
     */
    protected open fun promptConversionOptions(files: List<PsiFile>, project: Project): ConversionOptions? {
        val dialog = ConvertImageFormatDialog(files, project)
        if (!dialog.showAndGet()) return null
        val newFileName = if (files.size == 1) dialog.newFileName else null
        val targetDirectory = dialog.targetDirectory ?: return null
        return ConversionOptions(newFileName, targetDirectory, dialog.targetFormatName)
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
    protected open suspend fun notifyFailed(project: Project, files: List<PsiFile>, failed: List<PsiFile>, targetFormatName: String) {
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
     * 以协程方式执行完整的转换流程。
     */
    internal suspend fun convertSuspend(files: List<PsiFile>, project: Project): ConversionResult {
        if (files.isEmpty()) return ConversionResult(emptyList(), emptyList())

        // Stage 1: EDT 交互
        val options = onEdt {
            val promptOptions = promptConversionOptions(files, project) ?: return@onEdt null
            if (!CommonRefactoringUtil.checkReadOnlyStatus(project, setOf(promptOptions.targetDirectory), true)) return@onEdt null
            promptOptions
        } ?: return ConversionResult(emptyList(), emptyList())
        val targetDirectory = options.targetDirectory
        val targetFormatName = options.targetFormatName

        val title = ChronicleBundle.message("convertImageFormat.command.name", targetFormatName)

        try {
            // Stage 2: readAction 解析 + 冲突预询问
            val tasks = collectConversionTasks(files, options, targetDirectory)
            val tasksToRun = resolveOverwriteChoices(tasks, targetDirectory, title)

            // Stage 3: 后台进度转码 + 短暂写操作落位
            val added = mutableListOf<PsiFile>()
            val failed = mutableListOf<PsiFile>()
            if (tasksToRun.isNotEmpty()) {
                runConversionTasks(tasksToRun, targetFormatName, project, added, failed)
            }

            // Stage 4: 结果汇报
            if (failed.isNotEmpty()) notifyFailed(project, files, failed, targetFormatName)
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
                val sourceFormat = sourceVirtualFile.extension.orNull() ?: return@mapNotNull null
                val targetFileName = options.newFileName ?: getNewFileName(file.name, options.targetFormatName)
                val sourcePath = sourceVirtualFile.toNioPath()
                val targetPath = targetDirectoryPath.resolve(targetFileName)
                // 同路径时跳过，避免自覆盖或无意义的同路径拷贝
                if (sourcePath.clean() == targetPath.clean()) return@mapNotNull null
                val existingTarget = targetDirectory.findFile(targetFileName)
                ConversionTask(
                    sourceFile = file,
                    sourcePath = sourcePath,
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
        targetFormatName: String,
        project: Project,
        added: MutableList<PsiFile>,
        failed: MutableList<PsiFile>,
    ) {
        withBackgroundProgress(project, ChronicleBundle.message("convertImageFormat.progress.title"), cancellable = true) {
            reportProgressScope(tasks.size) { reporter ->
                for (task in tasks) {
                    reporter.itemStep(task.sourceFile.name) {
                        convertTask(task, targetFormatName, project, added, failed)
                    }
                }
            }
        }
    }

    private suspend fun convertTask(
        task: ConversionTask,
        targetFormatName: String,
        project: Project,
        added: MutableList<PsiFile>,
        failed: MutableList<PsiFile>,
    ) {
        val targetFormat = targetFormatName.lowercase()
        // NOTE 临时文件必须保留目标扩展名，以确保 texconv、magick 等外部工具正确识别输出格式
        val tempPath = ChronicleDataPathService.getInstance().imagesTempPath
            .resolve("${UUID.randomUUID()}.$targetFormat")
        try {
            // 无锁、非 EDT 地执行转码和落位
            withContext(Dispatchers.IO) {
                ImageService.getInstance().convertImageFormat(task.sourcePath, tempPath, task.sourceFormat, targetFormat)
                tempPath.moveToAtomically(task.targetPath, overwrite = true)
            }
            // 仅在刷新 VFS 并转换为 PSI 时进入写操作
            val convertedFile = edtWriteAction {
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
            tempPath.deleteQuietly()
        }
    }

    /**
     * 在 EDT 中执行交互表。已经处于 EDT 时直接执行，避免不必要的重新调度。
     */
    private suspend fun <T> onEdt(action: () -> T): T {
        val application = ApplicationManager.getApplication()
        return if (application.isDispatchThread) action() else withContext(Dispatchers.EDT) { action() }
    }

}

/**
 * 支持的目标图片格式。
 */
internal val supportedTargetFormatNames = listOf("PNG", "JPG", "GIF", "DDS", "TGA")

/**
 * 根据输入的源文件名和目标格式生成默认的目标文件名。
 *
 * 保留主文件名（包括其中的其他点号），并将扩展名替换为目标格式的小写形式；如果不存在扩展名，则直接追加。
 */
internal fun getNewFileName(fileName: String, targetFormatName: String): String {
    val baseName = fileName.substringBeforeLast('.')
    return "$baseName.${targetFormatName.lowercase()}"
}
