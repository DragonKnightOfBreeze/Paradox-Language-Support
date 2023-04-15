package icu.windea.pls.core.tool.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.tool.*

/**
 * 用于从指定的本地化文件生成其他语言区域的本地化文件。
 * 
 * * 支持选中多个直接位于同一目录下的本地化文件的情况。
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
            val project = e.getData(CommonDataKeys.PROJECT)
            if(project == null) return@run
            val allFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: VirtualFile.EMPTY_ARRAY
            when {
                allFiles.size == 1 -> {
                    val file = allFiles.single()
                    if(file.fileType != ParadoxLocalisationFileType) return@run
                    if(file.fileInfo == null) return@run
                    if(ParadoxFileManager.isLightFile(file)) return@run
                    visible = true
                    //文件名中必须带有某个语言区域
                    val allLocales = getCwtConfig().core.localisationLocalesNoDefaultNoPrefix
                    val localeString = allLocales.keys.find { file.name.contains(it) }
                    if(localeString == null) return@run
                    //这里不检查是否其他所有语言区域对应的本地化文件都存在，因此没有必要再生成其他语言区域的本地化文件
                    enabled = true
                }
                else -> {
                    val locFiles = allFiles.filter { file ->
                        if(file.fileType != ParadoxLocalisationFileType) return@filter false
                        if(file.fileInfo == null) return@filter false
                        if(ParadoxFileManager.isLightFile(file)) return@filter false
                        true
                    }
                    //所有合法的本地化文件必须直接位于同一目录下
                    if(locFiles.mapTo(mutableSetOf()) { it.parent }.size != 1) return@run
                    visible = true
                    //任意文件的文件名中必须带有某个语言区域
                    val localeMap = getCwtConfig().core.localisationLocalesNoDefaultNoPrefix
                    if(locFiles.all { file -> localeMap.keys.none { file.name.contains(it) } }) return@run
                    //这里不检查是否其他所有语言区域对应的本地化文件都存在，因此没有必要再生成其他语言区域的本地化文件
                    enabled = true
                }
            }
        }
        e.presentation.isVisible = visible
        e.presentation.isEnabled = enabled
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val allFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: VirtualFile.EMPTY_ARRAY
        if(allFiles.isEmpty()) return
        val locFiles = allFiles.filter { file ->
            if(file.fileType != ParadoxLocalisationFileType) return@filter false
            if(file.fileInfo == null) return@filter false
            if(ParadoxFileManager.isLightFile(file)) return@filter false
            true
        }
        if(locFiles.mapTo(mutableSetOf()) { it.parent }.size != 1) return
        val allLocales = getCwtConfig().core.localisationLocalesNoDefaultNoPrefix
        //nameTemplate : (baseFile, files) 
        val locFileGroup: MutableMap<String, GenerationInfo> = mutableMapOf()
        for(file in locFiles) {
            val fileName = file.name
            //文件名中必须带有某个语言区域，并且文件文本中必须仅带有这个语言区域
            for(localeString in allLocales.keys) {
                val i = fileName.indexOf(localeString)
                if(i == -1) continue
                val localeConfig = file.toPsiFile<ParadoxLocalisationFile>(project)?.propertyLists?.singleOrNull()?.localeConfig
                if(localeConfig != allLocales[localeString]) continue
                val prefix = fileName.substring(0, i)
                val suffix = fileName.substring(i + localeString.length)
                val nameTemplate = prefix + "$" + suffix
                val info = locFileGroup.getOrPut(nameTemplate) { GenerationInfo(file) }
                info.files.put(localeString, file)
                break
            }
        }
        if(locFileGroup.isEmpty()) return
        
        val taskTitle = PlsBundle.message("progress.generateLocalisationFiles")
        val task = object : Task.Modal(project, taskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                val fileDocumentManager = FileDocumentManager.getInstance()
                val documentManager = PsiDocumentManager.getInstance(project)
                
                val generationSettings = getSettings().generation
                val strategy = generationSettings.localisationTextGenerationStrategy
                
                val parent = locFiles.first().parent
                for((nameTemplate, info) in locFileGroup) {
                    val (baseFile, files) = info
                    val missingLocales = allLocales.keys.toMutableSet().apply { removeAll(files.keys) }
                    if(missingLocales.isEmpty()) continue
                    val baseDocument = fileDocumentManager.getDocument(baseFile) ?: continue
                    val baseText = baseDocument.text
                    //基于一组中第一个文件的文本生成新文件的文本
                    for((index, missingLocale) in missingLocales.withIndex()) {
                        val newName = nameTemplate.replace("$", missingLocale)
                        indicator.isIndeterminate = false
                        indicator.text = PlsBundle.message("progress.text.generateLocalisationFile", newName)
                        indicator.fraction = index / missingLocales.size.toDouble()
                        val newFile = parent.createChildData(this, newName)
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
                                    LocalisationTextGenerationStrategy.EmptyText -> e.setValue("")
                                    LocalisationTextGenerationStrategy.SpecificText -> e.setValue(generationSettings.localisationText.orEmpty())
                                    LocalisationTextGenerationStrategy.FromLocale -> {
                                        //使用对应语言区域的文本，如果不存在，以及其他任何意外，直接使用空字符串
                                        val locale = getLocale(generationSettings.localisationTextLocale.orEmpty())
                                        val selector = localisationSelector(project, baseFile).contextSensitive().locale(locale)
                                        val localisation = ParadoxLocalisationSearch.search(e.name, selector).find()
                                        e.setValue(localisation?.propertyValue?.text.orEmpty())
                                    }
                                }
                            }
                            true
                        }
                        documentManager.doPostponedOperationsAndUnblockDocument(newDocument)
                    }
                }
            }
        }
        executeCommand(project) {
            runUndoTransparentWriteAction {
                ProgressManager.getInstance().run(task)
            }
        }
    }
}