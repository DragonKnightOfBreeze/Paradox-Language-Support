package icu.windea.pls.dds.actions

import com.intellij.codeInspection.*
import com.intellij.ide.scratch.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ex.*
import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.registry.*
import com.intellij.psi.*
import com.intellij.psi.impl.file.*
import com.intellij.refactoring.*
import com.intellij.refactoring.util.*
import com.intellij.util.*
import com.intellij.util.containers.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.tool.*
import java.io.*
import java.util.concurrent.atomic.*
import java.util.function.Consumer

//com.intellij.refactoring.copy.CopyFilesOrDirectoriesHandler

/**
 * 将DDS图片转化为PNG图片。保存到指定的路径。可以批量转化。
 */
@Suppress("UnstableApiUsage")
class ConvertDdsToPngAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val enabled = when {
            project == null -> false
            editor != null -> e.getData(LangDataKeys.VIRTUAL_FILE)?.let { it.fileType == DdsFileType } == true
            else -> e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY)?.any { it.fileType == DdsFileType } == true
        }
        e.presentation.isEnabledAndVisible = enabled
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        val files = if(editor != null) {
            val file = e.getData(LangDataKeys.VIRTUAL_FILE)?.takeIf { it.fileType == DdsFileType }?.toPsiFile(project) ?: return
            listOf(file)
        } else {
            e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY)?.filter { it.fileType == DdsFileType }?.mapNotNull { it.toPsiFile(project) } ?: return
        }
        if(files.isEmpty()) return
        convert(files, project)
    }
    
    private fun convert(files: List<PsiFile>, project: Project) {
        val defaultNewName = if(files.size == 1) getNewName(files.first().name) else null
        val defaultTargetDirectory = files.first().parent?.takeUnless { ScratchUtil.isScratch(it.virtualFile) } ?: return
        
        val newName: String?
        val targetDirectory: PsiDirectory?
        val dialog = ConvertDdsToPngDialog(files, defaultNewName, defaultTargetDirectory, project)
        if(dialog.showAndGet()) {
            newName = if(files.size == 1) dialog.newName else null
            targetDirectory = dialog.targetDirectory
        } else {
            return
        }
        if(targetDirectory != null) {
            val command = { doConvert(files, newName, targetDirectory) }
            CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("dds.command.convertDdsToPng.name"), null)
        }
    }
    
    private fun doConvert(files: List<PsiFile>, newName: String?, targetDirectory: PsiDirectory) {
        val project = targetDirectory.project
        if(!CommonRefactoringUtil.checkReadOnlyStatus(project, setOf(targetDirectory), true)) {
            return
        }
        
        try {
            val title = PlsBundle.message("dds.command.convertDdsToPng.name")
            val choice = if(files.size > 1 || files[0].isDirectory) intArrayOf(-1) else null
            val added = mutableListOf<PsiFile>()
            saveToDirectory(files, newName, targetDirectory, choice, title, added)
            
        } catch(e: Exception) {
            Messages.showErrorDialog(project, e.message, PlsBundle.message("error.title"))
        }
        
    }
    
    private fun saveToDirectory(files: List<PsiFile>, newName: String?, targetDirectory: PsiDirectory, choice: IntArray?, title: String, added: MutableList<PsiFile>) {
        val existingFiles = MultiMap<PsiDirectory, PsiFile>()
        val app = ApplicationManagerEx.getApplicationEx()
        if(Registry.`is`("run.refactorings.under.progress")) {
            val thrown = AtomicReference<Throwable>()
            val action = Consumer { pi: ProgressIndicator? ->
                try {
                    for(file in files) {
                        saveToDirectoryUnderProgress(file, newName, targetDirectory, added, existingFiles, pi)
                    }
                } catch(e: Exception) {
                    thrown.set(e)
                }
            }
            CommandProcessor.getInstance().executeCommand(targetDirectory.project, { app.runWriteActionWithCancellableProgressInDispatchThread(ObjectUtils.notNull(title, PlsBundle.message("dds.command.convert.name")), targetDirectory.project, null, action) }, title, null)
            val throwable = thrown.get()
            if(throwable is ProcessCanceledException) {
                //process was canceled, don't proceed with existing files
                return
            }
            rethrow(throwable)
        } else {
            WriteCommandAction.writeCommandAction(targetDirectory.project).withName(title).run<IOException> {
                for(file in files) {
                    saveToDirectoryUnderProgress(file, newName, targetDirectory, added, existingFiles, null)
                }
            }
        }
        
        handleExistingFiles(newName, targetDirectory, choice, title, existingFiles, added)
    }
    
    private fun saveToDirectoryUnderProgress(file: PsiFile, newName: String?, targetDirectory: PsiDirectory, added: MutableList<PsiFile>, existingFiles: MultiMap<PsiDirectory, PsiFile>, pi: ProgressIndicator?) {
        if(pi != null) {
            pi.text2 = InspectionsBundle.message("processing.progress.text", file.name)
        }
        
        val backendPngFile = ParadoxImageResolver.getPngFile(file.virtualFile) ?: return
        val name = newName ?: getNewName(file.name)
        val existing = targetDirectory.findFile(name)
        if(existing != null && existing != file) {
            existingFiles.putValue(targetDirectory, file)
            return
        }
        targetDirectory.cast<PsiDirectoryImpl>().executeWithUpdatingAddedFilesDisabled<Throwable> {
            val backendPngPsiFile = backendPngFile.toPsiFile(targetDirectory.project) ?: return@executeWithUpdatingAddedFilesDisabled
            val savedPsiFile = targetDirectory.copyFileFrom(name, backendPngPsiFile)
            added.add(savedPsiFile)
        }
    }
    
    private fun rethrow(throwable: Throwable?) {
        if(throwable is IOException) {
            throw (throwable as IOException?)!!
        } else if(throwable is IncorrectOperationException) {
            throw (throwable as IncorrectOperationException?)!!
        } else if(throwable != null) {
            throw IncorrectOperationException(throwable)
        }
    }
    
    private fun getNewName(name: String): String {
        return if(name.takeLast(4).equals(".dds", true)) name.dropLast(4) + ".png" else "$name.png"
    }
    
    private fun handleExistingFiles(newName: String?, targetDirectory: PsiDirectory, choice: IntArray?, title: String, existingFiles: MultiMap<PsiDirectory, PsiFile>, added: MutableList<PsiFile>) {
        var defaultChoice = if(choice != null && choice[0] > -1) SkipOverwriteChoice.values()[choice[0]] else null
        try {
            defaultChoice = handleExistingFiles(defaultChoice, choice, newName, targetDirectory, title, existingFiles, added, null)
        } finally {
            if(choice != null && defaultChoice != null) {
                choice[0] = defaultChoice.ordinal % 2
            }
        }
    }
    
    private fun handleExistingFiles(
        defaultChoice: SkipOverwriteChoice?, choice: IntArray?, newName: String?,
        targetDirectory: PsiDirectory, title: String, existingFiles: MultiMap<PsiDirectory, PsiFile>,
        added: MutableList<PsiFile>, progressIndicator: ProgressIndicator?
    ): SkipOverwriteChoice? {
        for(tDirectory in existingFiles.keySet()) {
            val replacementFiles = existingFiles[tDirectory]
            val iterator = replacementFiles.iterator()
            while(iterator.hasNext()) {
                val replacement = iterator.next()
                val name = if(newName == null || tDirectory !== targetDirectory) replacement.name else newName
                val existing = tDirectory.findFile(name) ?: continue
                var userChoice = defaultChoice
                val project = targetDirectory.project
                if(userChoice == null) {
                    userChoice = SkipOverwriteChoice.askUser(targetDirectory, name, title, choice != null)
                    if(userChoice == SkipOverwriteChoice.SKIP_ALL) {
                        return userChoice
                    } else if(userChoice == SkipOverwriteChoice.OVERWRITE_ALL) {
                        val r = Consumer { pi: ProgressIndicator? ->
                            handleExistingFiles(SkipOverwriteChoice.OVERWRITE_ALL, choice, newName, targetDirectory, title, existingFiles, added, pi)
                        }
                        if(Registry.`is`("run.refactorings.under.progress")) {
                            CommandProcessor.getInstance().executeCommand(project, { ApplicationManagerEx.getApplicationEx().runWriteActionWithCancellableProgressInDispatchThread(title, project, null, r) }, title, null)
                        } else {
                            r.accept(null)
                        }
                        return SkipOverwriteChoice.OVERWRITE_ALL
                    }
                }
                iterator.remove()
                val doCopy = ThrowableRunnable<RuntimeException> {
                    if(progressIndicator != null) {
                        progressIndicator.text2 = InspectionsBundle.message("processing.progress.text", existing.name)
                    }
                    existing.delete()
                    (targetDirectory as PsiDirectoryImpl).executeWithUpdatingAddedFilesDisabled<RuntimeException> {
                        ContainerUtil.addIfNotNull(added, tDirectory.copyFileFrom(name, replacement))
                    }
                }
                if(userChoice == SkipOverwriteChoice.OVERWRITE || userChoice == SkipOverwriteChoice.OVERWRITE_ALL && !Registry.`is`("run.refactorings.under.progress")) {
                    WriteCommandAction.writeCommandAction(project)
                        .withName(title)
                        .run(doCopy)
                } else if(userChoice == SkipOverwriteChoice.OVERWRITE_ALL) {
                    doCopy.run()
                }
            }
        }
        return defaultChoice
    }
}
