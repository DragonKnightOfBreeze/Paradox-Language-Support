package icu.windea.pls.lang.actions.localisation

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.executeWriteCommand
import icu.windea.pls.core.processChild
import icu.windea.pls.core.toPath
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.unquote
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.ide.notification.ChronicleNotificationGroups
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.locale
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.settings.ChronicleSettingsStrategies
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 用于从指定的本地化文件生成其他语言环境的本地化文件。
 */
class GenerateLocalisationFileAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val files = findFiles(e)
        if (files.none()) return
        e.presentation.isVisible = true
        val allLocales = findLocales(e)
        val fileMap = buildFileMap(files.toList(), allLocales, project)
        if (fileMap.isEmpty()) return
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val files = findFiles(e)
        // if (files.none()) return
        val allLocales = findLocales(e)
        val fileMap = buildFileMap(files.toList(), allLocales, project)
        if (fileMap.isEmpty()) return

        val taskTitle = ChronicleBundle.message("progress.generateLocalisationFiles")
        val task = object : Task.Modal(project, taskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                val fileDocumentManager = FileDocumentManager.getInstance()
                val documentManager = PsiDocumentManager.getInstance(project)

                val generationSettings = ChronicleSettings.getInstance().state.generation
                val strategy = generationSettings.localisationStrategy

                val specificText = generationSettings.localisationStrategyText.orEmpty()
                val fromLocale = ParadoxLocaleManager.getResolvedLocaleConfig(generationSettings.localisationStrategyLocale.orEmpty())

                // 文件名以及文件所在的某个父目录中可以带有语言环境
                // 生成的本地化文件会使用合适的文件名，放到合适的目录下

                val missingLocaleMap = mutableMapOf<String, Set<String>>()
                for (pathPattern in fileMap.keys) {
                    val missingLocales = allLocales.keys.filterTo(mutableSetOf()) { l ->
                        val p = pathPattern.replace("$", l).toPath()
                        p.toVirtualFile()?.takeIf { it.isValid } == null
                    }
                    if (missingLocales.isNotEmpty()) {
                        missingLocaleMap.putIfAbsent(pathPattern, missingLocales)
                    }
                }

                var generated = 0
                val total = missingLocaleMap.size

                for ((pathPattern, baseFile) in fileMap) {
                    val missingLocales = missingLocaleMap[pathPattern]
                    if (missingLocales.isNullOrEmpty()) continue

                    val baseDocument = fileDocumentManager.getDocument(baseFile) ?: continue
                    val baseText = baseDocument.text
                    // 基于一组中第一个文件的文本生成新文件的文本
                    for ((index, missingLocale) in missingLocales.withIndex()) {
                        val newPath = pathPattern.replace("$", missingLocale).toPath()
                        val newParentPath = newPath.parent
                        val newFileName = newPath.fileName
                        indicator.isIndeterminate = false
                        indicator.text = ChronicleBundle.message("progress.text.generateLocalisationFile", newFileName)
                        indicator.fraction = index / missingLocales.size.toDouble()

                        try {
                            VfsUtil.createDirectoryIfMissing(newParentPath.toString())
                            val newParent = newParentPath.toVirtualFile(refreshIfNeed = true) ?: continue
                            val newFile = newParent.createChildData(this, newFileName.toString())
                            val newDocument = fileDocumentManager.getDocument(newFile) ?: continue
                            newDocument.insertString(0, baseText)
                            documentManager.commitDocument(newDocument)
                            val newPsiFile = documentManager.getPsiFile(newDocument) as? ParadoxLocalisationFile ?: continue
                            // 替换文件中的语言环境和和本地化文本
                            val missingLocaleConfig = allLocales[missingLocale] ?: continue
                            val propertyList = newPsiFile.propertyLists.firstOrNull() ?: continue
                            propertyList.processChild { e ->
                                if (e is ParadoxLocalisationLocale) {
                                    e.setName(missingLocaleConfig.name)
                                } else if (e is ParadoxLocalisationProperty) {
                                    when (strategy) {
                                        ChronicleSettingsStrategies.LocalisationGeneration.EmptyText -> {
                                            e.setValue("")
                                        }
                                        ChronicleSettingsStrategies.LocalisationGeneration.SpecificText -> {
                                            e.setValue(specificText)
                                        }
                                        ChronicleSettingsStrategies.LocalisationGeneration.FromLocale -> {
                                            // 使用对应语言环境的文本，如果不存在，或者其他任何意外，直接使用空字符串
                                            val selector = ParadoxLocalisationSearch.selector(project, baseFile).contextSensitive().locale(fromLocale)
                                            val localisation = ParadoxLocalisationSearch.searchNormal(e.name, selector).find()
                                            e.setValue(localisation?.propertyValue?.text?.unquote().orEmpty())
                                        }
                                    }
                                }
                                true
                            }
                            documentManager.doPostponedOperationsAndUnblockDocument(newDocument)
                            generated++
                        } catch (e: Exception) {
                            if (e is ProcessCanceledException) throw e
                            thisLogger().warn(e)
                        }
                    }
                }

                val notificationTitle = ChronicleBundle.message("notification.generateLocalisationFile.success.title")
                val notificationContent = ChronicleBundle.message("notification.generateLocalisationFile.success.content", generated, total)
                ChronicleNotificationGroups.global().createNotification(notificationTitle, notificationContent, NotificationType.INFORMATION).notify(project)
            }
        }
        val commandName = ChronicleBundle.message("command.generateLocalisationFiles")
        executeWriteCommand(project, commandName) {
            ProgressManager.getInstance().run(task)
        }
    }

    private fun isValidFile(file: VirtualFile): Boolean {
        if (file.fileType !is ParadoxLocalisationFileType) return false
        if (file.fileInfo == null) return false
        if (VirtualFileService.isLightFile(file)) return false
        return true
    }

    private fun findFiles(e: AnActionEvent): Sequence<VirtualFile> {
        return VirtualFileService.findFiles(e, deep = true).filter { isValidFile(it) }
    }

    private fun findLocales(e: AnActionEvent): Map<String, CwtLocaleConfig> {
        val configGroup = ChronicleFacade.getConfigGroup(e)
        val supportedLocales = ParadoxLocaleManager.getSupportedLocales(configGroup)
        return supportedLocales.associateBy { it.shortId }
    }

    private fun buildFileMap(files: Collection<VirtualFile>, allLocales: Map<String, CwtLocaleConfig>, project: Project): Map<String, VirtualFile> {
        // 任意文件的文件名中必须带有某个语言环境，并且文件文本中必须仅带有这个语言环境
        val fileMap = mutableMapOf<String, VirtualFile>()
        files.forEach f@{ file ->
            val localeString = allLocales.keys.find { file.name.contains(it) }
            if (localeString == null) return@f
            val localeConfig = file.toPsiFile(project)?.castOrNull<ParadoxLocalisationFile>()?.let { selectLocale(it) }
            if (localeConfig != allLocales[localeString]) return@f
            val pathPattern = file.path.replace(localeString, "$")
            fileMap.putIfAbsent(pathPattern, file)
        }
        return fileMap
    }
}
