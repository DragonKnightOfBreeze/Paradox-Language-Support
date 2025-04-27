package icu.windea.pls.lang.codeInsight.generation

import com.intellij.application.options.*
import com.intellij.ide.actions.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.*

object ParadoxLocalisationGenerator {
    val currentContext = ThreadLocal<ParadoxLocalisationCodeInsightContext>()

    fun showChooser(context: ParadoxLocalisationCodeInsightContext, members: List<ParadoxGenerateLocalisationsChooser.Localisation>, project: Project): ParadoxGenerateLocalisationsChooser? {
        try {
            currentContext.set(context)
            return doShowChooser(members, project, context)
        } finally {
            currentContext.remove()
        }
    }

    private fun doShowChooser(members: List<ParadoxGenerateLocalisationsChooser.Localisation>, project: Project, context: ParadoxLocalisationCodeInsightContext): ParadoxGenerateLocalisationsChooser? {
        val memberArray = members.toTypedArray()
        val chooser = ParadoxGenerateLocalisationsChooser(memberArray, project)
        chooser.title = getChooserName(context)
        //by default, select all checked missing localisations
        val missingMemberArray = memberArray.filter { it.info.check && it.info.missing }.toTypedArray()
        chooser.selectElements(missingMemberArray)
        chooser.show()
        if (chooser.exitCode != DialogWrapper.OK_EXIT_CODE) return null
        return chooser
    }

    fun getMembers(context: ParadoxLocalisationCodeInsightContext, locale: CwtLocalisationLocaleConfig): List<ParadoxGenerateLocalisationsChooser.Localisation> {
        val members = mutableListOf<ParadoxGenerateLocalisationsChooser.Localisation>()
        doGetMembers(members, context, locale)
        return members
    }

    private fun doGetMembers(members: MutableList<ParadoxGenerateLocalisationsChooser.Localisation>, context: ParadoxLocalisationCodeInsightContext, locale: CwtLocalisationLocaleConfig) {
        val onlyMissing = context.fromInspection
        context.children.forEach { child ->
            doGetMembers(members, child, locale)
        }
        context.infos.forEach f@{ info ->
            if (info.locale != locale) return@f
            if (onlyMissing && !info.missing) return@f //if from inspection, only missing localisations should be included here
            val name = info.name ?: return@f
            members += ParadoxGenerateLocalisationsChooser.Localisation(name, info, context)
        }
    }

    private fun getChooserName(context: ParadoxLocalisationCodeInsightContext): String {
        val onlyMissing = context.fromInspection
        return if (onlyMissing) {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.chooserTitle.0.missing")
                else -> PlsBundle.message("generation.localisation.chooserTitle.1.missing")
            }
        } else {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.chooserTitle.0")
                else -> PlsBundle.message("generation.localisation.chooserTitle.1")
            }
        }
    }

    fun generate(context: ParadoxLocalisationCodeInsightContext, members: List<ParadoxGenerateLocalisationsChooser.Localisation>, project: Project, file: PsiFile, locale: CwtLocalisationLocaleConfig) {
        val taskTitle = getProcessFileName(context)
        val task = object : Task.Modal(project, taskTitle, true) {
            var generatedFile: VirtualFile? = null

            override fun run(indicator: ProgressIndicator) {
                generatedFile = generateFile(context, members, project, file, locale) //生成本地化文件
            }

            override fun onSuccess() {
                val fileToOpen = generatedFile ?: return
                OpenFileAction.openFile(fileToOpen, project) //在编辑器中打开临时文件
            }
        }
        ProgressManager.getInstance().run(task)
    }

    private fun getProcessFileName(context: ParadoxLocalisationCodeInsightContext): String {
        val onlyMissing = context.fromInspection
        return if (onlyMissing) {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.processName.0.missing", context.name)
                Type.Definition -> PlsBundle.message("generation.localisation.processName.1.missing", context.name)
                Type.Modifier -> PlsBundle.message("generation.localisation.processName.2.missing", context.name)
                Type.LocalisationReference -> PlsBundle.message("generation.localisation.processName.3.missing", context.name)
                Type.SyncedLocalisationReference -> PlsBundle.message("generation.localisation.processName.4.missing", context.name)
            }
        } else {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.processName.0", context.name)
                Type.Definition -> PlsBundle.message("generation.localisation.processName.1", context.name)
                Type.Modifier -> PlsBundle.message("generation.localisation.processName.2", context.name)
                Type.LocalisationReference -> PlsBundle.message("generation.localisation.processName.3", context.name)
                Type.SyncedLocalisationReference -> PlsBundle.message("generation.localisation.processName.4", context.name)
            }
        }
    }

    private fun generateFile(context: ParadoxLocalisationCodeInsightContext, members: List<ParadoxGenerateLocalisationsChooser.Localisation>, project: Project, file: PsiFile, locale: CwtLocalisationLocaleConfig): VirtualFile {
        val generatedFileName = getGeneratedFileName(context)
        val namesToDistinct = mutableSetOf<String>()
        val text = buildString {
            append(locale.id).append(":\n")
            val indentSize = CodeStyle.getSettings(project).getIndentOptions(ParadoxLocalisationFileType.INSTANCE).INDENT_SIZE
            val indent = " ".repeat(indentSize)
            for (localisation in members) {
                //exclude duplicate localisation names
                if (namesToDistinct.add(localisation.name)) {
                    appendLocalisationLine(indent, localisation.name, project, file)
                }
            }
        }
        return createLocalisationTempFile(generatedFileName, text)
    }

    private fun getGeneratedFileName(context: ParadoxLocalisationCodeInsightContext): String {
        val onlyMissing = context.fromInspection
        return if (onlyMissing) {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.fileName.0.missing", context.name)
                Type.Definition -> PlsBundle.message("generation.localisation.fileName.1.missing", context.name)
                Type.Modifier -> PlsBundle.message("generation.localisation.fileName.2.missing", context.name)
                Type.LocalisationReference -> PlsBundle.message("generation.localisation.fileName.3.missing", context.name)
                Type.SyncedLocalisationReference -> PlsBundle.message("generation.localisation.fileName.4.missing", context.name)
            }
        } else {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.fileName.0", context.name)
                Type.Definition -> PlsBundle.message("generation.localisation.fileName.1", context.name)
                Type.Modifier -> PlsBundle.message("generation.localisation.fileName.2", context.name)
                Type.LocalisationReference -> PlsBundle.message("generation.localisation.fileName.3", context.name)
                Type.SyncedLocalisationReference -> PlsBundle.message("generation.localisation.fileName.4", context.name)
            }
        }
    }

    private fun StringBuilder.appendLocalisationLine(indent: String, localisationName: String, project: Project, file: PsiFile) {
        append(indent)
        append(localisationName)
        append(": \"")
        val generationSettings = getSettings().generation
        val strategy = generationSettings.localisationStrategy
        val text = when (strategy) {
            LocalisationGenerationStrategy.EmptyText -> ""
            LocalisationGenerationStrategy.SpecificText -> generationSettings.localisationStrategyText.orEmpty()
            LocalisationGenerationStrategy.FromLocale -> {
                //使用对应语言区域的文本，如果不存在，以及其他任何意外，直接使用空字符串
                val locale = ParadoxLocaleManager.getLocaleConfig(generationSettings.localisationStrategyLocale.orEmpty())
                val selector = selector(project, file).localisation().contextSensitive().locale(locale)
                val localisation = ParadoxLocalisationSearch.search(localisationName, selector).find()
                localisation?.propertyValue?.text.orEmpty()
            }
        }
        append(text)
        append("\"\n")
    }

    private fun createLocalisationTempFile(fileName: String, text: String): VirtualFile {
        val lightFile = ParadoxFileManager.createLightFile(fileName, text, ParadoxLocalisationLanguage.INSTANCE)
        lightFile.bom = PlsConstants.utf8Bom //这里需要直接这样添加bom
        return lightFile
    }
}
