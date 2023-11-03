package icu.windea.pls.core.tool.actions

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于从指定的本地化文件生成其他语言区域的本地化文件。
 */
class GenerateLocalisationFileAction : AnAction() {
    data class GenerationInfo(
        val baseFile: VirtualFile,
        val files: MutableMap<String, VirtualFile> = mutableMapOf()
    )
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun update(e: AnActionEvent) {
        var visible = false
        var enabled = false
        run {
            val project = e.getData(CommonDataKeys.PROJECT) ?: return@run
            val allFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: VirtualFile.EMPTY_ARRAY
            if(allFiles.isEmpty()) return@run
            val files = allFiles.filter { file ->
                if(file.fileType != ParadoxLocalisationFileType) return@filter false
                if(file.fileInfo == null) return@filter false
                if(ParadoxFileManager.isLightFile(file)) return@filter false
                true
            }
            if(files.isEmpty()) return@run
            visible = true
            //任意文件的文件名中必须带有某个语言区域，并且文件文本中必须仅带有这个语言区域
            val allLocales = ParadoxLocaleHandler.getLocaleConfigMapByShortId()
            val fileMap = mutableMapOf<String, VirtualFile>()
            files.forEach { file ->
                val localeString = allLocales.keys.find { file.name.contains(it) }
                if(localeString == null) return@forEach
                val localeConfig = file.toPsiFile(project)?.castOrNull<ParadoxLocalisationFile>()?.let { selectLocale(it) }
                if(localeConfig != allLocales[localeString]) return@forEach
                val pathPattern = file.path.replace(localeString, "$")
                fileMap.putIfAbsent(pathPattern, file)
            }
            if(fileMap.isEmpty()) return@run
            enabled = true
        }
        e.presentation.isVisible = visible
        e.presentation.isEnabled = enabled
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val allFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: VirtualFile.EMPTY_ARRAY
        if(allFiles.isEmpty()) return
        val files = allFiles.filter { file ->
            if(file.fileType != ParadoxLocalisationFileType) return@filter false
            if(file.fileInfo == null) return@filter false
            if(ParadoxFileManager.isLightFile(file)) return@filter false
            true
        }
        if(files.isEmpty()) return
        //任意文件的文件名中必须带有某个语言区域，并且文件文本中必须仅带有这个语言区域
        val allLocales = ParadoxLocaleHandler.getLocaleConfigMapByShortId()
        val fileMap = mutableMapOf<String, VirtualFile>()
        files.forEach { file ->
            val localeString = allLocales.keys.find { file.name.contains(it) }
            if(localeString == null) return@forEach
            val localeConfig = file.toPsiFile(project)?.castOrNull<ParadoxLocalisationFile>()?.let { selectLocale(it) }
            if(localeConfig != allLocales[localeString]) return@forEach
            val pathPattern = file.path.replace(localeString, "$")
            fileMap.putIfAbsent(pathPattern, file)
        }
        if(fileMap.isEmpty()) return
        
        val taskTitle = PlsBundle.message("progress.generateLocalisationFiles")
        val task = object : Task.Modal(project, taskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                val fileDocumentManager = FileDocumentManager.getInstance()
                val documentManager = PsiDocumentManager.getInstance(project)
                
                val generationSettings = getSettings().generation
                val strategy = generationSettings.localisationStrategy
                
                //文件名以及文件所在的某个父目录中可以带有语言区域
                //生成的本地化文件会使用合适的文件名，放到合适的目录下
                
                val missingLocaleMap = mutableMapOf<String, Set<String>>()
                for(pathPattern in fileMap.keys) {
                    val missingLocales = allLocales.keys.filterTo(mutableSetOf()) { l ->
                        val p = pathPattern.replace("$", l).toPath()
                        VfsUtil.findFile(p, false)?.takeIf { it.isValid } == null
                    }
                    if(missingLocales.isNotEmpty()) {
                        missingLocaleMap.putIfAbsent(pathPattern, missingLocales)
                    }
                }
                
                var generated = 0
                val total = missingLocaleMap.size
                
                for((pathPattern, baseFile) in fileMap) {
                    val missingLocales = missingLocaleMap[pathPattern]
                    if(missingLocales.isNullOrEmpty()) continue
                    
                    val baseDocument = fileDocumentManager.getDocument(baseFile) ?: continue
                    val baseText = baseDocument.text
                    //基于一组中第一个文件的文本生成新文件的文本
                    for((index, missingLocale) in missingLocales.withIndex()) {
                        val newPath = pathPattern.replace("$", missingLocale).toPath()
                        val newParentPath = newPath.parent
                        val newFileName = newPath.fileName
                        indicator.isIndeterminate = false
                        indicator.text = PlsBundle.message("progress.text.generateLocalisationFile", newFileName)
                        indicator.fraction = index / missingLocales.size.toDouble()
                        
                        try {
                            VfsUtil.createDirectoryIfMissing(newParentPath.toString())
                            val newParent = VfsUtil.findFile(newParentPath, true) ?: continue
                            val newFile = newParent.createChildData(this, newFileName.toString())
                            val newDocument = fileDocumentManager.getDocument(newFile) ?: continue
                            newDocument.insertString(0, baseText)
                            documentManager.commitDocument(newDocument)
                            val newPsiFile = documentManager.getPsiFile(newDocument) as? ParadoxLocalisationFile ?: continue
                            //替换文件中的语言区域和和本地化文本
                            val missingLocaleConfig = allLocales[missingLocale] ?: continue
                            val propertyList = newPsiFile.propertyLists.firstOrNull() ?: continue
                            propertyList.processChild { e ->
                                if(e is ParadoxLocalisationLocale) {
                                    e.setName(missingLocaleConfig.id)
                                } else if(e is ParadoxLocalisationProperty) {
                                    when(strategy) {
                                        LocalisationGenerationStrategy.EmptyText -> e.setValue("")
                                        LocalisationGenerationStrategy.SpecificText -> e.setValue(generationSettings.localisationStrategyText.orEmpty())
                                        LocalisationGenerationStrategy.FromLocale -> {
                                            //使用对应语言区域的文本，如果不存在，以及其他任何意外，直接使用空字符串
                                            val locale = ParadoxLocaleHandler.getLocale(generationSettings.localisationStrategyLocale.orEmpty())
                                            val selector = localisationSelector(project, baseFile).contextSensitive().locale(locale)
                                            val localisation = ParadoxLocalisationSearch.search(e.name, selector).find()
                                            e.setValue(localisation?.propertyValue?.text?.unquote().orEmpty())
                                        }
                                    }
                                }
                                true
                            }
                            documentManager.doPostponedOperationsAndUnblockDocument(newDocument)
                            generated++
                        } catch(e: Exception) {
                            if(e is ProcessCanceledException) throw e
                            thisLogger().warn(e)
                        }
                    }
                }
                
                NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
                    PlsBundle.message("notification.generateLocalisationFile.success.title"),
                    PlsBundle.message("notification.generateLocalisationFile.success.content", generated, total),
                    NotificationType.INFORMATION
                ).notify(project)
            }
        }
        executeCommand(project) {
            runUndoTransparentWriteAction {
                ProgressManager.getInstance().run(task)
            }
        }
    }
}