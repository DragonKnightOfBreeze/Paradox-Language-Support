package icu.windea.pls.model.codeInsight

import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.lang.codeInsight.generation.ParadoxLocalisationGenerationElement
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.*

object ParadoxLocalisationGenerationContextBuilder {
    fun build(
        file: PsiFile,
        locale: CwtLocaleConfig,
        tooltip: String?,
        elements: List<ParadoxLocalisationGenerationElement.Item>,
    ): ParadoxLocalisationGenerationContext {
        val newChildren = mutableListOf<ParadoxLocalisationGenerationContext>()
        val newContext = ParadoxLocalisationGenerationContext(file.project, file, locale, tooltip, emptyList(), newChildren)
        if (elements.isEmpty()) return newContext

        val namesToDistinct = mutableSetOf<String>() // 去重
        val group = mutableMapOf<String, MutableList<ParadoxLocalisationGenerationInfo>>()
        for (element in elements) {
            if (!namesToDistinct.add(element.name)) continue
            val groupKey = getGroupKey(element)
            group.getOrPut(groupKey) { mutableListOf() } += getInfo(element)
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

    private fun getInfo(element: ParadoxLocalisationGenerationElement.Item): ParadoxLocalisationGenerationInfo {
        return ParadoxLocalisationGenerationInfo(element.name)
    }

    private fun handleGroup(group: MutableMap<String, MutableList<ParadoxLocalisationGenerationInfo>>) {
        val settings = PlsSettings.getInstance().state.generation

        // #296 如果某个来自本地化引用的本地化的名字与某个分组名匹配（将分组名作为前缀，移除后是空字符串，或者以有效分隔符开始的字符串），则移入此分组
        run {
            if (!settings.moveInfoLocalisationGroups) return@run
            val infos = group["__"]
            if (infos.isNullOrEmpty()) return@run
            val infosToRemove = mutableSetOf<ParadoxLocalisationGenerationInfo>()
            val groupNames = group.keys.mapNotNull { it.substringAfter(':', "").orNull() }
            for (info in infos) {
                val targetGroupName = groupNames.find { groupName ->
                    val remain = info.name.removePrefixOrNull(groupName)
                    remain != null && (remain.isEmpty() || remain.first() in "_.-")
                }
                if (targetGroupName == null) continue
                group.getValue(targetGroupName) += info
                infosToRemove += info
            }
            infos.removeAll(infosToRemove)
        }
    }
}
