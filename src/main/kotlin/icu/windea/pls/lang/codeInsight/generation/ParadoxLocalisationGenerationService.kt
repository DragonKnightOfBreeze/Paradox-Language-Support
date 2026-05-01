package icu.windea.pls.lang.codeInsight.generation

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.core.util.createKey
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.locale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.model.codeInsight.ParadoxLocalisationGenerationContext
import icu.windea.pls.model.codeInsight.ParadoxLocalisationGenerationContextBuilder
import icu.windea.pls.model.codeInsight.ParadoxLocalisationGenerationInfo
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.lang.settings.PlsSettingsStrategies.LocalisationGeneration as LocalisationGenerationStrategy

object ParadoxLocalisationGenerationService {
    val fileLocaleKey = createKey<CwtLocaleConfig>("pls.localiation.generation.file.locale")
    val fileTooltipKey = createKey<String>("pls.localiation.generation.file.tooltip")

    fun generateFile(context: ParadoxLocalisationGenerationContext): VirtualFile {
        val fileName = getFileName(context)
        val fileText = getFileText(context)
        val file = ParadoxFileManager.createLightFile(fileName, fileText, ParadoxLocalisationLanguage)
        file.bom = PlsConstants.utf8Bom // 这里需要直接这样添加bom
        file.putUserData(fileLocaleKey, context.locale) // 添加语言环境元数据
        file.putUserData(fileTooltipKey, context.tooltip) // 添加文件提示元数据，后续会在文件通知中显示
        return file
    }

    fun generateFile(file: PsiFile, locale: CwtLocaleConfig, tooltip: String?, elements: List<ParadoxLocalisationGenerationElement.Item>): VirtualFile {
        val generationContext = ParadoxLocalisationGenerationContextBuilder.build(file, locale, tooltip, elements)
        return generateFile(generationContext)
    }

    fun getFileName(context: ParadoxLocalisationGenerationContext): String {
        return "generated_localisations_${context.locale.id}.yml"
    }

    fun getFileText(context: ParadoxLocalisationGenerationContext): String {
        val project = context.project
        val text = buildString {
            val indentSize = CodeStyle.getSettings(project).getIndentOptions(ParadoxLocalisationFileType).INDENT_SIZE
            val indent = " ".repeat(indentSize)
            appendLocaleLine(context)
            appendLocalisationLines(context, indent)
        }
        return text.trim()
    }

    private fun StringBuilder.appendLocaleLine(context: ParadoxLocalisationGenerationContext) {
        append(context.locale.id).append(":\n")
    }

    private fun StringBuilder.appendLocalisationLines(context: ParadoxLocalisationGenerationContext, indent: String) {
        for (info in context.infos) {
            appendLocalisationLine(context, info, indent)
        }
        if (context.infos.isNotEmpty() && context.children.isNotEmpty()) {
            appendNewLine()
        }
        val marker = OnceMarker()
        for (childContext in context.children) {
            if (marker.mark()) appendNewLine()
            appendLocalisationLines(childContext, indent)
        }
    }

    private fun StringBuilder.appendLocalisationLine(context: ParadoxLocalisationGenerationContext, info: ParadoxLocalisationGenerationInfo, indent: String) {
        append(indent)
        append(info.name)
        append(": \"")
        val text = getLocalisationText(context, info)
        append(text)
        append("\"\n")
    }

    private fun getLocalisationText(context: ParadoxLocalisationGenerationContext, info: ParadoxLocalisationGenerationInfo): String {
        info.text?.let { return it }

        val generationSettings = PlsSettings.getInstance().state.generation
        val strategy = generationSettings.localisationStrategy
        val text = when (strategy) {
            LocalisationGenerationStrategy.EmptyText -> ""
            LocalisationGenerationStrategy.SpecificText -> generationSettings.localisationStrategyText.orEmpty()
            LocalisationGenerationStrategy.FromLocale -> {
                // 使用对应语言环境的文本，如果不存在，以及其他任何意外，直接使用空字符串
                val locale = ParadoxLocaleManager.getResolvedLocaleConfig(generationSettings.localisationStrategyLocale.orEmpty())
                val selector = selector(context.project, context.context).localisation().contextSensitive().locale(locale)
                val localisation = ParadoxLocalisationSearch.searchNormal(info.name, selector).find()
                localisation?.propertyValue?.text.orEmpty()
            }
        }
        return text
    }

    private fun StringBuilder.appendNewLine() {
        val generationSettings = PlsSettings.getInstance().state.generation
        val add = generationSettings.newLineBetweenLocalisationGroups
        if (add) appendLine()
    }
}
