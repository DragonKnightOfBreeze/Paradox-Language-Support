package icu.windea.pls.lang.codeInsight.generation

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.vfs.VirtualFileBomService
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext.*
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.locale
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.lang.settings.ChronicleSettingsStrategies.LocalisationGeneration as LocalisationGenerationStrategy

object ParadoxLocalisationGenerationService {
    val fileLocaleKey = createKey<CwtLocaleConfig>("chronicle.localiation.generation.file.locale")
    val fileTooltipKey = createKey<String>("chronicle.localiation.generation.file.tooltip")

    fun createContext(file: PsiFile, locale: CwtLocaleConfig, tooltip: String?, elements: List<ParadoxLocalisationGenerationElement.Item>): ParadoxLocalisationGenerationContext {
        val newChildren = mutableListOf<ParadoxLocalisationGenerationContext>()
        val newContext = ParadoxLocalisationGenerationContext(file.project, file, locale, tooltip, emptyList(), newChildren)
        if (elements.isEmpty()) return newContext

        val namesToDistinct = mutableSetOf<String>() // 去重
        val group = mutableMapOf<String, MutableList<ParadoxLocalisationGenerationInfo>>()
        for (element in elements) {
            if (!namesToDistinct.add(element.name)) continue
            val info = ParadoxLocalisationGenerationInfo(element.name)
            val groupKey = getGroupKey(element)
            group.getOrPut(groupKey) { mutableListOf() } += info
        }
        handleGroup(group)
        group.values.mapTo(newChildren) {
            newContext.copy(infos = it, children = emptyList())
        }
        return newContext
    }

    private fun getGroupKey(element: ParadoxLocalisationGenerationElement.Item): String {
        val context = element.context
        val groupName = when (context.type) {
            Type.Definition -> "d:${context.name}"
            Type.Modifier -> "m:${context.name}"
            Type.LocalisationReference -> "__"
            else -> "_"
        }
        return groupName
    }

    private fun handleGroup(group: MutableMap<String, MutableList<ParadoxLocalisationGenerationInfo>>) {
        val settings = ChronicleSettings.getInstance().state.generation

        // #296 如果某个来自本地化引用的本地化的名字与某个分组名匹配（将分组名作为前缀，移除后是空字符串，或者以有效分隔符开始的字符串），则移入此分组
        run {
            if (!settings.moveIntoLocalisationGroups) return@run
            val infos = group["__"]
            if (infos.isNullOrEmpty()) return@run
            val infosToRemove = mutableSetOf<ParadoxLocalisationGenerationInfo>()
            val groupKeys = group.keys.filter { it != "__" && it != "_" }.sortedDescending() // 更长的分组名要放在前面
            for (info in infos) {
                val targetGroupKey = groupKeys.find { groupKey ->
                    val groupName = groupKey.substringAfter(':')
                    val remain = info.name.removePrefixOrNull(groupName)
                    remain != null && (remain.isEmpty() || remain.first() in "_.-")
                }
                if (targetGroupKey == null) continue
                group.getValue(targetGroupKey) += info
                infosToRemove += info
            }
            infos.removeAll(infosToRemove)
        }
    }

    fun generateFile(context: ParadoxLocalisationGenerationContext): VirtualFile {
        val fileName = getFileName(context)
        val fileText = getFileText(context)
        val file = ParadoxFileManager.createLightFile(fileName, fileText, ParadoxLocalisationLanguage)
        file.bom = VirtualFileBomService.utf8Bom // 这里需要直接这样添加bom
        file.putUserData(fileLocaleKey, context.locale) // 添加语言环境元数据
        file.putUserData(fileTooltipKey, context.tooltip) // 添加文件提示元数据，后续会在文件通知中显示
        return file
    }

    fun getFileName(context: ParadoxLocalisationGenerationContext): String {
        return "generated_localisations_${context.locale.name}.yml"
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
        append(context.locale.name).append(":\n")
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

        val generationSettings = ChronicleSettings.getInstance().state.generation
        val strategy = generationSettings.localisationStrategy
        val text = when (strategy) {
            LocalisationGenerationStrategy.EmptyText -> ""
            LocalisationGenerationStrategy.SpecificText -> generationSettings.localisationStrategyText.orEmpty()
            LocalisationGenerationStrategy.FromLocale -> {
                // 使用对应语言环境的文本，如果不存在，以及其他任何意外，直接使用空字符串
                val locale = ParadoxLocaleManager.getResolvedLocaleConfig(generationSettings.localisationStrategyLocale.orEmpty())
                val selector = ParadoxLocalisationSearch.selector(context.project, context.context).contextSensitive().locale(locale)
                val localisation = ParadoxLocalisationSearch.searchNormal(info.name, selector).find()
                localisation?.propertyValue?.text.orEmpty()
            }
        }
        return text
    }

    private fun StringBuilder.appendNewLine() {
        val generationSettings = ChronicleSettings.getInstance().state.generation
        val add = generationSettings.blankLineBetweenLocalisationGroups
        if (add) appendLine()
    }
}
