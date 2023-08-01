package icu.windea.pls.core.psi

import com.intellij.application.options.*
import com.intellij.ide.actions.*
import com.intellij.notification.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.generation.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.psi.*

object ParadoxPsiGenerator {
    fun getDefaultGenerateLocalisationsContext(definitionInfo: ParadoxDefinitionInfo): GenerateLocalisationsContext? {
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        
        val definitionName = definitionInfo.name
        val localisationInfos = definitionInfo.localisations
        if(localisationInfos.isEmpty()) return null
        val localisationNames = localisationInfos.mapNotNullTo(mutableSetOf()) { it.locationExpression.resolvePlaceholder(definitionName) }
        return GenerateLocalisationsContext(
            definitionName = definitionName,
            localisationNames = localisationNames
        )
    }
    
    fun getDefaultGenerateLocalisationsInFileContext(file: PsiFile): GenerateLocalisationsInFileContext {
        val context = GenerateLocalisationsInFileContext(file.name, mutableListOf())
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptDefinitionElement) visitDefinition(element)
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitDefinition(element: ParadoxScriptDefinitionElement) {
                val definitionInfo = element.definitionInfo ?: return
                val context0 = getDefaultGenerateLocalisationsContext(definitionInfo) ?: return
                context.contextList.add(context0)
            }
        })
        return context
    }
    
    fun generateLocalisations(context: GenerateLocalisationsContext, project: Project, file: PsiFile) {
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
        return createLocalisationTempFile(name, text)
    }
    
    fun generateLocalisationsInFile(context: GenerateLocalisationsInFileContext, project: Project, file: PsiFile) {
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
        return createLocalisationTempFile(name, text)
    }
    
    private fun StringBuilder.appendLocalisationLine(indent: String, localisationName: String, project: Project, file: PsiFile) {
        append(indent)
        append(localisationName)
        append(": \"")
        val generationSettings = getSettings().generation
        val strategy = generationSettings.localisationStrategy
        val text = when(strategy) {
            LocalisationGenerationStrategy.EmptyText -> ""
            LocalisationGenerationStrategy.SpecificText -> generationSettings.localisationStrategyText.orEmpty()
            LocalisationGenerationStrategy.FromLocale -> {
                //使用对应语言区域的文本，如果不存在，以及其他任何意外，直接使用空字符串
                val locale = getLocale(generationSettings.localisationStrategyLocale.orEmpty())
                val selector = localisationSelector(project, file).contextSensitive().locale(locale)
                val localisation = ParadoxLocalisationSearch.search(localisationName, selector).find()
                localisation?.propertyValue?.text.orEmpty()
            }
        }
        append(text)
        append("\"\n")
    }
    
    private fun createLocalisationTempFile(name: String, text: String): VirtualFile {
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