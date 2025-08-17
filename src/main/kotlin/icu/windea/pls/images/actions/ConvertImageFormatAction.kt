package icu.windea.pls.images.actions

import com.intellij.codeInspection.*
import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ex.*
import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.registry.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.impl.file.*
import com.intellij.refactoring.*
import com.intellij.refactoring.util.*
import com.intellij.util.*
import com.intellij.util.containers.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.images.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import java.io.*
import java.util.concurrent.atomic.*
import java.util.function.Consumer

//com.intellij.refactoring.copy.CopyFilesOrDirectoriesHandler

/**
 * 将选中的图片转化为指定的图片格式，并保存到指定的路径。可以批量转化。
 */
@Suppress("UnstableApiUsage")
abstract class ConvertImageFormatAction(
    val targetFormatName: String,
) : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    protected open fun isAvailableForFile(file: VirtualFile): Boolean {
        return ImageManager.isImageFileType(file.fileType)
    }

    protected open fun getNewFileName(fileName: String): String {
        return fileName.substringBeforeLast('.') + "." + targetFormatName.lowercase()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val enabled = project != null && PlsVfsManager.findFiles(e).any { isAvailableForFile(it) }
        e.presentation.isEnabledAndVisible = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val files = PlsVfsManager.findFiles(e).filter { isAvailableForFile(it) }.mapNotNull { it.toPsiFile(project) }
        if (files.isEmpty()) return
        convert(files, project)
    }

    private fun convert(files: List<PsiFile>, project: Project) {
        val defaultNewFileName = if (files.size == 1) getNewFileName(files.first().name) else null

        val newFileName: String?
        val targetDirectory: PsiDirectory?
        val dialog = ConvertImageFormatDialog(files, project, defaultNewFileName, targetFormatName)
        if (dialog.showAndGet()) {
            newFileName = if (files.size == 1) dialog.newFileName else null
            targetDirectory = dialog.targetDirectory
        } else {
            return
        }
        if (targetDirectory != null) {
            val command = { doConvert(files, newFileName, targetDirectory) }
            val title = PlsBundle.message("convertImageFormat.command.name", targetFormatName)
            CommandProcessor.getInstance().executeCommand(project, command, title, null)
        }
    }

    private fun doConvert(files: List<PsiFile>, newFileName: String?, targetDirectory: PsiDirectory) {
        val project = targetDirectory.project
        if (!CommonRefactoringUtil.checkReadOnlyStatus(project, setOf(targetDirectory), true)) {
            return
        }

        try {
            val title = PlsBundle.message("convertImageFormat.command.name", targetFormatName)
            val choice = if (files.size > 1 || files[0].isDirectory) intArrayOf(-1) else null
            val added = mutableListOf<PsiFile>()
            val failed = mutableListOf<PsiFile>()
            save(files, newFileName, targetDirectory, choice, title, added, failed)

            if (failed.isNotEmpty()) {
                val content = when (failed.size) {
                    1 -> PlsBundle.message("convertImageFormat.error.0", files.first().virtualFile.presentableUrl, targetFormatName)
                    files.size -> PlsBundle.message("convertImageFormat.error.1", targetFormatName)
                    else -> PlsBundle.message("convertImageFormat.error.2", targetFormatName)
                }
                createNotification(content, NotificationType.WARNING).notify(targetDirectory.project)
            }
        } catch (e: Exception) {
            Messages.showErrorDialog(project, e.message, PlsBundle.message("error.title"))
        }

    }

    private fun save(
        files: List<PsiFile>,
        newFileName: String?,
        targetDirectory: PsiDirectory,
        choice: IntArray?,
        title: String,
        added: MutableList<PsiFile>,
        failed: MutableList<PsiFile>
    ) {
        val existingFiles = MultiMap<PsiDirectory, PsiFile>()
        val app = ApplicationManagerEx.getApplicationEx()
        if (Registry.`is`("run.refactorings.under.progress")) {
            val thrown = AtomicReference<Throwable>()
            val action = Consumer { pi: ProgressIndicator? ->
                try {
                    for (file in files) {
                        doSave(file, newFileName, targetDirectory, added, failed, existingFiles, pi)
                    }
                } catch (e: Exception) {
                    thrown.set(e)
                }
            }
            CommandProcessor.getInstance().executeCommand(targetDirectory.project, { app.runWriteActionWithCancellableProgressInDispatchThread(title, targetDirectory.project, null, action) }, title, null)
            val throwable = thrown.get()
            if (throwable is ProcessCanceledException) {
                //process was canceled, don't proceed with existing files
                return
            }
            rethrow(throwable)
        } else {
            WriteCommandAction.writeCommandAction(targetDirectory.project).withName(title).run<IOException> {
                for (file in files) {
                    doSave(file, newFileName, targetDirectory, added, failed, existingFiles, null)
                }
            }
        }

        handleExistingFiles(newFileName, targetDirectory, choice, title, existingFiles, added, failed)
    }

    private fun doSave(
        file: PsiFile,
        newFileName: String?,
        targetDirectory: PsiDirectory,
        added: MutableList<PsiFile>,
        failed: MutableList<PsiFile>,
        existingFiles: MultiMap<PsiDirectory, PsiFile>,
        pi: ProgressIndicator?
    ) {
        if (pi != null) {
            pi.text2 = InspectionsBundle.message("processing.progress.text", file.name)
        }

        val fileName = newFileName ?: getNewFileName(file.name)
        val existing = targetDirectory.findFile(fileName)
        if (existing != null && existing != file) {
            existingFiles.putValue(targetDirectory, file)
            return
        }

        doConvert(file, targetDirectory, fileName, added, failed)
    }

    private fun rethrow(throwable: Throwable?) {
        if (throwable is IOException) {
            throw throwable
        } else if (throwable is IncorrectOperationException) {
            throw throwable
        } else if (throwable != null) {
            throw IncorrectOperationException(throwable)
        }
    }

    private fun handleExistingFiles(
        newFileName: String?,
        targetDirectory: PsiDirectory,
        choice: IntArray?,
        title: String,
        existingFiles: MultiMap<PsiDirectory, PsiFile>,
        added: MutableList<PsiFile>,
        failed: MutableList<PsiFile>
    ) {
        var defaultChoice = if (choice != null && choice[0] > -1) SkipOverwriteChoice.entries[choice[0]] else null
        try {
            defaultChoice = handleExistingFiles(defaultChoice, choice, newFileName, targetDirectory, title, existingFiles, added, failed, null)
        } finally {
            if (choice != null && defaultChoice != null) {
                choice[0] = defaultChoice.ordinal % 2
            }
        }
    }

    private fun handleExistingFiles(
        defaultChoice: SkipOverwriteChoice?,
        choice: IntArray?,
        newFileName: String?,
        targetDirectory: PsiDirectory,
        title: String,
        existingFiles: MultiMap<PsiDirectory, PsiFile>,
        added: MutableList<PsiFile>,
        failed: MutableList<PsiFile>,
        progressIndicator: ProgressIndicator?
    ): SkipOverwriteChoice? {
        for (tDirectory in existingFiles.keySet()) {
            val replacementFiles = existingFiles[tDirectory]
            val iterator = replacementFiles.iterator()
            while (iterator.hasNext()) {
                val replacement = iterator.next()
                val fileName = newFileName ?: getNewFileName(replacement.name)
                val existing = tDirectory.findFile(fileName) ?: continue
                var userChoice = defaultChoice
                val project = targetDirectory.project
                if (userChoice == null) {
                    userChoice = SkipOverwriteChoice.askUser(targetDirectory, fileName, title, choice != null)
                    if (userChoice == SkipOverwriteChoice.SKIP_ALL) {
                        return userChoice
                    } else if (userChoice == SkipOverwriteChoice.OVERWRITE_ALL) {
                        val r = Consumer { pi: ProgressIndicator? ->
                            handleExistingFiles(SkipOverwriteChoice.OVERWRITE_ALL, choice, newFileName, targetDirectory, title, existingFiles, added, failed, pi)
                        }
                        if (Registry.`is`("run.refactorings.under.progress")) {
                            val action = Runnable { ApplicationManagerEx.getApplicationEx().runWriteActionWithCancellableProgressInDispatchThread(title, project, null, r) }
                            CommandProcessor.getInstance().executeCommand(project, action, title, null)
                        } else {
                            r.accept(null)
                        }
                        return SkipOverwriteChoice.OVERWRITE_ALL
                    }
                }
                iterator.remove()
                val doCopy = ThrowableRunnable<RuntimeException> {
                    if (progressIndicator != null) {
                        progressIndicator.text2 = InspectionsBundle.message("processing.progress.text", existing.name)
                    }
                    existing.delete()
                    doConvert(replacement, targetDirectory, fileName, added, failed)
                }
                if (userChoice == SkipOverwriteChoice.OVERWRITE || userChoice == SkipOverwriteChoice.OVERWRITE_ALL && !Registry.`is`("run.refactorings.under.progress")) {
                    WriteCommandAction.writeCommandAction(project)
                        .withName(title)
                        .run(doCopy)
                } else if (userChoice == SkipOverwriteChoice.OVERWRITE_ALL) {
                    doCopy.run()
                }
            }
        }
        return defaultChoice
    }

    private fun doConvert(file: PsiFile, targetDirectory: PsiDirectory, fileName: String, added: MutableList<PsiFile>, failed: MutableList<PsiFile>) {
        targetDirectory.cast<PsiDirectoryImpl>().executeWithUpdatingAddedFilesDisabled<Throwable> action@{
            val savedPsiFile = runCatchingCancelable { doConvertImageFormat(file, targetDirectory, fileName) }.getOrNull()
            if (savedPsiFile != null) {
                added.add(savedPsiFile)
            } else {
                failed.add(file)
            }
        }
    }

    private fun doConvertImageFormat(file: PsiFile, targetDirectory: PsiDirectory, targetFileName: String): PsiFile? {
        val s = file.virtualFile ?: return null
        val td = targetDirectory.virtualFile
        val sf = s.extension?.orNull() ?: return null
        val path = s.toNioPath()
        val targetPath = td.toNioPath().resolve(targetFileName)
        val sourceFormat = sf.lowercase()
        val targetFormat = targetFormatName.lowercase()
        ImageManager.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        val t = VfsUtil.findFile(targetPath, true)
        return t?.toPsiFile(file.project)
    }
}
