package icu.windea.pls.core.psi

import com.intellij.application.options.*
import com.intellij.ide.actions.*
import com.intellij.notification.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.codeInsight.generation.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.localisation.*
import icu.windea.pls.tool.*

object ParadoxPsiGenerator {
    fun generateLocalisations(context: GenerateLocalisationsContext, project: Project, editor: Editor, file: PsiFile) {
        if(context.localisationNames.isEmpty()) return noLocalisations(project)
        
        val taskTitle = PlsBundle.message("process.generateLocalisations", context.definitionName)
        val task = object : Task.Modal(project, taskTitle, true) {
            var generatedFile: VirtualFile? = null
            
            override fun run(indicator: ProgressIndicator) {
                generatedFile = doGenerateLocalisations(context, project, file)
            }
            
            override fun onSuccess() {
                val fileToOpen = generatedFile ?: return
                OpenFileAction.openFile(fileToOpen, project) //在编辑器中打开临时文件
            }
        }
        ProgressManager.getInstance().run(task)
    }
    
    private fun doGenerateLocalisations(context: GenerateLocalisationsContext, project: Project, file: PsiFile): VirtualFile {
        val name = "generated localisations of definition ${context.definitionName}"
        val localeConfig = preferredParadoxLocale()
        val text = buildString {
            append(localeConfig.id).append(":\n")
            val indentSize = CodeStyle.getSettings(project).getIndentOptions(ParadoxLocalisationFileType).INDENT_SIZE
            val indent = " ".repeat(indentSize)
            for(localisationName in context.localisationNames) {
                appendLocalisationLine(indent, localisationName, project, file)
            }
        }
        return createLocalisationTempFile(name, text, project)
    }
    
    fun generateLocalisationsInFile(context: GenerateLocalisationsInFileContext, project: Project, editor: Editor, file: PsiFile) {
        if(context.contextList.all { it.localisationNames.isEmpty() }) return noLocalisations(project)
        
        val taskTitle = PlsBundle.message("process.generateLocalisationsInFile", context.fileName)
        val task = object : Task.Modal(project, taskTitle, true) {
            var generatedFile: VirtualFile? = null
            
            override fun run(indicator: ProgressIndicator) {
                generatedFile = doGenerateLocalisationsInFile(context, project, file)
            }
            
            override fun onSuccess() {
                val fileToOpen = generatedFile ?: return
                OpenFileAction.openFile(fileToOpen, project) //在编辑器中打开临时文件
            }
        }
        ProgressManager.getInstance().run(task)
    }
    
    private fun doGenerateLocalisationsInFile(context: GenerateLocalisationsInFileContext, project: Project, file: PsiFile): VirtualFile {
        val name = "generated localisations of file ${context.fileName}"
        val localeConfig = preferredParadoxLocale()
        val text = buildString {
            append(localeConfig.id).append(":\n")
            val indentSize = CodeStyle.getSettings(project).getIndentOptions(ParadoxLocalisationFileType).INDENT_SIZE
            val indent = " ".repeat(indentSize)
            for(context0 in context.contextList) {
                for(localisationName in context0.localisationNames) {
                    appendLocalisationLine(indent, localisationName, project, file)
                }
            }
        }
        return createLocalisationTempFile(name, text, project)
    }
    
    private fun StringBuilder.appendLocalisationLine(indent: String, localisationName: String, project: Project, file: PsiFile) {
        append(indent)
        append(localisationName)
        append(": \"")
        val generationSettings = getSettings().generation
        val strategy = generationSettings.localisationTextGenerationStrategy
        val text = when(strategy) {
            LocalisationTextGenerationStrategy.EmptyText -> ""
            LocalisationTextGenerationStrategy.SpecificText -> generationSettings.localisationText.orEmpty()
            LocalisationTextGenerationStrategy.FromLocale -> {
                //使用对应语言区域的文本，如果不存在，以及其他任何意外，直接使用空字符串
                val locale = getLocale(generationSettings.localisationTextLocale.orEmpty())
                val selector = localisationSelector(project, file).contextSensitive().locale(locale)
                val localisation = ParadoxLocalisationSearch.search(localisationName, selector).find()
                localisation?.propertyValue?.text.orEmpty()
            }
        }
        append(text)
        append("\"\n")
    }
    
    private fun createLocalisationTempFile(name: String, text: String, project: Project): VirtualFile {
        val lightFile = ParadoxFileManager.createLightFile(name, text, ParadoxLocalisationLanguage)
        lightFile.bom = PlsConstants.utf8Bom //这里需要直接这样添加bom
        return lightFile
    }
    
    private fun noLocalisations(project: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
            PlsBundle.message("no.localisations.to.be.generated"),
            NotificationType.INFORMATION
        ).notify(project)
    }
}